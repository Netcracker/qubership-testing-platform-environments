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

package org.qubership.atp.environments.migration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.qubership.atp.crypt.exception.AtpEncryptException;
import org.qubership.atp.environments.db.changes.v1.encrypting.EncryptingCustomTask;
import org.qubership.atp.environments.mocks.EntitiesGenerator;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.repo.impl.ConnectionRepositoryImpl;
import org.qubership.atp.environments.service.direct.EncryptorService;
import org.qubership.atp.environments.utils.TestEntityUtils;

import com.querydsl.sql.dml.SQLUpdateClause;

public class EncryptingCustomChangeTest {

    private final ThreadLocal<EncryptorService> encryptor = new ThreadLocal<>();
    private final ThreadLocal<ConnectionRepositoryImpl> connectionRepository = new ThreadLocal<>();

    private static final String encryptTestFlag = "ENCRYPTED";

    List<Connection> connectionList;
    @BeforeEach
    public void setUp() {
        encryptor.set(mock(EncryptorService.class));
        connectionRepository.set(mock(ConnectionRepositoryImpl.class));
        connectionList = TestEntityUtils.createConnectionList();
        connectionList.forEach(connection -> connection.getParameters().put("password","value"));
    }

    @Test
    public void encryptAllConnections_withSecretParameters_encryptedCorrect() throws AtpEncryptException {
        Mockito.when(connectionRepository.get().getAll()).thenReturn(connectionList);
        Mockito.when(connectionRepository.get().getUpdateParametersQuery(any(), any())).thenReturn(mock(SQLUpdateClause.class));
        Mockito.when(connectionRepository.get().getConnectionTemplates()).thenReturn(EntitiesGenerator.generateConnectionTemplates());
        Mockito.when(encryptor.get().encryptString(anyString())).thenReturn(encryptTestFlag);
        EncryptingCustomTask encryptingCustomTask = new EncryptingCustomTask(encryptor.get(), connectionRepository.get());
        encryptingCustomTask.execute();
        Assertions.assertEquals(connectionList.get(0).getParameters().get("password"), encryptTestFlag);
    }

}
