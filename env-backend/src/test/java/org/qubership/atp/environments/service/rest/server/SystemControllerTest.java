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

package org.qubership.atp.environments.service.rest.server;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.Mockito;
import org.qubership.atp.environments.Main;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.ParametersGettingVersion;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.impl.ConnectionImpl;
import org.qubership.atp.environments.model.impl.Context;
import org.qubership.atp.environments.model.impl.EnvironmentImpl;
import org.qubership.atp.environments.model.impl.SystemImpl;
import org.qubership.atp.environments.model.utils.enums.Status;
import org.qubership.atp.environments.repo.impl.ContextRepository;
import org.qubership.atp.environments.service.direct.ConcurrentModificationService;
import org.qubership.atp.environments.service.direct.SystemService;
import org.qubership.atp.environments.service.rest.server.dto.CreateSystemDto;
import org.qubership.atp.environments.service.rest.server.dto.SharingRequestDto;
import org.qubership.atp.environments.service.rest.server.dto.SystemDto;
import org.qubership.atp.environments.service.rest.server.request.SynchronizeCloudServicesRequest;
import org.qubership.atp.environments.service.rest.server.response.ShortExternalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * SystemControllerTest - test for {@link SystemController}
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = Main.class)
@TestPropertySource("classpath:application-test-rest-api.properties")
@Isolated
public class SystemControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SystemService systemService;

    private System system;
    private Connection connection;
    private Environment environment;
    private final ObjectMapper om = new ObjectMapper();

    @BeforeEach
    public void setUp() throws Exception {
        system = new SystemImpl();
        system.setId(UUID.randomUUID());
        system.setName("Test System");
        system.setDescription("Test Description");
        system.setStatus(Status.PASS);
        system.setVersion("test Version");
        system.setDateOfCheckVersion(1635189430738L);
        system.setExternalId(UUID.randomUUID());
        connection = new ConnectionImpl();
        connection.setId(UUID.randomUUID());
        connection.setName("Test Connection");
        connection.setDescription("Connection Description");
        connection.setParameters(new ConnectionParameters());
        connection.setConnectionType("Connection Type");
        connection.setSourceTemplateId(UUID.randomUUID());
        connection.setSystemId(system.getId());
        connection.setServices(Collections.singletonList("Test Service"));
        system.setConnections(Collections.singletonList(connection));
        environment = new EnvironmentImpl();
        environment.setId(UUID.randomUUID());
        environment.setName("Test Environment");
        environment.setDescription("Environment Description");
        environment.setSystems(Collections.singletonList(system));
        environment.setProjectId(UUID.randomUUID());
        environment.setCategoryId(UUID.randomUUID());
        system.setEnvironments(Collections.singletonList(environment));
    }

    @Test
    public void getAll_RequestPassed_GoodRequest() throws Exception {
        when(systemService.getAll()).thenReturn(Collections.singletonList(system));
        mockMvc.perform(MockMvcRequestBuilders.
                        get("/api/systems")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[0].id").value(system.getId().toString()))
                .andExpect(jsonPath("$[0].name").value(system.getName()))
                .andExpect(jsonPath("$[0].description").value(system.getDescription()))
                .andExpect(jsonPath("$[0].status").value(system.getStatus().toString()))
                .andExpect(jsonPath("$[0].version").value(system.getVersion()))
                .andExpect(jsonPath("$[0].dateOfCheckVersion").value(system.getDateOfCheckVersion()))
                .andExpect(jsonPath("$[0].externalId").value(system.getExternalId().toString()))
                .andExpect(jsonPath("$[0].connections[0]").value(connection.getId().toString()))
                .andExpect(jsonPath("$[0].environmentIds[0]").value(environment.getId().toString()));
    }

    @Test
    public void getCachedVersion_RequestPassed_GoodRequest() throws Exception {
        when(systemService.getCachedVersionById(any())).thenReturn(system);
        mockMvc.perform(MockMvcRequestBuilders.
                        get("/api/public/v1/systems/{systemId}/version", system.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.id").value(system.getId().toString()))
                .andExpect(jsonPath("$.name").value(system.getName()))
                .andExpect(jsonPath("$.description").value(system.getDescription()))
                .andExpect(jsonPath("$.status").value(system.getStatus().toString()))
                .andExpect(jsonPath("$.version").value(system.getVersion()))
                .andExpect(jsonPath("$.dateOfCheckVersion").value(system.getDateOfCheckVersion()))
                .andExpect(jsonPath("$.externalId").value(system.getExternalId().toString()))
                .andExpect(jsonPath("$.connections[0]").value(connection.getId().toString()))
                .andExpect(jsonPath("$.environmentIds[0]").value(environment.getId().toString()));
    }

    @Test
    public void getCachedVersion_ThrowException_ReturnNull() throws Exception {
        when(systemService.getCachedVersionById(any())).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders.
                        get("/api/public/v1/systems/{systemId}/version", system.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getSystem_RequestPassed_GoodRequest() throws Exception {
        when(systemService.get(any())).thenReturn(system);
        mockMvc.perform(MockMvcRequestBuilders.
                        get("/api/systems/{systemId}", system.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.id").value(system.getId().toString()))
                .andExpect(jsonPath("$.name").value(system.getName()))
                .andExpect(jsonPath("$.description").value(system.getDescription()))
                .andExpect(jsonPath("$.status").value(system.getStatus().toString()))
                .andExpect(jsonPath("$.version").value(system.getVersion()))
                .andExpect(jsonPath("$.dateOfCheckVersion").value(system.getDateOfCheckVersion()))
                .andExpect(jsonPath("$.externalId").value(system.getExternalId().toString()))
                .andExpect(jsonPath("$.connections[0]").value(connection.getId().toString()))
                .andExpect(jsonPath("$.environmentIds[0]").value(environment.getId().toString()));
    }

    @Test
    public void getSystem_ThrowException_ReturnNull() throws Exception {
        when(systemService.get(any())).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders.
                        get("/api/systems/{systemId}", system.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getSystemV2_RequestPassed_GoodRequest() throws Exception {
        when(systemService.getV2(any())).thenReturn(system);
        mockMvc.perform(MockMvcRequestBuilders.
                        get("/api/v2/systems/{systemId}", system.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.id").value(system.getId().toString()))
                .andExpect(jsonPath("$.name").value(system.getName()))
                .andExpect(jsonPath("$.description").value(system.getDescription()))
                .andExpect(jsonPath("$.status").value(system.getStatus().toString()))
                .andExpect(jsonPath("$.version").value(system.getVersion()))
                .andExpect(jsonPath("$.dateOfCheckVersion").value(system.getDateOfCheckVersion()))
                .andExpect(jsonPath("$.externalId").value(system.getExternalId().toString()))
                .andExpect(jsonPath("$.connections[0]").value(connection.getId().toString()))
                .andExpect(jsonPath("$.environments[0]").value(environment.getId().toString()));
    }

    public void getSystemV2_ThrowException_ReturnNull() throws Exception {
        when(systemService.getV2(any())).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders.
                        get("/api/v2/systems/{systemId}", system.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getSystemConnections_RequestPassed_GoodRequest() throws Exception {
        when(systemService.getConnections(any())).thenReturn(Collections.singletonList(connection));
        mockMvc.perform(MockMvcRequestBuilders.
                        get("/api/systems/{systemId}/connections", system.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[0].id").value(connection.getId().toString()))
                .andExpect(jsonPath("$[0].name").value(connection.getName()))
                .andExpect(jsonPath("$[0].description").value(connection.getDescription()))
                .andExpect(jsonPath("$[0].sourceTemplateId").value(connection.getSourceTemplateId().toString()))
                .andExpect(jsonPath("$[0].connectionType").value(connection.getConnectionType()))
                .andExpect(jsonPath("$[0].services[0]").value(connection.getServices().get(0)))
                .andExpect(jsonPath("$[0].systemId").value(connection.getSystemId().toString()));
    }

    @Test
    public void getAllSystems_RequestPassed_GoodRequest() throws Exception {
        when(systemService.getAll()).thenReturn(Collections.singletonList(system));
        mockMvc.perform(MockMvcRequestBuilders.
                        get("/api/systems/short")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[0].id").value(system.getId().toString()))
                .andExpect(jsonPath("$[0].name").value(system.getName()))
                .andExpect(jsonPath("$[0].environmentIds[0]").value(environment.getId().toString()))
                .andExpect(jsonPath("$[0].description").doesNotExist())
                .andExpect(jsonPath("$[0].status").doesNotExist())
                .andExpect(jsonPath("$[0].version").doesNotExist())
                .andExpect(jsonPath("$[0].dateOfCheckVersion").doesNotExist())
                .andExpect(jsonPath("$[0].externalId").doesNotExist())
                .andExpect(jsonPath("$[0].connections").doesNotExist());
    }

    @Test
    public void createSystem_RequestPassed_GoodRequest() throws Exception {
        when(systemService.create(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(
                system);
        CreateSystemDto systemDto = new CreateSystemDto();
        systemDto.setName("Test System");
        systemDto.setEnvironmentId(UUID.randomUUID());
        String requestJson = objectMapper.writer().writeValueAsString(systemDto);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/systems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.id").value(system.getId().toString()))
                .andExpect(jsonPath("$.name").value(system.getName()))
                .andExpect(jsonPath("$.description").value(system.getDescription()))
                .andExpect(jsonPath("$.status").value(system.getStatus().toString()))
                .andExpect(jsonPath("$.version").value(system.getVersion()))
                .andExpect(jsonPath("$.dateOfCheckVersion").value(system.getDateOfCheckVersion()))
                .andExpect(jsonPath("$.externalId").value(system.getExternalId().toString()))
                .andExpect(jsonPath("$.connections[0]").value(connection.getId().toString()))
                .andExpect(jsonPath("$.environmentIds[0]").value(environment.getId().toString()));
    }

    @Test
    public void createSystem_ThrowException_SystemNameNull() throws Exception {
        when(systemService.create(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(
                system);
        CreateSystemDto systemDto = new CreateSystemDto();
        systemDto.setEnvironmentId(UUID.randomUUID());
        String requestJson = objectMapper.writer().writeValueAsString(systemDto);
        Exception exception = mockMvc.perform(MockMvcRequestBuilders.post("/api/systems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().is(500))
                .andReturn()
                .getResolvedException();
        Assertions.assertTrue(exception.getMessage().contains("System name can't be null"));
    }

    @Test
    public void createSystem_ThrowException_SystemNameEmpty() throws Exception {
        when(systemService.create(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(
                system);
        CreateSystemDto systemDto = new CreateSystemDto();
        systemDto.setName("");
        systemDto.setEnvironmentId(UUID.randomUUID());
        String requestJson = objectMapper.writer().writeValueAsString(systemDto);
        Exception exception = mockMvc.perform(MockMvcRequestBuilders.post("/api/systems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().is(500))
                .andReturn()
                .getResolvedException();
        Assertions.assertTrue(exception.getMessage().contains("System name can't be empty"));
    }

    @Test
    public void createSystem_ThrowException_EnvironmentIdEmpty() throws Exception {
        when(systemService.create(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(
                system);
        CreateSystemDto systemDto = new CreateSystemDto();
        systemDto.setName("Test Name");
        String requestJson = objectMapper.writer().writeValueAsString(systemDto);
        Exception exception = mockMvc.perform(MockMvcRequestBuilders.post("/api/systems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().is(500))
                .andReturn()
                .getResolvedException();
        Assertions.assertTrue(exception.getMessage().contains("Environment ID can't be empty"));
    }

    @Test
    public void copy_RequestPassed_GoodRequest() throws Exception {
        when(systemService.copy(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any())).thenReturn(system);
        CreateSystemDto systemDto = new CreateSystemDto();
        systemDto.setName("Test System");
        systemDto.setEnvironmentId(UUID.randomUUID());
        String requestJson = objectMapper.writer().writeValueAsString(systemDto);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/systems/{systemId}/copy", system.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.id").value(system.getId().toString()))
                .andExpect(jsonPath("$.name").value(system.getName()))
                .andExpect(jsonPath("$.description").value(system.getDescription()))
                .andExpect(jsonPath("$.status").value(system.getStatus().toString()))
                .andExpect(jsonPath("$.version").value(system.getVersion()))
                .andExpect(jsonPath("$.dateOfCheckVersion").value(system.getDateOfCheckVersion()))
                .andExpect(jsonPath("$.externalId").value(system.getExternalId().toString()))
                .andExpect(jsonPath("$.connections[0]").value(connection.getId().toString()))
                .andExpect(jsonPath("$.environmentIds[0]").value(environment.getId().toString()));
    }

    @Test
    public void copy_ThrowException_SystemNameNull() throws Exception {
        when(systemService.copy(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any())).thenReturn(system);
        CreateSystemDto systemDto = new CreateSystemDto();
        systemDto.setEnvironmentId(UUID.randomUUID());
        String requestJson = objectMapper.writer().writeValueAsString(systemDto);
        Exception exception =
                mockMvc.perform(MockMvcRequestBuilders.post("/api/systems/{systemId}/copy", system.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                        .andExpect(status().is(500))
                        .andReturn()
                        .getResolvedException();
        Assertions.assertTrue(exception.getMessage().contains("System name can't be null"));
    }

    @Test
    public void copy_ThrowException_SystemNameEmpty() throws Exception {
        when(systemService.copy(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any())).thenReturn(system);
        CreateSystemDto systemDto = new CreateSystemDto();
        systemDto.setName("");
        systemDto.setEnvironmentId(UUID.randomUUID());
        String requestJson = objectMapper.writer().writeValueAsString(systemDto);
        Exception exception =
                mockMvc.perform(MockMvcRequestBuilders.post("/api/systems/{systemId}/copy", system.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                        .andExpect(status().is(500))
                        .andReturn()
                        .getResolvedException();
        Assertions.assertTrue(exception.getMessage().contains("System name can't be empty"));
    }

    @Test
    public void copy_ThrowException_EnvironmentIdEmpty() throws Exception {
        when(systemService.copy(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any())).thenReturn(system);
        CreateSystemDto systemDto = new CreateSystemDto();
        systemDto.setName("Test Name");
        String requestJson = objectMapper.writer().writeValueAsString(systemDto);
        Exception exception =
                mockMvc.perform(MockMvcRequestBuilders.post("/api/systems/{systemId}/copy", system.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                        .andExpect(status().is(500))
                        .andReturn()
                        .getResolvedException();
        Assertions.assertTrue(exception.getMessage().contains("Environment ID can't be empty"));
    }

    @Test
    public void share_RequestPassed_ModifiedParameterEmpty() throws Exception {
        ConcurrentModificationService concurrentModificationService = Mockito.mock(ConcurrentModificationService.class);
        ContextRepository contextRepository = Mockito.mock(ContextRepository.class);
        Context context = Mockito.mock(Context.class);
        when(systemService.share(any(), any())).thenReturn(system);
        when(concurrentModificationService.getConcurrentModificationHttpStatus(any(), any(), any())).thenReturn(
                HttpStatus.OK);
        when(contextRepository.getContext()).thenReturn(context);
        doNothing().when(context).setFieldsToUnfold(any(), any());
        String requestJson = om.writeValueAsString(
                SharingRequestDto.builder()
                        .shareList(Collections.singletonList(UUID.randomUUID()))
                        .unShareList(Collections.emptyList())
                        .build());
        mockMvc.perform(MockMvcRequestBuilders.put("/api/systems/{systemId}/share", system.getId())
                        .param("environmentId", UUID.randomUUID().toString())
                        .content(requestJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @Disabled
    public void share_ThrowException_EnvironmentIdEmpty() throws Exception {
        ConcurrentModificationService concurrentModificationService = Mockito.mock(ConcurrentModificationService.class);
        ContextRepository contextRepository = Mockito.mock(ContextRepository.class);
        Context context = Mockito.mock(Context.class);
        when(systemService.share(any(), any())).thenReturn(system);
        when(concurrentModificationService.getConcurrentModificationHttpStatus(any(), any(), any())).thenReturn(
                HttpStatus.OK);
        when(contextRepository.getContext()).thenReturn(context);
        doNothing().when(context).setFieldsToUnfold(any(), any());
        String requestJson = om.writeValueAsString(
                SharingRequestDto.builder()
                        .shareList(Collections.singletonList(UUID.randomUUID()))
                        .unShareList(Collections.emptyList())
                        .build());
        Exception exception =
                mockMvc.perform(MockMvcRequestBuilders.put("/api/systems/{systemId}/share", system.getId())
                                .content(requestJson)
                                .param("environmentId", "")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().is(500))
                        .andReturn()
                        .getResolvedException();
        Assertions.assertTrue(exception.getMessage().contains("Environment ID can't be empty"));
    }

    @Test
    public void openshiftUpdateRoutes_RequestPassed_IdPopulated() throws Exception {
        when(systemService.updateOpenshiftRoute(any(), any())).thenReturn(Collections.singletonList(connection));
        CreateSystemDto systemDto = new CreateSystemDto();
        systemDto.setId(UUID.randomUUID());
        systemDto.setName("Test System");
        systemDto.setEnvironmentId(UUID.randomUUID());
        String requestJson = objectMapper.writer().writeValueAsString(systemDto);
        mockMvc.perform(MockMvcRequestBuilders.put("/api/systems/openshift")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[0].id").value(connection.getId().toString()))
                .andExpect(jsonPath("$[0].name").value(connection.getName()))
                .andExpect(jsonPath("$[0].description").value(connection.getDescription()))
                .andExpect(jsonPath("$[0].sourceTemplateId").value(connection.getSourceTemplateId().toString()))
                .andExpect(jsonPath("$[0].connectionType").value(connection.getConnectionType()))
                .andExpect(jsonPath("$[0].services[0]").value(connection.getServices().get(0)))
                .andExpect(jsonPath("$[0].systemId").value(connection.getSystemId().toString()));
    }

    @Test
    public void openshiftUpdateRoutes_RequestPassed_IdEmpty() throws Exception {
        when(systemService.updateOpenshiftRoute(any())).thenReturn(Collections.singletonList(connection));
        CreateSystemDto systemDto = new CreateSystemDto();
        systemDto.setName("Test System");
        systemDto.setEnvironmentId(UUID.randomUUID());
        String requestJson = objectMapper.writer().writeValueAsString(systemDto);
        mockMvc.perform(MockMvcRequestBuilders.put("/api/systems/openshift")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[0].id").value(connection.getId().toString()))
                .andExpect(jsonPath("$[0].name").value(connection.getName()))
                .andExpect(jsonPath("$[0].description").value(connection.getDescription()))
                .andExpect(jsonPath("$[0].sourceTemplateId").value(connection.getSourceTemplateId().toString()))
                .andExpect(jsonPath("$[0].connectionType").value(connection.getConnectionType()))
                .andExpect(jsonPath("$[0].services[0]").value(connection.getServices().get(0)))
                .andExpect(jsonPath("$[0].systemId").value(connection.getSystemId().toString()));
    }

    @Test
    public void openshiftUpdateRoutes_ThrowException_EnvironmentIdEmpty() throws Exception {
        when(systemService.updateOpenshiftRoute(any(), any())).thenReturn(Collections.singletonList(connection));
        CreateSystemDto systemDto = new CreateSystemDto();
        systemDto.setId(UUID.randomUUID());
        systemDto.setName("Test System");
        String requestJson = objectMapper.writer().writeValueAsString(systemDto);
        Exception exception = mockMvc.perform(MockMvcRequestBuilders.put("/api/systems/openshift")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().is(500))
                .andReturn()
                .getResolvedException();
        Assertions.assertTrue(exception.getMessage().contains("Environment ID can't be empty"));
    }

    @Test
    public void updateSystem_PassedRequest_GoodRequest() throws Exception {
        SystemDto systemDto = new SystemDto();
        systemDto.setId(UUID.randomUUID());
        systemDto.setName("Test System");
        String requestJson = objectMapper.writer().writeValueAsString(systemDto);
        ConcurrentModificationService concurrentModificationService = Mockito.mock(ConcurrentModificationService.class);
        ContextRepository contextRepository = Mockito.mock(ContextRepository.class);
        Context context = Mockito.mock(Context.class);
        when(concurrentModificationService.getConcurrentModificationHttpStatus(any(), any(), any())).thenReturn(
                HttpStatus.OK);
        when(contextRepository.getContext()).thenReturn(context);
        when(systemService.update(any(SystemDto.class))).thenReturn(system);
        doNothing().when(context).setFieldsToUnfold(anyString());
        mockMvc.perform(MockMvcRequestBuilders.put("/api/systems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.id").value(system.getId().toString()))
                .andExpect(jsonPath("$.name").value(system.getName()))
                .andExpect(jsonPath("$.description").value(system.getDescription()))
                .andExpect(jsonPath("$.status").value(system.getStatus().toString()))
                .andExpect(jsonPath("$.version").value(system.getVersion()))
                .andExpect(jsonPath("$.dateOfCheckVersion").value(system.getDateOfCheckVersion()))
                .andExpect(jsonPath("$.externalId").value(system.getExternalId().toString()))
                .andExpect(jsonPath("$.connections[0]").value(connection.getId().toString()))
                .andExpect(jsonPath("$.environmentIds[0]").value(environment.getId().toString()));
    }

    @Test
    public void updateSystem_ThrowException_SystemIdEmpty() throws Exception {
        SystemDto systemDto = new SystemDto();
        systemDto.setName("Test System");
        String requestJson = objectMapper.writer().writeValueAsString(systemDto);
        ConcurrentModificationService concurrentModificationService = Mockito.mock(ConcurrentModificationService.class);
        ContextRepository contextRepository = Mockito.mock(ContextRepository.class);
        Context context = Mockito.mock(Context.class);
        when(concurrentModificationService.getConcurrentModificationHttpStatus(any(), any(), any())).thenReturn(
                HttpStatus.OK);
        when(contextRepository.getContext()).thenReturn(context);
        when(systemService.update(any(SystemDto.class))).thenReturn(system);
        doNothing().when(context).setFieldsToUnfold(anyString());
        Exception exception = mockMvc.perform(MockMvcRequestBuilders.put("/api/systems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().is(500))
                .andReturn()
                .getResolvedException();
        Assertions.assertTrue(exception.getMessage().contains("System id can't be empty"));
    }

    @Test
    public void updateSystem_ThrowException_SystemNameNull() throws Exception {
        SystemDto systemDto = new SystemDto();
        systemDto.setId(UUID.randomUUID());
        String requestJson = objectMapper.writer().writeValueAsString(systemDto);
        ConcurrentModificationService concurrentModificationService = Mockito.mock(ConcurrentModificationService.class);
        ContextRepository contextRepository = Mockito.mock(ContextRepository.class);
        Context context = Mockito.mock(Context.class);
        when(concurrentModificationService.getConcurrentModificationHttpStatus(any(), any(), any())).thenReturn(
                HttpStatus.OK);
        when(contextRepository.getContext()).thenReturn(context);
        when(systemService.update(any(SystemDto.class))).thenReturn(system);
        doNothing().when(context).setFieldsToUnfold(anyString());
        Exception exception = mockMvc.perform(MockMvcRequestBuilders.put("/api/systems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().is(500))
                .andReturn()
                .getResolvedException();
        Assertions.assertTrue(exception.getMessage().contains("System name can't be null"));
    }

    @Test
    public void updateSystem_ThrowException_SystemNameEmpty() throws Exception {
        SystemDto systemDto = new SystemDto();
        systemDto.setId(UUID.randomUUID());
        systemDto.setName("");
        String requestJson = objectMapper.writer().writeValueAsString(systemDto);
        ConcurrentModificationService concurrentModificationService = Mockito.mock(ConcurrentModificationService.class);
        ContextRepository contextRepository = Mockito.mock(ContextRepository.class);
        Context context = Mockito.mock(Context.class);
        when(concurrentModificationService.getConcurrentModificationHttpStatus(any(), any(), any())).thenReturn(
                HttpStatus.OK);
        when(contextRepository.getContext()).thenReturn(context);
        when(systemService.update(any(SystemDto.class))).thenReturn(system);
        doNothing().when(context).setFieldsToUnfold(anyString());
        Exception exception = mockMvc.perform(MockMvcRequestBuilders.put("/api/systems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().is(500))
                .andReturn()
                .getResolvedException();
        Assertions.assertTrue(exception.getMessage().contains("System name can't be empty"));
    }

    @Test
    public void deleteSystem_PassedRequest_GoodRequest() throws Exception {
        doNothing().when(systemService).delete(any(), any());
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/systems/{systemId}", system.getId())
                        .param("environmentId", String.valueOf(UUID.randomUUID())))
                .andExpect(status().isNoContent());
    }

    @Test
    public void updateVersion_PassedRequest_GoodRequest() throws Exception {
        when(systemService.updateVersionBySystemId(any(), anyBoolean())).thenReturn(system);
        mockMvc.perform(MockMvcRequestBuilders.
                        get("/api/systems/{systemId}/version", system.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.id").value(system.getId().toString()))
                .andExpect(jsonPath("$.name").value(system.getName()))
                .andExpect(jsonPath("$.description").value(system.getDescription()))
                .andExpect(jsonPath("$.status").value(system.getStatus().toString()))
                .andExpect(jsonPath("$.version").value(system.getVersion()))
                .andExpect(jsonPath("$.dateOfCheckVersion").value(system.getDateOfCheckVersion()))
                .andExpect(jsonPath("$.externalId").value(system.getExternalId().toString()))
                .andExpect(jsonPath("$.connections[0]").value(connection.getId().toString()))
                .andExpect(jsonPath("$.environmentIds[0]").value(environment.getId().toString()));
    }

    @Test
    public void updateVersion_ThrowException_ResponseNull() throws Exception {
        when(systemService.updateVersionBySystemId(any(), anyBoolean())).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders.
                        get("/api/systems/{systemId}/version", system.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getHtmlVersion_PassedRequest_GoodRequest() throws Exception {
        when(systemService.updateVersionBySystemId(any(), anyBoolean())).thenReturn(system);
        when(systemService.transformSystemVersionToHtml(any())).thenReturn(system);
        mockMvc.perform(MockMvcRequestBuilders.
                        get("/api/systems/{systemId}/htmlVersion", system.getId())
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$").value(system.getVersion()));
    }

    @Test
    public void getPublicHtmlVersion_PassedRequest_GoodRequest() throws Exception {
        when(systemService.getCachedVersionById(any())).thenReturn(system);
        when(systemService.transformSystemVersionToHtml(any())).thenReturn(system);
        mockMvc.perform(MockMvcRequestBuilders.
                        get("/api/public/v1/systems/{systemId}/htmlVersion", system.getId())
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$").value(system.getVersion()));
    }

    @Test
    public void updateParametersGettingVersion_PassedRequest_GoodRequest() throws Exception {
        when(systemService.updateParametersGettingVersion(any(), any())).thenReturn(system);
        ParametersGettingVersion parametersGettingVersion = new ParametersGettingVersion();
        parametersGettingVersion.setParameters("Name");
        parametersGettingVersion.setParsingValue("Test");
        String requestJson = objectMapper.writer().writeValueAsString(parametersGettingVersion);
        mockMvc.perform(MockMvcRequestBuilders.put("/api/systems/{systemId}/parametersGettingVersion", system.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.id").value(system.getId().toString()))
                .andExpect(jsonPath("$.name").value(system.getName()))
                .andExpect(jsonPath("$.description").value(system.getDescription()))
                .andExpect(jsonPath("$.status").value(system.getStatus().toString()))
                .andExpect(jsonPath("$.version").value(system.getVersion()))
                .andExpect(jsonPath("$.dateOfCheckVersion").value(system.getDateOfCheckVersion()))
                .andExpect(jsonPath("$.externalId").value(system.getExternalId().toString()))
                .andExpect(jsonPath("$.connections[0]").value(connection.getId().toString()))
                .andExpect(jsonPath("$.environmentIds[0]").value(environment.getId().toString()));
    }

    @Test
    public void createServicesFromKubernetes_PassedRequest_GoodRequest() throws Exception {
        when(systemService.createListFromCloudServer(any(), any(), any(), any())).thenReturn(
                Collections.singletonList(system));
        List<UUID> serviceIds =
                Arrays.asList(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        List<UUID> removedServiceIds =
                Arrays.asList(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        SynchronizeCloudServicesRequest request = new SynchronizeCloudServicesRequest(serviceIds, removedServiceIds);
        String requestJson = objectMapper.writer().writeValueAsString(request);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/systems/kubeServices/{environmentId}/{systemId}",
                                environment.getId(),
                                system.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[0].id").value(system.getId().toString()))
                .andExpect(jsonPath("$[0].name").value(system.getName()))
                .andExpect(jsonPath("$[0].description").value(system.getDescription()))
                .andExpect(jsonPath("$[0].status").value(system.getStatus().toString()))
                .andExpect(jsonPath("$[0].version").value(system.getVersion()))
                .andExpect(jsonPath("$[0].dateOfCheckVersion").value(system.getDateOfCheckVersion()))
                .andExpect(jsonPath("$[0].externalId").value(system.getExternalId().toString()))
                .andExpect(jsonPath("$[0].connections[0]").value(connection.getId().toString()))
                .andExpect(jsonPath("$[0].environmentIds[0]").value(environment.getId().toString()));
    }

    @Test
    public void updateServicesFromKubernetes_PassedRequest_GoodRequest() throws Exception {
        when(systemService.updateServicesFromCloudServer(any(), any())).thenReturn(Collections.singletonList(system));
        mockMvc.perform(MockMvcRequestBuilders.
                        get("/api/systems/kubeServices/{systemId}", system.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[0].id").value(system.getId().toString()))
                .andExpect(jsonPath("$[0].name").value(system.getName()))
                .andExpect(jsonPath("$[0].description").value(system.getDescription()))
                .andExpect(jsonPath("$[0].status").value(system.getStatus().toString()))
                .andExpect(jsonPath("$[0].version").value(system.getVersion()))
                .andExpect(jsonPath("$[0].dateOfCheckVersion").value(system.getDateOfCheckVersion()))
                .andExpect(jsonPath("$[0].externalId").value(system.getExternalId().toString()))
                .andExpect(jsonPath("$[0].connections[0]").value(connection.getId().toString()))
                .andExpect(jsonPath("$[0].environments[0]").value(environment.getId().toString()));
    }

    @Test
    public void getKubernetesServiceNames_PassedRequest_GoodRequest() throws Exception {
        List<ShortExternalService> testKubernetesServices = Collections.singletonList(new ShortExternalService("left", "right"));
        when(systemService.getShortExternalServices(any(), any())).thenReturn(testKubernetesServices);
        mockMvc.perform(MockMvcRequestBuilders.
                        get("/api/systems/shortKubeServices/{systemId}", system.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[0].name").value("right"));
    }
}
