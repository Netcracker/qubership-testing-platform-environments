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

package org.qubership.atp.environments.db.changes.v1.encrypting;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.qubership.atp.crypt.api.CryptoProvider;
import org.qubership.atp.crypt.provider.BouncyCastleProvider;
import org.qubership.atp.environments.errorhandling.connection.EnvironmentSqlConnectionParamEncryptException;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.repo.impl.ConnectionRepositoryImpl;
import org.qubership.atp.environments.service.direct.EncryptorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class EncryptingCustomTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptingCustomTask.class);
    private final EncryptorService encryptor;
    private final ConnectionRepositoryImpl connectionRepository;
    private final CryptoProvider cryptoProvider = new BouncyCastleProvider();
    private final List<String> encryptValues = Arrays.asList("password", "passphrase", "key", "token", "credentials",
            "authentication");

    public EncryptingCustomTask(EncryptorService encryptor, ConnectionRepositoryImpl connectionRepository) {
        this.encryptor = encryptor;
        this.connectionRepository = connectionRepository;
    }

    /**
     * Task change execution.
     */
    public void execute() {
        LOGGER.info("Starting migration encrypting");
        Map<UUID, ConnectionParameters> templateMap = connectionRepository.getConnectionTemplates()
                .stream().collect(Collectors.toMap(Connection::getId, Connection::getParameters));
        connectionRepository.getAll()
                .stream()
                .filter(connection -> connection.getSourceTemplateId() != null)
                .forEach(connection -> {
                    try {
                        ConnectionParameters connectionParameters = connection.getParameters();
                        for (Map.Entry<String, String> parameter : connectionParameters.entrySet()) {
                            if (!StringUtils.isEmpty(parameter.getValue())
                                    && !isEncrypted(parameter.getValue())
                                    && templateMap.containsKey(connection.getSourceTemplateId())
                                    && templateMap.get(connection.getSourceTemplateId())
                                    .containsKey(parameter.getKey())
                                    && encryptValues.stream()
                                    .anyMatch(encryptValue -> parameter.getKey().contains(encryptValue))) {
                                connectionParameters.put(parameter.getKey(),
                                        encryptor.encryptString(parameter.getValue().trim()));
                                connectionRepository.getUpdateParametersQuery(connection.getId(),
                                        connectionParameters).execute();
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error("Error while encrypting parameters of SQL connection [id: {}, name: {}]",
                                connection.getId(), connection.getName(), e);
                        throw new EnvironmentSqlConnectionParamEncryptException();
                    }
                });
        LOGGER.info("Migration encrypting finished");
    }

    public boolean isEncrypted(String value) {
        return cryptoProvider.isEncrypted(value);
    }
}
