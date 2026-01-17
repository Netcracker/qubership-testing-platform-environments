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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mockito;
import org.qubership.atp.auth.springbootstarter.entities.UserInfo;
import org.qubership.atp.auth.springbootstarter.exceptions.AtpIllegalNullableArgumentException;
import org.qubership.atp.auth.springbootstarter.security.permissions.PolicyEnforcement;
import org.qubership.atp.auth.springbootstarter.ssl.Provider;
import org.qubership.atp.environments.mocks.EntitiesGenerator;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.Project;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.impl.Context;
import org.qubership.atp.environments.model.impl.ProjectImpl;
import org.qubership.atp.environments.model.impl.SystemImpl;
import org.qubership.atp.environments.model.utils.Constants;
import org.qubership.atp.environments.repo.impl.ContextRepository;
import org.qubership.atp.environments.repo.impl.EnvironmentRepositoryImpl;
import org.qubership.atp.environments.repo.impl.ProjectRepositoryImpl;
import org.qubership.atp.environments.service.direct.ConnectionService;
import org.qubership.atp.environments.service.direct.EnvironmentService;
import org.qubership.atp.environments.service.direct.ProjectAccessService;
import org.qubership.atp.environments.service.direct.ProjectService;
import org.qubership.atp.environments.service.direct.SystemService;
import org.qubership.atp.environments.service.rest.server.dto.CreateSystemDto;
import org.qubership.atp.environments.service.rest.server.dto.EnvironmentDto;
import org.qubership.atp.environments.utils.DateTimeUtil;

public class ProjectServiceTest extends AbstractServiceTest {

    private final ThreadLocal<ProjectRepositoryImpl> projectRepository = new ThreadLocal<>();
    private final ThreadLocal<EnvironmentRepositoryImpl> environmentRepository = new ThreadLocal<>();
    private final ThreadLocal<EnvironmentService> environmentService = new ThreadLocal<>();
    private final ThreadLocal<SystemService> systemService = new ThreadLocal<>();
    private final ThreadLocal<ConnectionService> connectionService = new ThreadLocal<>();
    private final ThreadLocal<Provider> userInfoProvider = new ThreadLocal<>();
    private final ThreadLocal<ProjectService> projectService = new ThreadLocal<>();

    @BeforeEach
    public void setUp() {
        ProjectRepositoryImpl projectRepositoryMock = Mockito.mock(ProjectRepositoryImpl.class);
        EnvironmentRepositoryImpl environmentRepositoryMock = Mockito.mock(EnvironmentRepositoryImpl.class);
        EnvironmentService environmentServiceMock = Mockito.mock(EnvironmentService.class);
        SystemService systemServiceMock = Mockito.mock(SystemService.class);
        ConnectionService connectionServiceMock = Mockito.mock(ConnectionService.class);
        Provider userInfoProviderMock = Mockito.mock(Provider.class);
        Mockito.when(userInfoProviderMock.get()).thenReturn(new UserInfo());

        Mockito.when(projectRepositoryMock.getContext()).thenReturn(new Context(true));

        projectRepository.set(projectRepositoryMock);
        environmentRepository.set(environmentRepositoryMock);
        environmentService.set(environmentServiceMock);
        systemService.set(systemServiceMock);
        connectionService.set(connectionServiceMock);
        userInfoProvider.set(userInfoProviderMock);
        projectService.set(new ProjectServiceImpl(
                projectRepositoryMock,
                environmentRepositoryMock,
                environmentServiceMock,
                systemServiceMock,
                connectionServiceMock,
                new DateTimeUtil(),
                Mockito.mock(ContextRepository.class),
                userInfoProviderMock,
                Mockito.mock(PolicyEnforcement.class),
                Mockito.mock(ProjectAccessService.class)));
    }

    private void mockProjectService(Project project) {
        Mockito.when(projectRepository.get().getById(any())).thenReturn(project);
    }

    @Test
    public void getProject_ById_Successful(TestInfo testInfo) {
        String methodName = testInfo.getTestMethod().get().getName();
        Project expectedProject = EntitiesGenerator.generateProject(methodName);
        Mockito.when(projectRepository.get().getById(any(UUID.class))).thenReturn(expectedProject);
        Project actualProject = projectService.get().get(UUID.randomUUID());
        Assertions.assertNotNull(actualProject);
        Assertions.assertEquals(projectService.get().get(UUID.randomUUID()).getName(), expectedProject.getName());
    }

    @Test
    public void existsProject_ById_Successful() {
        UUID projectId = UUID.randomUUID();
        UUID negativeResultId = UUID.randomUUID();
        Mockito.when(projectRepository.get().existsById(eq(negativeResultId))).thenReturn(false);
        Mockito.when(projectRepository.get().existsById(eq(projectId))).thenReturn(true);
        Assertions.assertTrue(projectService.get().existsById(projectId));
        Assertions.assertFalse(projectService.get().existsById(negativeResultId));
    }

    @Test
    public void createEnvironment_ByDto_Successful(TestInfo testInfo) {
        String methodName = testInfo.getTestMethod().get().getName();
        UUID projectId = UUID.randomUUID();
        Environment expectedEnvironment = EntitiesGenerator.generateEnvironment(methodName);
        expectedEnvironment.setGraylogName(methodName);
        expectedEnvironment.setDescription(methodName);
        expectedEnvironment.setId(UUID.randomUUID());
        EnvironmentDto environmentDto = new EnvironmentDto();
        environmentDto.setName(methodName);
        environmentDto.setGraylogName(methodName);
        environmentDto.setDescription(methodName);
        CreateSystemDto systemDto = new CreateSystemDto();
        systemDto.setName(methodName);
        environmentDto.setSystems(Collections.singletonList(systemDto));
        SystemImpl expectedSystem = new SystemImpl();
        expectedSystem.setName(methodName);
        Mockito.when(environmentService.get().create(eq(projectId), eq(environmentDto.getName()),
                eq(environmentDto.getGraylogName()), eq(environmentDto.getDescription()),
                eq(environmentDto.getSsmSolutionAlias()), eq(environmentDto.getSsmInstanceAlias()),
                eq(environmentDto.getConsulEgressConfigPath()), any(UUID.class), any()))
                .thenReturn(expectedEnvironment);
        Mockito.when(systemService.get().create(eq(expectedEnvironment.getId()), eq(systemDto))).thenReturn(expectedSystem);
        Environment actualEnvironment = projectService.get().create(projectId, environmentDto, UUID.randomUUID());
        Assertions.assertNotNull(actualEnvironment);
        Assertions.assertFalse(CollectionUtils.isEmpty(actualEnvironment.getSystems()));
        Assertions.assertEquals(actualEnvironment.getName(), expectedEnvironment.getName());
    }

    @Test
    public void replicateProject_ById_Successful(TestInfo testInfo) {
        String methodName = testInfo.getTestMethod().get().getName();
        UUID projectId = UUID.randomUUID();
        Project expectedProject = new ProjectImpl();
        expectedProject.setId(projectId);
        expectedProject.setName(methodName);
        Mockito.when(projectRepository.get().create(eq(projectId), eq(methodName), any(), any(), any()))
                .thenReturn(expectedProject);
        Project actualProject = projectService.get().replicate(projectId,
                methodName, "", "", null);
        Assertions.assertNotNull(actualProject);
        Assertions.assertEquals(actualProject.getName(), expectedProject.getName());
    }

    @Test
    public void replicateProject_ById_throwException() {
        UUID projectId = UUID.randomUUID();
        Project expectedProject = new ProjectImpl();
        expectedProject.setId(projectId);
        AtpIllegalNullableArgumentException exception = Assertions.assertThrows(AtpIllegalNullableArgumentException.class,
                () -> projectService.get().replicate(projectId,
                        "", "", "", null));
        Assertions.assertEquals("Found illegal nullable project name for the validated method argument", exception.getMessage());
    }

    @Test
    public void copyProject_ById_Successful(TestInfo testInfo) {
        String methodName = testInfo.getTestMethod().get().getName();
        UUID projectId = UUID.randomUUID();
        Project expectedProject = EntitiesGenerator.generateProject(methodName);
        expectedProject.setId(projectId);
        Mockito.when(projectRepository.get().getById(any(UUID.class))).thenReturn(expectedProject);
        Mockito.when(projectRepository.get().create(eq(methodName), any(), any(), any()))
                .thenReturn(expectedProject);
        Project actualProject = projectService.get().copy(projectId,
                methodName, "", "");
        Assertions.assertNotNull(actualProject);
        Assertions.assertEquals(actualProject.getName(), actualProject.getName());
    }

    @Test
    public void copyProject_ById_ThrowException(TestInfo testInfo) {
        String methodName = testInfo.getTestMethod().get().getName();
        UUID projectId = UUID.randomUUID();
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class,
                () -> projectService.get().copy(projectId,
                        methodName, "", ""));
        Assertions.assertTrue(exception.getMessage().contains("Project is not found by id: " + projectId));
    }

    @Test
    public void getEnvironments_ByProjectId_Successful(TestInfo testInfo) {
        String methodName = testInfo.getTestMethod().get().getName();
        UUID projectId = UUID.randomUUID();
        Mockito.when(environmentRepository.get().getAllByParentId(eq(projectId),
                eq(Constants.Environment.Category.ENVIRONMENT))).thenReturn(Collections.singletonList(EntitiesGenerator.generateEnvironment(methodName)));
        mockProjectService(EntitiesGenerator.generateProject(methodName, projectId));
        List<Environment> environments = projectService.get().getEnvironments(projectId);
        Assertions.assertNotNull(environments);
        Assertions.assertEquals(environments.get(0).getName(), methodName);
    }

    @Test
    public void getTemporaryEnvironments_ByProjectId_Successful(TestInfo testInfo) {
        String methodName = testInfo.getTestMethod().get().getName();
        UUID projectId = UUID.randomUUID();
        Mockito.when(environmentRepository.get().getAllByParentId(eq(projectId),
                eq(Constants.Environment.Category.TEMPORARY_ENVIRONMENT))).thenReturn(Collections.singletonList(EntitiesGenerator.generateEnvironment(methodName)));
        mockProjectService(EntitiesGenerator.generateProject(methodName, projectId));
        List<Environment> environments = projectService.get().getTemporaryEnvironments(projectId);
        Assertions.assertNotNull(environments);
        Assertions.assertEquals(environments.get(0).getName(), methodName);
    }

    @Test
    public void getAllEnvironments_ByProjectId_Successful(TestInfo testInfo) {
        String methodName = testInfo.getTestMethod().get().getName();
        UUID projectId = UUID.randomUUID();
        Mockito.when(environmentRepository.get().getAllByParentId(eq(projectId),
                eq(Constants.Environment.Category.ENVIRONMENT),
                eq(Constants.Environment.Category.TEMPORARY_ENVIRONMENT)))
                .thenReturn(Collections.singletonList(EntitiesGenerator.generateEnvironment(methodName)));
        mockProjectService(EntitiesGenerator.generateProject(methodName, projectId));
        List<Environment> environments = projectService.get().getAllEnvironments(projectId);
        Assertions.assertNotNull(environments);
        Assertions.assertEquals(environments.get(0).getName(), methodName);
    }

    @Test
    public void getTools_ByProjectId_Successful(TestInfo testInfo) {
        String methodName = testInfo.getTestMethod().get().getName();
        UUID projectId = UUID.randomUUID();
        Mockito.when(environmentRepository.get().getAllByParentId(eq(projectId),
                eq(Constants.Environment.Category.TOOL))).thenReturn(Collections.singletonList(EntitiesGenerator.generateEnvironment(methodName)));
        mockProjectService(EntitiesGenerator.generateProject(methodName, projectId));
        List<Environment> environments = projectService.get().getTools(projectId);
        Assertions.assertNotNull(environments);
        Assertions.assertEquals(environments.get(0).getName(), methodName);
    }

    @Test
    public void getSystems_ByProjectId_Successful(TestInfo testInfo) {
        String methodName = testInfo.getTestMethod().get().getName();
        UUID projectId = UUID.randomUUID();
        Mockito.when(systemService.get().getSystemsByProjectId(eq(projectId)))
                .thenReturn(Collections.singletonList(EntitiesGenerator.generateSystem(methodName)));
        List<System> systems = projectService.get().getSystemsByProjectId(projectId);
        Assertions.assertNotNull(systems);
        Assertions.assertEquals(systems.get(0).getName(), methodName);
    }

    @Test
    public void getSystems_ByProjectIdAndCategoryName_Successful(TestInfo testInfo) {
        String methodName = testInfo.getTestMethod().get().getName();
        UUID projectId = UUID.randomUUID();
        Mockito.when(systemService.get().getSystemsByProjectIdAndCategoryName(eq(projectId), anyString()))
                .thenReturn(Collections.singletonList(EntitiesGenerator.generateSystem(methodName)));
        List<System> systems = projectService.get().getSystemsByProjectIdAndCategoryName(projectId, "SomeCategory");
        Assertions.assertNotNull(systems);
        Assertions.assertEquals(systems.get(0).getName(), methodName);
    }

    @Test
    public void getConnections_ByProjectId_Successful(TestInfo testInfo) {
        String methodName = testInfo.getTestMethod().get().getName();
        UUID projectId = UUID.randomUUID();
        Mockito.when(systemService.get().getSystemsByProjectIdAndCategoryName(eq(projectId), anyString()))
                .thenReturn(Collections.singletonList(EntitiesGenerator.generateSystem(methodName)));
        List<System> systems = projectService.get().getSystemsByProjectIdAndCategoryName(projectId, "SomeCategory");
        Assertions.assertNotNull(systems);
        Assertions.assertEquals(systems.get(0).getName(), methodName);
    }

    @Test
    public void getSystemNames_ByProjectId_Successful(TestInfo testInfo) {
        String methodName = testInfo.getTestMethod().get().getName();
        UUID projectId = UUID.randomUUID();
        Mockito.when(environmentRepository.get().getSystemNamesByProjectId(eq(projectId)))
                .thenReturn(Collections.singletonList(methodName));
        List<String> systems = projectService.get().getSystemNames(projectId);
        Assertions.assertNotNull(systems);
        Assertions.assertEquals(systems.get(0), methodName);
    }

    @Test
    public void getConnectionNames_ByProjectId_Successful(TestInfo testInfo) {
        String methodName = testInfo.getTestMethod().get().getName();
        UUID projectId = UUID.randomUUID();
        Mockito.when(environmentRepository.get().getConnectionNamesByProjectId(eq(projectId)))
                .thenReturn(Collections.singletonList(methodName));
        List<String> systems = projectService.get().getConnectionNames(projectId);
        Assertions.assertNotNull(systems);
        Assertions.assertEquals(systems.get(0), methodName);
    }

    @Test
    public void getProjects_ByHost_Successful(TestInfo testInfo) {
        String methodName = testInfo.getTestMethod().get().getName();
        Connection connection = EntitiesGenerator.generateConnection(methodName);
        System system = EntitiesGenerator.generateSystem(methodName);
        Environment environment = EntitiesGenerator.generateEnvironment(methodName);
        Project project = EntitiesGenerator.generateProject(methodName);
        environment.setProjectId(project.getId());
        system.setEnvironments(Collections.singletonList(environment));
        connection.setSystemId(system.getId());
        project.setEnvironments(Collections.singletonList(environment));
        Mockito.when(connectionService.get().getConnectionByHost("host"))
                .thenReturn(Collections.singletonList(connection));
        Mockito.when(systemService.get().getByIds(Collections.singletonList(system.getId())))
                .thenReturn(Collections.singletonList(system));
        Mockito.when(projectRepository.get().getByIds(Collections.singleton(project.getId())))
                .thenReturn(Collections.singletonList(project));
        Assertions.assertEquals(projectService.get().getProjectsByHost("host").get(0).getName(), methodName);
    }
}
