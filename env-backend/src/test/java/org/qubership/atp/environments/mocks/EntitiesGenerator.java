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

package org.qubership.atp.environments.mocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.EnvironmentCategory;
import org.qubership.atp.environments.model.Project;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.SystemCategory;
import org.qubership.atp.environments.model.impl.ConnectionImpl;
import org.qubership.atp.environments.model.impl.EnvironmentCategoryImpl;
import org.qubership.atp.environments.model.impl.EnvironmentImpl;
import org.qubership.atp.environments.model.impl.ProjectImpl;
import org.qubership.atp.environments.model.impl.SystemCategoryImpl;
import org.qubership.atp.environments.model.impl.SystemImpl;
import org.qubership.atp.environments.model.utils.Constants;

public class EntitiesGenerator {

    public static Project generateProject(String name) {
        return generateProject(name, UUID.randomUUID());
    }

    public static Project generateProject(String name, UUID projectId) {
        Project project = new ProjectImpl();
        project.setId(projectId);
        project.setName(name);
        project.setEnvironments(new ArrayList<>());
        return project;
    }

    public static List<Connection> generateConnectionTemplates() {
        Connection dbTemplate = new ConnectionImpl();
        dbTemplate.setId(Constants.Environment.System.Connection.DB);
        ConnectionParameters dbParameters = new ConnectionParameters();
        dbParameters.put("db_password", "value");
        dbTemplate.setParameters(dbParameters);
        Connection sshTemplate = new ConnectionImpl();
        sshTemplate.setId(Constants.Environment.System.Connection.SSH);
        ConnectionParameters sshParameters = new ConnectionParameters();
        sshParameters.put("ssh_key", "value");
        sshTemplate.setParameters(sshParameters);
        Connection httpTemplate = new ConnectionImpl();
        httpTemplate.setId(Constants.Environment.System.Connection.HTTP);
        ConnectionParameters httpParameters = new ConnectionParameters();
        httpParameters.put("password", "value");
        httpTemplate.setParameters(httpParameters);
        return new ArrayList<>(Arrays.asList(dbTemplate, sshTemplate, httpTemplate));
    }

    public static EnvironmentCategory generateEnvironmentCategory(String name) {
        EnvironmentCategory category = new EnvironmentCategoryImpl();
        category.setId(UUID.randomUUID());
        category.setName(name);
        return category;
    }

    public static System generateSystem(String name) {
        System system = new SystemImpl();
        system.setId(UUID.randomUUID());
        system.setName(name);
        return system;
    }

    public static SystemCategory generateSystemCategory(String name) {
        return generateSystemCategory(UUID.randomUUID(), name);
    }

    public static SystemCategory generateSystemCategory(UUID id, String name) {
        SystemCategory systemCategory = new SystemCategoryImpl();
        systemCategory.setId(id);
        systemCategory.setName(name);
        return systemCategory;
    }

    public static Environment generateEnvironment(String name) {
        Environment environment = new EnvironmentImpl();
        environment.setId(UUID.randomUUID());
        environment.setName(name);
        environment.setModified(new Date().getTime());
        environment.setProjectId(UUID.randomUUID());
        environment.setCategoryId(UUID.randomUUID());
        return environment;
    }

    public static Connection generateConnection(String name) {
        Connection connection = new ConnectionImpl();
        connection.setId(UUID.randomUUID());
        connection.setName(name);
        return connection;
    }
}
