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

import org.qubership.atp.environments.model.AlertEvent;

public interface AlertEventService {

    @Nullable
    AlertEvent get(@Nonnull UUID alertId, UUID entityId);

    @Nonnull
    List<AlertEvent> getAll();

    @Nonnull
    AlertEvent create(@Nonnull UUID alertId, UUID entityId, String tagList, Integer status);

    @Nonnull
    void update(@Nonnull UUID alertId, UUID entityId, String tagList, Integer status);

    void delete(@Nonnull UUID alertId, UUID entityId);
}
