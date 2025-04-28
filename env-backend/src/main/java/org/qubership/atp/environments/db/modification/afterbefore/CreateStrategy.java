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

package org.qubership.atp.environments.db.modification.afterbefore;

import java.util.UUID;

import org.aopalliance.intercept.MethodInvocation;
import org.qubership.atp.environments.db.modification.EntityTypeStrategy;
import org.qubership.atp.environments.db.modification.ModificationInterceptor;
import org.qubership.atp.environments.errorhandling.internal.EnvironmentNotIdentifiableObjectException;
import org.qubership.atp.environments.model.Identified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreateStrategy implements TrackedMethodStrategy {

    private static Logger LOG = LoggerFactory.getLogger(CreateStrategy.class);

    private final ModificationInterceptor interceptor;
    private final EntityTypeStrategy idsStrategy;

    public CreateStrategy(ModificationInterceptor interceptor, EntityTypeStrategy idsStrategy) {
        this.interceptor = interceptor;
        this.idsStrategy = idsStrategy;
    }

    @Override
    public Object proceed(MethodInvocation invocation) throws Throwable {
        Object result = invocation.proceed();
        try {
            UUID id;
            if (result instanceof Identified) {
                id = ((Identified) result).getId();
            } else if (result instanceof UUID) {
                id = (UUID) result;
            } else {
                log.error("Returned object expected to be Identified or UUID, but was: {}", result.getClass());
                throw new EnvironmentNotIdentifiableObjectException();
            }
            idsStrategy.prepareNotification(id, interceptor).run();
        } catch (Exception e) {
            LOG.error("Can not handle event for " + invocation.getMethod().getName(), e);
        }
        return result;
    }
}
