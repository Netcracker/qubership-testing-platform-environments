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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.qubership.atp.environments.Main;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.Project;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.impl.EnvironmentImpl;
import org.qubership.atp.environments.model.impl.ProjectImpl;
import org.qubership.atp.environments.model.impl.SystemImpl;
import org.qubership.atp.environments.model.utils.Constants;
import org.qubership.atp.environments.service.direct.EnvironmentService;
import org.qubership.atp.environments.service.direct.SystemService;
import org.qubership.atp.environments.service.rest.server.dto.EnvironmentDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * ToolControllerTest - test for {@link ToolController}
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = Main.class)
@TestPropertySource("classpath:application-test-rest-api.properties")
@Isolated
public class ToolControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EnvironmentService environmentService;
    @MockBean
    private SystemService systemService;

    private Environment environmentTool;
    private System system;
    private List<Environment> toolsList = new ArrayList<>();
    private List<System> systemList = new ArrayList<>();

    @BeforeEach
    public void setUp() throws Exception {
        Project project = new ProjectImpl(UUID.randomUUID(), "Test project", "Test project", "Test project "
                + "description", toolsList, null, null);
        environmentTool = new EnvironmentImpl(UUID.randomUUID(), "Test tool group", "", null, "", "", "",  null, null, null, null,
                project.getId(), systemList, Constants.Environment.Category.TOOL, null, Collections.emptyList());
        toolsList.add(environmentTool);
        system = new SystemImpl(UUID.randomUUID(), "System name", "System description", null, null, null, null,
                toolsList, null, null, null, null, "Unknown", null, null, null, null, null, null, null, null, null);
        systemList.add(system);
    }

    @Test
    public void getAll_PassedRequest_GoodRequest() throws Exception {
        when(environmentService.getAll(any())).thenReturn(toolsList);

        mockMvc.perform(MockMvcRequestBuilders.
                        get("/api/tools")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[0].id").value(environmentTool.getId().toString()))
                .andExpect(jsonPath("$[0].name").value(environmentTool.getName()))
                .andExpect(jsonPath("$[0].projectId").value(environmentTool.getProjectId().toString()))
                .andExpect(jsonPath("$[0].systems[0]").value(system.getId().toString()));

    }

    @Test
    public void getEnvironment_PassedRequest_GoodRequest() throws Exception {
        when(environmentService.get(any())).thenReturn(environmentTool);

        mockMvc.perform(MockMvcRequestBuilders.
                        get("/api/tools/{toolGroupId}", UUID.randomUUID())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.id").value(environmentTool.getId().toString()))
                .andExpect(jsonPath("$.name").value(environmentTool.getName()))
                .andExpect(jsonPath("$.projectId").value(environmentTool.getProjectId().toString()))
                .andExpect(jsonPath("$.systems[0]").value(system.getId().toString()));
    }

    @Test
    public void getToolSystems_PassedRequest_SystemTypePopulated() throws Exception {
        when(environmentService.getSystems(any(), any())).thenReturn(systemList);

        mockMvc.perform(MockMvcRequestBuilders.
                        get("/api/tools/{toolGroupId}/systems", UUID.randomUUID())
                        .param("system_type", "TestType")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[0].id").value(system.getId().toString()))
                .andExpect(jsonPath("$[0].name").value(system.getName()))
                .andExpect(jsonPath("$[0].description").value(system.getDescription()))
                .andExpect(jsonPath("$[0].version").value(system.getVersion()))
                .andExpect(jsonPath("$[0].environmentIds[0]").value(environmentTool.getId().toString()));
    }

    @Test
    public void getToolSystems_PassedRequest_SystemTypeEmpty() throws Exception {
        when(environmentService.getSystems(any())).thenReturn(systemList);

        mockMvc.perform(MockMvcRequestBuilders.
                        get("/api/tools/{toolGroupId}/systems", UUID.randomUUID())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[0].id").value(system.getId().toString()))
                .andExpect(jsonPath("$[0].name").value(system.getName()))
                .andExpect(jsonPath("$[0].description").value(system.getDescription()))
                .andExpect(jsonPath("$[0].version").value(system.getVersion()))
                .andExpect(jsonPath("$[0].environmentIds[0]").value(environmentTool.getId().toString()));
    }

    @Test
    public void getSystemV2_PassedRequest_SystemTypePopulated() throws Exception {
        when(environmentService.getSystemsV2(any(), any())).thenReturn(systemList);

        mockMvc.perform(MockMvcRequestBuilders.
                        get("/api/v2/tools/{toolGroupId}/systems", UUID.randomUUID())
                        .param("system_type", "TestType")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[0].id").value(system.getId().toString()))
                .andExpect(jsonPath("$[0].name").value(system.getName()))
                .andExpect(jsonPath("$[0].description").value(system.getDescription()))
                .andExpect(jsonPath("$[0].version").value(system.getVersion()))
                .andExpect(jsonPath("$[0].environments[0]").value(environmentTool.getId().toString()));
    }

    @Test
    public void getSystemV2_PassedRequest_SystemTypeEmpty() throws Exception {
        when(environmentService.getSystemsV2(any())).thenReturn(systemList);

        mockMvc.perform(MockMvcRequestBuilders.
                        get("/api/v2/tools/{toolGroupId}/systems", UUID.randomUUID())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[0].id").value(system.getId().toString()))
                .andExpect(jsonPath("$[0].name").value(system.getName()))
                .andExpect(jsonPath("$[0].description").value(system.getDescription()))
                .andExpect(jsonPath("$[0].version").value(system.getVersion()))
                .andExpect(jsonPath("$[0].environments[0]").value(environmentTool.getId().toString()));
    }

    @Test
    public void createTool_PassedRequest_GoodRequest() throws Exception {
        when(environmentService.create(any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(environmentTool);
        EnvironmentDto environmentDto = new EnvironmentDto();
        environmentDto.setName("Test Environment");
        environmentDto.setDescription("Test Description");
        String requestJson = objectMapper.writer().writeValueAsString(environmentDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/tools")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.id").value(environmentTool.getId().toString()))
                .andExpect(jsonPath("$.name").value(environmentTool.getName()))
                .andExpect(jsonPath("$.description").value(environmentTool.getDescription()))
                .andExpect(jsonPath("$.projectId").value(environmentTool.getProjectId().toString()))
                .andExpect(jsonPath("$.systems[0]").value(system.getId().toString()));
    }

    @Test
    public void copyTool_PassedRequest_GoodRequest() throws Exception {
        when(environmentService.copy(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(environmentTool);
        EnvironmentDto environmentDto = new EnvironmentDto();
        environmentDto.setName("Test Environment");
        environmentDto.setDescription("Test Description");
        String requestJson = objectMapper.writer().writeValueAsString(environmentDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/tools/{toolGroupId}/copy", UUID.randomUUID().toString())
                        .param("toolGroupId", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.id").value(environmentTool.getId().toString()))
                .andExpect(jsonPath("$.name").value(environmentTool.getName()))
                .andExpect(jsonPath("$.description").value(environmentTool.getDescription()))
                .andExpect(jsonPath("$.projectId").value(environmentTool.getProjectId().toString()))
                .andExpect(jsonPath("$.systems[0]").value(system.getId().toString()));
    }

    @Test
    public void updateVersion_PassedRequest_GoodRequest() throws Exception {
        when(systemService.updateVersionByEnvironmentId(any())).thenReturn(systemList);

        mockMvc.perform(MockMvcRequestBuilders.
                        get("/api/tools/{toolGroupId}/version", UUID.randomUUID())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[0].id").value(system.getId().toString()))
                .andExpect(jsonPath("$[0].name").value(system.getName()))
                .andExpect(jsonPath("$[0].description").value(system.getDescription()))
                .andExpect(jsonPath("$[0].version").value(system.getVersion()))
                .andExpect(jsonPath("$[0].environments[0]").value(environmentTool.getId().toString()));
    }

    @Test
    public void updateTool_PassedRequest_GoodRequest() throws Exception {
        doNothing().when(environmentService).update(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), anyList());
        EnvironmentDto environmentDto = new EnvironmentDto();
        environmentDto.setId(UUID.randomUUID());
        environmentDto.setName("Test Environment");
        environmentDto.setDescription("Test Description");
        String requestJson = objectMapper.writer().writeValueAsString(environmentDto);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/tools")
                        .param("toolGroupId", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$").value(""));
    }

    @Test
    public void updateTool_ThrowException_IdEmpty() throws Exception {
        doNothing().when(environmentService).update(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), anyList());
        EnvironmentDto environmentDto = new EnvironmentDto();
        environmentDto.setName("Test Environment");
        environmentDto.setDescription("Test Description");
        String requestJson = objectMapper.writer().writeValueAsString(environmentDto);

        Exception exception = mockMvc.perform(MockMvcRequestBuilders.put("/api/tools")
                        .param("toolGroupId", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().is(500))
                .andReturn()
                .getResolvedException();
        Assertions.assertTrue(exception.getMessage().contains("Tool group id can't be empty"));
    }

    @Test
    public void updateTool_ThrowException_NameEmpty() throws Exception {
        doNothing().when(environmentService).update(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), anyList());
        EnvironmentDto environmentDto = new EnvironmentDto();
        environmentDto.setId(UUID.randomUUID());
        environmentDto.setDescription("Test Description");
        String requestJson = objectMapper.writer().writeValueAsString(environmentDto);

        Exception exception = mockMvc.perform(MockMvcRequestBuilders.put("/api/tools")
                        .param("toolGroupId", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().is(500))
                .andReturn()
                .getResolvedException();
        Assertions.assertTrue(exception.getMessage().contains("Tool group name can't be empty"));
    }

    @Test
    public void deleteTool_PassedRequest_GoodRequest() throws Exception {
        doNothing().when(environmentService).delete(any(UUID.class));

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/tools/{toolGroupId}", UUID.randomUUID()))
                .andExpect(status().isNoContent());
    }
}

