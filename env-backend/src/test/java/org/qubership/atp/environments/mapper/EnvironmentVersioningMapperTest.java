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

package org.qubership.atp.environments.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.qubership.atp.environments.mocks.EntitiesGenerator;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.EnvironmentCategory;
import org.qubership.atp.environments.model.Project;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.impl.EnvironmentCategoryImpl;
import org.qubership.atp.environments.model.impl.ProjectImpl;
import org.qubership.atp.environments.service.direct.EnvironmentCategoryService;
import org.qubership.atp.environments.service.direct.ProjectService;
import org.qubership.atp.environments.service.direct.SystemService;
import org.qubership.atp.environments.versioning.model.entities.EnvironmentJ;
import org.qubership.atp.environments.versioning.model.mapper.EnvironmentVersioning;
import org.springframework.test.util.ReflectionTestUtils;

public class EnvironmentVersioningMapperTest {

    private static final String UNDEFINED = "Undefined name";

    private static EnvironmentJ environmentJ;
    private static Project project;
    private static Project emptyProject;
    private static EnvironmentCategory environmentCategory;
    private static EnvironmentCategory emptyEnvironmentCategory;
    private static System system1;
    private static System system2;
    private static List<System> systems;

    private final ThreadLocal<EnvironmentCategoryService> environmentCategoryService = new ThreadLocal<>();
    private final ThreadLocal<ProjectService> projectService = new ThreadLocal<>();
    private final ThreadLocal<SystemService> systemService = new ThreadLocal<>();
    private final ThreadLocal<EnvironmentVersioningMapper> environmentVersioningMapper = new ThreadLocal<>();

    @BeforeAll
    public static void init() {
        project = EntitiesGenerator.generateProject("Project");
        emptyProject = new ProjectImpl();
        emptyProject.setName("Empty project");

        environmentCategory = EntitiesGenerator.generateEnvironmentCategory("Category");
        emptyEnvironmentCategory = new EnvironmentCategoryImpl();
        emptyEnvironmentCategory.setName("Empty category");

        system1 = EntitiesGenerator.generateSystem("System 1");
        system2 = EntitiesGenerator.generateSystem("System 2");
        systems = Arrays.asList(system1, system2);

        Environment environment = EntitiesGenerator.generateEnvironment("Environment");
        environment.setProjectId(project.getId());
        environment.setCategoryId(environmentCategory.getId());
        environment.setSystems(systems);

        environmentJ = new EnvironmentJ(environment);
    }

    @BeforeEach
    public void setUp() {
        EnvironmentCategoryService environmentCategoryServiceMock = mock(EnvironmentCategoryService.class);
        ProjectService projectServiceMock = mock(ProjectService.class);
        SystemService systemServiceMock = mock(SystemService.class);

        EnvironmentVersioningMapper environmentVersioningMapperMock = new EnvironmentVersioningMapper(
                environmentCategoryServiceMock, projectServiceMock, systemServiceMock);
        ReflectionTestUtils.setField(environmentVersioningMapperMock, "mapper", new ModelMapper());
        environmentVersioningMapperMock.init();

        environmentCategoryService.set(environmentCategoryServiceMock);
        projectService.set(projectServiceMock);
        systemService.set(systemServiceMock);
        environmentVersioningMapper.set(environmentVersioningMapperMock);
    }

    @Test
    public void convertToEnvironmentVersioning_expectNameInsteadUUID() {
        when(projectService.get().get(project.getId())).thenReturn(project);
        when(environmentCategoryService.get().get(environmentCategory.getId())).thenReturn(environmentCategory);
        when(systemService.get().get(system1.getId())).thenReturn(system1);
        when(systemService.get().get(system2.getId())).thenReturn(system2);

        EnvironmentVersioning environmentVersioning = environmentVersioningMapper.get().map(environmentJ);

        assertEquals(environmentVersioning.getName(), environmentJ.getName());
        assertEquals(environmentVersioning.getModified(), environmentJ.getModified());
        assertEquals(environmentVersioning.getProjectName(), project.getName());
        assertEquals(environmentVersioning.getCategoryName(), environmentCategory.getName());
        assertEquals(environmentVersioning.getSystemNames().size(), systems.size());
    }

    @Test
    public void convertToEnvironmentVersioning_expectEmptyInsteadUUID() {
        when(projectService.get().get(project.getId())).thenReturn(emptyProject);
        when(environmentCategoryService.get().get(environmentCategory.getId())).thenReturn(emptyEnvironmentCategory);

        EnvironmentVersioning environmentVersioning = environmentVersioningMapper.get().map(environmentJ);

        assertEquals(environmentVersioning.getName(), environmentJ.getName());
        assertEquals(environmentVersioning.getModified(), environmentJ.getModified());
        assertEquals(environmentVersioning.getProjectName(), emptyProject.getName());
        assertEquals(environmentVersioning.getCategoryName(), emptyEnvironmentCategory.getName());
    }

    @Test
    public void convertToEnvironmentVersioning_expectUndefined() {

        EnvironmentVersioning environmentVersioning = environmentVersioningMapper.get().map(environmentJ);

        assertEquals(environmentVersioning.getName(), environmentJ.getName());
        assertEquals(environmentVersioning.getModified(), environmentJ.getModified());
        assertEquals(environmentVersioning.getSystemNames().size(), 0);
        assertEquals(environmentVersioning.getProjectName(), UNDEFINED);
        assertEquals(environmentVersioning.getCategoryName(), UNDEFINED);
    }
}
