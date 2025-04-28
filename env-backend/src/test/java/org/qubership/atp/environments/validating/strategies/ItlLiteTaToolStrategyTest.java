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

package org.qubership.atp.environments.validating.strategies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.impl.ConnectionImpl;
import org.qubership.atp.environments.model.impl.EnvironmentImpl;
import org.qubership.atp.environments.model.impl.SystemCategoryImpl;
import org.qubership.atp.environments.model.impl.SystemImpl;
import org.qubership.atp.environments.model.utils.Constants;
import org.qubership.atp.environments.service.rest.server.response.ValidateTaToolResponse;

public class ItlLiteTaToolStrategyTest {

    ValidationStrategy strategy = new ItlLiteTaToolStrategy();
    EnvironmentImpl.EnvironmentImplBuilder taTool = getTool();
    SystemImpl.SystemImplBuilder itfLiteSystem = getSystem();
    ConnectionImpl.ConnectionImplBuilder httpConnection = getHttpConnection();

    @Test
    public void test_whenTaToolDoesNotHaveItfLiteSystem_validationFailed() {
        ValidateTaToolResponse result = strategy.validate(taTool.build());
        Assertions.assertFalse(result.isValidated());
        Assertions.assertEquals(result.getMessage(), "A Tool entity has to contain system with category "
                + "'ITF Lite' and this system has to contain 'HTTP' connection"
                + " and 'HTTP' connection has to contain 'URL' property");
    }

    @Test
    public void test_whenTaToolDoesNotHaveHttpConnection_validationFailed() {
        List<System> systemList = Collections.singletonList(itfLiteSystem.build());
        ValidateTaToolResponse result = strategy.validate(taTool.systemsList(systemList).build());
        Assertions.assertFalse(result.isValidated());
        Assertions.assertEquals(result.getMessage(), "'ITF Lite' System has to contain 'HTTP' connection"
                + " and 'HTTP' connection has to contain 'URL' property");
    }

    @Test
    public void test_whenTaToolDoesNotHaveUrl_validationFailed() {
        List<Connection> connectionList = Collections.singletonList(httpConnection.build());
        List<System> systemList = Collections.singletonList(itfLiteSystem.connectionsList(connectionList).build());
        ValidateTaToolResponse result = strategy.validate(taTool.systemsList(systemList).build());
        Assertions.assertFalse(result.isValidated());
        Assertions.assertEquals(result.getMessage(), "'HTTP' connection has to contain 'URL' property");

    }

    @Test
    public void test_whenTaToolHaveFullItfLite_validationPassed() {
        ConnectionParameters parameters = new ConnectionParameters();
        parameters.put("url", "url");
        List<Connection> connectionList = Collections.singletonList(httpConnection.parameters(parameters).build());
        List<System> systemList = Collections.singletonList(itfLiteSystem.connectionsList(connectionList).build());
        ValidateTaToolResponse result = strategy.validate(taTool.systemsList(systemList).build());
        Assertions.assertTrue(result.isValidated());
    }

    private SystemImpl.SystemImplBuilder getSystem() {
        return SystemImpl.builder()
                .uuid(UUID.randomUUID())
                .name("Test copy system")
                .systemCategory(new SystemCategoryImpl(Constants.SystemCategories.ITF_LITE, "ITF Lite", null, null, null));
    }

    private EnvironmentImpl.EnvironmentImplBuilder getTool() {
        return EnvironmentImpl.builder()
                .uuid(UUID.randomUUID())
                .name("Test Tool")
                .systemsList(new ArrayList<>());
    }

    private ConnectionImpl.ConnectionImplBuilder getHttpConnection() {
        return ConnectionImpl.builder()
                .uuid(UUID.randomUUID())
                .name("Test http")
                .sourceTemplateId(Constants.Environment.System.Connection.HTTP)
                .parameters(new ConnectionParameters());
    }
}
