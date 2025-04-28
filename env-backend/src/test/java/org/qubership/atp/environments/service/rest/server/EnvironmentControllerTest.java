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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.qubership.atp.common.logging.interceptor.RestTemplateLogInterceptor;
import org.qubership.atp.ei.node.clients.ExportImportFeignClient;
import org.qubership.atp.environments.Main;
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
import org.qubership.atp.environments.service.direct.impl.MetricService;
import org.qubership.atp.environments.service.rest.client.CatalogFeignClient;
import org.qubership.atp.environments.service.rest.client.HealthcheckFeignClient;
import org.qubership.atp.environments.service.rest.server.dto.SystemTemporaryDto;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(SpringExtension.class)
@SpringBootTest()
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = Main.class)
@TestPropertySource("classpath:application-test-rest-api.properties")
@Isolated
public class EnvironmentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private EnvironmentController environmentController;
    @MockBean
    private CatalogFeignClient catalogFeignClient;
    @MockBean
    private HealthcheckFeignClient healthcheckFeignClient;
    @MockBean
    private ExportImportFeignClient exportImportFeignClient;
    @MockBean
    private RestTemplateLogInterceptor restTemplateLogInterceptor;
    @MockBean
    private MetricService metricService;

    private Environment envTest;
    private System systemTest;
    private Connection connectionTest;
    private List<Environment> listEnv = new ArrayList<>();
    private List<System> listSys = new ArrayList<>();
    private List<Connection> listConn = new ArrayList<>();

    @BeforeEach
    public void setUp() throws Exception {
        Project project = new ProjectImpl(UUID.randomUUID(), "Test project", "Test project", "Test project "
                + "description", listEnv, null, null);
        envTest = new EnvironmentImpl(UUID.randomUUID(), "Test environment 1", "", null, "", "","", null, null, null, null,
                project.getId(),
                listSys, Constants.Environment.Category.ENVIRONMENT, null, Collections.emptyList());
        listEnv.add(envTest);
        systemTest = SystemImpl.builder()
                .uuid(UUID.randomUUID())
                .name("Some name")
                .description("Some description")
                .environments(listEnv)
                .connectionsList(listConn)
                .version("Unknown")
                .build();
        listSys.add(systemTest);
        ConnectionParameters paramsHttp = new ConnectionParameters();
        paramsHttp.put("http", "");
        paramsHttp.put("login", "");
        paramsHttp.put("password", "");
        connectionTest = new ConnectionImpl(UUID.randomUUID(), "HTTP", null, paramsHttp, null, null, null, null,
                systemTest.getId(), null, Constants.Environment.System.Connection.HTTP, null, null );
        listConn.add(connectionTest);
    }

    @Test
    public void onEnvironmentController_GetEnvironments_ArrayEnvironments() throws Exception {
        when(environmentController.getAll()).thenReturn(listEnv);
        this.mockMvc.perform(MockMvcRequestBuilders.
                        get("/api/environments")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0]").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(envTest.getName()));
    }

    @Test
    public void onEnvironmentController_GetEnvironmentsByFullParam_EnvironmentsNotUnfolded() throws Exception {
        when(environmentController.getAll()).thenReturn(listEnv);
        this.mockMvc.perform(MockMvcRequestBuilders.
                        get("/api/environments").param("full", "")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].systems").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].systems[0]").value(systemTest.getId().toString()));
    }

    @Test
    public void onEnvironmentController_GetSystemsInShortView_ShortViewArraySystems() throws Exception {
        when(environmentController.getSystemsShort(envTest.getId())).thenReturn(listSys);
        this.mockMvc.perform(MockMvcRequestBuilders.
                        get("/api/environments/" + envTest.getId() + "/systems/short")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0]").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(systemTest.getId().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(systemTest.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").doesNotExist());
    }

    @Test
    public void onEnvironmentController_PostTemporaryEnvironments_CreatedTemporaryEnvironments() throws Exception {
        File file = new File("src/test/resources/updateSystems.json");
        List<SystemTemporaryDto> listSystem = objectMapper.readValue(file,
                new TypeReference<List<SystemTemporaryDto>>() {
                });
        when(environmentController.temporary(envTest.getId(), listSystem)).thenReturn(envTest);
        this.mockMvc.perform(MockMvcRequestBuilders.
                        post("/api/environments/" + envTest.getId() + "/temporary")
                        .content(objectMapper.writeValueAsString(listSystem))
                        .characterEncoding("utf-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(envTest.getId().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(envTest.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").doesNotExist());
    }
}

