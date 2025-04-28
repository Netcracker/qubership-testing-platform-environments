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

package org.qubership.atp.environments.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.core.HazelcastInstance;
import liquibase.integration.spring.SpringLiquibase;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Configuration
@EnableConfigurationProperties(LiquibaseProperties.class)
@Slf4j
public class SpringLiquibaseConfig {

    @Value("${spring.application.name}")
    private String serviceName;
    @Value("${service.entities.migration.enabled:false}")
    private String migrationEnabled;

    private DataSource dataSource;
    private LiquibaseProperties properties;

    public SpringLiquibaseConfig(DataSource dataSource, LiquibaseProperties properties) {
        this.dataSource = dataSource;
        this.properties = properties;
    }

    /**
     * Create spring liquibase config.
     */
    @Bean
    public SpringLiquibase liquibase(@Autowired(required = false)
                                     @Qualifier("hazelcastClient") HazelcastInstance hazelcastClient) {
        HazelcastConfig.clearHazelcastMap(hazelcastClient);
        SpringLiquibase liquibase = new BeanAwareSpringLiquibaseConfiguration();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(this.properties.getChangeLog());
        liquibase.setContexts(this.properties.getContexts());
        liquibase.setDefaultSchema(this.properties.getDefaultSchema());
        liquibase.setDropFirst(this.properties.isDropFirst());
        liquibase.setShouldRun(this.properties.isEnabled());
        liquibase.setLabels(this.properties.getLabels());
        Map<String, String> props = this.properties.getParameters();
        if (props == null) {
            props = new HashMap<>();
        }
        props.put("spring.application.name", serviceName);
        props.put("service.entities.migration.enabled", migrationEnabled);
        liquibase.setChangeLogParameters(props);
        liquibase.setRollbackFile(this.properties.getRollbackFile());
        return liquibase;
    }
}
