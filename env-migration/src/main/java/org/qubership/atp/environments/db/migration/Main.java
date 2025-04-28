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

package org.qubership.atp.environments.db.migration;

import java.sql.SQLException;

import org.qubership.atp.environments.db.migration.classloader.ParentLastClassloader;
import org.qubership.atp.environments.db.migration.configuration.DbConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.google.common.base.Strings;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

@Configuration
@ComponentScan(basePackages = {"org.qubership.atp.environments"})
@Import({DbConfiguration.class,
        WebMvcAutoConfiguration.class,
        DispatcherServletAutoConfiguration.class,
        ServletWebServerFactoryAutoConfiguration.class
})
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    /**
     * Runs db migration.
     *
     * @param args expected empty
     * @throws SQLException       if something went wrong
     * @throws LiquibaseException if something went wrong with migration
     */
    public static void main(String[] args) throws SQLException, LiquibaseException {
        ConfigurableApplicationContext configurableApplicationContext = SpringApplication.run(Main.class, args);
        String sourcePath = System.getProperty("lb.libs.path", "env-scripts/src/main/scripts");
        String jdbcType = System.getProperty("jdbc_type");
        if (Strings.isNullOrEmpty(jdbcType)) {
            throw new IllegalStateException("You haven't specified system property 'jdbc_type'");
        }
        ParentLastClassloader customClassLoader = new ParentLastClassloader(sourcePath, jdbcType);
        try {
            LiquibaseFactory lbFactory = configurableApplicationContext.getBean(LiquibaseFactory.class);
            Liquibase lb = lbFactory.create("install.xml");
            if (Boolean.getBoolean("drop.database.for.tests")) {
                LOGGER.info("drop database because of flag (drop.database.for.tests)");
                lb.dropAll();
            }
            lb.update(new Contexts(), new LabelExpression());
            lb = lbFactory.create("update.xml", new ClassLoaderResourceAccessor(customClassLoader));
            lb.update(new Contexts(), new LabelExpression());
            configurableApplicationContext.close();
        } catch (Throwable e) {
            LOGGER.error("Uncaught exception", e);
            throw e;
        }
    }

}
