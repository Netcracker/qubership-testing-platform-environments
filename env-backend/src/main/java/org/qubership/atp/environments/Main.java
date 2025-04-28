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

package org.qubership.atp.environments;

import org.qubership.atp.auth.springbootstarter.security.oauth2.client.config.annotation.EnableOauth2FeignClientInterceptor;
import org.qubership.atp.auth.springbootstarter.security.oauth2.client.config.annotation.EnableTokenRelayWebClient;
import org.qubership.atp.common.lock.annotation.EnableAtpLockManager;
import org.qubership.atp.common.probes.annotation.EnableProbes;
import org.qubership.atp.crypt.config.annotation.AtpCryptoEnable;
import org.qubership.atp.crypt.config.annotation.AtpDecryptorEnable;
import org.qubership.atp.environments.config.LocaleResolverConfiguration;
import org.qubership.atp.environments.config.SchedulerConfig;
import org.qubership.atp.environments.config.ThreadPoolConfig;
import org.qubership.atp.environments.db.AppConfiguration;
import org.qubership.atp.environments.db.DbConfiguration;
import org.qubership.atp.environments.db.ModificationInterceptorConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan(
        basePackages = {"org.qubership.atp.environments", "org.qubership.atp.auth.springbootstarter"})
@Import({DbConfiguration.class,
        WebMvcAutoConfiguration.class,
        DispatcherServletAutoConfiguration.class,
        ServletWebServerFactoryAutoConfiguration.class,
        AppConfiguration.class,
        ModificationInterceptorConfiguration.class,
        WebSocketServletAutoConfiguration.class,
        ThreadPoolConfig.class,
        SchedulerConfig.class,
        LocaleResolverConfiguration.class
})
@EnableAutoConfiguration
@EnableTokenRelayWebClient
@EnableFeignClients(basePackages = {"org.qubership.atp.integration.configuration.feign",
        "org.qubership.atp.environments.service.rest.client"})
@EnableOauth2FeignClientInterceptor
@EnableScheduling
@EnableCaching
@AtpCryptoEnable
@AtpDecryptorEnable
@EnableAtpLockManager
@EnableProbes
public class Main {

    /**
     * Runs application.
     *
     * @param args args
     * @throws Exception if something went wrong.
     */
    public static void main(String[] args) throws Exception {
        SpringApplicationBuilder app = new SpringApplicationBuilder(Main.class);
        app.build().addListeners(
                new ApplicationPidFileWriter("application.pid")
        );
        app.run(args);
    }
}
