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

import java.util.UUID;

import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.Mockito;
import org.qubership.atp.auth.springbootstarter.entities.UserInfo;
import org.qubership.atp.auth.springbootstarter.security.permissions.PolicyEnforcement;
import org.qubership.atp.auth.springbootstarter.ssl.Provider;
import org.qubership.atp.environments.Main;
import org.qubership.atp.environments.model.Project;
import org.qubership.atp.environments.model.impl.ProjectImpl;
import org.qubership.atp.environments.repo.impl.ContextRepository;
import org.qubership.atp.environments.service.direct.ConnectionService;
import org.qubership.atp.environments.service.direct.EnvironmentCategoryService;
import org.qubership.atp.environments.service.direct.EnvironmentService;
import org.qubership.atp.environments.service.direct.ProjectService;
import org.qubership.atp.environments.service.direct.SystemCategoriesService;
import org.qubership.atp.environments.service.direct.SystemService;
import org.qubership.atp.environments.utils.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.querydsl.core.QueryException;

@SpringBootTest(properties = {"spring.cloud.vault.enabled=false", "spring.cloud.consul.config.enabled=false"})
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = Main.class)
@TestPropertySource("classpath:application-test-rest-api.properties")
@Isolated
public abstract class AbstractServiceTest {

    @Autowired
    protected ProjectService projectService;
    @Autowired
    protected EnvironmentService environmentService;
    @Autowired
    protected SystemService systemService;
    @Autowired
    protected SystemCategoriesService systemCategoriesService;
    @Autowired
    protected EnvironmentCategoryService environmentCategoryService;
    @Autowired
    protected ConnectionService connectionService;
    @Autowired
    protected CloseableHttpClient httpClient;
    @Autowired
    protected DateTimeUtil dateTimeUtil;
    @Autowired
    protected ContextRepository contextRepo;
    @MockBean
    protected Provider<UserInfo> userInfoProvider;
    @MockBean
    protected PolicyEnforcement policyEnforcement;

    private UUID projectId = null;

    @BeforeEach
    public void set() {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(UUID.randomUUID());
        Mockito.when(userInfoProvider.get()).thenReturn(userInfo);
    }

    protected Project createProject(String name) {
        try {
            return innerCreateProject(name);
        } catch (QueryException  | DataIntegrityViolationException e) {
            Project project1 =
                    projectService.getAll().stream().filter(project ->
                            name.equals(project.getName())).findAny().orElseThrow(() -> e);
            projectId = project1.getId();
            cleanUp();
            return innerCreateProject(name);
        }
    }

    protected Project createProject(String name, UUID id) {
        Project project = new ProjectImpl();
        project.setId(id);
        project.setName(name);
        return project;
    }

    private Project innerCreateProject(String name) {
        Project project = projectService.create(new ProjectImpl(null, name, null, null, null, null, null));
        projectId = project.getId();
        return project;
    }

    @AfterEach
    public final void cleanUp() {
        if (projectId != null && projectService.existsById(projectId)) {
            projectService.getEnvironments(projectId).forEach(environment
                    -> {
                environmentService.getSystems(environment.getId())
                        .forEach(system -> systemService.delete(system.getId(), environment.getId()));
                environmentService.delete(environment.getId());
            });
            projectService.delete(projectId);
            projectId = null;
        }
    }
}
