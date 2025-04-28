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

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.qubership.atp.auth.springbootstarter.entities.UserInfo;
import org.qubership.atp.auth.springbootstarter.ssl.Provider;
import org.qubership.atp.environments.helper.JsonPathHandler;
import org.qubership.atp.environments.helper.RegexpHandler;
import org.qubership.atp.environments.mocks.MockTuple;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.ParametersGettingVersion;
import org.qubership.atp.environments.model.Project;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.impl.ConnectionImpl;
import org.qubership.atp.environments.model.impl.Context;
import org.qubership.atp.environments.model.impl.EnvironmentImpl;
import org.qubership.atp.environments.model.impl.ProjectImpl;
import org.qubership.atp.environments.model.impl.SystemCategoryImpl;
import org.qubership.atp.environments.model.impl.SystemImpl;
import org.qubership.atp.environments.model.utils.Constants;
import org.qubership.atp.environments.repo.impl.ConnectionRepositoryImpl;
import org.qubership.atp.environments.repo.impl.EnvironmentRepositoryImpl;
import org.qubership.atp.environments.repo.impl.SystemRepositoryImpl;
import org.qubership.atp.environments.service.direct.ConnectionService;
import org.qubership.atp.environments.service.direct.DecryptorService;
import org.qubership.atp.environments.service.direct.EncryptorService;
import org.qubership.atp.environments.service.direct.KafkaService;
import org.qubership.atp.environments.service.direct.SystemCategoriesService;
import org.qubership.atp.environments.service.rest.server.dto.SystemDto;
import org.qubership.atp.environments.utils.DateTimeUtil;
import org.qubership.atp.environments.utils.cloud.ExternalCloudClient;
import org.qubership.atp.environments.utils.cloud.KubeClient;
import org.qubership.atp.environments.utils.cloud.OpenshiftClient;
import org.qubership.atp.environments.utils.cloud.model.CloudService;
import org.qubership.atp.environments.version.checkers.VersionCheckerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.Tuple;

public class SystemServiceImplTest {

    private final ThreadLocal<SystemRepositoryImpl> systemRepository = new ThreadLocal<>();
    private final ThreadLocal<ConnectionRepositoryImpl> connectionRepository = new ThreadLocal<>();
    private final ThreadLocal<ConnectionService> connectionService = new ThreadLocal<>();
    private final ThreadLocal<SystemCategoriesService> systemCategoriesService = new ThreadLocal<>();
    private final ThreadLocal<EnvironmentRepositoryImpl> environmentRepository = new ThreadLocal<>();
    private final ThreadLocal<EncryptorService> encryptorService = new ThreadLocal<>();
    private final ThreadLocal<DecryptorService> decryptorService = new ThreadLocal<>();
    private final ThreadLocal<Provider<UserInfo>> userInfoProvider = new ThreadLocal<>();
    private final ThreadLocal<KafkaService> kafkaService = new ThreadLocal<>();
    private final ThreadLocal<SystemServiceImpl> systemService = new ThreadLocal<>();


    private static final RegexpHandler regexpHandler = new RegexpHandler();
    private static final JsonPathHandler jsonHandler = new JsonPathHandler();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ConnectionParameters openShiftProjectConnectionParameters = new ConnectionParameters();
    private static final ConnectionParameters openShiftRootConnectionParameters = new ConnectionParameters();
    private SystemDto sharedSystemDto;
    private Connection connection;
    private Connection decryptedConnection;
    private Connection encryptedConnection;
    private Connection httpConnectionTemplate;
    private Connection rootConnection;
    private System sharedSystem;
    private System systemOrigin;
    private Environment environment;
    private final List<Environment> listEnv = new ArrayList<>();

    @BeforeAll
    public static void init() {
        openShiftProjectConnectionParameters.put("login", "login");
        openShiftProjectConnectionParameters.put("password", "password");
        openShiftProjectConnectionParameters.put("etalon_project", "etalon_project");
        openShiftProjectConnectionParameters.put("project", "");
        openShiftRootConnectionParameters.put("root_synchronize_project", "");
        openShiftRootConnectionParameters.put("route_name", "route");
        openShiftProjectConnectionParameters.put("token", "");
        openShiftProjectConnectionParameters.put("url", "http://url");
        openShiftRootConnectionParameters.put("url", "http://expResult");
    }

    @BeforeEach
    public void setUp() throws Exception {
        SystemRepositoryImpl systemRepositoryMock = mock(SystemRepositoryImpl.class);
        ConnectionRepositoryImpl connectionRepositoryMock = mock(ConnectionRepositoryImpl.class);
        ConnectionService connectionServiceMock = mock(ConnectionService.class);
        SystemCategoriesService systemCategoriesServiceMock = mock(SystemCategoriesService.class);
        EnvironmentRepositoryImpl environmentRepositoryMock = mock(EnvironmentRepositoryImpl.class);
        EncryptorService encryptorServiceMock = mock(EncryptorService.class);
        DecryptorService decryptorServiceMock = mock(DecryptorService.class);
        Provider userInfoProviderMock = Mockito.mock(Provider.class);
        KafkaService kafkaServiceMock = mock(KafkaService.class);

        UserInfo userInfo = new UserInfo();
        userInfo.setId(UUID.randomUUID());
        Mockito.when(userInfoProviderMock.get()).thenReturn(userInfo);
        when(systemRepositoryMock.getContext()).thenReturn(new Context(true));

        systemRepository.set(systemRepositoryMock);
        connectionRepository.set(connectionRepositoryMock);
        connectionService.set(connectionServiceMock);
        systemCategoriesService.set(systemCategoriesServiceMock);
        environmentRepository.set(environmentRepositoryMock);
        encryptorService.set(encryptorServiceMock);
        decryptorService.set(decryptorServiceMock);
        userInfoProvider.set(userInfoProviderMock);
        kafkaService.set(kafkaServiceMock);

        VersionCheckerFactory versionCheckerFactoryMock = new VersionCheckerFactory(decryptorServiceMock, null, mock(SystemRepositoryImpl.class));
        systemService.set(new SystemServiceImpl(systemRepositoryMock, connectionRepositoryMock,
                connectionServiceMock, systemCategoriesServiceMock, new DateTimeUtil(), environmentRepositoryMock,
                encryptorServiceMock, decryptorServiceMock, userInfoProviderMock, kafkaServiceMock,
                regexpHandler, jsonHandler, versionCheckerFactoryMock, mock(MetricService.class)));


        Project project = new ProjectImpl(UUID.randomUUID(), "Test project", "Test project", "Test project "
                + "description", null, null, null);
        environment = EnvironmentImpl.builder()
                .uuid(UUID.randomUUID())
                .name("Test environment 1")
                .description("")
                .projectId(project.getId())
                .categoryId(Constants.Environment.Category.ENVIRONMENT)
                .build();

        listEnv.add(environment);
        systemOrigin = getDefaultSystem()
                .version("Unknown")
                .parametersGettingVersion(new ParametersGettingVersion())
                .build();
        sharedSystem = getDefaultSystem()
                .version("Unknown")
                .connectionsList(new ArrayList<>())
                .build();
        sharedSystemDto = SystemDto.convert(sharedSystem);
        connection = getDefaultConnection().build();
        rootConnection = getDefaultConnection().build();
        decryptedConnection = new ConnectionImpl();
        decryptedConnection.setParameters(new ConnectionParameters());
        encryptedConnection = new ConnectionImpl();
        decryptedConnection.setParameters(new ConnectionParameters());
        httpConnectionTemplate = getDefaultConnection().build();
    }

    @Test
    public void updateSystem_shareEnv_updateEnvsInSystem() throws Exception {
        setMockingForSharingSystems();
        List<UUID> sharedEnvironments = asList(environment.getId(), environment.getId());
        List<Environment> expectedEnvList = asList(environment, environment);
        sharedSystemDto.setEnvironmentIds(sharedEnvironments);
        System result = systemService.get().update(sharedSystemDto);
        Assertions.assertEquals(objectMapper.writeValueAsString(expectedEnvList),
                objectMapper.writeValueAsString(result.getEnvironmentIds()));
    }

    @Test
    public void updateRoots_withEmptyOpenShiftServer_returnEmptyConnections() {
        List<Connection> result = systemService.get().updateUrlRoutes(UUID.randomUUID(), UUID.randomUUID(),
                new ArrayList<>());
        Assertions.assertEquals(result.size(), 0);
    }

    @Test
    public void updateRoots_withEmptyOpenShiftRootConnections_returnEmptyConnections() {
        UUID environmentId = UUID.randomUUID();
        UUID openShiftSystemConnection = UUID.randomUUID();
        Mockito.doReturn(new ArrayList<Connection>()).when(connectionRepository.get()).getAll(environmentId,
                openShiftSystemConnection);
        ArrayList<Connection> openShiftServer = new ArrayList<>();
        connection.setParameters(openShiftProjectConnectionParameters);
        openShiftServer.add(connection);
        Mockito.doReturn(openShiftProjectConnectionParameters).when(decryptorService.get())
                .decryptParameters(connection.getParameters());
        try (MockedStatic<ExternalCloudClient> externalCloudClientMock = Mockito.mockStatic(ExternalCloudClient.class)) {
            OpenshiftClient openshiftClient = Mockito.mock(OpenshiftClient.class);
            externalCloudClientMock.when(() -> ExternalCloudClient.createClient(any(), eq(OpenshiftClient.class)))
                    .thenReturn(openshiftClient);
            List<Connection> result = systemService.get()
                    .updateUrlRoutes(environmentId, openShiftSystemConnection, openShiftServer);
            Assertions.assertEquals(result.size(), 0);
        }
    }

    @Test
    public void updateRoots_withNotEmptyRootConnectionsWithProjectDependency_returnDiffConnections() {
        String routeUrl = "http://expResult";
        UUID environmentId = UUID.randomUUID();
        rootConnection.getParameters().put("root_synchronize_project", connection.getId().toString());
        UUID openShiftSystemConnection = UUID.randomUUID();
        List<Connection> openShiftRootConnections = new ArrayList<>();
        openShiftRootConnections.add(rootConnection);
        Mockito.doReturn(openShiftRootConnections).when(connectionRepository.get()).getAll(environmentId,
                openShiftSystemConnection);
        ArrayList<Connection> openShiftServer = new ArrayList<>();
        connection.setParameters(openShiftProjectConnectionParameters);
        openShiftServer.add(connection);

        try (MockedStatic<ExternalCloudClient> externalCloudClientMock = Mockito.mockStatic(ExternalCloudClient.class)){
            OpenshiftClient openshiftClient = Mockito.mock(OpenshiftClient.class);
            externalCloudClientMock.when(() -> ExternalCloudClient.createClient(any(),eq(OpenshiftClient.class)))
                    .thenReturn(openshiftClient);
            Mockito.when(openshiftClient.getRouteUrl(eq(rootConnection.getParameters().get("route_name")), anyList()))
                    .thenReturn(routeUrl);
            Mockito.doReturn(openShiftProjectConnectionParameters).when(decryptorService.get())
                    .decryptParameters(connection.getParameters());
            Mockito.doReturn(rootConnection.getParameters()).when(decryptorService.get())
                    .decryptParameters(rootConnection.getParameters());

            List<Connection> result = systemService.get()
                    .updateUrlRoutes(environmentId, openShiftSystemConnection, openShiftServer);
            Assertions.assertEquals(result.get(0).getParameters().get("url"), routeUrl);
        }
    }
    @Test
    public void updateRoots_withNotEmptyRootConnectionsWithoutProjectDependency_returnInputConnections() {
        UUID environmentId = UUID.randomUUID();
        UUID openShiftSystemConnection = UUID.randomUUID();
        List<Connection> openShiftRootConnections = new ArrayList<>();
        openShiftRootConnectionParameters.put("root_synchronize_project", connection.getId().toString());
        rootConnection.setParameters(openShiftRootConnectionParameters);
        openShiftRootConnections.add(rootConnection);
        Mockito.doReturn(openShiftRootConnections).when(connectionRepository.get()).getAll(environmentId,
                openShiftSystemConnection);
        ArrayList<Connection> openShiftServer = new ArrayList<>();
        connection.setParameters(openShiftProjectConnectionParameters);
        openShiftServer.add(connection);
        Mockito.doReturn(openShiftProjectConnectionParameters).when(decryptorService.get())
                .decryptParameters(connection.getParameters());
        try (MockedStatic<ExternalCloudClient> externalCloudClientMock = Mockito.mockStatic(ExternalCloudClient.class)) {
            OpenshiftClient openshiftClient = Mockito.mock(OpenshiftClient.class);
            externalCloudClientMock.when(() -> ExternalCloudClient.createClient(any(), eq(OpenshiftClient.class)))
                    .thenReturn(openshiftClient);
            Mockito.when(openshiftClient.getRouteUrl(any(), any()))
                    .thenReturn(rootConnection.getParameters().get("url"));
            List<Connection> result = systemService.get()
                    .updateUrlRoutes(environmentId, openShiftSystemConnection, openShiftServer);
            Assertions.assertEquals(result, openShiftRootConnections);
        }
    }


    @Test
    public void updateSystem_unShareEnv_updateEnvsInSystem() throws Exception {
        setMockingForSharingSystems();
        List<UUID> sharedEnvironments = asList(environment.getId());
        List<Environment> expectedEnvList = asList(environment);
        sharedSystemDto.setEnvironmentIds(sharedEnvironments);
        System result = systemService.get().update(sharedSystemDto);
        Assertions.assertEquals(objectMapper.writeValueAsString(expectedEnvList),
                objectMapper.writeValueAsString(result.getEnvironmentIds()));
    }

    @Test
    public void createSystem_systemNameIsUnique_systemCreated() {
        System newSystem = getDefaultSystem().build();
        List<Tuple> foundSystems = new ArrayList<>();
        Mockito.when(systemRepository.get().checkSystemNameIsUniqueUnderEnvironment(environment.getId(), newSystem.getName())).thenReturn(foundSystems);
        Mockito.when(systemRepository.get().create(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(newSystem);
        System createdSystem = systemService.get().create(environment.getId(), newSystem.getName(),
                newSystem.getDescription(),
                newSystem.getSystemCategoryId(), newSystem.getParametersGettingVersion(),
                newSystem.getParentSystemId(), newSystem.getServerItf(), newSystem.getMergeByName()
                , newSystem.getLinkToSystemId(), newSystem.getExternalId(), newSystem.getExternalName());
        Assertions.assertEquals(newSystem.getName(), createdSystem.getName());
    }

    @Test
    public void createSystem_systemNameIsNotUnique_gotException() {
        System newSystem = getDefaultSystem().build();
        List<Tuple> foundSystems = new ArrayList<>();
        Tuple sys = new MockTuple(new Object[]{newSystem.getName(), newSystem.getId()});
        foundSystems.add(sys);
        Mockito.when(systemRepository.get().checkSystemNameIsUniqueUnderEnvironment(environment.getId(), newSystem.getName()))
                .thenReturn(foundSystems);
        Assertions.assertThrows(IllegalArgumentException.class, () -> systemService.get().create(environment.getId(),
                newSystem.getName(), newSystem.getDescription(),
                newSystem.getSystemCategoryId(), newSystem.getParametersGettingVersion(),
                newSystem.getParentSystemId(), newSystem.getServerItf()
                , newSystem.getMergeByName(), newSystem.getLinkToSystemId(), newSystem.getExternalId(),
                newSystem.getExternalName()));
    }

    @Test
    public void createServicesFromKubernetes_shortKubernetesServiceWithIngresses_gotSystems() {
        UUID environmentId = UUID.randomUUID();
        UUID parentSystemId = UUID.randomUUID();
        when(systemRepository.get().getById(parentSystemId)).thenReturn(systemOrigin);
        doReturn(Collections.singletonList(connection)).when(connectionRepository.get())
                .getAllByParentIdAndConnectionType(systemOrigin.getId(), Constants.Environment.System.Connection.KUBERNETES_PROJECT);
        when(connectionRepository.get().getById(Constants.Environment.System.Connection.HTTP)).thenReturn(httpConnectionTemplate);
        List<CloudService> kubeServices = getKubernetesServicesWithIngresses();

        try (MockedConstruction<KubeClient> ignored = Mockito.mockConstruction(KubeClient.class,
                (mock, context) -> when(mock.getServicesByServiceId(anyList())).thenReturn(kubeServices))) {
            getKubernetesSystems(parentSystemId, kubeServices).forEach(newSystem -> doReturn(newSystem)
                    .when(systemRepository.get()).create(
                    eq(environmentId),
                    eq(newSystem.getName()),
                    any(),
                    any(),
                    any(),
                    eq(Constants.SystemCategories.KUBERNETES_SERVICE),
                    any(ParametersGettingVersion.class),
                    any(), any(), any(), eq(parentSystemId),
                    eq(newSystem.getExternalId()),any()));
            doReturn(decryptedConnection).when(decryptorService.get()).decryptConnection(connection);
            List<System> returnedSystems = systemService.get().createListFromCloudServer(kubeServices
                            .stream().map(CloudService::getId).collect(Collectors.toList()), parentSystemId,
                    environmentId, KubeClient.class);
            Assertions.assertEquals(returnedSystems.size(), 3);
            returnedSystems.forEach(system -> {
                Assertions.assertTrue(kubeServices.stream().anyMatch(service -> service.getName().equals(system.getName())));
                Assertions.assertEquals(system.getLinkToSystemId(), parentSystemId);
                Assertions.assertTrue(kubeServices.stream().anyMatch(service -> service.getId().equals(system.getExternalId())));
            });
        }
    }

    @Test
    public void updateServicesFromKubernetes_systemsWereChanged_gotSystems() {
        UUID parentSystemId = UUID.randomUUID();
        when(systemRepository.get().getById(parentSystemId)).thenReturn(systemOrigin);
        doReturn(Collections.singletonList(connection)).when(connectionRepository.get()).getAllByParentIdAndConnectionType(systemOrigin.getId(),
                Constants.Environment.System.Connection.KUBERNETES_PROJECT);
        List<CloudService> kubeServices = getKubernetesServicesWithIngresses();

        try (MockedConstruction<KubeClient> ignored = Mockito.mockConstruction(KubeClient.class,
                (mock, context) -> {
                    when(mock.getServicesByServiceId(anyList())).thenReturn(kubeServices);
                    when(mock.getServicesByExistingServices(anyList())).thenReturn(kubeServices);
                })) {

            List<System> systems = getKubernetesSystems(parentSystemId, kubeServices);
            List<System> expectedSystems = getKubernetesSystems(parentSystemId, kubeServices);
            updateSystems(systems);
            systems.forEach(system -> {
                doReturn(system).when(systemRepository.get()).update(eq(system.getId()), eq(system.getName()), any(),
                        any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), eq(parentSystemId),
                        eq(system.getExternalId()), any(), eq(true), eq(true));
                doReturn(system).when(systemRepository.get()).getById(system.getId());
            });
            when(connectionRepository.get().getById(Constants.Environment.System.Connection.HTTP)).thenReturn(httpConnectionTemplate);
            doReturn(systems).when(systemRepository.get()).getByLinkToSystemId(parentSystemId);
            systems.forEach(system ->
                    doReturn(getDefaultConnection()
                            .systemId(system.getId())
                            .sourceTemplateId(httpConnectionTemplate.getId())
                            .build())
                            .when(connectionService.get())
                            .create(eq(system.getId()), any(), any(), any(), any(), eq(httpConnectionTemplate.getId()), any(), any()));
            doReturn(decryptedConnection).when(decryptorService.get()).decryptConnection(connection);
            List<System> actualSystems = systemService.get().updateServicesFromCloudServer(parentSystemId, KubeClient.class);
            Assertions.assertEquals(actualSystems.size(), expectedSystems.size());
            actualSystems.forEach(system -> {
                Assertions.assertTrue(expectedSystems.stream().anyMatch(service -> service.getName().equals(system.getName())));
                Assertions.assertFalse(system.getConnections().isEmpty());
            });
        }
    }

    @Test
    public void getVersionWithHtmlMarking_versionMoreThenOne_gotHtmlTableWithVersions() {
        String expectedHtmlTable = "<table><tr><td>version_1</td>"
                + "</tr><tr><td>version_2</td></tr></table>";
        String expectedVersionByKubernetesImages = "version_1\nversion_2";
        systemOrigin.setVersion(expectedVersionByKubernetesImages);
        String actualHtmlTable = systemService.get().transformSystemVersionToHtml(systemOrigin).getVersion();
        Assertions.assertEquals(actualHtmlTable, expectedHtmlTable);
    }

    @Test
    public void getShortKubernetesServices_encryptedParametersInConnection_decryptConnectionMethodWasCalled() {
        System kubernetesServerSystem = new SystemImpl();
        kubernetesServerSystem.setId(UUID.randomUUID());
        List<Connection> connections = Arrays.asList(encryptedConnection);
        kubernetesServerSystem.setConnections(connections);
        Mockito.when(systemRepository.get().getById(kubernetesServerSystem.getId())).thenReturn(kubernetesServerSystem);

        when(connectionRepository.get().getAllByParentIdAndConnectionType(eq(kubernetesServerSystem.getId()),
                eq(Constants.Environment.System.Connection.KUBERNETES_PROJECT)))
                .thenReturn(connections);
        when(decryptorService.get().decryptConnection(encryptedConnection)).thenReturn(decryptedConnection);

        try (MockedStatic<ExternalCloudClient> externalCloudClientMock = Mockito.mockStatic(ExternalCloudClient.class)) {
            KubeClient kubeClient = Mockito.mock(KubeClient.class);
            externalCloudClientMock.when(() -> ExternalCloudClient.createClient(any(),eq(KubeClient.class)))
                    .thenReturn(kubeClient);
            systemService.get().getShortExternalServices(kubernetesServerSystem.getId(), KubeClient.class);
            Mockito.verify(decryptorService.get(), times(1)).decryptConnection(encryptedConnection);
        }
    }

    public void setMockingForSharingSystems() {
        Mockito.when(systemRepository.get().unShare(sharedSystemDto.getId(), environment.getId(), null, null))
                .thenReturn(sharedSystem);
        Mockito.when(systemService.get().get(sharedSystemDto.getId())).thenReturn(sharedSystem);
        Mockito.when(environmentRepository.get().getById(environment.getId())).thenReturn(environment);
        Mockito.when(environmentRepository.get().getById(environment.getId())).thenReturn(environment);
        Mockito.when(systemRepository.get().update(
                    eq(sharedSystemDto.getId()), eq(sharedSystemDto.getName()),
                    eq(sharedSystemDto.getDescription()), any(),
                    any(), eq(sharedSystemDto.getSystemCategoryId()),
                    any(), eq(null),
                    any(), any(),
                    eq(sharedSystemDto.getParametersGettingVersion()), eq(sharedSystemDto.getParentSystemId()),
                    eq(sharedSystemDto.getServerItf()), eq(sharedSystemDto.getMergeByName()),
                    eq(sharedSystem.getLinkToSystemId()), eq(sharedSystem.getExternalId()),
                    eq(sharedSystem.getExternalName()), eq(true), eq(true)))
            .thenReturn(sharedSystem);
    }

    public List<CloudService> getKubernetesServicesWithIngresses() {
        List<CloudService> serviceList = new ArrayList<>();
        serviceList.add(new CloudService("service_name_1", UUID.randomUUID(), "service_host_1"));
        serviceList.add(new CloudService("service_name_2", UUID.randomUUID(), "service_host_2"));
        serviceList.add(new CloudService("service_name_3", UUID.randomUUID(), "service_host_3"));
        return serviceList;
    }

    public List<System> getKubernetesSystems(UUID parentSystemId, List<CloudService> services) {
        List<System> systemList = new ArrayList<>();
        for (CloudService service : services) {
            systemList.add(
                    SystemImpl.builder()
                            .uuid(UUID.randomUUID())
                            .name(service.getName())
                            .connectionsList(new ArrayList<>())
                            .version("")
                            .dateOfCheckVersion(0L)
                            .linkToSystemId(parentSystemId)
                            .externalId(service.getId())
                            .systemCategory(new SystemCategoryImpl(Constants.SystemCategories.KUBERNETES_SERVICE,
                                    "Kubernetes Service",
                                    null ,
                                    null ,
                                    null ))
                            .build());
        }
        return systemList;
    }

    public void updateSystems(List<System> systems) {
        systems.forEach(system -> system.setName(system.getName() + "_Changed"));
    }

    @Test
    public void update_systemNameIsUnique_gotUpdatedSystem() {
        Environment environment = new EnvironmentImpl();
        environment.setId(UUID.randomUUID());
        List<UUID> environmentIds = new ArrayList<>();
        environmentIds.add(environment.getId());
        System system = new SystemImpl();
        system.setName("Test system");
        system.setId(UUID.randomUUID());
        system.setEnvironmentIds(asList(environment));
        System existedSystem = new SystemImpl();
        existedSystem.setName("Test system");
        existedSystem.setId(system.getId());
        existedSystem.setEnvironmentIds(asList(environment));
        SystemDto systemDto = new SystemDto();
        systemDto.setName("Test dto system");
        systemDto.setEnvironmentIds(environmentIds);
        when(systemRepository.get().checkSystemNameIsUniqueUnderEnvironment(environment.getId(), systemDto.getName()))
                .thenReturn(new ArrayList<>());
        when(systemRepository.get().update(
                eq(systemDto.getId()), eq(systemDto.getName()),
                eq(systemDto.getDescription()), any(), any(),
                eq(systemDto.getSystemCategoryId()), any(), eq(null), any(), any(),
                eq(systemDto.getParametersGettingVersion()), eq(systemDto.getParentSystemId()),
                eq(systemDto.getServerItf()), eq(systemDto.getMergeByName()),
                eq(systemDto.getLinkToSystemId()), eq(systemDto.getExternalId()),
                eq(systemDto.getExternalName()), eq(true), eq(true))).thenReturn(system);
        doReturn(environment).when(environmentRepository.get()).getById(environment.getId());
        doReturn(existedSystem).when(systemRepository.get()).getById(systemDto.getId());
        Assertions.assertEquals("Test system", systemService.get().update(systemDto).getName());
    }

    @Test
    public void update_systemNameIsDuplicated_gotIllegalArgumentException() {
        Environment environment = new EnvironmentImpl();
        environment.setId(UUID.randomUUID());
        List<UUID> environmentIds = new ArrayList<>();
        environmentIds.add(environment.getId());
        SystemDto systemDto = new SystemDto();
        systemDto.setName("Test dto system");
        systemDto.setEnvironmentIds(environmentIds);
        System existedSystem = new SystemImpl();
        existedSystem.setEnvironmentIds(asList(environment));
        List<String> existedSystemNames = new ArrayList<>();
        existedSystemNames.add(systemDto.getName());
        doReturn(existedSystemNames).when(systemRepository.get()).checkSystemNameIsUniqueUnderEnvironment(environment.getId(),
                systemDto.getName());
        doReturn(environment).when(environmentRepository.get()).getById(environment.getId());
        doReturn(existedSystem).when(systemRepository.get()).getById(systemDto.getId());
        Assertions.assertThrows(IllegalArgumentException.class, () -> systemService.get().update(systemDto));
    }

    private SystemImpl.SystemImplBuilder getDefaultSystem() {
        return SystemImpl.builder()
                .uuid(UUID.randomUUID())
                .name("Test copy system")
                .description("Test copy description")
                .environments(listEnv);
    }

    private ConnectionImpl.ConnectionImplBuilder getDefaultConnection() {
        return ConnectionImpl.builder()
                .uuid(UUID.randomUUID())
                .parameters(new ConnectionParameters())
                .systemId(UUID.randomUUID())
                .sourceTemplateId(UUID.randomUUID());
    }
}
