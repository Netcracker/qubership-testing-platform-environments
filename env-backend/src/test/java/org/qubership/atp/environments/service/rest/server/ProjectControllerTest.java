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
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.Project;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.impl.EnvironmentImpl;
import org.qubership.atp.environments.model.impl.ProjectImpl;
import org.qubership.atp.environments.model.utils.Constants;
import org.qubership.atp.environments.service.direct.impl.MetricService;
import org.qubership.atp.environments.service.rest.client.CatalogFeignClient;
import org.qubership.atp.environments.service.rest.client.HealthcheckFeignClient;
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

@ExtendWith(SpringExtension.class)
@SpringBootTest()
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = Main.class)
@TestPropertySource("classpath:application-test-rest-api.properties")
@Isolated
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ProjectController projectController;
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

    private Project project;
    private Environment environment;
    private Environment taToolGroup;
    private List<Environment> listEnv = new ArrayList<>();
    private List<System> listSys = new ArrayList<>();

    @BeforeEach
    public void setUp() throws Exception {
        project = new ProjectImpl(UUID.randomUUID(), "Test project", "Test project", "Test project "
                + "description", listEnv, null, null);
        environment = new EnvironmentImpl(UUID.randomUUID(), "Test environment", "", null, "", "", "", null, null, null,
                null, project.getId(),
                listSys, Constants.Environment.Category.ENVIRONMENT, null, Collections.emptyList());
        taToolGroup = new EnvironmentImpl(UUID.randomUUID(), "Test tool group", "", null, "", "", "", null, null, null,
                null, project.getId(),
                listSys, Constants.Environment.Category.TOOL, null, Collections.emptyList());
        listEnv.add(environment);
        listEnv.add(taToolGroup);
    }

    @Test
    public void onProjectController_GetEnvironmentsInShortView_ShortViewArrayEnvironments() throws Exception {
        when(projectController.getEnvironmentsShort(project.getId())).thenReturn(listEnv);
        this.mockMvc.perform(MockMvcRequestBuilders.
                get("/api/projects/" + project.getId() + "/environments/short")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0]").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(environment.getId().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(environment.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").doesNotExist());
    }

    @Test
    public void onProjectController_GetTaToolsGroupInShortView_ShortViewArrayTaToolsGroup() throws Exception {
        when(projectController.getToolsShort(project.getId())).thenReturn(listEnv);
        this.mockMvc.perform(MockMvcRequestBuilders.
                get("/api/projects/" + project.getId() + "/tools/short")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1]").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value(taToolGroup.getId().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value(taToolGroup.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].description").doesNotExist());
    }
}
