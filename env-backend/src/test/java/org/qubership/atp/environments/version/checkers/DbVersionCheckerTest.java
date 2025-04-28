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

import static java.lang.String.format;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.qubership.atp.environments.errorhandling.connection.EnvironmentConnectionJdbcUrlFormatException;
import org.qubership.atp.environments.errorhandling.connection.EnvironmentIllegalConnectionTypeException;
import org.qubership.atp.environments.errorhandling.database.EnvironmentUnsupportedDatabaseConnectionFetchException;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.model.impl.ConnectionImpl;

public class DbVersionCheckerTest {

    private final ThreadLocal<java.sql.Connection> connectionSql = new ThreadLocal<>();
    private final ThreadLocal<Statement> statement = new ThreadLocal<>();
    private final ThreadLocal<ResultSet> resultSet = new ThreadLocal<>();
    private final ThreadLocal<DbVersionChecker> dbVersionChecker = new ThreadLocal<>();

    @BeforeEach
    public void setUp() throws Exception {
        java.sql.Connection connectionSqlMock = Mockito.mock(java.sql.Connection.class);
        Statement statementMock = Mockito.mock(Statement.class);
        ResultSet resultSetMock = Mockito.mock(ResultSet.class);
        when(connectionSqlMock.createStatement()).thenReturn(statementMock);
        when(resultSetMock.next()).thenReturn(true).thenReturn(false);

        connectionSql.set(connectionSqlMock);
        statement.set(statementMock);
        resultSet.set(resultSetMock);
        dbVersionChecker.set(new DbVersionChecker());
    }

    @Test
    public void getVersion_PassedVersionReceived_OracleUrlBlank() throws SQLException {
        ConnectionImpl connectionModel = getConnectionModel();
        String test_version = "Test Version";
        when(resultSet.get().getString(eq(1))).thenReturn(test_version);
        when(statement.get().executeQuery(any())).thenReturn(resultSet.get());
        connectionModel.getParameters().put("db_type", "oracle");
        dbVersionChecker.get().setConnectionParameters(connectionModel);

        try (MockedStatic<Executors> executor = Mockito.mockStatic(Executors.class);
             MockedStatic<DriverManager> dmMock = Mockito.mockStatic(DriverManager.class)) {
            dmMock.when(() -> DriverManager.getConnection(any(String.class), any(String.class), any(String.class)))
                    .thenReturn(connectionSql.get());
            ExecutorService executorService = Mockito.mock(ExecutorService.class);
            when(executorService.submit(any(Callable.class))).thenAnswer(invocation -> {
                    final Callable<?> callable = invocation.getArgument(0);
                    Object value = callable.call();
                    return CompletableFuture.completedFuture(value);
            });
            executor.when(Executors::newSingleThreadExecutor).thenReturn(executorService);
            when(DriverManager.getConnection(any(String.class), any(String.class), any(String.class)))
                    .thenReturn(connectionSql.get());
            String version = dbVersionChecker.get().getVersion();
            Assertions.assertEquals(test_version, version);
        }
    }

    @Test
    public void getVersion_PassedVersionReceived_PostgresqlUrlBlank() throws SQLException {
        ConnectionImpl connectionModel = getConnectionModel();
        String test_version = "Test Version";
        when(resultSet.get().getString(eq(1))).thenReturn(test_version);
        when(statement.get().executeQuery(any())).thenReturn(resultSet.get());
        connectionModel.getParameters().put("db_type", "postgresql");
        dbVersionChecker.get().setConnectionParameters(connectionModel);

        try (MockedStatic<Executors> executor = Mockito.mockStatic(Executors.class);
             MockedStatic<DriverManager> dmMock = Mockito.mockStatic(DriverManager.class)) {
            dmMock.when(() -> DriverManager.getConnection(any(String.class), any(String.class), any(String.class)))
                    .thenReturn(connectionSql.get());
            ExecutorService executorService = Mockito.mock(ExecutorService.class);
            when(executorService.submit(any(Callable.class))).thenAnswer(invocation -> {
                final Callable<?> callable = invocation.getArgument(0);
                Object value = callable.call();
                return CompletableFuture.completedFuture(value);
            });
            executor.when(Executors::newSingleThreadExecutor).thenReturn(executorService);
            String version = dbVersionChecker.get().getVersion();
            Assertions.assertEquals(test_version, version);
        }
    }

    @Test
    public void getVersion_PassedVersionReceived_UrlPopulated() throws SQLException {
        ConnectionImpl connectionModel = getConnectionModel();
        String test_version = "Test Version";
        when(resultSet.get().getString(eq(1))).thenReturn(test_version);
        when(statement.get().executeQuery(any())).thenReturn(resultSet.get());
        connectionModel.getParameters().put("db_type", "oracle");
        connectionModel.getParameters().put("jdbc_url", "jdbc_url_example");
        dbVersionChecker.get().setConnectionParameters(connectionModel);

        try (MockedStatic<Executors> executor = Mockito.mockStatic(Executors.class);
             MockedStatic<DriverManager> dmMock = Mockito.mockStatic(DriverManager.class)) {
            dmMock.when(() -> DriverManager.getConnection(any(String.class), any(String.class), any(String.class)))
                    .thenReturn(connectionSql.get());
            ExecutorService executorService = Mockito.mock(ExecutorService.class);
            when(executorService.submit(any(Callable.class))).thenAnswer(invocation -> {
                final Callable<?> callable = invocation.getArgument(0);
                Object value = callable.call();
                return CompletableFuture.completedFuture(value);
            });
            executor.when(Executors::newSingleThreadExecutor).thenReturn(executorService);
            String version = dbVersionChecker.get().getVersion();
            Assertions.assertEquals(test_version, version);
        }
    }

    @Test
    public void getVersion_ReturnUnknown_ExceptionSQL() throws SQLException {
        ConnectionImpl connectionModel = getConnectionModel();
        when(statement.get().executeQuery(any())).thenThrow(new SQLException("Test"));
        connectionModel.getParameters().put("db_type", "oracle");
        connectionModel.getParameters().put("jdbc_url", "jdbc_url_example");
        dbVersionChecker.get().setConnectionParameters(connectionModel);

        try (MockedStatic<DriverManager> dmMock = Mockito.mockStatic(DriverManager.class)) {
            dmMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(connectionSql.get());
            Assertions.assertThrows(RuntimeException.class, () -> dbVersionChecker.get().getVersion());
        }
    }

    @Test
    public void getVersion_ThrowException_CannotCheckVersionCassandra() {
        ConnectionImpl connectionModel = getConnectionModel();
        connectionModel.getParameters().put("db_type", "cassandra");
        dbVersionChecker.get().setConnectionParameters(connectionModel);


        try (MockedStatic<DriverManager> dmMock = Mockito.mockStatic(DriverManager.class)) {
            dmMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(connectionSql.get());
            EnvironmentUnsupportedDatabaseConnectionFetchException exception =
                    Assertions.assertThrows(EnvironmentUnsupportedDatabaseConnectionFetchException.class,
                            () -> dbVersionChecker.get().getVersion());
            String expectedErrorMessage =
                    format(EnvironmentUnsupportedDatabaseConnectionFetchException.DEFAULT_MESSAGE, "cassandra");
            Assertions.assertEquals(expectedErrorMessage, exception.getMessage());
        }
    }

    @Test
    public void getVersion_ThrowException_CannotCheckVersionHive2() {
        ConnectionImpl connectionModel = getConnectionModel();
        connectionModel.getParameters().put("db_type", "hive2");
        dbVersionChecker.get().setConnectionParameters(connectionModel);

        try (MockedStatic<DriverManager> dmMock = Mockito.mockStatic(DriverManager.class)) {
            dmMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(connectionSql.get());
            EnvironmentUnsupportedDatabaseConnectionFetchException exception =
                    Assertions.assertThrows(EnvironmentUnsupportedDatabaseConnectionFetchException.class,
                            () -> dbVersionChecker.get().getVersion());
            String expectedErrorMessage =
                    format(EnvironmentUnsupportedDatabaseConnectionFetchException.DEFAULT_MESSAGE, "hive2");
            Assertions.assertEquals(expectedErrorMessage, exception.getMessage());
        }
    }

    @Test
    public void getVersion_ThrowException_CannotCheckVersionMongo() {
        ConnectionImpl connectionModel = getConnectionModel();
        connectionModel.getParameters().put("db_type", "mongo");
        dbVersionChecker.get().setConnectionParameters(connectionModel);

        try (MockedStatic<DriverManager> dmMock = Mockito.mockStatic(DriverManager.class)) {
            dmMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(connectionSql.get());
            EnvironmentUnsupportedDatabaseConnectionFetchException exception =
                    Assertions.assertThrows(EnvironmentUnsupportedDatabaseConnectionFetchException.class,
                            () -> dbVersionChecker.get().getVersion());
            String expectedErrorMessage =
                    format(EnvironmentUnsupportedDatabaseConnectionFetchException.DEFAULT_MESSAGE, "mongo");
            Assertions.assertEquals(expectedErrorMessage, exception.getMessage());
        }
    }

    @Test
    public void getVersion_ThrowException_CannotCheckVersionMysql() {
        ConnectionImpl connectionModel = getConnectionModel();
        connectionModel.getParameters().put("db_type", "mysql");
        dbVersionChecker.get().setConnectionParameters(connectionModel);

        try (MockedStatic<DriverManager> dmMock = Mockito.mockStatic(DriverManager.class)) {
            dmMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(connectionSql.get());
            EnvironmentUnsupportedDatabaseConnectionFetchException exception =
                    Assertions.assertThrows(EnvironmentUnsupportedDatabaseConnectionFetchException.class,
                            () -> dbVersionChecker.get().getVersion());
            String expectedErrorMessage =
                    format(EnvironmentUnsupportedDatabaseConnectionFetchException.DEFAULT_MESSAGE, "mysql");
            Assertions.assertEquals(expectedErrorMessage, exception.getMessage());
        }
    }

    @Test
    public void getVersion_ThrowException_CannotFindDriver() {
        ConnectionImpl connectionModel = getConnectionModel();
        connectionModel.getParameters().put("db_type", "testType");
        connectionModel.getParameters().put("jdbc_url", "jdbc_url_example");
        dbVersionChecker.get().setConnectionParameters(connectionModel);

        try (MockedStatic<DriverManager> dmMock = Mockito.mockStatic(DriverManager.class)) {
            dmMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(connectionSql.get());
            EnvironmentIllegalConnectionTypeException exception =
                    Assertions.assertThrows(EnvironmentIllegalConnectionTypeException.class,
                            () -> dbVersionChecker.get().getVersion());
            String expectedErrorMessage = String.format(EnvironmentIllegalConnectionTypeException.DEFAULT_MESSAGE, "testType");
            Assertions.assertEquals(expectedErrorMessage, exception.getMessage());
        }
    }

    @Test
    public void setConnectionParameters_ThrowException_UrlBlankAndNotFormed() {
        ConnectionImpl connectionModel = getConnectionModel();
        connectionModel.getParameters().put("db_type", "test type");

        try (MockedStatic<DriverManager> dmMock = Mockito.mockStatic(DriverManager.class)) {
            dmMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(connectionSql.get());
            EnvironmentConnectionJdbcUrlFormatException exception =
                    Assertions.assertThrows(EnvironmentConnectionJdbcUrlFormatException.class,
                            () -> dbVersionChecker.get().setConnectionParameters(connectionModel));
            String expectedErrorMessage = String.format(EnvironmentConnectionJdbcUrlFormatException.DEFAULT_MESSAGE, "test type");
            Assertions.assertEquals(expectedErrorMessage, exception.getMessage());
        }
    }

    private ConnectionImpl getConnectionModel() {
        ConnectionImpl connectionModel = new ConnectionImpl();
        connectionModel.setParameters(new ConnectionParameters());
        connectionModel.getParameters().putAll(
                Stream.of(new String[][]{
                                {"db_host", "db_host_example"},
                                {"db_port", "5432"},
                                {"db_login", "login"},
                                {"db_password", "password"},
                                {"db_name", "name"}
                        })
                        .collect(HashMap::new, (m, v) -> m.put(v[0], v[1]), HashMap::putAll));
        return connectionModel;
    }
}
