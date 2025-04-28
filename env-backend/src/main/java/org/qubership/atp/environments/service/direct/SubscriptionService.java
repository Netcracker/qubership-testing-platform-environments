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

import org.qubership.atp.environments.model.Subscription;

public interface SubscriptionService extends IdentifiedService<Subscription> {

    @Nonnull
    Subscription create(Integer subscriptionType, UUID projectId, UUID environmentId, UUID systemId, UUID subscriberId,
                        Integer status, Long lastUpdated);

    @Nonnull
    List<UUID> getListIdSubscriptionsForProject(@Nonnull UUID projectId);

    @Nonnull
    List<UUID> getListIdSubscriptionsForProject(@Nonnull UUID projectId, boolean cascade);

    @Nonnull
    List<UUID> getListIdSubscriptionsForEnvironment(@Nonnull UUID environmentId);

    @Nonnull
    List<UUID> getListIdSubscriptionsForEnvironment(@Nonnull UUID environmentId, boolean cascade);

    @Nonnull
    List<UUID> getListIdSubscriptionsForSystem(@Nonnull UUID systemId);

    @Nonnull
    List<UUID> getListIdSubscriptionsForSystem(@Nonnull UUID systemId, boolean cascade);

    @Nonnull
    List<Subscription> getSubscriberSubscriptions(@Nonnull UUID subscriberId);

    @Nonnull
    List<Subscription> getProjectSubscriptions(@Nonnull UUID projectId);

    @Nonnull
    List<Subscription> getEnvironmentSubscriptions(@Nonnull UUID environmentId);

    @Nonnull
    List<Subscription> getSystemSubscriptions(@Nonnull UUID systemId);

    @Nonnull
    void update(UUID id, Integer subscriptionType, UUID projectId, UUID environmentId, UUID systemId, UUID subscriberId,
                Integer status, Long lastUpdated);

    void delete(UUID subscriptionId);

    @Nonnull
    List<UUID> getListSubscriptionsByConditions(UUID projectId, UUID environmentId, UUID systemId,
                                                @Nonnull UUID subscriberId);
}