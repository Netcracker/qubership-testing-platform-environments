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

package org.qubership.atp.environments.service.ei;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.qubership.atp.crypt.api.Decryptor;
import org.qubership.atp.ei.node.dto.ExportImportData;
import org.qubership.atp.ei.node.dto.ValidationResult;
import org.qubership.atp.ei.node.dto.validation.ValidationType;
import org.qubership.atp.ei.node.services.ObjectLoaderFromDiskService;
import org.qubership.atp.environments.ei.EnvironmentsImportExecutor;
import org.qubership.atp.environments.ei.service.DuplicateNameChecker;
import org.qubership.atp.environments.ei.service.EnvironmentsImporter;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.Project;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.impl.ConnectionImpl;
import org.qubership.atp.environments.model.impl.EnvironmentImpl;
import org.qubership.atp.environments.model.impl.ProjectImpl;
import org.qubership.atp.environments.model.impl.SystemImpl;
import org.qubership.atp.environments.model.utils.Constants;
import org.qubership.atp.environments.model.utils.enums.Status;
import org.qubership.atp.environments.service.direct.ConnectionService;
import org.qubership.atp.environments.service.direct.EnvironmentService;
import org.qubership.atp.environments.service.direct.ProjectService;
import org.qubership.atp.environments.service.direct.SystemCategoriesService;
import org.qubership.atp.environments.service.direct.SystemService;

public class EnvironmentsImportExecutorTest {

    private final ThreadLocal<EnvironmentService> environmentService = new ThreadLocal<>();
    private final ThreadLocal<SystemService> systemService = new ThreadLocal<>();
    private final ThreadLocal<SystemCategoriesService> systemCategoriesService = new ThreadLocal<>();
    private final ThreadLocal<ConnectionService> connectionService = new ThreadLocal<>();
    private final ThreadLocal<EnvironmentsImporter> environmentsImporter = new ThreadLocal<>();
    private final ThreadLocal<EnvironmentsImportExecutor> environmentsImportExecutor = new ThreadLocal<>();

    private static final UUID projectId = UUID.fromString("9f052227-79d7-4f3f-bd55-aeb2efbcb103");

    @BeforeEach
    public void setUp() {
        EnvironmentService environmentServiceMock = Mockito.mock(EnvironmentService.class);
        SystemService systemServiceMock = Mockito.mock(SystemService.class);
        SystemCategoriesService systemCategoriesServiceMock = Mockito.mock(SystemCategoriesService.class);
        ConnectionService connectionServiceMock = Mockito.mock(ConnectionService.class);
        ProjectService projectServiceMock = Mockito.mock(ProjectService.class);

        EnvironmentsImporter environmentsImporterMock = new EnvironmentsImporter(
                new ObjectLoaderFromDiskService(),
                environmentServiceMock,
                systemServiceMock,
                systemCategoriesServiceMock,
                connectionServiceMock,
                projectServiceMock,
                mock(DuplicateNameChecker.class),
                mock(Decryptor.class));

        environmentService.set(environmentServiceMock);
        systemService.set(systemServiceMock);
        systemCategoriesService.set(systemCategoriesServiceMock);
        connectionService.set(connectionServiceMock);
        environmentService.set(environmentServiceMock);
        environmentsImporter.set(environmentsImporterMock);
        environmentsImportExecutor.set(new EnvironmentsImportExecutor(environmentsImporterMock));

        Project project = new ProjectImpl(projectId, "some name", "some name", "test "
                + "project", null, java.lang.System.currentTimeMillis(), null);
        when(projectServiceMock.replicate(any(), eq("some name"), any(), any(), any())).thenReturn(project);
        when(projectServiceMock.get(any())).thenReturn(project);
    }

    @Test
    public void importData_dataShouldBeImportedCorrectlyAndContainsAppropriateQuantityOfSystemsAndConnections()
            throws Exception {
        List<System> systems = new ArrayList<>();
        List<Connection> connections = new ArrayList<>();
        ConnectionParameters parameters = new ConnectionParameters();
        parameters.put("password", "");
        parameters.put("login", "");
        parameters.put("url", "some url");
        Connection connection = new ConnectionImpl(
                UUID.fromString("b02108b5-b1d3-49d8-ab8c-2091751c0219"),
                "HTTP",
                null,
                parameters,
                java.lang.System.currentTimeMillis(), null,
                java.lang.System.currentTimeMillis(), null,
                UUID.fromString("be964954-829b-4a6c-9db9-97961759fdb9"),
                null,
                UUID.fromString("2a0eab16-0fe7-4a12-8155-78c0c151abdf"),null, null);

        connections.add(connection);
        System system = new SystemImpl(UUID.fromString("be964954-829b-4a6c-9db9-97961759fdb9"), "MIA",
                null,
                java.lang.System.currentTimeMillis(), null,
                java.lang.System.currentTimeMillis(), null,
                null, null, connections, Status.NOTHING, null, null, null,
                null, null, null, false, null, null, null, null);

        systems.add(system);
        UUID environmentId = UUID.fromString("794abcd6-7f93-4ddf-aedd-613143b4b030");
        Optional<Environment> environmentOpt = Optional.of(new EnvironmentImpl(environmentId,
                "env", "envGraylog", null, "", "", "", java.lang.System.currentTimeMillis(), null, java.lang.System.currentTimeMillis(), null,
                projectId, systems, Constants.Environment.Category.ENVIRONMENT, null, Collections.emptyList()));

        List<Environment> environments = new ArrayList<>();
        environments.add(environmentOpt.get());
        system.setEnvironments(environments);

        when(environmentService.get().getOrElse(any())).thenReturn(environmentOpt);
        when(systemService.get().getByIds(any())).thenReturn(Collections.singletonList(system));
        when(connectionService.get().getByIds(any())).thenReturn(Collections.singletonList(connection));
        when(systemCategoriesService.get().get(any())).thenReturn(null);

        Path workDir = Paths.get("src/test/resources/ei/import/atp-environments");
        ExportImportData importData = mock(ExportImportData.class);
        environmentsImportExecutor.get().importData(importData, workDir);

        verify(environmentService.get(), times(1)).update(any());

        verify(systemService.get(), times(1)).update((System) any());

        verify(connectionService.get(), times(1)).update(any());
    }

    @Test
    public void validateData_shouldReturnEmptyValidationMessagesList_whenPassValidation() {
        Path workDir = Paths.get("src/test/resources/ei/import/atp-environments");
        List<String> messages = environmentsImporter.get().validateEnvironments(workDir,
                false, new HashMap<>());
        Assertions.assertTrue(messages.isEmpty(), "There must be no validation messages.");
    }


    @Test
    public void validateData_shouldReturnEmptyValidationMessagesList_whenNewProject() throws Exception {
        Path workDir = Paths.get("src/test/resources/ei/import/atp-environments");
        ExportImportData exportImportData = new ExportImportData(null, null, null, true, true, null, new HashMap<>(), new HashMap<>(),
                ValidationType.VALIDATE, false);

        ValidationResult validationResult = environmentsImportExecutor.get().validateData(exportImportData, workDir);


        Assertions.assertTrue(validationResult.getDetails().isEmpty(), "There must be no validation messages.");
        Assertions.assertEquals(3, validationResult.getReplacementMap().keySet().size(), "Replacement map has all keys.");
    }

    @Test
    public void validateData_shouldReturnEmptyValidationMessagesList_whenInterProjectFirtsTime() throws Exception {
        Path workDir = Paths.get("src/test/resources/ei/import/atp-environments");
        ExportImportData exportImportData = new ExportImportData(null, null, null, false, true, null, new HashMap<>(), new HashMap<>(),
                ValidationType.VALIDATE, false);

        when(environmentService.get().getBySourceIdAndProjectId(any(),any())).thenReturn(null);

        ValidationResult validationResult = environmentsImportExecutor.get().validateData(exportImportData, workDir);

        Assertions.assertTrue(validationResult.getDetails().isEmpty(), "There must be no validation messages.");
        Assertions.assertEquals(3, validationResult.getReplacementMap().keySet().size(), "Replacement map has all keys.");

    }
}
