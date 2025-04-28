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

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.qubership.atp.environments.mocks.EntitiesGenerator;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.SystemCategory;
import org.qubership.atp.environments.model.utils.enums.Status;
import org.qubership.atp.environments.repo.impl.ConnectionRepositoryImpl;
import org.qubership.atp.environments.repo.impl.EnvironmentRepositoryImpl;
import org.qubership.atp.environments.repo.impl.ProjectRepositoryImpl;
import org.qubership.atp.environments.repo.impl.SystemCategoryRepositoryImpl;
import org.qubership.atp.environments.repo.impl.SystemRepositoryImpl;

import com.google.inject.util.Providers;
import com.querydsl.core.Tuple;

public class SystemProjectionWithoutProxyTest extends AbstractProjectionTest {
    SystemProjectionWithoutProxy systemProjectionWithoutProxy;
    EnvironmentRepositoryImpl environmentRepository;
    ProjectRepositoryImpl projectRepo;
    SystemRepositoryImpl systemRepository;
    SystemCategoryRepositoryImpl systemCategoryRepository;
    ConnectionRepositoryImpl connectionRepository;




    @Before
    public void setUp() {
        environmentRepository = mock(EnvironmentRepositoryImpl.class);
        projectRepo = mock(ProjectRepositoryImpl.class);
        systemRepository = mock(SystemRepositoryImpl.class);
        systemCategoryRepository = mock(SystemCategoryRepositoryImpl.class);
        connectionRepository = mock(ConnectionRepositoryImpl.class);
        systemProjectionWithoutProxy = new SystemProjectionWithoutProxy(systemRepository);
    }

    @Test
    public void testMap_SystemProjectionWithoutProxy_gotSystem() {
        System system = EntitiesGenerator.generateSystem("System");
        List<Connection> connections = Collections.singletonList(EntitiesGenerator.generateConnection("Connection"));
        system.setCreated(0L);
        system.setStatus(Status.PASS);
        List<Environment> environments = Collections.singletonList(EntitiesGenerator.generateEnvironment("Environment"));
        system.setEnvironments(environments);
        system.setConnections(connections);
        when(systemRepository.getSystemCategoryRepo()).thenReturn(Providers.of(systemCategoryRepository));
        when(systemRepository.getEnvironmentRepo()).thenReturn(Providers.of(environmentRepository));
        when(systemRepository.getConnectionRepo()).thenReturn(Providers.of(connectionRepository));
        when(environmentRepository.getAllBySystemId(system.getId())).thenReturn(environments);
        SystemCategory systemCategory = EntitiesGenerator.generateSystemCategory("SomeCategory");
        system.setSystemCategory(systemCategory);
        when(systemCategoryRepository.getById(systemCategory.getId())).thenReturn(systemCategory);
        when(connectionRepository.getAllByParentId(system.getId())).thenReturn(connections);
        Tuple tuple = mockTupleForSystem(system);
        System resultSystem = systemProjectionWithoutProxy.map(tuple);
        Assert.assertEquals(resultSystem.getId(), system.getId());
        Assert.assertEquals(resultSystem.getName(), system.getName());
        Assert.assertNotNull(resultSystem.getEnvironments());
        Assert.assertNotNull(resultSystem.getConnections());
    }
}
