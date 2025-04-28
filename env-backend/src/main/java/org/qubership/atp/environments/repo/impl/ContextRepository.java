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

package org.qubership.atp.environments.repo.impl;

import javax.annotation.Nonnull;

import org.qubership.atp.environments.model.impl.Context;
import org.springframework.stereotype.Repository;

@Repository
public class ContextRepository {

    private final ThreadLocal<Context> contexts = new ThreadLocal<>();

    /**
     * Get context.
     * @return {@link Context} instance of current thread.
     */
    @Nonnull
    public Context getContext() {
        if (contexts.get() == null) {
            setContext(new Context(true));
        }
        return contexts.get();
    }

    /**
     * Set context.
     * @param context {@link Context} instance of current thread.
     */
    public void setContext(@Nonnull Context context) {
        contexts.set(context);
    }

    /**
     * Remove context of current thread.
     */
    public void removeContext() {
        contexts.remove();
    }
}
