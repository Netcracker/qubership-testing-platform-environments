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

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.qubership.atp.environments.config.PactConfiguration;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.Project;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.service.rest.server.ConnectionController;
import org.qubership.atp.environments.service.rest.server.EnvironmentController;
import org.qubership.atp.environments.service.rest.server.ProjectController;
import org.qubership.atp.environments.service.rest.server.SystemController;
import org.qubership.atp.environments.utils.PactTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@PactUrl(urls = {"src/test/resources/pacts/atp-itf-executor-atp-environments.json"})
@AutoConfigureMockMvc(addFilters = false, webDriverEnabled = false)
@WebMvcTest(controllers = {SystemController.class, ProjectController.class,
        EnvironmentController.class, ConnectionController.class})
@ContextConfiguration(classes = {PactConfiguration.TestApp.class})
@EnableAutoConfiguration
@Import({JacksonAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class,
        SystemController.class, ProjectController.class, EnvironmentController.class, ConnectionController.class})
@Slf4j
@Isolated
public class EnvironmentsAndItfExecutorContractTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private SystemController systemController;
    @MockBean
    private ProjectController projectController;
    @MockBean
    private EnvironmentController environmentController;
    @MockBean
    private ConnectionController connectionController;

    public void beforeAll() throws Exception {
        log.info("EnvironmentsAndItfExecutorContractTest tests started");

        Project project = PactTestUtils.formProject();
        Environment environment = PactTestUtils.formEnvironment();
        Connection connection = PactTestUtils.formConnection();
        System system = PactTestUtils.formSystem();

        when(projectController.getAllShort()).thenReturn(asList(project));

        when(projectController.getProject(any(UUID.class))).thenReturn(project);

        when(projectController.getEnvironmentsShort(any(UUID.class))).thenReturn(asList(environment));

        when(projectController.getEnvironments(any(UUID.class))).thenReturn(asList(environment));

        when(environmentController.getEnvironment(any(UUID.class))).thenReturn(environment);

        when(environmentController.getSystemsShort(any(UUID.class))).thenReturn(asList(system));

        when(connectionController.getConnection(any(UUID.class))).thenReturn(connection);

        when(systemController.getSystem(any(UUID.class))).thenReturn(new ResponseEntity<>(system, HttpStatus.OK));

        when(systemController.getSystemConnections(any(UUID.class))).thenReturn(asList(connection));
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
