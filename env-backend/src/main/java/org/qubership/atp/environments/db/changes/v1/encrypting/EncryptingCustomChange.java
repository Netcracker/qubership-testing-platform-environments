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

import org.qubership.atp.environments.config.BeanAwareSpringLiquibaseConfiguration;
import org.qubership.atp.environments.repo.impl.ConnectionRepositoryImpl;
import org.qubership.atp.environments.service.direct.EncryptorService;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class EncryptingCustomChange implements CustomTaskChange {

    @SneakyThrows
    @Override
    @SuppressWarnings("CPD-START")
    public void execute(Database database) {
            EncryptorService encryptor = BeanAwareSpringLiquibaseConfiguration.getBean(EncryptorService.class);
            ConnectionRepositoryImpl connectionRepository = BeanAwareSpringLiquibaseConfiguration
                    .getBean(ConnectionRepositoryImpl.class);
            EncryptingCustomTask encryptingCustomTask = new EncryptingCustomTask(encryptor, connectionRepository);
            encryptingCustomTask.execute();
    }

    @Override
    public String getConfirmationMessage() {
        return null;
    }

    @Override
    public void setUp() throws SetupException {
    }

    @Override
    public void setFileOpener(ResourceAccessor accessor) {
    }

    @SneakyThrows
    @Override
    public ValidationErrors validate(Database database) {
        return null;
    }
}
