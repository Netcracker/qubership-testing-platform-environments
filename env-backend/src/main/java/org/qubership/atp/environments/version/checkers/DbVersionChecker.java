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

package org.qubership.atp.environments.version.checkers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;
import org.qubership.atp.environments.errorhandling.AtpEnvironmentException;
import org.qubership.atp.environments.errorhandling.connection.EnvironmentConnectionJdbcUrlFormatException;
import org.qubership.atp.environments.errorhandling.connection.EnvironmentIllegalConnectionTypeException;
import org.qubership.atp.environments.errorhandling.database.EnvironmentDatabaseDriverLoadException;
import org.qubership.atp.environments.errorhandling.database.EnvironmentDatabaseVersionCheckException;
import org.qubership.atp.environments.errorhandling.database.EnvironmentUnsupportedDatabaseConnectionFetchException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DbVersionChecker implements VersionChecker {

    private String url;
    private String host;
    private String port;
    private String name;
    private String login;
    private String password;
    private String type;
    private String parameters;

    @Override
    public void setConnectionParameters(org.qubership.atp.environments.model.Connection parameters) {
        this.host = parameters.getParameters().get("db_host");
        this.port = parameters.getParameters().get("db_port");
        this.name = parameters.getParameters().get("db_name");
        this.login = parameters.getParameters().get("db_login");
        this.password = parameters.getParameters().get("db_password");
        this.type = !StringUtils.isNotBlank(parameters.getParameters().get("db_type")) ? null :
                parameters.getParameters().get("db_type");
        if (StringUtils.isNotBlank(parameters.getParameters().get("jdbc_url"))) {
            this.url = parameters.getParameters().get("jdbc_url");
        } else {
            this.url = getJdbcType(type);
        }
    }

    private String getJdbcType(String type) {
        if ("oracle".equals(type)) {
            return "jdbc:" + type + ":thin:@" + host + ":" + port + "/" + name;
        } else if ("postgresql".equals(type)) {
            return "jdbc:" + type + "://" + host + ":" + port + "/" + name;
        } else if ("cassandra".equals(type)) {
            return "jdbc:" + type + "://" + host + ":" + port + "/" + name;
        } else if ("mysql".equals(type)) {
            return "jdbc:" + type + "://" + host + ":" + port + "/" + name;
        } else if ("mongo".equals(type)) {
            return "jdbc:" + type + "://" + host + ":" + port + "/" + name;
        } else if ("hive2".equals(type)) {
            return "jdbc:" + type + "://" + host + ":" + port + "/" + name;
        } else if ("bigquery".equals(type)) {
            return "jdbc:" + type + "://" + host + ":" + port;
        } else {
            log.error("Failed to create jdbc url for database type: {}", type);
            throw new EnvironmentConnectionJdbcUrlFormatException(type);
        }
    }

    @Override
    public void setParametersVersionCheck(String parameters) {
        this.parameters = parameters;
    }

    @SuppressFBWarnings("SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE")
    @Override
    public String getVersion() {
        String version = null;
        Connection dbConnection = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        try {
            dbConnection = getDBConnection(this.type);
            stmt = dbConnection.createStatement();
            resultSet = stmt.executeQuery(this.parameters);
            while (resultSet.next()) {
                version = resultSet.getString(1);
            }
            dbConnection.close();
        } catch (AtpEnvironmentException e) {
            log.error("Error occurred while executing DbChecker", e);
            throw e;
        } catch (SQLException e) {
            log.error("Failed to get database version for type '{}' by sql", this.type, e);
            throw new EnvironmentDatabaseVersionCheckException();
        } finally {
            closeSafely(resultSet);
            closeSafely(stmt);
            closeSafely(dbConnection);
        }
        return !StringUtils.isBlank(version) ? version : "Unknown";
    }

    private void closeSafely(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception exception) {
                log.error("Error while occurred closing database recourse", exception);
            }
        }
    }

    private Connection getDBConnection(String type) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Connection> future = executor.submit(() -> DriverManager.getConnection(url, login, password));
        Connection connection = null;
        try {
            switch (type) {
                case "oracle":
                    Class.forName("oracle.jdbc.driver.OracleDriver");
                    break;
                case "postgresql":
                    Class.forName("org.postgresql.Driver");
                    break;
                case "cassandra":
                case "mysql":
                case "mongo":
                case "hive2":
                case "bigquery":
                    log.error("Obtaining database connection for type '{}' is unsupported", type);
                    throw new EnvironmentUnsupportedDatabaseConnectionFetchException(type);
                default:
                    log.error("Failed to find connection for type: {}", type);
                    throw new EnvironmentIllegalConnectionTypeException(type);
            }
            connection = future.get(5, TimeUnit.SECONDS);
        } catch (ClassNotFoundException | InterruptedException | ExecutionException | TimeoutException ex) {
            log.error("Failed to load database driver for type: {}", type, ex);
            throw new EnvironmentDatabaseDriverLoadException(type);
        }
        executor.shutdownNow();
        return connection;
    }
}
