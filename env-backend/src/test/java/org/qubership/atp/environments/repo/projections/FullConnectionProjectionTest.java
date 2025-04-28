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

import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.qubership.atp.environments.mocks.EntitiesGenerator;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.repo.impl.ConnectionRepositoryImpl;

import com.querydsl.core.Tuple;

public class FullConnectionProjectionTest extends AbstractProjectionTest {

    FullConnectionProjection fullConnectionProjection;
    ConnectionRepositoryImpl repo;

    @Before
    public void setUp() {
        repo = mock(ConnectionRepositoryImpl.class);
        fullConnectionProjection = new FullConnectionProjection(repo);
    }

    @Test
    public void testMap_FullConnectionProjection_gotConnection() {
        Connection connection = EntitiesGenerator.generateConnection("Connection");
        connection.setCreated(0L);
        connection.setSystemId(UUID.randomUUID());
        Tuple tuple = mockTupleForConnection(connection);
        Connection resultConnection = fullConnectionProjection.map(tuple);
        Assert.assertEquals(connection.getId(), resultConnection.getId());
        Assert.assertEquals(connection.getName(), resultConnection.getName());
    }


}
