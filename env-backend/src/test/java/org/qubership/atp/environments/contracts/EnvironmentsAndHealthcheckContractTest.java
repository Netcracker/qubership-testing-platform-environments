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

package org.qubership.atp.environments.contracts;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.qubership.atp.environments.utils.PactTestUtils.formConnection;
import static org.qubership.atp.environments.utils.PactTestUtils.formEnvironment;
import static org.qubership.atp.environments.utils.PactTestUtils.formProject;
import static org.qubership.atp.environments.utils.PactTestUtils.formSystemCategory;
import static org.qubership.atp.environments.utils.PactTestUtils.formSystems;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.qubership.atp.environments.config.ModelMapperConfig;
import org.qubership.atp.environments.config.PactConfiguration;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.Project;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.SystemCategory;
import org.qubership.atp.environments.model.impl.Context;
import org.qubership.atp.environments.repo.impl.ContextRepository;
import org.qubership.atp.environments.service.direct.ConcurrentModificationService;
import org.qubership.atp.environments.service.direct.ConnectionService;
import org.qubership.atp.environments.service.direct.EnvironmentService;
import org.qubership.atp.environments.service.direct.ProjectService;
import org.qubership.atp.environments.service.direct.SystemCategoriesService;
import org.qubership.atp.environments.service.direct.SystemService;
import org.qubership.atp.environments.service.rest.server.ConnectionController;
import org.qubership.atp.environments.service.rest.server.EnvironmentController;
import org.qubership.atp.environments.service.rest.server.ProjectController;
import org.qubership.atp.environments.service.rest.server.SystemCategoryController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactUrl;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import lombok.extern.slf4j.Slf4j;

@Provider("atp-environments")
@PactUrl(urls = {"src/test/resources/pacts/atp-healthcheck-atp-environments.json"})
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = {ProjectController.class, EnvironmentController.class,
        SystemCategoryController.class, ConnectionController.class})
@ContextConfiguration(classes = {PactConfiguration.TestApp.class})
@EnableAutoConfiguration
@Import({JacksonAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class,
        ModelMapperConfig.class, ProjectController.class, EnvironmentController.class,
        SystemCategoryController.class, ConnectionController.class})
@Slf4j
@Isolated
public class EnvironmentsAndHealthcheckContractTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ProjectService projectService;
    @MockBean
    private EnvironmentService environmentService;
    @MockBean
    private SystemCategoriesService systemCategoriesService;
    @MockBean
    private SystemService systemService;
    @MockBean
    private ConnectionService connectionService;
    @MockBean
    private ConcurrentModificationService concurrentModificationService;
    @MockBean
    private ContextRepository contextRepository;
    @MockBean
    Context context;

    public void beforeAll() throws Exception {
        log.info("EnvironmentsAndHealthcheckContractTest tests started");

        List<Connection> connections = asList(formConnection());
        when(connectionService.getConnectionTemplates()).thenReturn(connections);

        Connection connection = formConnection();
        when(connectionService.get(any())).thenReturn(connection);

        List<Environment> environments = asList(formEnvironment());
        when(environmentService.getAll(any(UUID.class))).thenReturn(environments);

        Environment environment = formEnvironment();
        when(environmentService.get(any(UUID.class))).thenReturn(environment);

        List<System> system = formSystems();
        when(environmentService.getSystems(any(UUID.class))).thenReturn(system);
        when(environmentService.getShortSystems(any(UUID.class))).thenReturn(system);


        List<Project> projects = asList(formProject());
        when(projectService.getAll()).thenReturn(projects);
        when(projectService.getAllShort()).thenReturn(projects);

        Project project = formProject();
        when(projectService.get(any(UUID.class))).thenReturn(project);

        when(projectService.getEnvironments(any(UUID.class))).thenReturn(environments);
        when(projectService.getShortEnvironments(any(UUID.class))).thenReturn(environments);


        when(projectService.getSystemsByProjectId(any(UUID.class))).thenReturn(system);

        List<SystemCategory> systemCategories = asList(formSystemCategory());
        when(systemCategoriesService.getAll()).thenReturn(systemCategories);

        Project project2 = formProject();
        when(projectService.getProjectWithSpecifiedEnvironments(any(UUID.class), any())).thenReturn(project2);
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @BeforeEach
    void before(PactVerificationContext context) throws Exception {
        beforeAll();
        context.setTarget(new MockMvcTestTarget(mockMvc));
    }

    @State("all ok")
    public void allPass() {
    }

}
