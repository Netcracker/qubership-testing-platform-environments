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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.Project;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.utils.Constants;
import org.qubership.atp.environments.model.utils.enums.Status;
import org.qubership.atp.environments.service.rest.server.dto.ConnectionDto;
import org.qubership.atp.environments.service.rest.server.dto.CreateSystemDto;
import org.qubership.atp.environments.service.rest.server.dto.EnvironmentDto;
import org.springframework.dao.DataIntegrityViolationException;

import com.google.common.collect.Lists;

public class EnvironmentServiceTest extends AbstractServiceTest {

    @Test
    public void createEnvironment_systemWithWrongCategory_gotException() {
        Project projectTest = createProject("Test_project_1");
        CreateSystemDto invalidSystemDto = new CreateSystemDto(null, "Test system name", null, null,
                UUID.fromString("89252951-b234-4e80-a91d-42eb915ab54a"), null, Status.NOTHING,
                null, null, null, null, null, null, null, null, null, null);
        EnvironmentDto environmentDto = new EnvironmentDto(null, "Test environment name",
                Lists.newArrayList(invalidSystemDto));

        Exception ex = Assertions.assertThrows(NullPointerException.class,
                () -> projectService.create(projectTest.getId(), environmentDto, Constants.Environment.Category.ENVIRONMENT));
        Assertions.assertTrue(ex.getMessage().contains("categoryId"));
    }

    @Test
    public void createEnvironment_systemWithWrongCategory_gotRollback() {
        Project projectTest = createProject("Test_project_2");
        CreateSystemDto invalidSystemDto = new CreateSystemDto(null, "Test system name", null, null,
                UUID.fromString("89252951-b234-4e80-a91d-42eb915ab54a"), null, Status.NOTHING,
                null, null, null, null, null, null, null,null, null, null);
        EnvironmentDto environmentDto = new EnvironmentDto(null, "Test environment name",
                Lists.newArrayList(invalidSystemDto));
        Assertions.assertThrows(NullPointerException.class,
                () -> projectService.create(projectTest.getId(), environmentDto, Constants.Environment.Category.ENVIRONMENT));
        List<Environment> environments =  projectService.getEnvironments(projectTest.getId());
        Assertions.assertTrue(environments.isEmpty());
    }

    @Test
    public void createEnvironment_connectionWithWrongSourceTemplate_gotException() {
        Project projectTest = createProject("Test_project_3");
        ConnectionDto invalidConnectionDto = new ConnectionDto(null, "Test Conn", null, null,
                UUID.fromString("2cb3b9e0-0063-46bf-8f18-b203fdc19a73"), null, null, null, null);
        CreateSystemDto systemDto = new CreateSystemDto(null, "Test system name", null, null,
                null, Lists.newArrayList(invalidConnectionDto), Status.NOTHING, null, null, null,
                null, null, null, null, null, null, null);
        EnvironmentDto environmentDto = new EnvironmentDto(null, "Test environment name",
                Lists.newArrayList(systemDto));

        Exception ex = Assertions.assertThrows(NullPointerException.class,
                () -> projectService.create(projectTest.getId(), environmentDto, Constants.Environment.Category.ENVIRONMENT));
        Assertions.assertTrue(ex.getMessage().contains("sourceTemplateId"));
    }

    @Test
    public void createEnvironment_connectionWithWrongSourceTemplate_gotRollback() {
        Project projectTest = createProject("Test_project_4");
        ConnectionDto invalidConnectionDto = new ConnectionDto(null, "Test Conn", null, null,
                UUID.fromString("2cb3b9e0-0063-46bf-8f18-b203fdc19a73"), null, null, null, null);
        CreateSystemDto systemDto = new CreateSystemDto(null, "Test system name", null, null,
                null, Lists.newArrayList(invalidConnectionDto), Status.NOTHING, null, null, null,
                null, null, null, null, null, null, null);
        EnvironmentDto environmentDto = new EnvironmentDto(null, "Test environment name",
                Lists.newArrayList(systemDto));
        Assertions.assertThrows(NullPointerException.class,
                () -> projectService.create(projectTest.getId(), environmentDto, Constants.Environment.Category.ENVIRONMENT));
        List<Environment> environments = projectService.getEnvironments(projectTest.getId());
        Assertions.assertTrue(environments.isEmpty());
    }

    @Test
    public void createEnvironment_connectionWithWrongNameConnection_gotRollback() {
        Project projectTest = createProject("Test_project_5");
        ConnectionDto invalidConnectionDto = new ConnectionDto(null,
                "Test_Conn_Test_Conn_Test_Conn_Test_Conn_Test_Conn_Test_Conn_Test_Conn_Test_Conn_Test_Conn_Test_Conn",
                null, null, Constants.Environment.System.Connection.HTTP, null, null, null, null);
        CreateSystemDto systemDto = new CreateSystemDto(null, "Test system name", null, null,
                null, Lists.newArrayList(invalidConnectionDto), Status.NOTHING, null, null, null,
                null, null, null, null, null, null, null);
        EnvironmentDto environmentDto = new EnvironmentDto(null, "Test environment name",
                Lists.newArrayList(systemDto));
        Assertions.assertThrows(DataIntegrityViolationException.class,
                () -> projectService.create(projectTest.getId(), environmentDto, Constants.Environment.Category.ENVIRONMENT));
        List<Environment> environments = projectService.getEnvironments(projectTest.getId());
        Assertions.assertTrue(environments.isEmpty());
    }

    @Test
    public void getListSystems() {
        Project projectTest = createProject("Test_project_6");
        Environment environment = environmentService.create(projectTest.getId(), projectTest.getName(), "",
                projectTest.getDescription(), "", "", "", Constants.Environment.Category.ENVIRONMENT, Collections.emptyList());
        System system = systemService.create(environment.getId(), environment.getName(),
                environment.getDescription(),
                UUID.fromString("71ce56bd-9dbd-4e7d-afcd-3e6f4f6cc949"), null, null, null, null, null, null, null);
        List<System> systems = environmentService.getSystems(environment.getId());
        Assertions.assertEquals(Lists.newArrayList(system), systems);
    }
}
