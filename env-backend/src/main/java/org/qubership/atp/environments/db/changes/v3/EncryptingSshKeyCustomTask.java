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

package org.qubership.atp.environments.db.changes.v3;

import org.qubership.atp.environments.errorhandling.ssh.EnvironmentSqlConnectionSshKeyEncryptException;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.model.utils.Constants;
import org.qubership.atp.environments.repo.impl.ConnectionRepositoryImpl;
import org.qubership.atp.environments.service.direct.EncryptorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class EncryptingSshKeyCustomTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptingSshKeyCustomTask.class);
    private final EncryptorService encryptor;
    private final ConnectionRepositoryImpl connectionRepository;
    private final String sshKeyParameterName = "ssh_key";

    public EncryptingSshKeyCustomTask(EncryptorService encryptor, ConnectionRepositoryImpl connectionRepository) {
        this.encryptor = encryptor;
        this.connectionRepository = connectionRepository;
    }

    /**
     * Task change execution.
     */
    public void execute() {
        LOGGER.info("Starting migration encrypting of ssh key");
        connectionRepository.getAllByTemplateId(Constants.Environment.System.Connection.SSH)
                .forEach(connection -> {
                    try {
                        ConnectionParameters parameters = connection.getParameters();
                        if (parameters.containsKey(sshKeyParameterName)
                                && !StringUtils.isEmpty(parameters.get(sshKeyParameterName))) {
                            parameters.put(sshKeyParameterName,
                                    encryptor.encryptString(parameters.get(sshKeyParameterName)));
                            connectionRepository.getUpdateParametersQuery(connection.getId(),
                                    parameters).execute();
                        }
                    } catch (Exception e) {
                        LOGGER.error("Error while encrypting ssh key of Connection [id: {}, name: {}]",
                                connection.getId(), connection.getName(), e);
                        throw new EnvironmentSqlConnectionSshKeyEncryptException();
                    }
                });
        LOGGER.info("Migration encrypting of ssh key finished");
    }
}
