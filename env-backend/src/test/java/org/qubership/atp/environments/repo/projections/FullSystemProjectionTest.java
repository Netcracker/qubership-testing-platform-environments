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
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.SystemCategory;
import org.qubership.atp.environments.repo.impl.ConnectionRepositoryImpl;
import org.qubership.atp.environments.repo.impl.EnvironmentRepositoryImpl;
import org.qubership.atp.environments.repo.impl.ProjectRepositoryImpl;
import org.qubership.atp.environments.repo.impl.SystemCategoryRepositoryImpl;
import org.qubership.atp.environments.repo.impl.SystemRepositoryImpl;

import com.google.inject.util.Providers;

public class FullSystemProjectionTest {

    FullSystemProjection fullSystemProjection;
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
        fullSystemProjection = new FullSystemProjection(systemRepository);
    }

    @Test
    public void testCreateWithoutProxy_FullSystemProjection_gotProjection() {
        Environment environment = EntitiesGenerator.generateEnvironment("Environment");
        System system = EntitiesGenerator.generateSystem("System");
        environment.setSystems(Collections.singletonList(system));
        Connection connection = EntitiesGenerator.generateConnection("Connection");
        List<Connection> connections = Collections.singletonList(connection);
        List<Environment> environments = Collections.singletonList(environment);
        system.setConnections(connections);
        system.setEnvironments(environments);
        when(systemRepository.getSystemCategoryRepo()).thenReturn(Providers.of(systemCategoryRepository));
        when(systemRepository.getEnvironmentRepo()).thenReturn(Providers.of(environmentRepository));
        when(systemRepository.getConnectionRepo()).thenReturn(Providers.of(connectionRepository));
        when(environmentRepository.getAllBySystemId(system.getId())).thenReturn(environments);
        SystemCategory systemCategory = EntitiesGenerator.generateSystemCategory("SomeCategory");
        system.setSystemCategory(systemCategory);
        when(systemCategoryRepository.getById(systemCategory.getId())).thenReturn(systemCategory);
        when(connectionRepository.getAllByParentId(system.getId())).thenReturn(connections);
        System resultSystem = fullSystemProjection.createWithoutProxy(system.getId(),
                system.getName(),
                system.getDescription(),
                new Timestamp(0L),
                system.getCreatedBy(),
                new Timestamp(0L),
                system.getModifiedBy(),
                systemCategory.getId(),
                null,
                new Timestamp(0L),
                system.getVersion(),
                new Timestamp(0L),
                "{}",
                system.getParentSystemId(),
                "{}",
                system.getMergeByName(),
                system.getLinkToSystemId(),
                system.getExternalId(),
                system.getSourceId(),
                system.getExternalName(),
                environments);
        Assert.assertEquals(system.getId(), resultSystem.getId());
        Assert.assertEquals(system.getName(), resultSystem.getName());
        Assert.assertEquals(resultSystem.getConnections().size(), 1);
        Assert.assertEquals(resultSystem.getSystemCategory().getId(), systemCategory.getId());
    }

    @Test
    public void testCreate_FullSystemProjection_gotProjection() {
        Environment environment = EntitiesGenerator.generateEnvironment("Environment");
        System system = EntitiesGenerator.generateSystem("System");
        environment.setSystems(Collections.singletonList(system));
        Connection connection = EntitiesGenerator.generateConnection("Connection");
        List<Connection> connections = Collections.singletonList(connection);
        List<Environment> environments = Collections.singletonList(environment);
        system.setConnections(connections);
        system.setEnvironments(environments);
        when(systemRepository.getSystemCategoryRepo()).thenReturn(Providers.of(systemCategoryRepository));
        when(systemRepository.getEnvironmentRepo()).thenReturn(Providers.of(environmentRepository));
        when(systemRepository.getConnectionRepo()).thenReturn(Providers.of(connectionRepository));
        when(environmentRepository.getAllBySystemId(system.getId())).thenReturn(environments);
        SystemCategory systemCategory = EntitiesGenerator.generateSystemCategory("SomeCategory");
        system.setSystemCategory(systemCategory);
        when(systemCategoryRepository.getById(systemCategory.getId())).thenReturn(systemCategory);
        when(connectionRepository.getAllByParentId(system.getId())).thenReturn(connections);
        System resultSystem = fullSystemProjection.create(system.getId(),
                system.getName(),
                system.getDescription(),
                new Timestamp(0L),
                system.getCreatedBy(),
                new Timestamp(0L),
                system.getModifiedBy(),
                systemCategory.getId(),
                null,
                new Timestamp(0L),
                system.getVersion(),
                new Timestamp(0L),
                "{}",
                system.getParentSystemId(),
                "{}",
                system.getMergeByName(),
                system.getLinkToSystemId(),
                system.getExternalId(),
                system.getSourceId(),
                system.getExternalName(),
                environments);
        Assert.assertEquals(system.getId(), resultSystem.getId());
        Assert.assertEquals(system.getName(), resultSystem.getName());
        Assert.assertEquals(resultSystem.getConnections().size(), 1);
        Assert.assertEquals(resultSystem.getSystemCategory().getId(), systemCategory.getId());
    }
}
