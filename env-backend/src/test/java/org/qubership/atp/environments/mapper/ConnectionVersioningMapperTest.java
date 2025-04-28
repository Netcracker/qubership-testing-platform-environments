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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.qubership.atp.environments.mocks.EntitiesGenerator;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.impl.ConnectionImpl;
import org.qubership.atp.environments.service.direct.ConnectionService;
import org.qubership.atp.environments.versioning.model.mapper.ConnectionVersioning;
import org.qubership.atp.environments.versioning.model.values.ConnectionJ;
import org.springframework.test.util.ReflectionTestUtils;

public class ConnectionVersioningMapperTest {

    private static final String UNDEFINED = "Undefined name";

    private static ConnectionJ connectionJ;
    private static Connection connection;
    private static Connection emptyConnection;

    private final ThreadLocal<ConnectionService> connectionService = new ThreadLocal<>();

    private final ThreadLocal<ConnectionVersioningMapper> connectionVersioningMapper = new ThreadLocal<>();

    @BeforeAll
    public static void init() {
        connection = EntitiesGenerator.generateConnection("Connection 2");
        emptyConnection = new ConnectionImpl();
        emptyConnection.setName("Empty Connection");

        Connection connection1 = EntitiesGenerator.generateConnection("Connection 1");
        connection1.setSourceTemplateId(connection.getId());

        connectionJ = new ConnectionJ(connection1);
    }

    @BeforeEach
    public void setUp() {

        ConnectionService connectionServiceMock = mock(ConnectionService.class);
        ConnectionVersioningMapper connectionVersioningMapperMock = new ConnectionVersioningMapper(connectionServiceMock);
        ReflectionTestUtils.setField(connectionVersioningMapperMock, "mapper", new ModelMapper());
        connectionVersioningMapperMock.init();

        connectionService.set(connectionServiceMock);
        connectionVersioningMapper.set(connectionVersioningMapperMock);
    }

    @Test
    public void convertToConnectionVersioning_expectNameInsteadUUID() {
        when(connectionService.get().get(connection.getId())).thenReturn(connection);

        ConnectionVersioning connectionVersioning = connectionVersioningMapper.get().map(connectionJ);

        assertEquals(connectionVersioning.getId(), connectionJ.getId());
        assertEquals(connectionVersioning.getName(), connectionJ.getName());
        assertEquals(connectionVersioning.getModified(), connectionJ.getModified());
        assertEquals(connectionVersioning.getSourceTemplateName(), connection.getName());
    }

    @Test
    public void convertToConnectionVersioning_expectEmptyInsteadUUID() {
        when(connectionService.get().get(connection.getId())).thenReturn(emptyConnection);

        ConnectionVersioning connectionVersioning = connectionVersioningMapper.get().map(connectionJ);

        assertEquals(connectionVersioning.getId(), connectionJ.getId());
        assertEquals(connectionVersioning.getName(), connectionJ.getName());
        assertEquals(connectionVersioning.getModified(), connectionJ.getModified());
        assertEquals(connectionVersioning.getSourceTemplateName(), emptyConnection.getName());
    }

    @Test
    public void convertToConnectionVersioning_expectUndefined() {
        ConnectionVersioning connectionVersioning = connectionVersioningMapper.get().map(connectionJ);

        assertEquals(connectionVersioning.getId(), connectionJ.getId());
        assertEquals(connectionVersioning.getName(), connectionJ.getName());
        assertEquals(connectionVersioning.getModified(), connectionJ.getModified());
        assertEquals(connectionVersioning.getSourceTemplateName(), UNDEFINED);
    }
}
