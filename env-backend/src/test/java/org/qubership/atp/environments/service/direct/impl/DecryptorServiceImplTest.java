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

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.qubership.atp.crypt.api.Decryptor;
import org.qubership.atp.crypt.exception.AtpDecryptException;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.model.impl.ConnectionImpl;

public class DecryptorServiceImplTest {

    private final ThreadLocal<DecryptorServiceImpl> decryptorService = new ThreadLocal<>();
    private static final String encryptedValue = "Encrypted_value";
    private static ConnectionParameters connectionParameters;

    @BeforeAll
    public static void init() {
        connectionParameters = new ConnectionParameters();
        connectionParameters.put("parameter", "decrypted");
        connectionParameters.put("parameter1", "value");
    }

    @BeforeEach
    public void setUp() throws AtpDecryptException {
        Decryptor decryptor = Mockito.mock(Decryptor.class);
        Mockito.when(decryptor.decryptIfEncrypted("decrypted")).thenReturn(encryptedValue);
        decryptorService.set(new DecryptorServiceImpl(decryptor));
    }

    @Test
    public void decryptParameters_connectionParameters_successful() {
        Assertions.assertEquals(encryptedValue,
                decryptorService.get().decryptParameters(connectionParameters).get("parameter"));
    }

    @Test
    public void decryptConnection_connectionParameters_successful() {
        Connection connection = new ConnectionImpl();
        connection.setParameters(connectionParameters);
        connection.setSourceTemplateId(UUID.randomUUID());
        Assertions.assertEquals(encryptedValue,
                decryptorService.get().decryptConnection(connection).getParameters().get("parameter"));
    }
}
