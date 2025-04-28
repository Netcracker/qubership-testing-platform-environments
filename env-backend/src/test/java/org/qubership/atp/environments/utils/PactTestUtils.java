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

package org.qubership.atp.environments.utils;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.ParametersGettingVersion;
import org.qubership.atp.environments.model.Project;
import org.qubership.atp.environments.model.ServerItf;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.SystemCategory;
import org.qubership.atp.environments.model.impl.ConnectionImpl;
import org.qubership.atp.environments.model.impl.EnvironmentImpl;
import org.qubership.atp.environments.model.impl.ProjectImpl;
import org.qubership.atp.environments.model.impl.SystemCategoryImpl;
import org.qubership.atp.environments.model.impl.SystemImpl;
import org.qubership.atp.environments.model.utils.enums.Status;

public class PactTestUtils {

    public static Connection getConnection() {
        Connection connection = new ConnectionImpl();
        connection.setId(UUID.randomUUID());
        connection.setName("name");
        connection.setDescription("description");
        connection.setCreated(1L);
        connection.setCreatedBy(UUID.randomUUID());
        connection.setModified(1L);
        connection.setModifiedBy(UUID.randomUUID());
        connection.setParameters(null);
        connection.setServices(Collections.singletonList("any"));
        connection.setSourceTemplateId(UUID.randomUUID());
        connection.setConnectionType("connection type");
        connection.setSystemId(UUID.randomUUID());
        return connection;
    }

    public static Environment formEnvironment() {
        Environment environment = new EnvironmentImpl();
        environment.setId(UUID.randomUUID());
        List<UUID> environmentIds = new ArrayList<>();
        environmentIds.add(environment.getId());
        System system = new SystemImpl();
        system.setName("system name");
        system.setId(UUID.randomUUID());
        system.setEnvironmentIds(asList(environment));
        system.setModified(1L);
        system.setModifiedBy((UUID.randomUUID()));
        environment.setCreated(1L);
        environment.setCreatedBy(UUID.randomUUID());
        environment.setDescription("description");
        environment.setSystems(asList(system));
        environment.setModified(1L);
        environment.setModifiedBy(UUID.randomUUID());
        environment.setName("environment name");
        environment.setProjectId(UUID.randomUUID());
        environment.setGraylogName("grayLog name");
        return environment;
    }

    public static System formSystem() {
        System system = new SystemImpl();
        system.setCreated(1L);
        system.setCreatedBy(UUID.randomUUID());
        system.setDateOfCheckVersion(1L);
        system.setDateOfLastCheck(1L);
        system.setDescription("description");
        system.setExternalId(UUID.randomUUID());
        system.setExternalName("ext name");
        system.setId(UUID.randomUUID());
        system.setLinkToSystemId(UUID.randomUUID());
        system.setMergeByName(true);
        system.setModified(1L);
        system.setModifiedBy(UUID.randomUUID());
        system.setName("system name");
        system.setParentSystemId(UUID.randomUUID());
        system.setStatus(Status.FAIL);
        SystemCategory systemCategory = new SystemCategoryImpl();
        systemCategory.setId(UUID.randomUUID());
        systemCategory.setName("system category name");
        system.setSystemCategory(systemCategory);
        system.setVersion("version");
        system.setServerItf(new ServerItf());
        system.setParametersGettingVersion(new ParametersGettingVersion());
        system.setEnvironmentIds(asList(new EnvironmentImpl()));
        Connection connection = new ConnectionImpl();
        connection.setId(UUID.randomUUID());
        system.setConnections(asList(connection));
        return system;
    }

    public static List<System> formSystems() {
        return asList(formSystem());
    }

    public static Connection formConnection() {
        Connection connection = new ConnectionImpl();
        connection.setId(UUID.randomUUID());
        connection.setName("name");
        connection.setDescription("description");
        connection.setCreated(1L);
        connection.setCreatedBy(UUID.randomUUID());
        connection.setModified(1L);
        connection.setModifiedBy(UUID.randomUUID());
        ConnectionParameters connectionParameters = new ConnectionParameters();
        connectionParameters.put("key", "value");
        connection.setParameters(connectionParameters);
        connection.setSourceTemplateId(UUID.randomUUID());
        connection.setConnectionType("connection type");
        connection.setSystemId(UUID.randomUUID());
        connection.setServices(new ArrayList<>());

        return connection;
    }

    public static String formConnectionsName() {
        Connection connection = new ConnectionImpl();
        connection.setId(UUID.randomUUID());
        connection.setName("connection name");

        return connection.getName();
    }

    public static String formSystemsName() {
        System system = new SystemImpl();
        system.setId(UUID.randomUUID());
        system.setName("system name");

        return system.getName();
    }

    public static Project formProject() {
        Project project = new ProjectImpl();
        Environment environment = new EnvironmentImpl();
        environment.setModified(1L);
        environment.setModifiedBy(UUID.randomUUID());
        environment.setId(UUID.randomUUID());
        environment.setName("environment name");
        project.setEnvironments(asList(environment));
        project.setName("name");
        project.setCreated(1L);
        project.setModified(1L);
        project.setId(UUID.randomUUID());
        project.setDescription("description");
        project.setCreatedBy(UUID.randomUUID());
        project.setShortName("short name");
        project.setModifiedBy(UUID.randomUUID());
        return project;
    }

    public static SystemCategory formSystemCategory() {
        SystemCategory systemCategory = new SystemCategoryImpl();
        systemCategory.setId(UUID.randomUUID());
        systemCategory.setCreatedBy(UUID.randomUUID());
        systemCategory.setDescription("description");
        systemCategory.setName("name");
        systemCategory.setModifiedBy(UUID.randomUUID());
        systemCategory.setCreated(1L);
        systemCategory.setModified(1L);
        return systemCategory;
    }


    public static String formEnvironmentName() {
        Environment environment = new EnvironmentImpl();
        environment.setId(UUID.randomUUID());
        environment.setName("name");
        return environment.getName();
    }
}
