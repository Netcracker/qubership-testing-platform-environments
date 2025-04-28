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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubership.atp.environments.errorhandling.history.EnvironmentHistoryValidationException;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.ParametersGettingVersion;
import org.qubership.atp.environments.model.Project;
import org.qubership.atp.environments.model.ServerItf;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.utils.Constants;
import org.qubership.atp.environments.model.utils.enums.TypeGettingVersion;
import org.qubership.atp.environments.service.direct.EnvironmentService;
import org.qubership.atp.environments.versioning.model.entities.EnvironmentJ;
import org.qubership.atp.environments.versioning.model.entities.SystemJ;
import org.qubership.atp.environments.versioning.service.JaversRestoreServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class RestoreTest extends AbstractServiceTest {

    @Autowired
    private JaversRestoreServiceFactory javersRestoreServiceFactory;
    @Autowired
    private EnvironmentService environmentService;

    private static final String PROJECT_NAME = "Test_project_TICKET-14952";
    private static UUID PROJECT_ID;
    private static final UUID ENVIRONMENT_CATEGORY_ID = Constants.Environment.Category.ENVIRONMENT;
    private static UUID SYSTEM_CATEGORY_ID;


    @BeforeEach
    public void setUp() {
        Project projectTest = createProject(PROJECT_NAME);
        PROJECT_ID = projectTest.getId();
        SYSTEM_CATEGORY_ID = systemCategoriesService.getAll().get(0).getId();
    }

    @Test
    public void restoreEnvironmentWithSimpleFieldTest() {
        String environmentName = "RestoreEnvironmentWithSimpleField";
        String environmentNameChanged = environmentName + "Changed";

        Environment v1 = environmentService.create(PROJECT_ID, environmentName, "", null, "", "", "",
                ENVIRONMENT_CATEGORY_ID, Collections.emptyList());
        UUID environmentId = v1.getId();
        environmentService.update(environmentId, environmentNameChanged, "", null, "", "", "", PROJECT_ID,
                ENVIRONMENT_CATEGORY_ID, Collections.emptyList());

        Environment v2 = environmentService.get(environmentId);
        assertNotEquals(v1.getName(), v2.getName());

        javersRestoreServiceFactory.getRestoreService(EnvironmentJ.class)
                .restore(EnvironmentJ.class, environmentId, 1);

        Environment restoredV1 = environmentService.get(environmentId);
        assertEquals(v1.getName(), restoredV1.getName());
    }

    @Test
    public void referenceInvalidExceptionTest() {
        String categoryName = "Some_another_category";
        String environmentName = "Environment Test invalid reference";
        environmentCategoryService.getAll().forEach(category -> {
            if (category.getName().equals(categoryName)) {
                environmentCategoryService.delete(category.getId());
            }
        });
        UUID categoryId = environmentCategoryService.create(categoryName,
                null, null).getId();

        Environment environment = environmentService.create(PROJECT_ID, environmentName, "", null, "", "",
                "", categoryId, Collections.emptyList());
        UUID environmentId = environment.getId();

        environmentService.update(environmentId, environmentName, "", null, "", "","", PROJECT_ID,
                Constants.Environment.Category.ENVIRONMENT, Collections.emptyList());

        environmentCategoryService.delete(categoryId);

        Assertions.assertThrows(EnvironmentHistoryValidationException.class,
                () -> javersRestoreServiceFactory.getRestoreService(EnvironmentJ.class)
                        .restore(EnvironmentJ.class, environmentId, 1));
    }

    @Test
    public void restoreSystemWithDifficultFieldsTest() {
        UUID environmentId = environmentService.create(PROJECT_ID, "EnvForDifficultSystemFields", "", null,
                "", "", "",
                ENVIRONMENT_CATEGORY_ID, Collections.emptyList()).getId();
        String systemName = "RestoreSystemWithDifficultFields";

        ParametersGettingVersion parametersGettingVersionV1 = new ParametersGettingVersion();
        parametersGettingVersionV1.setType(TypeGettingVersion.BY_HTTP_ENDPOINT);
        parametersGettingVersionV1.setParameters("parameters 1");
        ServerItf serverItfV1 = new ServerItf();
        serverItfV1.setName("server name 1");
        serverItfV1.setUrl("url 1");
        System v1 = systemService.create(environmentId, systemName, null, SYSTEM_CATEGORY_ID,
                parametersGettingVersionV1, null, serverItfV1, false, null,null, null);
        UUID systemId = v1.getId();

        ParametersGettingVersion parametersGettingVersionV2 = new ParametersGettingVersion();
        parametersGettingVersionV2.setType(TypeGettingVersion.BY_SQL_QUERY);
        parametersGettingVersionV2.setParameters("parameters 2");
        ServerItf serverItfV2 = new ServerItf();
        serverItfV2.setName("server name 2");
        serverItfV2.setUrl("url 2");

        systemService.update(systemId, systemName, null, SYSTEM_CATEGORY_ID, parametersGettingVersionV2, null, serverItfV2, false, null,null, null);
        System v2 = systemService.get(systemId);

        assertNotEquals(v1.getParametersGettingVersion(), v2.getParametersGettingVersion());
        assertNotEquals(v1.getServerItf(), v2.getServerItf());

        javersRestoreServiceFactory.getRestoreService(SystemJ.class)
                .restore(SystemJ.class, systemId, 1);
        System restoredV1 = systemService.get(systemId);

        assertEquals(v1.getParametersGettingVersion(), restoredV1.getParametersGettingVersion());
        assertEquals(v1.getServerItf(), restoredV1.getServerItf());
    }

    @Test
    public void restoreSystemWithImmutableConnectionTest() {
        UUID environmentId = environmentService.create(PROJECT_ID, "EnvForSystemWithImmutableConnection", "", null, "", "","",
                ENVIRONMENT_CATEGORY_ID, Collections.emptyList()).getId();
        String systemName = "RestoreSystemWithImmutableConnection";
        System v1 = systemService.create(environmentId, systemName, null, SYSTEM_CATEGORY_ID, null, null, null, false, null ,null, null);
        UUID systemId = v1.getId();

        connectionService.create(systemId, "ImmutableConnection", null, null, null, null, null);
        System v2 = systemService.get(systemId);
        assertNotEquals(v1.getConnections(), v2.getConnections());

        systemService.update(systemId, systemName, "smth changed", SYSTEM_CATEGORY_ID, null, null, null, false,null ,null, null);

        javersRestoreServiceFactory.getRestoreService(SystemJ.class)
                .restore(SystemJ.class, systemId, 2);
        System restoredV2 = systemService.get(systemId);
        assertEquals(v2.getConnections(), restoredV2.getConnections());
    }

    @Test
    public void restoreSystemWithChangedConnectionTest() {
        UUID environmentId = environmentService.create(PROJECT_ID, "EnvForSystemWithChangedConnection", "", null, "", "","",
                ENVIRONMENT_CATEGORY_ID, Collections.emptyList()).getId();
        System v1 = systemService.create(environmentId, "RestoreSystemWithChangedConnection", null,
                SYSTEM_CATEGORY_ID, null, null, null, false,null ,null, null);
        UUID systemId = v1.getId();

        String connectionName = "ChangedConnection";
        ConnectionParameters connectionParametersV2 = new ConnectionParameters();
        connectionParametersV2.put("key 2", "value 2");
        String connectionTypeV2 = "connection type 2";
        Connection connectionV2 = connectionService.create(systemId, connectionName, null,
                connectionParametersV2, connectionTypeV2, null, null);
        System v2 = systemService.get(systemId);
        assertNotEquals(v1.getConnections(), v2.getConnections());

        ConnectionParameters connectionParametersV3 = new ConnectionParameters();
        connectionParametersV3.put("key 3", "value 3");
        String connectionTypeV3 = "connection type 3";
        connectionService.update(connectionV2.getId(), systemId, connectionName, null,
                connectionParametersV3, connectionTypeV3, null, new ArrayList<>());

        System v3 = systemService.get(systemId);
        Connection connectionV3 = v3.getConnections().get(0);
        assertNotEquals(connectionV2.getParameters(), connectionV3.getParameters());
        assertNotEquals(connectionV2.getConnectionType(), connectionV3.getConnectionType());

        javersRestoreServiceFactory.getRestoreService(SystemJ.class)
                .restore(SystemJ.class, systemId, 2);
        System restoredV2 = systemService.get(systemId);
        Connection restoredConnectionV2 = restoredV2.getConnections().get(0);
        assertEquals(connectionV2.getParameters(), restoredConnectionV2.getParameters());
        assertEquals(connectionV2.getConnectionType(), restoredConnectionV2.getConnectionType());
    }

    @Test
    public void restoreSystemWithAddedConnectionTest() {
        UUID environmentId = environmentService.create(PROJECT_ID, "EnvForSystemWithAddedConnection", "", null, "", "","",
                ENVIRONMENT_CATEGORY_ID, Collections.emptyList()).getId();
        System v1 = systemService.create(environmentId, "RestoreSystemWithAddedConnection", null,
                SYSTEM_CATEGORY_ID, null, null, null, false,null ,null, null);
        UUID systemId = v1.getId();

        Connection connectionV2 = connectionService.create(systemId, "AddedConnection", null,null, null, null, null);
        System v2 = systemService.get(systemId);
        assertNotEquals(v1.getConnections(), v2.getConnections());

        connectionService.delete(connectionV2.getId());
        System v3 = systemService.get(systemId);
        assertNotEquals(v2.getConnections(), v3.getConnections());

        javersRestoreServiceFactory.getRestoreService(SystemJ.class)
                .restore(SystemJ.class, systemId, 2);
        System restoredV2 = systemService.get(systemId);
        assertEquals(v2.getConnections(), restoredV2.getConnections());
    }

    @Test
    public void restoreSystemWithRemovedConnectionTest() {
        UUID environmentId = environmentService.create(PROJECT_ID, "EnvForSystemWithRemovedConnection", "", null, "", "","",
                ENVIRONMENT_CATEGORY_ID, Collections.emptyList()).getId();
        System v1 = systemService.create(environmentId, "RestoreSystemWithRemovedConnection", null,
                SYSTEM_CATEGORY_ID, null, null, null, false, null ,null, null);
        UUID systemId = v1.getId();

        connectionService.create(systemId, "RemovedConnection", null,null, null, null, null);
        System v2 = systemService.get(systemId);
        assertNotEquals(v1.getConnections(), v2.getConnections());

        javersRestoreServiceFactory.getRestoreService(SystemJ.class)
                .restore(SystemJ.class, systemId, 1);
        System restoredV1 = systemService.get(systemId);
        assertEquals(v1.getConnections(), restoredV1.getConnections());
    }
}
