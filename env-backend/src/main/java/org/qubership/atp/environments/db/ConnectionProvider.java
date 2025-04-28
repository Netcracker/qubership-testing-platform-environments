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

package org.qubership.atp.environments.db;

import java.sql.Connection;

import javax.inject.Provider;
import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DataSourceUtils;

import lombok.SneakyThrows;

public class ConnectionProvider implements Provider<Connection> {

    private final DataSource dataSource;

    public ConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * The method get connection and checks if it is transactional.
     *
     * @return connection
     */
    @SneakyThrows
    public Connection get() {
        Connection connection = DataSourceUtils.getConnection(this.dataSource);
        if (!DataSourceUtils.isConnectionTransactional(connection, this.dataSource)) {
            connection.close();
            connection = dataSource.getConnection();
        }
        return connection;
    }
}
