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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.qubership.atp.auth.springbootstarter.entities.UserInfo;
import org.qubership.atp.auth.springbootstarter.ssl.Provider;
import org.qubership.atp.environments.errorhandling.request.EnvironmentsWithFilterRequestException;
import org.qubership.atp.environments.mocks.EntitiesGenerator;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.Identified;
import org.qubership.atp.environments.model.ParametersGettingVersion;
import org.qubership.atp.environments.model.ServerItf;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.SystemCategory;
import org.qubership.atp.environments.model.impl.ConnectionImpl;
import org.qubership.atp.environments.model.impl.Context;
import org.qubership.atp.environments.model.impl.EnvironmentImpl;
import org.qubership.atp.environments.model.impl.SystemImpl;
import org.qubership.atp.environments.model.utils.enums.Status;
import org.qubership.atp.environments.repo.impl.ConnectionRepositoryImpl;
import org.qubership.atp.environments.repo.impl.EnvironmentRepositoryImpl;
import org.qubership.atp.environments.repo.impl.SystemCategoryRepositoryImpl;
import org.qubership.atp.environments.repo.impl.SystemRepositoryImpl;
import org.qubership.atp.environments.service.direct.ProjectAccessService;
import org.qubership.atp.environments.service.direct.SystemService;
import org.qubership.atp.environments.service.rest.server.dto.ConnectionTemporaryDto;
import org.qubership.atp.environments.service.rest.server.dto.SystemTemporaryDto;
import org.qubership.atp.environments.service.rest.server.request.EnvironmentsWithFilterRequest;
import org.qubership.atp.environments.service.rest.server.response.GroupedByTagEnvironmentResponse;
import org.qubership.atp.environments.utils.DateTimeUtil;
import org.springframework.util.CollectionUtils;

/**
 * EnvironmentServiceImplTest - test for {@link EnvironmentServiceImpl}
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EnvironmentServiceImplTest {

    private static final DateTimeUtil dateTimeUtil = new DateTimeUtil();

    private final ThreadLocal<EnvironmentRepositoryImpl> environmentRepository = new ThreadLocal<>();
    private final ThreadLocal<SystemRepositoryImpl> systemRepository = new ThreadLocal<>();
    private final ThreadLocal<ConnectionRepositoryImpl> connectionRepository = new ThreadLocal<>();
    private final ThreadLocal<SystemService> systemService = new ThreadLocal<>();
    private final ThreadLocal<SystemCategoryRepositoryImpl> systemCategoryRepository = new ThreadLocal<>();
    private final ThreadLocal<Provider<UserInfo>> userInfoProvider = new ThreadLocal<>();
    private final ThreadLocal<EnvironmentServiceImpl> environmentService = new ThreadLocal<>();

    private final ThreadLocal<Environment> environment1 = new ThreadLocal<>();
    private final ThreadLocal<Environment> environment2 = new ThreadLocal<>();
    private final ThreadLocal<Environment> environmentCreated = new ThreadLocal<>();
    private final ThreadLocal<System> system1 = new ThreadLocal<>();
    private final ThreadLocal<System> system2 = new ThreadLocal<>();
    private final ThreadLocal<Connection> connection1 = new ThreadLocal<>();
    private final ThreadLocal<Connection> connection2 = new ThreadLocal<>();
    private final ThreadLocal<SystemCategory> systemCategory = new ThreadLocal<>();

    @BeforeEach
    public void setUp() throws Exception {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(UUID.randomUUID());

        EnvironmentRepositoryImpl environmentRepositoryMock = mock(EnvironmentRepositoryImpl.class);
        SystemRepositoryImpl systemRepositoryMock = mock(SystemRepositoryImpl.class);
        ConnectionRepositoryImpl connectionRepositoryMock = mock(ConnectionRepositoryImpl.class);
        SystemService systemServiceMock = mock(SystemService.class);
        SystemCategoryRepositoryImpl systemCategoryRepositoryMock = mock(SystemCategoryRepositoryImpl.class);
        Provider userInfoProviderMock = mock(Provider.class);

        when(userInfoProviderMock.get()).thenReturn(userInfo);
        when(environmentRepositoryMock.getContext()).thenReturn(new Context(true));

        environmentRepository.set(environmentRepositoryMock);
        systemRepository.set(systemRepositoryMock);
        connectionRepository.set(connectionRepositoryMock);
        systemService.set(systemServiceMock);
        systemCategoryRepository.set(systemCategoryRepositoryMock);
        userInfoProvider.set(userInfoProviderMock);
        environmentService.set(new EnvironmentServiceImpl(
                environmentRepositoryMock,
                systemRepositoryMock,
                connectionRepositoryMock,
                systemServiceMock,
                systemCategoryRepositoryMock,
                dateTimeUtil,
                userInfoProviderMock,
                mock(ProjectAccessService.class),
                null,
                null));

        SystemCategory systemCategoryThread = EntitiesGenerator.generateSystemCategory(UUID.randomUUID(), "System Category");
        ConnectionImpl connection1Thread = new ConnectionImpl();
        connection1Thread.setId(UUID.randomUUID());
        connection1Thread.setName("connection1 name");
        connection1Thread.setParameters(new ConnectionParameters());
        connection1Thread.getParameters().putAll(
                Stream.of(new String[][]{
                                {"http", ""},
                                {"login", ""},
                                {"password", ""}})
                        .collect(HashMap::new, (m, v) -> m.put(v[0], v[1]), HashMap::putAll));
        ConnectionImpl connection2Thread = new ConnectionImpl();
        connection2Thread.setId(UUID.randomUUID());
        connection2Thread.setName("connection2 name");
        ServerItf serverItf = new ServerItf();
        serverItf.setName("ServerItf name");
        serverItf.setUrl("Test URL");
        UUID uuid = UUID.randomUUID();

        EnvironmentImpl environment1Thread = new EnvironmentImpl(UUID.randomUUID(), "Environment name", "graylogName",
                "Environment description", "", "", "", dateTimeUtil.timestampAsUtc(), UUID.randomUUID(),
                dateTimeUtil.timestampAsUtc(), UUID.randomUUID(), UUID.randomUUID(),
                new ArrayList<>(), UUID.randomUUID(), UUID.randomUUID(), Collections.emptyList());
        EnvironmentImpl environment2Thread = new EnvironmentImpl();
        SystemImpl system1Thread = new SystemImpl(uuid, "1 System name", "System description", dateTimeUtil.timestampAsUtc(),
                //TODO read from file
                null, dateTimeUtil.timestampAsUtc(), null,
                new ArrayList<>(), systemCategoryThread,
                new ArrayList<>(), Status.PASS, dateTimeUtil.timestampAsUtc(), "System version 1",
                dateTimeUtil.timestampAsUtc(), new ParametersGettingVersion(), UUID.randomUUID(), serverItf, true,
                uuid, UUID.randomUUID(), UUID.randomUUID(), null);
        system1Thread.getEnvironments().add(environment1Thread);
        system1Thread.getConnections().add(connection1Thread);
        //TODO read from file
        SystemImpl system2Thread = new SystemImpl(UUID.randomUUID(), "2 System name", "System description", dateTimeUtil.timestampAsUtc(),
                null,
                dateTimeUtil.timestampAsUtc(), null, new ArrayList<>(), systemCategoryThread, new ArrayList<>(),
                Status.PASS, dateTimeUtil.timestampAsUtc(), "System version 2", dateTimeUtil.timestampAsUtc(),
                new ParametersGettingVersion(), UUID.randomUUID(), serverItf, true, UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), null);
        system2Thread.getEnvironments().add(environment1Thread);
        system2Thread.getConnections().add(connection2Thread);
        environment1Thread.getSystems().add(system1Thread);
        environment1Thread.getSystems().add(system2Thread);
        EnvironmentImpl environmentCreatedThread = new EnvironmentImpl(UUID.randomUUID(), "Environment Copy name", "Copy graylogName",
                "Environment Copy description", "", "", "", dateTimeUtil.timestampAsUtc(), UUID.randomUUID(),
                dateTimeUtil.timestampAsUtc(), UUID.randomUUID(), environment1Thread.getProjectId(),
                new ArrayList<>(), UUID.randomUUID(), UUID.randomUUID(), Collections.emptyList());

        environment1.set(environment1Thread);
        environment2.set(environment2Thread);
        environmentCreated.set(environmentCreatedThread);
        system1.set(system1Thread);
        system2.set(system2Thread);
        connection1.set(connection1Thread);
        connection2.set(connection2Thread);
        systemCategory.set(systemCategoryThread);
    }

    @Test
    public void get_EnvironmentReturned_EnvironmentFound() {
        when(environmentRepository.get().getById(any(UUID.class))).thenReturn(environment1.get());
        Environment environmentResult = environmentService.get().get(UUID.randomUUID());
        assertEquals(environment1.get(), environmentResult);
    }

    @Test
    public void get_ThrowException_EnvironmentNotFound() {
        when(environmentRepository.get().getById(any(UUID.class))).thenReturn(null);
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> environmentService.get().get(UUID.randomUUID()));
        assertTrue(exception.getMessage().contains("Wrong environment id: "));
    }

    @Test
    public void getOrElse_OptionalPopulated_EnvironmentFound() {
        when(environmentRepository.get().getById(any(UUID.class))).thenReturn(environment1.get());
        Optional<Environment> orElse = environmentService.get().getOrElse(UUID.randomUUID());
        assertTrue(orElse.isPresent());
        assertEquals(environment1.get(), orElse.get());
    }

    @Test
    public void getOrElse_OptionalBlank_EnvironmentNotFound() {
        when(environmentRepository.get().getById(any(UUID.class))).thenReturn(null);
        Optional<Environment> orElse = environmentService.get().getOrElse(UUID.randomUUID());
        assertFalse(orElse.isPresent());
    }

    @Test
    public void copy_CopiedSuccessfully_UnSharedSystem() {
        when(environmentRepository.get().getById(any(UUID.class))).thenReturn(environment1.get());
        when(environmentRepository.get().create(anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyLong(),
                any(UUID.class), any(UUID.class), any(UUID.class), anyList())).thenReturn(environmentCreated.get());
        when(systemRepository.get().create(any(), anyString(), anyString(), anyLong(), any(),
                any(), any(ParametersGettingVersion.class), any(), any(ServerItf.class),
                anyBoolean(), any(), any(), any()))
                .thenReturn(system1.get())
                .thenReturn(system2.get());
        when(connectionRepository.get().create(any(), anyString(), anyString(), any(), anyLong(), any(), anyString(), any(),
                any(), any()))
                .thenReturn(connection1.get())
                .thenReturn(connection2.get());
        Environment environmentResult =
                environmentService.get().copy(environment1.get().getId(), environment1.get().getProjectId(),
                        environment1.get().getName(), environment1.get().getGraylogName(),
                        environment1.get().getDescription(), environment1.get().getSsmSolutionAlias(),
                        environment1.get().getSsmInstanceAlias(),
                        environment1.get().getConsulEgressConfigPath(), environment1.get().getCategoryId(),
                        environment1.get().getTags());
        assertEquals(environmentCreated.get(), environmentResult);
    }

    @Test
    public void copy_CopiedSuccessfully_SharedSystem() {
        environment2.get().setProjectId(environment1.get().getProjectId());
        system2.get().getEnvironments().add(environment2.get());
        when(environmentRepository.get().getById(any(UUID.class))).thenReturn(environment1.get());
        when(environmentRepository.get().create(anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyLong(),
                any(UUID.class), any(UUID.class), any(UUID.class), anyList())).thenReturn(environmentCreated.get());
        when(connectionRepository.get().create(any(), anyString(), anyString(), any(), anyLong(), any(), anyString(), any(),
                any(), any()))
                .thenReturn(connection1.get())
                .thenReturn(connection2.get());
        when(systemRepository.get().create(any(), anyString(), anyString(), anyLong(), any(),
                any(), any(ParametersGettingVersion.class), any(), any(ServerItf.class),
                anyBoolean(), any(), any(), any()))
                .thenReturn(system1.get())
                .thenReturn(system2.get());
        Environment environmentResult =
                environmentService.get().copy(environment1.get().getId(), environment1.get().getProjectId(),
                        environment1.get().getName(), environment1.get().getGraylogName(), environment1.get().getDescription(),
                        environment1.get().getSsmSolutionAlias(),
                        environment1.get().getSsmInstanceAlias(),
                        environment1.get().getConsulEgressConfigPath(),
                        environment1.get().getCategoryId(), environment1.get().getTags());
        assertEquals(environmentCreated.get(), environmentResult);
    }

    @Test
    public void copy_ThrowException_EnvironmentNotFound() {
        when(environmentRepository.get().getById(any(UUID.class))).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> environmentService.get().copy(environment1.get().getId(), environment1.get().getProjectId(), environment1.get().getName(),
                        environment1.get().getGraylogName(), environment1.get().getDescription(), environment1.get().getSsmSolutionAlias(),
                        environment1.get().getSsmInstanceAlias(),
                        environment1.get().getConsulEgressConfigPath(),
                        environment1.get().getCategoryId(), environment1.get().getTags()));
        assertTrue(exception.getMessage().contains("Environment"));
        assertTrue(exception.getMessage().contains("can't be empty"));
    }

    @Test
    public void temporary_EnvironmentCreatedSuccessfully_SystemNameNotMatchesSystemDTO() {
        ConnectionTemporaryDto connectionTemporaryDto = new ConnectionTemporaryDto();
        connectionTemporaryDto.setName(connection1.get().getName());
        connectionTemporaryDto.setParameters(connection1.get().getParameters());
        SystemTemporaryDto systemTemporaryDto = new SystemTemporaryDto();
        systemTemporaryDto.setName("systemTemporaryDto");
        systemTemporaryDto.setSystemCategory("SystemCategory");
        systemTemporaryDto.setConnections(new ArrayList<>());
        systemTemporaryDto.getConnections().add(connectionTemporaryDto);
        when(environmentRepository.get().getById(any(UUID.class))).thenReturn(environment1.get());
        when(environmentRepository.get().create(anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyLong(),
                any(UUID.class), any(UUID.class), any(UUID.class), anyList())).thenReturn(environmentCreated.get());
        when(systemRepository.get().create(any(), anyString(), anyString(), anyLong(), any(), any(),
                any(ParametersGettingVersion.class), any(), any(ServerItf.class), anyBoolean(), any(),
                any(), any())).thenReturn(system1.get());
        when(systemRepository.get().create(any(), anyString(), isNull(), anyLong(), any(), any(),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(system1.get());
        when(connectionRepository.get().create(any(), anyString(), anyString(), any(), anyLong(), any(), anyString(), any(),
                any(), any())).thenReturn(connection1.get());
        when(systemCategoryRepository.get().getByName(anyString())).thenReturn(systemCategory.get());
        when(connectionRepository.get().getConnectionTemplateByName(any())).thenReturn(connection1.get());
        Environment environmentResult =
                environmentService.get().temporary(UUID.randomUUID(), Collections.singletonList(systemTemporaryDto));
        assertEquals(environmentCreated.get(), environmentResult);
    }

    @Test
    public void temporary_EnvironmentCreatedSuccessfully_SystemNameMatchesSystemDTO() {
        ConnectionTemporaryDto connectionTemporaryDto = new ConnectionTemporaryDto();
        connectionTemporaryDto.setName(connection1.get().getName());
        connectionTemporaryDto.setParameters(connection1.get().getParameters());
        SystemTemporaryDto systemTemporaryDto = new SystemTemporaryDto();
        systemTemporaryDto.setName(system1.get().getName());
        systemTemporaryDto.setSystemCategory("SystemCategory");
        systemTemporaryDto.setConnections(new ArrayList<>());
        systemTemporaryDto.getConnections().add(connectionTemporaryDto);
        List<SystemTemporaryDto> systemList = new ArrayList<>();
        systemList.add(systemTemporaryDto);
        when(environmentRepository.get().getById(any(UUID.class))).thenReturn(environment1.get());
        when(environmentRepository.get().create(anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyLong(),
                any(UUID.class), any(UUID.class), any(UUID.class), anyList())).thenReturn(environmentCreated.get());
        when(systemRepository.get().create(any(), anyString(), anyString(), anyLong(), any(), any(),
                any(ParametersGettingVersion.class), any(), any(ServerItf.class), anyBoolean(), any(),
                any(), any())).thenReturn(system1.get());
        when(connectionRepository.get().create(any(), anyString(), anyString(), any(), anyLong(), any(), anyString(), any(),
                any(), any())).thenReturn(connection1.get());
        when(systemCategoryRepository.get().getByName(anyString())).thenReturn(systemCategory.get());
        when(connectionRepository.get().getById(any())).thenReturn(connection1.get());
        Environment environmentResult =
                environmentService.get().temporary(UUID.randomUUID(), systemList);
        assertEquals(environmentCreated.get(), environmentResult);
    }

    @Test
    public void temporary_ThrowException_EnvironmentNotFound() {
        SystemTemporaryDto systemTemporaryDto = new SystemTemporaryDto();
        when(environmentRepository.get().getById(any(UUID.class))).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> environmentService.get().temporary(UUID.randomUUID(), Collections.singletonList(systemTemporaryDto)));
        assertTrue(exception.getMessage().contains("Environment"));
        assertTrue(exception.getMessage().contains("can't be empty"));
    }

    @Test
    public void test_getEnvironmentsByFilterRequest_throwsExceptionIfFieldsEmpty() {
        EnvironmentsWithFilterRequest request = new EnvironmentsWithFilterRequest();

        assertThrows(EnvironmentsWithFilterRequestException.class, () ->
                environmentService.get().getEnvironmentsByFilterRequest(request, 1, 10)
        );
    }

    @Test
    public void test_getEnvironmentsByFilterRequest_calculatesOffset() {
        EnvironmentsWithFilterRequest request = new EnvironmentsWithFilterRequest();
        request.setFields(Arrays.asList("id", "name"));

        List<Environment> environments = Arrays.asList(environment1.get(), environment2.get());
        when(environmentRepository.get().getEnvironmentsByFilterPaged(request, 10, 10)).thenReturn(environments);

        List<Environment> result = environmentService.get().getEnvironmentsByFilterRequest(request, 2, 10);

        assertEquals(environments, result);
    }

    @Test
    public void test_getEnvironmentsByFilterRequest_callsRepositoryWithCorrectParameters() {
        EnvironmentsWithFilterRequest request = new EnvironmentsWithFilterRequest();
        request.setFields(Arrays.asList("id", "name"));

        environmentService.get().getEnvironmentsByFilterRequest(request, 2, 10);

        verify(environmentRepository.get(), times(1)).getEnvironmentsByFilterPaged(request, 10, 10);
    }

    @Test
    public void getHtmlVersionByEnvironmentIds_allEnvironmentsExists_gotVersions() {
        List<Environment> listEnv = getEnvironmentsListForCheckVersion();
        String expectedHtmlVersion = "<html><body><table style=\"border-collapse: collapse;\"><tr><td><b>Test "
                + "environment 1</b></td></tr><tr style=\"border-collapse: collapse;\"><td style=\"border: 1px solid rgb"
                + "(0,0,0)\">Test system</td><td style=\"border: 1px solid rgb(0,0,0)\">Unknown</td></tr><tr><td><b>Test "
                + "environment 2</b></td></tr><tr style=\"border-collapse: collapse;\"><td style=\"border: 1px solid rgb"
                + "(0,0,0)\">Test system</td><td style=\"border: 1px solid rgb(0,0,0)"
                + "\">Unknown</td></tr></table></body></html>";

        Mockito.when(environmentRepository.get().getByIds(any())).thenReturn(listEnv);
        String htmlVersion = environmentService.get().getHtmlVersionByEnvironments(
                listEnv.stream().map(Identified::getId).collect(Collectors.toList())
        );

        Assertions.assertEquals(expectedHtmlVersion, htmlVersion);
    }

    @Test
    public void getHtmlVersionByEnvironmentIds_oneEnvironmentNotExists_gotNull() {
        List<Environment> listEnv = getEnvironmentsListForCheckVersion();
        Mockito.when(environmentRepository.get().getByIds(any())).thenReturn(listEnv);
        List<UUID> environmentIds = listEnv.stream()
                .map(Identified::getId).collect(Collectors.toList());
        environmentIds.add(UUID.randomUUID());
        String htmlVersion = environmentService.get().getHtmlVersionByEnvironments(environmentIds);
        Assertions.assertEquals(htmlVersion, "");
    }

    @Test
    public void getGroupedByTagEnvironments_withFilledTags_gotResponses() {
        String tag = "DEV";
        List<Environment> listEnv = getEnvironmentsWithTags(tag);
        Mockito.when(environmentRepository.get().getAllShortByParentId(any(), any())).thenReturn(listEnv);
        Collection<GroupedByTagEnvironmentResponse> responses =
                environmentService.get().getGroupedByTagEnvironments(UUID.randomUUID());
        Assertions.assertTrue(responses.stream().anyMatch(response -> response.getTag().equals(tag)));
        Assertions.assertTrue(responses.stream().filter(response -> response.getTag().equals(tag))
                .anyMatch(response -> response.getEnvironments().get(0).getTags().contains(tag)));
        Assertions.assertTrue(responses.stream().anyMatch(response -> response.getTag().equals("No Tags")));
        Assertions.assertTrue(responses.stream().filter(response -> response.getTag().equals("No Tags"))
                .anyMatch(response -> CollectionUtils.isEmpty(response.getEnvironments().get(0).getTags())));
    }

    private List<Environment> getEnvironmentsListForCheckVersion() {
        UUID projectId = UUID.randomUUID();
        Environment envTest1 = new EnvironmentImpl(UUID.randomUUID(), "Test environment 1", "",
                null, "", "", "", null, null, null, null,
                projectId, null, null, null, Collections.emptyList());
        Environment envTest2 = new EnvironmentImpl(UUID.randomUUID(), "Test environment 2", "",
                null, "", "", "", null, null, null, null,
                projectId, null, null, null, Collections.emptyList());
        List<Environment> listEnv = Arrays.asList(envTest1, envTest2);
        SystemImpl systemOrigin = new SystemImpl(UUID.randomUUID(), "Test system", null, null, null,
                null, null, listEnv,
                null, null, null,
                null, "Unknown", null,
                null,
                null, null, false, null, null, null, null);
        envTest1.setSystems(Collections.singletonList(systemOrigin));
        envTest2.setSystems(Collections.singletonList(systemOrigin));
        Mockito.when(systemService.get().getCachedVersionBySystem(any())).thenReturn(systemOrigin);
        return listEnv;
    }

    private List<Environment> getEnvironmentsWithTags(String tag) {
        UUID projectId = UUID.randomUUID();
        Environment envTest1 = new EnvironmentImpl(UUID.randomUUID(), "Test environment 1", "",
                null, "", "", "", null, null, null, null,
                projectId, null, null, null, Collections.singleton(tag));
        Environment envTest2 = new EnvironmentImpl(UUID.randomUUID(), "Test environment 2", "",
                null, "", "", "", null, null, null, null,
                projectId, null, null, null, Collections.emptyList());
        List<Environment> listEnv = Arrays.asList(envTest1, envTest2);
        return listEnv;
    }

    @Test
    public void getSystemsYamlZipArchive_WithSystemType_ReturnsValidZipArchive() throws IOException {
        // Given
        UUID environmentId = UUID.randomUUID();
        String systemType = "TEST_SYSTEM_TYPE";
        System testSystem = createSystemWithConnections("TestSystem", "connection1");
        Collection<System> systems = Collections.singletonList(testSystem);

        when(systemRepository.get().getAllByParentIdV2(eq(environmentId), eq(systemType))).thenReturn(systems);
        when(systemService.get().generateSystemsYaml(any()))
                .thenReturn(new String[]{"deployment-params-yaml", "credentials-yaml"});

        // When
        byte[] zipBytes = environmentService.get().getSystemsYamlZipArchive(environmentId, systemType);

        // Then
        assertNotNull(zipBytes);
        assertThat(zipBytes.length).isGreaterThan(0);

        // Verify ZIP structure
        verifyZipContents(zipBytes, "deployment-parameters.yaml", "credentials.yaml");

        // Verify repository was called with correct parameters
        verify(systemRepository.get(), times(1)).getAllByParentIdV2(environmentId, systemType);
        verify(systemRepository.get(), times(0)).getAllByParentIdV2(environmentId);
        verify(systemService.get(), times(1)).generateSystemsYaml(systems);
    }

    @Test
    public void getSystemsYamlZipArchive_WithoutSystemType_ReturnsValidZipArchive() throws IOException {
        // Given
        UUID environmentId = UUID.randomUUID();
        System testSystem1 = createSystemWithConnections("System1", "conn1");
        System testSystem2 = createSystemWithConnections("System2", "conn2");
        Collection<System> systems = Arrays.asList(testSystem1, testSystem2);

        when(systemRepository.get().getAllByParentIdV2(eq(environmentId))).thenReturn(systems);
        when(systemService.get().generateSystemsYaml(any()))
                .thenReturn(new String[]{"deployment-params-yaml", "credentials-yaml"});

        // When
        byte[] zipBytes = environmentService.get().getSystemsYamlZipArchive(environmentId, null);

        // Then
        assertNotNull(zipBytes);
        assertThat(zipBytes.length).isGreaterThan(0);

        // Verify ZIP structure
        verifyZipContents(zipBytes, "deployment-parameters.yaml", "credentials.yaml");

        // Verify repository was called with correct parameters
        verify(systemRepository.get(), times(1)).getAllByParentIdV2(environmentId);
        verify(systemRepository.get(), times(0)).getAllByParentIdV2(any(UUID.class), anyString());
        verify(systemService.get(), times(1)).generateSystemsYaml(systems);
    }

    @Test
    public void getSystemsYamlZipArchive_WithEmptySystems_ReturnsEmptyZipArchive() throws IOException {
        // Given
        UUID environmentId = UUID.randomUUID();
        Collection<System> emptySystems = Collections.emptyList();

        when(systemRepository.get().getAllByParentIdV2(eq(environmentId))).thenReturn(emptySystems);
        when(systemService.get().generateSystemsYaml(any()))
                .thenReturn(new String[]{"deployment-params-yaml", "credentials-yaml"});

        // When
        byte[] zipBytes = environmentService.get().getSystemsYamlZipArchive(environmentId, null);

        // Then
        assertNotNull(zipBytes);
        assertThat(zipBytes.length).isGreaterThan(0);

        // Verify ZIP structure still contains both files (empty YAML files)
        verifyZipContents(zipBytes, "deployment-parameters.yaml", "credentials.yaml");

        // Verify repository was called
        verify(systemRepository.get(), times(1)).getAllByParentIdV2(environmentId);
        verify(systemService.get(), times(1)).generateSystemsYaml(emptySystems);
    }

    @Test
    public void getSystemsYamlZipArchive_WithSystemTypeAndEmptySystems_ReturnsEmptyZipArchive() throws IOException {
        // Given
        UUID environmentId = UUID.randomUUID();
        String systemType = "NON_EXISTENT_TYPE";
        Collection<System> emptySystems = Collections.emptyList();

        when(systemRepository.get().getAllByParentIdV2(eq(environmentId), eq(systemType))).thenReturn(emptySystems);
        when(systemService.get().generateSystemsYaml(any()))
                .thenReturn(new String[]{"deployment-params-yaml", "credentials-yaml"});

        // When
        byte[] zipBytes = environmentService.get().getSystemsYamlZipArchive(environmentId, systemType);

        // Then
        assertNotNull(zipBytes);
        assertThat(zipBytes.length).isGreaterThan(0);

        // Verify ZIP structure
        verifyZipContents(zipBytes, "deployment-parameters.yaml", "credentials.yaml");

        // Verify repository was called with system type
        verify(systemRepository.get(), times(1)).getAllByParentIdV2(environmentId, systemType);
        verify(systemService.get(), times(1)).generateSystemsYaml(emptySystems);
    }

    @Test
    public void getSystemsYamlZipArchive_ContainsDeploymentParametersYaml() throws IOException {
        // Given
        UUID environmentId = UUID.randomUUID();
        System testSystem = createSystemWithConnections("TestSystem", "TestConnection");
        ConnectionParameters params = testSystem.getConnections().get(0).getParameters();
        params.put("host", "localhost");
        params.put("port", "8080");

        String deploymentParamsYaml =
                "ATP_ENVGENE_CONFIGURATION:\n" +
                        "  systems:\n" +
                        "  - TestSystem:\n" +
                        "      connections:\n" +
                        "      - TestConnection:\n" +
                        "          host: localhost\n" +
                        "          port: \"8080\"\n";
        Collection<System> systems = Collections.singletonList(testSystem);
        when(systemRepository.get().getAllByParentIdV2(eq(environmentId))).thenReturn(systems);
        when(systemService.get().generateSystemsYaml(any()))
                .thenReturn(new String[]{deploymentParamsYaml, "credentials-yaml"});

        // When
        byte[] zipBytes = environmentService.get().getSystemsYamlZipArchive(environmentId, null);

        // Then
        String deploymentParamsContent = extractZipEntryContent(zipBytes, "deployment-parameters.yaml");
        assertNotNull(deploymentParamsContent);
        assertThat(deploymentParamsContent).contains("ATP_ENVGENE_CONFIGURATION");
        assertThat(deploymentParamsContent).contains("systems");
        assertThat(deploymentParamsContent).contains("TestSystem");
        assertThat(deploymentParamsContent).contains("host");
        assertThat(deploymentParamsContent).contains("localhost");
        verify(systemService.get(), times(1)).generateSystemsYaml(systems);
    }

    @Test
    public void getSystemsYamlZipArchive_ContainsCredentialsYaml() throws IOException {
        // Given
        UUID environmentId = UUID.randomUUID();
        System testSystem = createSystemWithConnections("TestSystem", "TestConnection");
        ConnectionParameters params = testSystem.getConnections().get(0).getParameters();
        params.put("password", "secret123");
        params.put("token", "token123");
        String encryptedValue = "{ENC}encrypted_value";
        String decryptedValue = "decrypted_value";
        params.put("encrypted_param", encryptedValue);

        String credentialsYaml = String.format(
                "ATP_ENVGENE_CONFIGURATION:\n" +
                        "  systems:\n" +
                        "  - TestSystem:\n" +
                        "      connections:\n" +
                        "      - TestConnection:\n" +
                        "          password: secret123\n" +
                        "          token: token123\n" +
                        "          encrypted_param: %s\n",
                decryptedValue
        );
        Collection<System> systems = Collections.singletonList(testSystem);
        when(systemRepository.get().getAllByParentIdV2(eq(environmentId))).thenReturn(systems);
        when(systemService.get().generateSystemsYaml(any()))
                .thenReturn(new String[]{"deployment-params-yaml", credentialsYaml});

        // When
        byte[] zipBytes = environmentService.get().getSystemsYamlZipArchive(environmentId, null);

        // Then
        String credentialsContent = extractZipEntryContent(zipBytes, "credentials.yaml");
        assertNotNull(credentialsContent);
        assertThat(credentialsContent).contains("ATP_ENVGENE_CONFIGURATION");
        assertThat(credentialsContent).contains("systems");
        assertThat(credentialsContent).contains("password");
        assertThat(credentialsContent).contains("secret123");
        assertThat(credentialsContent).contains("token");
        assertThat(credentialsContent).contains("token123");
        assertThat(credentialsContent).contains("encrypted_param");
        // Verify that encrypted value is decrypted in credentials.yaml
        assertThat(credentialsContent).contains(decryptedValue);
        assertThat(credentialsContent).doesNotContain(encryptedValue);
        verify(systemService.get(), times(1)).generateSystemsYaml(systems);
    }

    @Test
    public void getSystemsYamlZipArchive_WithMultipleSystems_GeneratesCorrectYaml() throws IOException {
        // Given
        UUID environmentId = UUID.randomUUID();
        System system1 = createSystemWithConnections("System1", "Conn1");
        System system2 = createSystemWithConnections("System2", "Conn2");
        Collection<System> systems = Arrays.asList(system1, system2);

        String deploymentParamsYaml =
                "ATP_ENVGENE_CONFIGURATION:\n" +
                        "  systems:\n" +
                        "  - System1:\n" +
                        "      connections:\n" +
                        "      - Conn1:\n" +
                        "          key1: value1\n" +
                        "  - System2:\n" +
                        "      connections:\n" +
                        "      - Conn2:\n" +
                        "          key1: value1\n";
        when(systemRepository.get().getAllByParentIdV2(eq(environmentId))).thenReturn(systems);
        when(systemService.get().generateSystemsYaml(any()))
                .thenReturn(new String[]{deploymentParamsYaml, "credentials-yaml"});

        // When
        byte[] zipBytes = environmentService.get().getSystemsYamlZipArchive(environmentId, null);

        // Then
        String deploymentParamsContent = extractZipEntryContent(zipBytes, "deployment-parameters.yaml");
        assertNotNull(deploymentParamsContent);
        assertThat(deploymentParamsContent).contains("System1");
        assertThat(deploymentParamsContent).contains("System2");
        verify(systemService.get(), times(1)).generateSystemsYaml(systems);
    }

    @Test
    public void getSystemsYamlZipArchive_WithSystemWithoutConnections_GeneratesEmptyYaml() throws IOException {
        // Given
        UUID environmentId = UUID.randomUUID();
        System systemWithoutConnections = new SystemImpl();
        systemWithoutConnections.setId(UUID.randomUUID());
        systemWithoutConnections.setName("SystemWithoutConnections");
        systemWithoutConnections.setConnections(Collections.emptyList());

        String deploymentParamsYaml = "ATP_ENVGENE_CONFIGURATION:\n" +
                "  systems: []\n";
        Collection<System> systems = Collections.singletonList(systemWithoutConnections);
        when(systemRepository.get().getAllByParentIdV2(eq(environmentId))).thenReturn(systems);
        when(systemService.get().generateSystemsYaml(any()))
                .thenReturn(new String[]{deploymentParamsYaml, "credentials-yaml"});

        // When
        byte[] zipBytes = environmentService.get().getSystemsYamlZipArchive(environmentId, null);

        // Then
        assertNotNull(zipBytes);
        String deploymentParamsContent = extractZipEntryContent(zipBytes, "deployment-parameters.yaml");
        assertNotNull(deploymentParamsContent);
        // Should contain empty systems list
        assertThat(deploymentParamsContent).contains("ATP_ENVGENE_CONFIGURATION");
        assertThat(deploymentParamsContent).contains("systems");
        verify(systemService.get(), times(1)).generateSystemsYaml(systems);
    }

    /**
     * Helper method to create a System with connections for testing.
     */
    private System createSystemWithConnections(String systemName, String connectionName) {
        System system = new SystemImpl();
        system.setId(UUID.randomUUID());
        system.setName(systemName);

        Connection connection = new ConnectionImpl();
        connection.setId(UUID.randomUUID());
        connection.setName(connectionName);
        connection.setParameters(new ConnectionParameters());
        connection.getParameters().put("key1", "value1");
        connection.getParameters().put("key2", "value2");

        system.setConnections(Collections.singletonList(connection));
        return system;
    }

    /**
     * Helper method to verify ZIP archive contains expected entries.
     */
    private void verifyZipContents(byte[] zipBytes, String... expectedEntries) throws IOException {
        List<String> entryNames = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                entryNames.add(entry.getName());
                zis.closeEntry();
            }
        }

        assertThat(entryNames).containsExactlyInAnyOrder(expectedEntries);
    }

    /**
     * Helper method to extract content from a specific ZIP entry.
     */
    private String extractZipEntryContent(byte[] zipBytes, String entryName) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals(entryName)) {
                    byte[] buffer = new byte[1024];
                    StringBuilder content = new StringBuilder();
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        content.append(new String(buffer, 0, len, "UTF-8"));
                    }
                    zis.closeEntry();
                    return content.toString();
                }
                zis.closeEntry();
            }
        }
        return null;
    }
}
