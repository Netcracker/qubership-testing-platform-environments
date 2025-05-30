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

import org.qubership.atp.environments.service.rest.gracefulshutdown.GracefulUndertowShutdown;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;

public class GracefulShutdownConfiguration {

    @Bean
    public GracefulUndertowShutdown gracefulUndertowShutdown() {
        return new GracefulUndertowShutdown();
    }

    /**
     * Return {@link WebServerFactoryCustomizer} by specified parameters.
     *
     * @return {@link WebServerFactoryCustomizer} by specified parameters
     */
    @Bean
    public WebServerFactoryCustomizer<UndertowServletWebServerFactory> undertowCustomizer(
            GracefulUndertowShutdown gracefulUndertowShutdown) {
        return (factory) -> {
            factory.addDeploymentInfoCustomizers((builder) -> {
                builder.addInitialHandlerChainWrapper(gracefulUndertowShutdown);
            });
        };
    }
}
