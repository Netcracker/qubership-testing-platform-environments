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

package org.qubership.atp.environments.ei;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.qubership.atp.crypt.api.Decryptor;
import org.qubership.atp.ei.node.ExportExecutor;
import org.qubership.atp.ei.node.dto.ExportImportData;
import org.qubership.atp.ei.node.exceptions.ExportException;
import org.qubership.atp.ei.node.services.ObjectSaverToDiskService;
import org.qubership.atp.ei.ntt.impl.NttProjectConverter;
import org.qubership.atp.environments.ei.model.Environment;
import org.qubership.atp.environments.ei.model.PostmanEnvironment;
import org.qubership.atp.environments.ei.model.PostmanValue;
import org.qubership.atp.environments.enums.UserManagementEntities;
import org.qubership.atp.environments.model.SystemCategory;
import org.qubership.atp.environments.service.direct.EnvironmentService;
import org.qubership.atp.environments.service.direct.SystemCategoriesService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class EnvironmentsExportExecutor implements ExportExecutor {

    @Value("${spring.application.name}")
    private String implementationName;

    private final EnvironmentService environmentService;
    private final SystemCategoriesService systemCategoriesService;
    private final ObjectSaverToDiskService objectSaverToDiskService;
    private final Decryptor decryptor;

    private static final String POSTMAN_KEY_TEMPLATE = "ENV.%s.%s.%s";

    /**
     * Export data to json files.
     *
     * @param exportData project id and id's of environments.
     * @param workDir    directory for export.
     */
    @Override
    public void exportToFolder(ExportImportData exportData, Path workDir) throws ExportException {
        log.info("Start export by request {}", exportData);
        Set<String> environmentsStringIds =
                exportData.getExportScope().getEntities().getOrDefault(
                        ServiceScopeEntities.ENTITY_ENVIRONMENTS.getValue(), new HashSet<>());
        environmentsStringIds.addAll(
                exportData.getExportScope().getEntities().getOrDefault(ServiceScopeEntities.ENTITY_TA_TOOLS.getValue(),
                        new HashSet<>()));
        Set<UUID> environmentsIds =
                environmentsStringIds.stream().map(UUID::fromString).collect(Collectors.toSet());
        switch (exportData.getFormat()) {
            case ATP:
                defaultExportToFolder(workDir, environmentsIds);
                break;
            case NTT:
                defaultExportToFolder(workDir, environmentsIds);
                log.info("Converting to ntt project. Project id {}", exportData.getProjectId());
                convertToNttFormat(workDir);
                break;
            case POSTMAN:
                postmanExportToFolder(workDir, environmentsIds);
                break;
            default:
                log.error("Export data unformatted: {}", exportData.getFormat());
        }

        log.info("Project {} was exported. Request {}", exportData.getProjectId(), exportData);
    }

    /**
     * Postman export data.
     */
    public void postmanExportToFolder(Path workDir, Set<UUID> environmentsIds) {
        List<org.qubership.atp.environments.model.Environment> environments =
                environmentService.getByIds(new ArrayList<>(environmentsIds));

        environments.stream()
                .filter(environment -> environment.getSystems() != null)
                .forEach(env -> {
                    PostmanEnvironment postmanEnvironment =
                            new PostmanEnvironment(env.getId(), env.getName(), new ArrayList<PostmanValue>());
                    List<PostmanValue> postmanValueList = postmanEnvironment.getValues();
                    env.getSystems().stream()
                            .filter(system -> system.getConnections() != null)
                            .forEach(system -> {
                                system.getConnections().stream()
                                        .filter(connection -> connection.getParameters() != null)
                                        .forEach(connection -> {
                                            connection.getParameters().forEach((key, value) -> {
                                                PostmanValue postmanValue = new PostmanValue();
                                                postmanValue.setKey(String.format(POSTMAN_KEY_TEMPLATE,
                                                        system.getName(), connection.getName(), key));
                                                if (decryptor.isEncrypted(value)) {
                                                    postmanValue.setValue("");
                                                } else {
                                                    postmanValue.setValue(value);
                                                }
                                                postmanValueList.add(postmanValue);
                                            });
                                        });
                            });
                    objectSaverToDiskService.writeAtpEntityToFile(env.getName() + "."
                                    + UserManagementEntities.ENVIRONMENT.name().toLowerCase(Locale.getDefault()),
                            postmanEnvironment,
                            UserManagementEntities.ENVIRONMENT.name().toLowerCase(Locale.getDefault()), workDir, true);
                });
    }

    /**
     * Atp and Ntt type export data.
     */
    public void defaultExportToFolder(Path workDir, Set<UUID> environmentsIds) {
        ModelMapper modelMapper = new ModelMapper();
        environmentService.getByIds(new ArrayList<>(environmentsIds))
                .stream()
                .filter(Objects::nonNull)
                .map(modelEnv -> modelMapper.map(modelEnv, Environment.class))
                .forEach(eiEnv -> {
                    if (eiEnv.getSystems() != null) {
                        eiEnv.getSystems().stream().filter(system -> system.getConnections() != null)
                                .forEach(system -> system.getConnections().stream()
                                        .filter(connection -> connection.getParameters() != null)
                                        .forEach(connection -> {
                                            connection.getParameters().entrySet().stream()
                                                    .filter(pair -> decryptor.isEncrypted(pair.getValue()))
                                                    .forEach(pair -> connection.getParameters()
                                                            .put(pair.getKey(), ""));
                                        }));
                    } // clear encrypted values
                    objectSaverToDiskService.exportAtpEntity(eiEnv.getId(), eiEnv, workDir);
                });
    }

    private void convertToNttFormat(Path workDir) throws ExportException {
        Map<UUID, String> categoryMap = new HashMap<>();
        List<SystemCategory> allCategories = systemCategoriesService.getAll();
        allCategories.forEach(systemCategory -> categoryMap.put(systemCategory.getId(), systemCategory.getName()));
        log.info("Starting to convert to ntt using export source dir {}", workDir);
        try {
            new NttProjectConverter(workDir).convertEnvironment(categoryMap).saveToFolder(workDir);
        } catch (Exception e) {
            ExportException.throwException("Cannot convert export to NTT format. Source {}", workDir, e);
        }
    }

    @Override
    public String getExportImplementationName() {
        return implementationName;
    }
}
