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

import org.qubership.atp.environments.db.modification.ModificationInterceptor;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModificationInterceptorConfiguration {
    private static final String INTERCEPTOR_ID = "modificationInterceptor";

    /**
     * Return {@link ModificationInterceptor} by specified parameters.
     *
     * @return {@link ModificationInterceptor} by specified parameters
     */
    @Bean(name = INTERCEPTOR_ID)
    public ModificationInterceptor modificationInterceptor() {
        ModificationInterceptor interceptor =
                new ModificationInterceptor();
        return interceptor;
    }

    /**
     * Return {@link BeanNameAutoProxyCreator} by specified parameters.
     *
     * @return {@link BeanNameAutoProxyCreator} by specified parameters
     */
    @Bean
    public BeanNameAutoProxyCreator autoProxyCreator() {
        BeanNameAutoProxyCreator proxyCreator = new BeanNameAutoProxyCreator();
        proxyCreator.setBeanNames(new String[]{"connectionService", "projectService", "systemService",
                "environmentService"});
        proxyCreator.setInterceptorNames(new String[]{INTERCEPTOR_ID});
        return proxyCreator;
    }
}
