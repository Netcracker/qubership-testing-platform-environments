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

package org.qubership.atp.environments.repo.projections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.qubership.atp.environments.mocks.EntitiesGenerator;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.Project;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.repo.impl.EnvironmentRepositoryImpl;
import org.qubership.atp.environments.repo.impl.ProjectRepositoryImpl;
import org.qubership.atp.environments.repo.impl.SystemRepositoryImpl;

import com.google.inject.util.Providers;

public class FullEnvironmentProjectionTest extends AbstractProjectionTest {

    FullEnvironmentProjection fullEnvironmentProjection;
    EnvironmentRepositoryImpl repo;
    ProjectRepositoryImpl projectRepo;
    SystemRepositoryImpl systemRepository;

    @Before
    public void setUp() {
        repo = mock(EnvironmentRepositoryImpl.class);
        fullEnvironmentProjection = new FullEnvironmentProjection(repo);
        projectRepo = mock(ProjectRepositoryImpl.class);
        systemRepository = mock(SystemRepositoryImpl.class);
    }

    @Test
    public void testMap_FullEnvironmentProjection_gotProjection() {
        Environment environment = EntitiesGenerator.generateEnvironment("Environment");
        Project project = EntitiesGenerator.generateProject("Project");
        List<System> systems = Collections.singletonList(EntitiesGenerator.generateSystem("System"));
        environment.setCreated(0L);
        environment.setProjectId(project.getId());
        environment.setSystems(systems);
        when(projectRepo.getShortById(project.getId())).thenReturn(project);
        when(systemRepository.getAllByParentId(environment.getId())).thenReturn(systems);
        when(repo.getProjectRepo()).thenReturn(Providers.of(projectRepo));
        when(repo.getSystemRepo()).thenReturn(Providers.of(systemRepository));
        Environment resultEnvironment = fullEnvironmentProjection.create(environment.getId(),
                environment.getName(), environment.getGraylogName(), environment.getDescription(),
                environment.getSsmSolutionAlias(),
                environment.getSsmInstanceAlias(), environment.getConsulEgressConfigPath(),
                new Timestamp(0L),
                environment.getCreatedBy(), new Timestamp(0L),
                environment.getModifiedBy(), environment.getProjectId(), environment.getCategoryId(),
                environment.getSourceId(), environment.getTags());
        Assert.assertEquals(environment.getId(), resultEnvironment.getId());
        Assert.assertEquals(environment.getName(), resultEnvironment.getName());
        Assert.assertEquals(environment.getSystems().size(), 1);
    }
}
