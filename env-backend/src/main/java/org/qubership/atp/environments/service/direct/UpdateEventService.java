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

package org.qubership.atp.environments.service.direct;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.qubership.atp.environments.model.UpdateEvent;

public interface UpdateEventService {

    @Nullable
    UpdateEvent get(@Nonnull UUID subscriptionId, @Nonnull UUID entityId);

    @Nullable
    UpdateEvent getSubscriptionUpdateEvents(@Nonnull UUID subscriptionId);

    @Nullable
    UpdateEvent getEntityUpdateEvents(@Nonnull UUID entityId);

    @Nonnull
    List<UpdateEvent> getAll();

    @Nonnull
    UpdateEvent create(@Nonnull UUID subscriptionId, @Nonnull UUID entityId, String tagList, Integer status,
                       String entityType);

    @Nonnull
    void update(UUID subscriptionId, UUID entityId, String tagList, Integer status, String entityType);

    void delete(UUID subscriptionId, UUID entityId);
}
