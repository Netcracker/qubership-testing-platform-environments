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

import java.util.Collections;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.qubership.atp.environments.mocks.EntitiesGenerator;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.SystemCategory;
import org.qubership.atp.environments.model.impl.ConnectionImpl;
import org.qubership.atp.environments.model.impl.SystemCategoryImpl;
import org.qubership.atp.environments.service.direct.ConnectionService;
import org.qubership.atp.environments.service.direct.SystemCategoriesService;
import org.qubership.atp.environments.versioning.model.entities.SystemJ;
import org.qubership.atp.environments.versioning.model.mapper.ConnectionVersioning;
import org.qubership.atp.environments.versioning.model.mapper.SystemVersioning;
import org.qubership.atp.environments.versioning.model.values.ConnectionJ;
import org.springframework.test.util.ReflectionTestUtils;

public class SystemVersioningMapperTest {

    private static final String UNDEFINED = "Undefined name";

    private static SystemJ systemJ;
    private static ConnectionJ connectionJ;
    private static Connection connection;
    private static Connection emptyConnection;
    private static SystemCategory systemCategory;
    private static SystemCategory emptySystemCategory;

    private final ThreadLocal<SystemCategoriesService> systemCategoriesService = new ThreadLocal<>();
    private final ThreadLocal<ConnectionService> connectionService = new ThreadLocal<>();

    private final ThreadLocal<ConnectionVersioningMapper> connectionVersioningMapper = new ThreadLocal<>();
    private final ThreadLocal<SystemVersioningMapper> systemVersioningMapper = new ThreadLocal<>();

    @BeforeAll
    public static void init() {
        systemCategory = EntitiesGenerator.generateSystemCategory("System Category");
        emptySystemCategory = new SystemCategoryImpl();
        emptySystemCategory.setName("Empty system category");
        connection = EntitiesGenerator.generateConnection("Connection");
        emptyConnection = new ConnectionImpl();

        System system = EntitiesGenerator.generateSystem("System");
        system.setSystemCategory(systemCategory);
        system.setConnections(Collections.singletonList(connection));

        systemJ = new SystemJ(system);
        connectionJ = new ConnectionJ(connection);
    }

    @BeforeEach
    public void setUp() {
        SystemCategoriesService systemCategoriesServiceMock = mock(SystemCategoriesService.class);
        ConnectionService connectionServiceMock = mock(ConnectionService.class);

        ConnectionVersioningMapper connectionVersioningMapperMock = new ConnectionVersioningMapper(connectionServiceMock);
        ReflectionTestUtils.setField(connectionVersioningMapperMock, "mapper", new ModelMapper());
        connectionVersioningMapperMock.init();

        SystemVersioningMapper systemVersioningMapperMock = new SystemVersioningMapper(systemCategoriesServiceMock, connectionVersioningMapperMock);
        ReflectionTestUtils.setField(systemVersioningMapperMock, "mapper", new ModelMapper());
        systemVersioningMapperMock.init();

        systemCategoriesService.set(systemCategoriesServiceMock);
        connectionService.set(connectionServiceMock);
        connectionVersioningMapper.set(connectionVersioningMapperMock);
        systemVersioningMapper.set(systemVersioningMapperMock);
    }

    @Test
    public void convertToSystemVersioning_expectNameInsteadUUID() {
        when(systemCategoriesService.get().get(systemCategory.getId())).thenReturn(systemCategory);
        when(connectionService.get().get(connection.getId())).thenReturn(connection);

        ConnectionVersioning connectionVersioning = connectionVersioningMapper.get().map(connectionJ);
        SystemVersioning systemVersioning = systemVersioningMapper.get().map(systemJ);

        assertEquals(systemVersioning.getId(), systemJ.getId());
        assertEquals(systemVersioning.getName(), systemJ.getName());
        assertEquals(systemVersioning.getModified(), systemJ.getModified());
        assertEquals(systemVersioning.getSystemCategoryName(), systemCategory.getName());
        assertEquals(systemVersioning.getConnections(), Collections.singletonList(connectionVersioning));
        assertEquals(systemVersioning.getParametersGettingVersion(), systemJ.getParametersGettingVersion());
        assertEquals(systemVersioning.getServerItf(), systemJ.getServerItf());
    }

    @Test
    public void convertToSystemVersioning_expectEmptyInsteadUUID() {
        when(systemCategoriesService.get().get(systemCategory.getId())).thenReturn(emptySystemCategory);
        when(connectionService.get().get(connection.getId())).thenReturn(emptyConnection);

        ConnectionVersioning connectionVersioning = connectionVersioningMapper.get().map(connectionJ);
        SystemVersioning systemVersioning = systemVersioningMapper.get().map(systemJ);

        assertEquals(systemVersioning.getId(), systemJ.getId());
        assertEquals(systemVersioning.getName(), systemJ.getName());
        assertEquals(systemVersioning.getModified(), systemJ.getModified());
        assertEquals(systemVersioning.getSystemCategoryName(), emptySystemCategory.getName());
        assertEquals(systemVersioning.getConnections(), Collections.singletonList(connectionVersioning));
        assertEquals(systemVersioning.getParametersGettingVersion(), systemJ.getParametersGettingVersion());
        assertEquals(systemVersioning.getServerItf(), systemJ.getServerItf());
    }

    @Test
    public void convertToSystemVersioning_expectUndefined() {
        ConnectionVersioning connectionVersioning = connectionVersioningMapper.get().map(connectionJ);
        SystemVersioning systemVersioning = systemVersioningMapper.get().map(systemJ);

        assertEquals(systemVersioning.getId(), systemJ.getId());
        assertEquals(systemVersioning.getName(), systemJ.getName());
        assertEquals(systemVersioning.getModified(), systemJ.getModified());
        assertEquals(systemVersioning.getSystemCategoryName(), UNDEFINED);
        assertEquals(systemVersioning.getConnections(), Collections.singletonList(connectionVersioning));
        assertEquals(systemVersioning.getParametersGettingVersion(), systemJ.getParametersGettingVersion());
        assertEquals(systemVersioning.getServerItf(), systemJ.getServerItf());
    }
}
