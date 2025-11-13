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

package org.qubership.atp.environments.service.direct.impl;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.qubership.atp.auth.springbootstarter.entities.UserInfo;
import org.qubership.atp.auth.springbootstarter.ssl.Provider;
import org.qubership.atp.environments.enums.MdcField;
import org.qubership.atp.environments.errorhandling.request.EnvironmentsWithFilterRequestException;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.Identified;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.utils.Constants;
import org.qubership.atp.environments.model.utils.HazelcastMapName;
import org.qubership.atp.environments.repo.impl.ConnectionRepositoryImpl;
import org.qubership.atp.environments.repo.impl.EnvironmentRepositoryImpl;
import org.qubership.atp.environments.repo.impl.SystemCategoryRepositoryImpl;
import org.qubership.atp.environments.repo.impl.SystemRepositoryImpl;
import org.qubership.atp.environments.service.direct.EnvironmentService;
import org.qubership.atp.environments.service.direct.ProjectAccessService;
import org.qubership.atp.environments.service.direct.SystemService;
import org.qubership.atp.environments.service.rest.server.dto.BaseSearchRequestDto;
import org.qubership.atp.environments.service.rest.server.dto.ConnectionTemporaryDto;
import org.qubership.atp.environments.service.rest.server.dto.CreateSystemDto;
import org.qubership.atp.environments.service.rest.server.dto.SystemTemporaryDto;
import org.qubership.atp.environments.service.rest.server.request.EnvironmentsWithFilterRequest;
import org.qubership.atp.environments.service.rest.server.request.ValidateTaToolsRequest;
import org.qubership.atp.environments.service.rest.server.response.GroupedByTagEnvironmentResponse;
import org.qubership.atp.environments.service.rest.server.response.ValidateTaToolResponse;
import org.qubership.atp.environments.service.rest.server.response.ValidateTaToolsResponse;
import org.qubership.atp.environments.utils.DateTimeUtil;
import org.qubership.atp.environments.validating.factories.ValidationStrategyFactory;
import org.qubership.atp.environments.validating.strategies.ValidationStrategy;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;
import j2html.TagCreator;
import j2html.tags.ContainerTag;
import joptsimple.internal.Strings;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("environmentService")
@SuppressWarnings("CPD-START")
public class EnvironmentServiceImpl implements EnvironmentService {

    private static final String NO_TAG = "No Tags";
    private final EnvironmentRepositoryImpl environmentRepository;
    private final SystemRepositoryImpl systemRepository;
    private final ConnectionRepositoryImpl connectionRepository;
    private final SystemService systemService;
    private final SystemCategoryRepositoryImpl systemCategoryRepository;
    private final DateTimeUtil dateTimeUtil;
    private final Provider<UserInfo> userInfoProvider;
    private final UUID categoryId = Constants.Environment.Category.TEMPORARY_ENVIRONMENT;
    private final String trStyle = "border-collapse: collapse;";
    private final String tdStyle = "border: 1px solid rgb(0,0,0)";
    private final ProjectAccessService projectAccessService;
    private final Cache environmentsBySystemIdCachedMap;
    private final ValidationStrategyFactory validationStrategyFactory;

    /**
     * Class constructor.
     */
    @Autowired
    public EnvironmentServiceImpl(EnvironmentRepositoryImpl environmentRepository,
                                  SystemRepositoryImpl systemRepository,
                                  ConnectionRepositoryImpl connectionRepository,
                                  SystemService systemService,
                                  SystemCategoryRepositoryImpl systemCategoryRepository,
                                  DateTimeUtil dateTimeUtil,
                                  Provider<UserInfo> userInfoProvider,
                                  ProjectAccessService projectAccessService,
                                  CacheManager cacheManager,
                                  ValidationStrategyFactory validationStrategyFactory) {
        this.environmentRepository = environmentRepository;
        this.systemRepository = systemRepository;
        this.connectionRepository = connectionRepository;
        this.systemService = systemService;
        this.systemCategoryRepository = systemCategoryRepository;
        this.dateTimeUtil = dateTimeUtil;
        this.userInfoProvider = userInfoProvider;
        this.projectAccessService = projectAccessService;
        this.validationStrategyFactory = validationStrategyFactory;
        this.environmentsBySystemIdCachedMap = cacheManager != null
                && cacheManager.getCache(HazelcastMapName.ENVIRONMENTS_BY_SYSTEM_ID) != null
                ? cacheManager.getCache(HazelcastMapName.ENVIRONMENTS_BY_SYSTEM_ID)
                : new NoOpCache(HazelcastMapName.ENVIRONMENTS_BY_SYSTEM_ID);
    }

    @Nullable
    @Override
    public Environment get(@Nonnull UUID id) {
        Environment env = environmentRepository.getById(id);
        Preconditions.checkNotNull(env, "Wrong environment id: %s", id);
        return env;
    }

    @Nullable
    @Override
    public String getEnvironmentNameById(@Nonnull UUID id) {
        return environmentRepository.getNameById(id);
    }

    @Override
    public Optional<Environment> getOrElse(@Nonnull UUID id) {
        return Optional.ofNullable(environmentRepository.getById(id));
    }

    @Override
    public boolean existsById(@Nonnull UUID id) {
        return environmentRepository.existsById(id);
    }

    @Nonnull
    @Override
    public List<Environment> getAll() {
        return environmentRepository.getAll();
    }

    @Nonnull
    public List<Environment> getAll(UUID categoryId) {
        return environmentRepository.getAll(categoryId);
    }

    @Nonnull
    @Override
    @Transactional
    public Environment create(UUID projectId, String name, String graylogName, String description,
                              String ssmSolutionAlias, String ssmInstanceAlias,
                              String consulEgressConfigPath, UUID categoryId,
                              List<String> tags) {
        environmentRepository.getContext().setFullDbFetching(true);
        UUID userId = userInfoProvider.get().getId();
        return environmentRepository.create(name.trim(),
                graylogName,
                description,
                ssmSolutionAlias,
                ssmInstanceAlias,
                consulEgressConfigPath,
                dateTimeUtil.timestampAsUtc(),
                userId,
                projectId,
                categoryId,
                tags);
    }

    @Override
    @Transactional
    public System create(UUID environmentId, CreateSystemDto systemDto) {
        environmentRepository.getContext().setFullDbFetching(true);
        return systemService.create(environmentId, systemDto);
    }

    @Nonnull
    @Override
    @Transactional
    public Environment replicate(@Nonnull UUID projectId, @Nonnull UUID environmentId, String name, String graylogName,
                                 String description, String ssmSolutionAlias, String ssmInstanceAlias,
                                 String consulEgressConfigPath, UUID categoryId,
                                 UUID sourceId, List<String> tags) {
        environmentRepository.getContext().setFullDbFetching(true);
        UUID userId = userInfoProvider.get().getId();
        return environmentRepository.create(environmentId, name.trim(), graylogName, description, ssmSolutionAlias,
                ssmInstanceAlias, consulEgressConfigPath, dateTimeUtil.timestampAsUtc(), userId, projectId,
                categoryId, sourceId, tags);
    }

    private HashMap<UUID, UUID> getMapReferencesToCopy(Environment environmentToCopy, UUID newEnvironmentId) {
        HashMap<UUID, UUID> referencesMap = new HashMap<>();
        environmentToCopy.getSystems().stream()
                .filter(system -> system.getLinkToSystemId() != null)
                .forEach(system -> {
                    if (!referencesMap.containsKey(system.getLinkToSystemId())) {
                        System parentSystem = getParentSystem(environmentToCopy, system.getLinkToSystemId());
                        if (nonNull(parentSystem)) {
                            referencesMap.put(parentSystem.getId(),
                                    createSystem(newEnvironmentId, parentSystem).getId());
                        }
                    }
                });
        environmentToCopy.getSystems().removeIf(system -> referencesMap.containsKey(system.getId()));
        return referencesMap;
    }

    private System getParentSystem(Environment environment, UUID linkToSystemId) {
        if (!isNull(environment.getSystems())) {
            for (System system : environment.getSystems()) {
                if (system.getId().equals(linkToSystemId)) {
                    return system;
                }
            }
        }
        return null;
    }

    @Override
    @Transactional
    public Environment copy(UUID id, UUID projectId, String name, String graylogName, String description,
                            String ssmSolutionAlias, String ssmInstanceAlias, String consulEgressConfigPath,
                            UUID categoryId, List<String> tags) {
        environmentRepository.getContext().setFullDbFetching(true);
        UUID userId = userInfoProvider.get().getId();
        Environment toCopy = environmentRepository.getById(id);
        Preconditions.checkNotNull(toCopy, "Environment %s can't be empty", id);
        Environment newEnv = environmentRepository.create(name,
                graylogName,
                description,
                ssmSolutionAlias,
                ssmInstanceAlias,
                consulEgressConfigPath,
                dateTimeUtil.timestampAsUtc(),
                userId,
                projectId,
                categoryId,
                tags);
        HashMap<UUID, UUID> referencesMap = getMapReferencesToCopy(toCopy, newEnv.getId());
        toCopy.getSystems().forEach(system -> {
            if (system.getEnvironments().size() > 1 && toCopy.getProjectId().equals(projectId)) {
                systemRepository.share(system.getId(), newEnv, dateTimeUtil.timestampAsUtc(), userId);
            } else {
                if (nonNull(system.getLinkToSystemId())) {
                    system.setLinkToSystemId(referencesMap.get(system.getLinkToSystemId()));
                }
                createSystem(newEnv.getId(), system);
            }
        });
        return newEnv;
    }

    /**
     * Creating a system with connections.
     */
    @Override
    @Transactional
    public System createSystem(UUID environmentId, System system) {
        environmentRepository.getContext().setFullDbFetching(true);
        UUID userId = userInfoProvider.get().getId();
        System newSys = systemRepository.create(environmentId,
                system.getName(),
                system.getDescription(),
                dateTimeUtil.timestampAsUtc(),
                userId,
                system.getSystemCategoryId(),
                system.getParametersGettingVersion(),
                system.getParentSystemId(),
                system.getServerItf(),
                system.getMergeByName(),
                system.getLinkToSystemId(),
                system.getExternalId(),
                system.getExternalName());
        system.getConnections().forEach(connection -> connectionRepository.create(newSys.getId(),
                connection.getName(),
                connection.getDescription(),
                connection.getParameters(),
                dateTimeUtil.timestampAsUtc(),
                userId,
                connection.getConnectionType(),
                connection.getSourceTemplateId(),
                connection.getServices(), connection.getSourceId()));
        return newSys;
    }

    /**
     * Creating a system with connections for temporary environments.
     */
    private void createSystem(UUID environmentId, SystemTemporaryDto system) {
        environmentRepository.getContext().setFullDbFetching(true);
        UUID userId = userInfoProvider.get().getId();
        System newSys = systemRepository.create(environmentId,
                system.getName(),
                null,
                dateTimeUtil.timestampAsUtc(),
                userId,
                StringUtils.isNotBlank(system.getSystemCategory())
                        ? systemCategoryRepository.getByName(system.getSystemCategory()).getId() : null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        system.getConnections().forEach(connection -> {
            createConnection(newSys.getId(), connection);
        });
    }

    private void createConnection(UUID systemId, ConnectionTemporaryDto connection) {
        environmentRepository.getContext().setFullDbFetching(true);
        UUID userId = userInfoProvider.get().getId();
        Connection connNew = connectionRepository.getConnectionTemplateByName(connection.getName());
        ConnectionParameters parameters = connection.getParameters();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            connNew.getParameters().put(entry.getKey(), entry.getValue());
        }
        connectionRepository.create(systemId,
                connNew.getName(),
                null,
                connNew.getParameters(),
                dateTimeUtil.timestampAsUtc(),
                userId,
                null,
                connNew.getId(),
                connNew.getServices(), connNew.getSourceId());
    }

    /**
     * Update a system with connections for temporary environments.
     */
    private void updateSystem(System system,
                              String systemCategory,
                              List<ConnectionTemporaryDto> connection) {
        environmentRepository.getContext().setFullDbFetching(true);
        UUID userId = userInfoProvider.get().getId();
        UUID systemCategoryId = StringUtils.isNotBlank(systemCategory)
                ? systemCategoryRepository.getByName(systemCategory).getId()
                : null;
        systemRepository.updateSystemCategory(system.getId(), systemCategoryId, dateTimeUtil.timestampAsUtc(), userId);
        system.getConnections().forEach(conn -> {
            Optional<ConnectionTemporaryDto> connectionUpdate =
                    connection.stream().filter(connUpd -> connUpd.getName().equals(conn.getName())).findAny();
            if (connectionUpdate.isPresent()) {
                updateConnection(conn.getId(), connectionUpdate.get());
                connection.remove(connectionUpdate.get());
            }
        });
        connection.forEach(connNew -> createConnection(system.getId(), connNew));
    }

    private void updateConnection(UUID id, ConnectionTemporaryDto connection) {
        environmentRepository.getContext().setFullDbFetching(true);
        UUID userId = userInfoProvider.get().getId();
        Connection connUpdate = connectionRepository.getById(id);
        ConnectionParameters parameters = connection.getParameters();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            connUpdate.getParameters().put(entry.getKey(), entry.getValue());
        }
        connectionRepository.update(id,
                connUpdate.getSystemId(),
                connUpdate.getName(),
                connUpdate.getDescription(),
                connUpdate.getParameters(),
                dateTimeUtil.timestampAsUtc(),
                userId,
                connUpdate.getConnectionType(),
                connUpdate.getSourceTemplateId(),
                connUpdate.getServices(), connUpdate.getSourceId());
    }

    @Override
    @Nonnull
    @Transactional
    public Environment temporary(UUID id, List<SystemTemporaryDto> systemList) {
        environmentRepository.getContext().setFullDbFetching(true);
        UUID userId = userInfoProvider.get().getId();
        Environment sourceEnv = environmentRepository.getById(id);
        Preconditions.checkNotNull(sourceEnv, "Environment %s can't be empty", id);
        Environment temporaryEnv =
                environmentRepository.create(sourceEnv.getName() + " " + dateTimeUtil.dataTimeAsUtc(),
                        sourceEnv.getGraylogName(),
                        sourceEnv.getDescription(),
                        sourceEnv.getSsmSolutionAlias(),
                        sourceEnv.getSsmInstanceAlias(),
                        sourceEnv.getConsulEgressConfigPath(),
                        dateTimeUtil.timestampAsUtc(),
                        userId,
                        sourceEnv.getProjectId(),
                        this.categoryId,
                        sourceEnv.getTags());
        sourceEnv.getSystems().forEach(system -> {
            System toCopy = createSystem(temporaryEnv.getId(), system);
            Optional<SystemTemporaryDto> toMerge = systemList.stream()
                    .filter(sysUpd -> sysUpd.getName().equals(toCopy.getName()))
                    .findAny();
            if (toMerge.isPresent()) {
                updateSystem(toCopy, toMerge.get().getSystemCategory(), toMerge.get().getConnections());
                systemList.remove(toMerge.get());
            }
        });
        systemList.forEach(sysNew -> createSystem(temporaryEnv.getId(), sysNew));
        return temporaryEnv;
    }

    @Override
    public UUID getProjectIdBySystemId(UUID systemId) {
        Preconditions.checkNotNull(systemId, "System id can't be empty");
        return environmentRepository.getProjectIdBySystemId(systemId);
    }

    @Override
    public UUID getProjectIdByEnvironmentId(@Nonnull UUID environmentId) {
        return environmentRepository.getProjectIdByEnvironmentId(environmentId);
    }

    @Override
    public List<Environment> getByProjectId(@NotNull UUID projectId) {
        return environmentRepository.getAllByParentId(projectId);
    }

    @Override
    public Environment getByNameAndProjectId(String name, UUID projectId) {
        return environmentRepository.getByNameAndProjectId(name, projectId);
    }

    @Override
    public List<Environment> findBySearchRequest(BaseSearchRequestDto searchRequest) throws Exception {
        return environmentRepository.findBySearchRequest(searchRequest, projectAccessService.getProjectIdsWithAccess());
    }

    @Override
    public void update(Environment environment) {
        environmentRepository.getContext().setFullDbFetching(true);
        update(environment.getId(), environment.getName().trim(), environment.getGraylogName(),
                environment.getDescription(), environment.getSsmSolutionAlias(), environment.getSsmInstanceAlias(),
                environment.getConsulEgressConfigPath(), environment.getProjectId(),
                environment.getCategoryId(), environment.getTags());
    }

    @Override
    @Transactional
    public void update(UUID id, String name, String graylogName, String description, String ssmSolutionAlias,
                       String ssmInstanceAlias, String consulEgressConfigPath, UUID projectId,
                       UUID categoryId, List<String> tags) {
        environmentRepository.getContext().setFullDbFetching(true);
        UUID userId = userInfoProvider.get().getId();
        environmentRepository.update(id, name.trim(), graylogName, description, ssmSolutionAlias, ssmInstanceAlias,
                consulEgressConfigPath, dateTimeUtil.timestampAsUtc(), userId, projectId, categoryId, tags);
        removeCacheEntriesWithEnvironment(id);
    }

    @Override
    @Transactional
    public void delete(UUID environmentId) {
        UUID userId = userInfoProvider.get().getId();
        environmentRepository.delete(environmentId, dateTimeUtil.timestampAsUtc(), userId);
        removeCacheEntriesWithEnvironment(environmentId);
    }

    private void removeCacheEntriesWithEnvironment(UUID environmentId) {
        getSystemIdsByEnvironmentCacheId(environmentId)
                .forEach(environmentsBySystemIdCachedMap::evictIfPresent);
    }

    private List<UUID> getSystemIdsByEnvironmentCacheId(UUID environmentId) {
        return getSystems(environmentId).stream().map(Identified::getId).collect(toList());
    }

    @Override
    public List<System> getSystems(UUID environmentId) {
        return systemRepository.getAllByParentId(environmentId);
    }

    @Override
    public List<System> getSystems(UUID environmentId, String systemType) {
        return systemRepository.getAllByParentId(environmentId, systemType);
    }

    @Override
    public List<System> getShortSystems(UUID environmentId) {
        return systemRepository.getAllShortByParentId(environmentId);
    }

    @Override
    public Collection<System> getSystemsV2(UUID environmentId) {
        log.info("Get systems for environment with id '{}'", environmentId);
        Collection<System> foundedSystems = systemRepository.getAllByParentIdV2(environmentId);
        log.info("Found systems with ids '{}'", foundedSystems.stream().map(System::getId).collect(toList()));
        return foundedSystems;
    }

    @Override
    public Collection<System> getSystemsV2(UUID environmentId, String systemType) {
        log.info("Get systems for environment with id '{}' and type '{}'", environmentId, systemType);
        return systemRepository.getAllByParentIdV2(environmentId, systemType);
    }

    @Override
    public Environment getBySourceIdAndProjectId(UUID sourceId, UUID projectId) {
        log.debug("Get environment by source id '{}' and project id '{}'", sourceId, projectId);
        return environmentRepository.getBySourceIdAndProjectId(sourceId, projectId);
    }

    @Override
    public List<Environment> getByIds(List<UUID> environmentIds) {
        List<Environment> environments = environmentRepository.getByIds(environmentIds);
        return environments != null ? environments : Collections.emptyList();
    }

    @Override
    public String getHtmlVersionByEnvironments(List<UUID> environmentIds) {
        environmentRepository.getContext().setFullDbFetching(true);
        List<Environment> environments = getByIds(environmentIds);
        if (isNull(environments) || environments.size() != environmentIds.size()) {
            return Strings.EMPTY;
        }
        ContainerTag tableTagWithVersion = TagCreator.body();
        ContainerTag table = TagCreator.table().withStyle(trStyle);
        for (Environment environment : environments) {
            MDC.put(MdcField.ENVIRONMENT_ID.toString(), environment.getId().toString());
            table.with(TagCreator.tr(TagCreator.td(TagCreator.b(environment.getName()))));
            if (!CollectionUtils.isEmpty(environment.getSystems())) {
                for (System system : environment.getSystems()) {
                    ContainerTag tableLine =
                            TagCreator.tr(TagCreator.td(system.getName()).withStyle(tdStyle)).withStyle(trStyle);
                    try {
                        String version = systemService.getCachedVersionBySystem(system).getVersion();
                        tableLine.with(TagCreator.td(version).withStyle(tdStyle));
                    } catch (Exception e) {
                        String message = String.format("Error occurred while getting version of %s", system.getName());
                        log.error(message, e);
                        tableLine.with(TagCreator.td(message).withStyle(tdStyle));
                    }
                    table.with(tableLine);
                }
            }
        }
        tableTagWithVersion.with(table);
        return TagCreator.html(tableTagWithVersion)
                .render();
    }

    @Override
    public ValidateTaToolsResponse validateTaTools(ValidateTaToolsRequest request) {
        environmentRepository.getContext().setFullDbFetching(true);
        List<ValidateTaToolResponse> toolResponses = new ArrayList<>();
        ValidationStrategy strategy = validationStrategyFactory.createStrategy(ValidationStrategyFactory.ITF_LITE);
        if (!CollectionUtils.isEmpty(request.getToolIds())) {
            List<Environment> taTools = environmentRepository.getByIds(request.getToolIds());
            toolResponses.addAll(taTools.stream().map(strategy::validate).collect(toList()));
        }
        return new ValidateTaToolsResponse(toolResponses);
    }



    @Override
    @Nullable
    public List<Connection> getConnections(UUID environmentId) {
        return connectionRepository.getAllByEnvironmentId(environmentId);
    }

    @Override
    @Nonnull
    public byte[] getSystemsYamlZipArchive(@Nonnull UUID environmentId, @Nullable String systemType) {
        log.debug("Generating systems YAML ZIP archive for environment '{}' with system type '{}'", 
                environmentId, systemType);
        
        // Fetch systems data
        Collection<System> systems;
        if (systemType != null) {
            systems = getSystemsV2(environmentId, systemType);
        } else {
            systems = getSystemsV2(environmentId);
        }
        
        if (CollectionUtils.isEmpty(systems)) {
            log.warn("No systems found for environment '{}'", environmentId);
            // Return empty ZIP archive with empty YAML files
            systems = Collections.emptyList();
        }
        
        // Generate YAML files
        String[] yamlFiles = systemService.generateSystemsYaml(systems);
        String deploymentParamsYaml = yamlFiles[0];
        String credentialsYaml = yamlFiles[1];
        
        // Create ZIP archive
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {
            
            // Add deployment-parameters.yaml
            ZipEntry deploymentEntry = new ZipEntry("deployment-parameters.yaml");
            zos.putNextEntry(deploymentEntry);
            zos.write(deploymentParamsYaml.getBytes("UTF-8"));
            zos.closeEntry();
            
            // Add credentials.yaml
            ZipEntry credentialsEntry = new ZipEntry("credentials.yaml");
            zos.putNextEntry(credentialsEntry);
            zos.write(credentialsYaml.getBytes("UTF-8"));
            zos.closeEntry();
            
            zos.finish();
            byte[] zipBytes = baos.toByteArray();
            log.info("Successfully generated ZIP archive with {} bytes for environment '{}'", 
                    zipBytes.length, environmentId);
            return zipBytes;
            
        } catch (IOException e) {
            log.error("Error creating ZIP archive for environment '{}'", environmentId, e);
            throw new RuntimeException("Failed to create ZIP archive", e);
        }
    }

    @Override
    public List<Environment> getEnvironmentsByFilterRequest(EnvironmentsWithFilterRequest request,
                                                            Integer page, Integer size) {
        if (CollectionUtils.isEmpty(request.getFields())) {
            throw new EnvironmentsWithFilterRequestException(request, "\nFields can not be empty");
        }
        Integer offset = null;
        if (page != null && size != null) {
            offset = (page - 1) * size;
        }
        return environmentRepository.getEnvironmentsByFilterPaged(request, size, offset);
    }

    @Override
    public Collection<GroupedByTagEnvironmentResponse> getGroupedByTagEnvironments(UUID projectId) {
        List<Environment> environments = environmentRepository.getAllShortByParentId(
                projectId,
                Constants.Environment.Category.ENVIRONMENT
        );
        if (CollectionUtils.isEmpty(environments)) {
            return new ArrayList<>();
        }
        Map<String, GroupedByTagEnvironmentResponse> resultMap = new HashMap<>();
        GroupedByTagEnvironmentResponse noTagResponse =
                new GroupedByTagEnvironmentResponse(new ArrayList<>(), NO_TAG);
        for (Environment environment: environments) {
            List<String> tags = environment.getTags();
            if (!CollectionUtils.isEmpty(tags)) {
                tags.forEach(tag -> processEnvironmentByTag(environment, tag, resultMap));
                continue;
            }
            noTagResponse.getEnvironments().add(environment);
        }
        Collection<GroupedByTagEnvironmentResponse> result = new ArrayList<>(resultMap.values());
        result.add(noTagResponse);
        return result;
    }

    private void processEnvironmentByTag(Environment environment,
                                          String tag,
                                          Map<String, GroupedByTagEnvironmentResponse> resultMap) {
        if (!resultMap.containsKey(tag)) {
            resultMap.put(tag, new GroupedByTagEnvironmentResponse(new ArrayList<>(), tag));
        }
        resultMap.get(tag).getEnvironments().add(environment);
    }

    @Override
    public long getEnvironmentsCountByFilter(EnvironmentsWithFilterRequest request) {
        return environmentRepository.getEnvironmentsCountByFilter(request);
    }
}
