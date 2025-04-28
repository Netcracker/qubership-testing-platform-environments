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

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.aopalliance.intercept.MethodInvocation;
import org.qubership.atp.environments.model.Identified;

public interface TrackedMethodStrategy {

    Object proceed(MethodInvocation invocation) throws Throwable;

    /**
     * Get first UUID argument.
     *
     * @param invocation MethodInvocation
     * @return UUID argument, IllegalArgumentException otherwise
     */
    default UUID getFirst(MethodInvocation invocation) {
        Optional<UUID> first = Arrays.stream(invocation.getArguments())
                .filter(arg -> arg instanceof Identified || arg instanceof UUID)
                .findFirst()
                .map(firstArg -> firstArg instanceof Identified
                        ? ((Identified) firstArg).getId() : (UUID) firstArg);
        return first.orElseThrow(() ->
                new IllegalArgumentException("Method arguments should contain Identified or UUID for method "
                        + invocation.getMethod()));
    }
}

