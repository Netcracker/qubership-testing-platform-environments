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

import java.util.Objects;

import javax.annotation.Nonnull;

import org.qubership.atp.crypt.api.Decryptor;
import org.qubership.atp.crypt.exception.AtpDecryptException;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.model.impl.ConnectionImpl;
import org.qubership.atp.environments.service.direct.DecryptorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("decryptorService")
public class DecryptorServiceImpl implements DecryptorService {

    protected Decryptor decryptor;

    @Autowired
    public DecryptorServiceImpl(@Nonnull Decryptor decryptor) {
        this.decryptor = decryptor;
    }

    @Override
    public ConnectionParameters decryptParameters(ConnectionParameters parameters) {
        ConnectionParameters result = new ConnectionParameters();
        parameters.forEach((key, value) -> {
            String parameter = decryptParameter(value);
            result.put(key, parameter);
        });
        return result;
    }

    @Override
    public String decryptParameter(String sourceParameter) {
        String decryptedValue = sourceParameter;
        try {
            decryptedValue = decryptor.decryptIfEncrypted(sourceParameter);
        } catch (AtpDecryptException e) {
            log.error("Unable to decrypt: ", e);
        }
        return decryptedValue;
    }

    @Override
    public Connection decryptConnection(Connection connection) {
        ConnectionParameters parameters = decryptParameters(connection.getParameters());
        return new ConnectionImpl(connection.getId(), connection.getName(), connection.getDescription(),
                parameters, connection.getCreated(), connection.getCreatedBy(),
                connection.getModified(), connection.getModifiedBy(),
                connection.getSystemId(), connection.getConnectionType(),
                Objects.requireNonNull(connection.getSourceTemplateId()),
                connection.getServices(), connection.getSourceId());
    }
}
