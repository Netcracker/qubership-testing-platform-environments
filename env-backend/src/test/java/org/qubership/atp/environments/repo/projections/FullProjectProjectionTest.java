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

import com.google.inject.util.Providers;

public class FullProjectProjectionTest {

    FullProjectProjection fullProjectProjection;
    ProjectRepositoryImpl repo;
    EnvironmentRepositoryImpl environmentRepository;

    @Before
    public void setUp() {
        repo = mock(ProjectRepositoryImpl.class);
        fullProjectProjection = new FullProjectProjection(repo);
        environmentRepository = mock(EnvironmentRepositoryImpl.class);
    }

    @Test
    public void testCreateWithDescription_FullProjectProjection_gotProjection() {
        Environment environment = EntitiesGenerator.generateEnvironment("Environment");
        Project project = EntitiesGenerator.generateProject("Project");
        List<System> systems = Collections.singletonList(EntitiesGenerator.generateSystem("System"));
        environment.setCreated(0L);
        environment.setProjectId(project.getId());
        environment.setSystems(systems);
        when(repo.getEnvironmentRepo()).thenReturn(Providers.of(environmentRepository));
        when(environmentRepository.getAllByParentId(project.getId())).thenReturn(Collections.singletonList(environment));
        Project resultProject = fullProjectProjection.create(project.getId(),
                project.getName(), project.getShortName(), project.getDescription(),
                new Timestamp(0L), new Timestamp(0L));
        Assert.assertEquals(project.getId(), resultProject.getId());
        Assert.assertEquals(project.getName(), resultProject.getName());
    }

    @Test
    public void testCreate_FullProjectProjection_gotProjection() {
        Environment environment = EntitiesGenerator.generateEnvironment("Environment");
        Project project = EntitiesGenerator.generateProject("Project");
        List<System> systems = Collections.singletonList(EntitiesGenerator.generateSystem("System"));
        environment.setCreated(0L);
        environment.setProjectId(project.getId());
        environment.setSystems(systems);
        when(repo.getEnvironmentRepo()).thenReturn(Providers.of(environmentRepository));
        when(environmentRepository.getAllByParentId(project.getId())).thenReturn(Collections.singletonList(environment));
        Project resultProject = fullProjectProjection.create(project.getId(),
                project.getName(), project.getShortName(),
                new Timestamp(0L), new Timestamp(0L));
        Assert.assertEquals(project.getId(), resultProject.getId());
        Assert.assertEquals(project.getName(), resultProject.getName());
    }
}
