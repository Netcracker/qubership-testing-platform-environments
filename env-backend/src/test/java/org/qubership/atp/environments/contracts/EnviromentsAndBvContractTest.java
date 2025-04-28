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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.qubership.atp.environments.utils.PactTestUtils.formEnvironment;
import static org.qubership.atp.environments.utils.PactTestUtils.formProject;
import static org.qubership.atp.environments.utils.PactTestUtils.getConnection;

import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.qubership.atp.environments.config.ModelMapperConfig;
import org.qubership.atp.environments.config.PactConfiguration;
import org.qubership.atp.environments.model.impl.Context;
import org.qubership.atp.environments.repo.impl.ContextRepository;
import org.qubership.atp.environments.service.direct.ConcurrentModificationService;
import org.qubership.atp.environments.service.direct.ConnectionService;
import org.qubership.atp.environments.service.direct.EnvironmentService;
import org.qubership.atp.environments.service.direct.ProjectService;
import org.qubership.atp.environments.service.direct.SystemCategoriesService;
import org.qubership.atp.environments.service.direct.SystemService;
import org.qubership.atp.environments.service.direct.TimestampService;
import org.qubership.atp.environments.service.rest.server.ConnectionController;
import org.qubership.atp.environments.service.rest.server.EnvironmentController;
import org.qubership.atp.environments.service.rest.server.PingController;
import org.qubership.atp.environments.service.rest.server.ProjectController;
import org.qubership.atp.environments.service.rest.server.SystemController;
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
@PactUrl(urls = {"src/test/resources/pacts/atp-bv-atp-environments.json"})
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = {
        ProjectController.class,
        EnvironmentController.class,
        SystemController.class,
        PingController.class,
        ConnectionController.class})
@ContextConfiguration(classes = {PactConfiguration.TestApp.class})
@EnableAutoConfiguration
@Import({JacksonAutoConfiguration.class,
        HttpMessageConvertersAutoConfiguration.class,
        ModelMapperConfig.class,
        ProjectController.class,
        EnvironmentController.class,
        SystemController.class,
        PingController.class,
        ConnectionController.class})
@Slf4j
@Isolated
public class EnviromentsAndBvContractTest {

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
    private TimestampService timestampService;

    @MockBean
    Context context;

    public void beforeAll() throws Exception {

        when(timestampService.isAlive()).thenReturn(true);
        when(connectionService.get(any(UUID.class))).thenReturn(getConnection());
        when(environmentService.get(any(UUID.class))).thenReturn(formEnvironment());
        when(systemService.getConnections(any(UUID.class)))
                .thenReturn(Collections.singletonList(getConnection()));

        when(projectService.getAll()).thenReturn(Collections.singletonList(formProject()));
        when(projectService.get(any(UUID.class))).thenReturn(formProject());
        when(projectService.getEnvironments(any(UUID.class)))
                .thenReturn(Collections.singletonList(formEnvironment()));
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
