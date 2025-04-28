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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateStrategy implements TrackedMethodStrategy {

    private static Logger LOG = LoggerFactory.getLogger(UpdateStrategy.class);

    private final ModificationInterceptor interceptor;
    private final EntityTypeStrategy idsStrategy;

    public UpdateStrategy(ModificationInterceptor interceptor, EntityTypeStrategy idsStrategy) {
        this.interceptor = interceptor;
        this.idsStrategy = idsStrategy;
    }

    @Override
    public Object proceed(MethodInvocation invocation) throws Throwable {
        try {
            UUID id = getFirst(invocation);
            idsStrategy.prepareNotification(id, interceptor).run();
        } catch (Exception e) {
            LOG.error("Can not handle event for " + invocation.getMethod().getName(), e);
        }
        Object result = invocation.proceed();
        return result;
    }
}
