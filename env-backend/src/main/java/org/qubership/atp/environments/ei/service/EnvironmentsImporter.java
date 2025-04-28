/*
 * # Copyright 2024-2025 NetCracker Technology Corporation
 * #
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * #      http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 */

package org.qubership.atp.environments.ei.service;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.qubership.atp.auth.springbootstarter.exceptions.AtpException;
import org.qubership.atp.crypt.api.Decryptor;
import org.qubership.atp.ei.node.dto.ExportImportData;
import org.qubership.atp.ei.node.exceptions.ExportException;
import org.qubership.atp.ei.node.services.ObjectLoaderFromDiskService;
import org.qubership.atp.environments.ei.model.Connection;
import org.qubership.atp.environments.ei.model.Environment;
import org.qubership.atp.environments.ei.model.System;
import org.qubership.atp.environments.errorhandling.connection.EnvironmentIncompatibleCategoryException;
import org.qubership.atp.environments.errorhandling.internal.EnvironmentImportFileLoadException;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.model.Identified;
import org.qubership.atp.environments.model.ServerItf;
import org.qubership.atp.environments.model.Sourced;
import org.qubership.atp.environments.model.SystemCategory;
import org.qubership.atp.environments.model.utils.Constants;
import org.qubership.atp.environments.service.direct.ConnectionService;
import org.qubership.atp.environments.service.direct.EnvironmentService;
import org.qubership.atp.environments.service.direct.ProjectService;
import org.qubership.atp.environments.service.direct.SystemCategoriesService;
import org.qubership.atp.environments.service.direct.SystemService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnvironmentsImporter {

    private final ObjectLoaderFromDiskService objectLoaderFromDiskService;
    private final EnvironmentService environmentService;
    private final SystemService systemService;
    private final SystemCategoriesService systemCategoriesService;
    private final ConnectionService connectionService;
    private final ProjectService projectService;
    private final DuplicateNameChecker duplicateNameChecker;
    private final Decryptor decryptor;

    /**
     * Imports the environments from filesystem to server.
     *
     * @param workDir directory where environment's files store.
     */
    public void importEnvironments(Path workDir, ExportImportData importData) throws Exception {
        log.info("Starts importEnvironments(workDir: {})", workDir);
        Map<UUID, Path> environmentsFiles = objectLoaderFromDiskService.getListOfObjects(workDir,
                Environment.class);
        log.debug("importEnvironments list: {}", environmentsFiles);
        Map<UUID, UUID> invertedRepMap = new HashMap<>(importData.getReplacementMap()).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        environmentsFiles.forEach((environmentId, path) -> {
            log.debug("importEnvironments starts import id: {}.", environmentId);
            Environment environmentObject;
            if (importData.isCreateNewProject() || importData.isInterProjectImport()) {
                Map<UUID, UUID> map = new HashMap<>(importData.getReplacementMap());
                environmentObject =
                        objectLoaderFromDiskService.loadFileAsObjectWithReplacementMap(path,
                                Environment.class, map, true, false);
            } else {
                environmentObject = objectLoaderFromDiskService.loadFileAsObject(path, Environment.class);
            }
            log.debug("Imports object: {}", environmentObject);
            if (environmentObject == null) {
                log.error("Failed to load the import file using the path: {}", path);
                throw new EnvironmentImportFileLoadException(path.toString());
            }
            UUID projectId = environmentObject.getProjectId();
            createProjectIfNotExists(projectId);
            checkCategoryCompatibility(environmentObject);
            Optional<org.qubership.atp.environments.model.Environment> environmentOpt =
                    environmentService.getOrElse(environmentObject.getId());
            if (!environmentOpt.isPresent()) {
                environmentObject.setSourceId(environmentId);
                createWithCheckName(environmentObject);
            } else {
                updateWithCheckName(environmentObject, environmentOpt.get());
            }
            importSystems(environmentObject, invertedRepMap);
        });
        log.info("End of importEnvironments(workDir: {})", workDir);
    }

    private void updateWithCheckName(Environment environmentObject,
                                     org.qubership.atp.environments.model.Environment environment) {
        checkAndCorrectName(environmentObject);
        environment.setName(environmentObject.getName());
        environment.setDescription(environmentObject.getDescription());
        environment.setProjectId(environmentObject.getProjectId());
        environmentService.update(environment);
    }

    private void createWithCheckName(Environment environment) {
        checkAndCorrectName(environment);
        environmentService.replicate(environment.getProjectId(), environment.getId(),
                environment.getName(),
                environment.getGraylogName(),
                environment.getDescription(),
                environment.getSsmSolutionAlias(),
                environment.getSsmInstanceAlias(),
                environment.getConsulEgressConfigPath(),
                environment.getCategoryId(), environment.getSourceId(),
                environment.getTags());
    }

    /**
     * Check and correct name.
     *
     * @param environment the environment
     */
    public void checkAndCorrectName(Environment environment) {
        duplicateNameChecker.checkAndCorrectName(environment, env -> isNameUsed(env));
    }

    private boolean isNameUsed(Environment environment) {
        org.qubership.atp.environments.model.Environment fromBase =
                environmentService.getByNameAndProjectId(environment.getName(), environment.getProjectId());
        return fromBase != null && !fromBase.getId().equals(environment.getId());
    }

    private void checkCategoryCompatibility(Environment objectToImport) {
        final UUID importObjectCategoryId = objectToImport.getCategoryId();
        final UUID environmentCategoryId = Constants.Environment.Category.ENVIRONMENT;
        final UUID toolCategoryId = Constants.Environment.Category.TOOL;

        if (!importObjectCategoryId.equals(environmentCategoryId) && !importObjectCategoryId.equals(toolCategoryId)) {
            log.error("Incompatible categories ids. In System ENVIRONMENT {}, TOOL {}. Import entity category {}",
                    environmentCategoryId, toolCategoryId, importObjectCategoryId);
            throw new EnvironmentIncompatibleCategoryException();
        }
    }

    /**
     * Replicates project if it not exists in database.
     *
     * @param projectId id of the project
     */
    private void createProjectIfNotExists(UUID projectId) {
        try {
            boolean ifProjectExists = projectService.existsById(projectId);
            log.debug("Project {}, ifProjectExists {}", projectId, ifProjectExists);
            if (!ifProjectExists) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyyHH:mm:ss");
                projectService.replicate(projectId,
                        "Exported VA " + simpleDateFormat.format(new Date()),
                        "Exported VA " + simpleDateFormat.format(new Date()),
                        null,
                        java.lang.System.currentTimeMillis());
                // there is no such important a name of project as its' id.
            }
        } catch (AtpException e) {
            String message = String.format("Cannot create project with id %s", projectId);
            throw new ExportException(message, e);
        }
    }

    /**
     * Imports the systems under environment.
     *
     * @param environment an instance of {@link Environment}
     */
    private void importSystems(Environment environment, Map<UUID, UUID> invertedRepMap) {
        List<System> systemsFromFile = environment.getSystems();
        Map<UUID, org.qubership.atp.environments.model.System> originalSystems = getOriginalSystemMap(environment);
        Map<UUID, SystemCategory> systemCategoryMap =
                systemCategoriesService.getAll().stream().collect(Collectors.toMap(Identified::getId,
                        category -> category));
        systemsFromFile.forEach(systemFromFile -> {
            log.info("Imports system {} for environment {}.", systemFromFile, environment);
            org.qubership.atp.environments.model.System system = originalSystems.get(systemFromFile.getId());
            duplicateNameChecker.checkAndCorrectSystemName(systemFromFile, environment, system);
            ServerItf serverItf = new ServerItf();
            System.ServerItf serverItfFromFile = systemFromFile.getServerItf();
            if (serverItfFromFile != null) {
                serverItf.setName(serverItfFromFile.getName());
                serverItf.setUrl(serverItfFromFile.getUrl());
            }
            if (system == null) {
                systemService.replicate(environment.getId(), systemFromFile.getId(), systemFromFile.getName(),
                        systemFromFile.getDescription(), systemFromFile.getSystemCategoryId(),
                        systemFromFile.getParametersGettingVersion(),
                        systemFromFile.getParentSystemId(), serverItf, systemFromFile.getLinkToSystemId(),
                        systemFromFile.getExternalId(),
                        invertedRepMap.getOrDefault(systemFromFile.getId(), null),
                        systemFromFile.getExternalName());
            } else {
                system.setExternalId(systemFromFile.getExternalId());
                system.setExternalName(systemFromFile.getExternalName());
                system.setName(systemFromFile.getName());
                system.setDescription(systemFromFile.getDescription());
                system.setParentSystemId(systemFromFile.getParentSystemId());
                if (systemFromFile.getSystemCategoryId() != null) {
                    system.setSystemCategory(systemCategoryMap.get(systemFromFile.getSystemCategoryId()));
                }
                system.setServerItf(serverItf);
                systemService.update(system);
                boolean isSharedWithImporting = system.getEnvironments().stream()
                        .anyMatch(environment1 -> environment.getId().equals(environment1.getId()));
                if (!isSharedWithImporting) {
                    systemService.share(system, Collections.singletonList(environment.getId()));
                }
            }
            importConnections(systemFromFile, invertedRepMap);
            log.info("End of import system {} for environment {}.", systemFromFile, environment);
        });
    }

    /**
     * Imports the connections from systems.
     *
     * @param systemFromFile system from file.
     */
    private void importConnections(System systemFromFile, Map<UUID, UUID> invertedRepMap) {
        List<Connection> connections = systemFromFile.getConnections();
        Map<UUID, org.qubership.atp.environments.model.Connection> originalConnections = connectionService.getByIds(
                connections.stream().map(Connection::getId).collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(Identified::getId, connection -> connection));
        connections.forEach(connectionFromFile -> {
            log.info("Imports connection {} for system {}.", connectionFromFile, systemFromFile);
            org.qubership.atp.environments.model.Connection connection =
                    originalConnections.get(connectionFromFile.getId());
            if (connection == null) {
                connection = connectionService.getByParentAndName(
                        connectionFromFile.getSystemId(), connectionFromFile.getName());
            }
            ConnectionParameters newConnectionParameters = new ConnectionParameters();
            newConnectionParameters.setValidation(false);
            newConnectionParameters.putAll(connectionFromFile.getParameters());
            if (connection == null) {
                connectionService.replicate(systemFromFile.getId(), connectionFromFile.getId(),
                        connectionFromFile.getName(),
                        connectionFromFile.getDescription(), newConnectionParameters,
                        connectionFromFile.getConnectionType(),
                        connectionFromFile.getSourceTemplateId(),
                        connectionFromFile.getServices(),
                        invertedRepMap.getOrDefault(connectionFromFile.getId(), null));
            } else {
                connection.setName(connectionFromFile.getName());
                connection.setDescription(connectionFromFile.getDescription());
                connection.setSourceTemplateId(connectionFromFile.getSourceTemplateId());
                connection.setConnectionType(connectionFromFile.getConnectionType());
                connection.setParameters(updateConnectionParameters(connection.getParameters(),
                        newConnectionParameters));
                connection.setSystemId(systemFromFile.getId());
                connection.setServices(connectionFromFile.getServices());
                connectionService.update(connection);
            }
            log.info("End of import connection {} for system {}.", connectionFromFile, systemFromFile);
        });
    }

    private ConnectionParameters updateConnectionParameters(ConnectionParameters existingParameters,
                                                            ConnectionParameters newParameters) {
        ConnectionParameters updatedParameters = new ConnectionParameters();
        updatedParameters.setValidation(false);
        for (Map.Entry<String, String> newParameter : newParameters.entrySet()) {
            String newKey = newParameter.getKey();
            String newValue = newParameter.getValue();
            if (existingParameters.containsKey(newKey)
                    && decryptor.isEncrypted(existingParameters.get(newKey))) {
                updatedParameters.put(newKey, existingParameters.get(newKey));
            } else {
                updatedParameters.put(newKey, newValue);
            }
        }
        return updatedParameters;
    }

    /**
     * Environments preValidating .
     *
     * @param workDir              directory with files for validation
     * @param isInterProjectImport interProject flag
     * @param repMap               id replacement map
     * @return messages with errors and warnings after validation
     */
    public List<String> preValidateEnvironments(Path workDir, boolean isInterProjectImport,
                                                Map<UUID, UUID> repMap) {
        List<String> result = new ArrayList<>();
        Map<UUID, Path> environmentsFiles = objectLoaderFromDiskService.getListOfObjects(workDir,
                Environment.class);
        environmentsFiles.forEach((id, path) -> {
            Environment environment = getEnvironmentFromFile(isInterProjectImport, repMap, path);
            log.debug("Pre validating object {}", environment);
            if (environment != null && !CollectionUtils.isEmpty(environment.getSystems())) {
                for (System system : environment.getSystems()) {
                    result.addAll(preValidateConnections(system, environment));
                }
            }
        });
        return result;
    }

    /**
     * Connections preValidating .
     *
     * @param system      system for validating.
     * @param environment environment for validating.
     * @return messages with errors and warnings after validation
     */
    public List<String> preValidateConnections(System system,
                                               Environment environment) {
        List<String> messages = new ArrayList<>();
        if (!CollectionUtils.isEmpty(system.getConnections())) {
            for (Connection connection : system.getConnections()) {
                try {
                    ConnectionParameters parameters = new ConnectionParameters();
                    parameters.putAll(connection.getParameters());
                    connectionService.validateTaEngineProviderParameters(connection.getSourceTemplateId(),
                            parameters);
                } catch (Exception e) {
                    String category = environment.getCategoryId()
                            .equals(Constants.Environment.Category.TOOL)
                            ? "TA Tool"
                            : "Environment";
                    messages.add(String.format("%s[Name:%s, ID:%s] "
                                    + " - System[Name:%s, ID:%s] "
                                    + " - Connection [Name:%s, ID:%s] ",
                            category, environment.getName(), environment.getId(),
                            system.getName(), system.getId(),
                            connection.getName(), connection.getId()
                                    + e.getMessage()));
                }
            }
        }
        return messages;
    }

    private Environment getEnvironmentFromFile(boolean isInterProjectImport, Map<UUID, UUID> repMap,
                                               Path path) {
        Environment environment;
        if (isInterProjectImport) {
            environment = objectLoaderFromDiskService.loadFileAsObjectWithReplacementMap(path,
                    Environment.class,
                    repMap);
        } else {
            environment = objectLoaderFromDiskService.loadFileAsObject(path, Environment.class);
        }
        return environment;
    }

    /**
     * Validates environments.
     *
     * @param workDir              directory with files for validation
     * @param isInterProjectImport interProject flag
     * @param repMap               id replacement map
     * @return messages with errors and warnings after validation
     */
    public List<String> validateEnvironments(Path workDir, boolean isInterProjectImport,
                                             Map<UUID, UUID> repMap) {
        log.info("Starts validateEnvironments(workDir: {})", workDir);
        List<String> result = new ArrayList<>();
        Map<UUID, Path> environmentsFiles = objectLoaderFromDiskService.getListOfObjects(workDir,
                Environment.class);
        environmentsFiles.forEach((id, path) -> {
            Environment object = getEnvironmentFromFile(isInterProjectImport, repMap, path);
            Map<UUID, org.qubership.atp.environments.model.System> originalSystems = getOriginalSystemMap(object);
            log.debug("importing object {}", object);
            if (object == null) {
                String message = String.format("Cannot load file by path %s", path.toString());
                log.error(message);
                result.add(message);
                return;
            }
            if (isNameUsed(object)) {
                String message = String.format(
                        "Environment with name '%s' already exists under the project. "
                                + "Imported one will be renamed to '%s Copy'",
                        object.getName(), object.getName());
                result.add(message);
                for (System system : object.getSystems()) {
                    if (duplicateNameChecker.isSystemNameUsed(object, system, originalSystems.get(system.getId()))) {
                        String systemMessage = String.format(
                                "System with name '%s' already exists under the environment '%s'. "
                                        + "Imported one will be renamed to '%s Copy'",
                                system.getName(), object.getName(), system.getName());
                        result.add(systemMessage);
                    }
                }
            }
        });
        return result;
    }

    /**
     * Gets DSL ids.
     *
     * @param workDir the work dir
     * @return the object ids
     */
    public List<UUID> getObjectIds(Path workDir) {
        Map<UUID, Path> environmentsFiles = objectLoaderFromDiskService.getListOfObjects(workDir,
                Environment.class);
        List<UUID> objectIds = new ArrayList<>(environmentsFiles.keySet());
        environmentsFiles.forEach((id, path) -> {
            Environment object = objectLoaderFromDiskService.loadFileAsObject(path,
                    Environment.class);
            object.getSystems().forEach(system -> {
                objectIds.add(system.getId());
                objectIds.addAll(system.getConnections().stream()
                        .map(Connection::getId).collect(Collectors.toList()));
            });
        });
        return objectIds;
    }

    /**
     * Fills Replacement map with envs, systems and connections source-target values.
     *
     * @param replacementMap the replacement map
     * @param workDir        the work dir
     */
    public void fillRepMapWithSourceTargetValues(Map<UUID, UUID> replacementMap,
                                                 Path workDir) {
        Map<UUID, Path> objectsToImport = objectLoaderFromDiskService.getListOfObjects(workDir,
                Environment.class);
        objectsToImport.forEach((id, path) -> {
            if (!replacementMap.containsKey(id)) {
                Environment object = objectLoaderFromDiskService.loadFileAsObject(objectsToImport.get(id),
                        Environment.class);
                org.qubership.atp.environments.model.Environment existingObject =
                        environmentService.getBySourceIdAndProjectId(id,
                                replacementMap.get(object.getProjectId()));
                if (existingObject == null) {
                    replacementMap.put(id, null);
                    object.getSystems().forEach(sys -> {
                                replacementMap.put(sys.getId(), null);
                                sys.getConnections().forEach(con -> replacementMap.put(con.getId(), null));
                            }
                    );
                } else {
                    replacementMap.put(id, existingObject.getId());
                    Map<UUID, org.qubership.atp.environments.model.System> existingSourceIdSystemsMap = existingObject
                            .getSystems().stream().filter(system -> system.getSourceId() != null)
                            .collect(Collectors.toMap(Sourced::getSourceId, Function.identity()));
                    object.getSystems().forEach(importSys -> {
                        if (existingSourceIdSystemsMap.containsKey(importSys.getId())) {
                            org.qubership.atp.environments.model.System existingSys =
                                    existingSourceIdSystemsMap.get(importSys.getId());
                            collectCollectionSourceTargetMapForExistingSystem(replacementMap, importSys, existingSys);
                        } else {
                            org.qubership.atp.environments.model.System sysWithoutEnv =
                                    systemService.getBySourceId(importSys.getId());
                            if (sysWithoutEnv == null) {
                                replacementMap.put(importSys.getId(), null);
                                importSys.getConnections().forEach(con -> replacementMap.put(con.getId(), null));
                            } else {
                                collectCollectionSourceTargetMapForExistingSystem(replacementMap,
                                        importSys, sysWithoutEnv);
                            }
                        }
                    });
                }
            }
        });
    }

    private void collectCollectionSourceTargetMapForExistingSystem(Map<UUID, UUID> replacementMap,
                                                                   System importSys,
                                                                   org.qubership.atp.environments.model.System
                                                                           sysWithoutEnv) {
        replacementMap.put(importSys.getId(), sysWithoutEnv.getId());
        Map<UUID, org.qubership.atp.environments.model.Connection> existingSourceIdConnectionMap =
                sysWithoutEnv
                        .getConnections().stream().filter(system -> system.getSourceId() != null)
                        .collect(Collectors.toMap(Sourced::getSourceId, Function.identity()));
        importSys.getConnections().forEach(
                importConn -> {
                    if (existingSourceIdConnectionMap.containsKey(importConn.getId())) {
                        replacementMap.put(importConn.getId(),
                                existingSourceIdConnectionMap
                                        .get(importConn.getId()).getId());
                    } else {
                        replacementMap.put(importConn.getId(), null);
                    }
                }
        );
    }

    private Map<UUID, org.qubership.atp.environments.model.System> getOriginalSystemMap(Environment environment) {
        if (environment == null || CollectionUtils.isEmpty(environment.getSystems())) {
            return Collections.emptyMap();
        }
        List<System> systemsFromFile = environment.getSystems();
        return systemService.getByIds(systemsFromFile
                .stream()
                .map(System::getId).collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(Identified::getId, system -> system));
    }
}
