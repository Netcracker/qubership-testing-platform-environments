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
import java.sql.SQLException;
import java.util.function.Supplier;

import javax.sql.DataSource;

import org.qubership.atp.environments.errorhandling.connection.EnvironmentSqlConnectionCloseException;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.querydsl.sql.AbstractSQLQuery;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLBaseListener;
import com.querydsl.sql.SQLListenerContext;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.spring.SpringConnectionProvider;
import com.querydsl.sql.spring.SpringExceptionTranslator;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;

@org.springframework.context.annotation.Configuration
@EnableTransactionManagement
@Slf4j
public class DbConfiguration {

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * Get query dsl configuration.
     */
    @Bean
    public Configuration querydslConfiguration(DataSource dataSource) {
        SQLTemplates templates = PostgreSQLTemplates.builder().build();
        Configuration configuration = new Configuration(templates);
        configuration.setExceptionTranslator(new SpringExceptionTranslator());
        configuration.addListener(new ConnectionClosedListener(dataSource));
        return configuration;
    }

    @Bean
    public SQLQueryFactory queryFactory(DataSource dataSource, Configuration configuration) {
        Supplier<Connection> springConnectionProvider = new SpringConnectionProvider(dataSource);
        return new SQLQueryFactory(configuration, springConnectionProvider);
    }

    private static class ConnectionClosedListener extends SQLBaseListener {

        private final DataSource dataSource;

        public ConnectionClosedListener(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        public void end(SQLListenerContext context) {
            Connection connection = context.getConnection();
            if (connection != null
                    && !DataSourceUtils.isConnectionTransactional(connection, this.dataSource)
                    && context.getData(AbstractSQLQuery.class.getName() + "#PARENT_CONTEXT") == null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.error("Failed to close SQL connection", e);
                    throw new EnvironmentSqlConnectionCloseException();
                }
            }
        }
    }

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(dataSource);
    }
}
