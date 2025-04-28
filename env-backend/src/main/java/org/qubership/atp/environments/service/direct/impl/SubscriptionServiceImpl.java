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

package org.qubership.atp.environments.service.direct.impl;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.qubership.atp.environments.model.Subscription;
import org.qubership.atp.environments.model.UpdateEvent;
import org.qubership.atp.environments.repo.impl.SubscriptionRepositoryImpl;
import org.qubership.atp.environments.service.direct.SubscriptionService;
import org.qubership.atp.environments.utils.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("subscriptionService")
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepositoryImpl subscriptionRepository;
    private final DateTimeUtil dateTimeUtil;

    @Autowired
    public SubscriptionServiceImpl(SubscriptionRepositoryImpl subscriptionRepository, DateTimeUtil dateTimeUtil) {
        this.subscriptionRepository = subscriptionRepository;
        this.dateTimeUtil = dateTimeUtil;
    }

    @Nullable
    @Override
    public Subscription get(@Nonnull UUID id) {
        return subscriptionRepository.getById(id);
    }

    @Override
    public boolean existsById(@Nonnull UUID id) {
        return subscriptionRepository.existsById(id);
    }

    @Nonnull
    @Override
    public List<Subscription> getAll() {
        return subscriptionRepository.getAll();
    }

    @Nonnull
    @Override
    public List<UUID> getListIdSubscriptionsForProject(@Nonnull UUID projectId) {
        return subscriptionRepository.getListIdSubscriptionsForProject(projectId);
    }

    @Nonnull
    @Override
    public List<UUID> getListIdSubscriptionsForProject(@Nonnull UUID projectId, boolean cascade) {
        return subscriptionRepository.getListIdSubscriptionsForProject(projectId, cascade);
    }

    @Nonnull
    @Override
    public List<UUID> getListIdSubscriptionsForEnvironment(@Nonnull UUID environmentId) {
        return subscriptionRepository.getListIdSubscriptionsForEnvironment(environmentId);
    }

    @Nonnull
    @Override
    public List<UUID> getListIdSubscriptionsForEnvironment(@Nonnull UUID environmentId, boolean cascade) {
        return subscriptionRepository.getListIdSubscriptionsForEnvironment(environmentId, cascade);
    }

    @Nonnull
    @Override
    public List<UUID> getListIdSubscriptionsForSystem(@Nonnull UUID systemId) {
        return subscriptionRepository.getListIdSubscriptionsForSystem(systemId);
    }

    @Nonnull
    @Override
    public List<UUID> getListIdSubscriptionsForSystem(@Nonnull UUID systemId, boolean cascade) {
        return subscriptionRepository.getListIdSubscriptionsForSystem(systemId, cascade);
    }

    @Nonnull
    @Override
    public List<Subscription> getSubscriberSubscriptions(@Nonnull UUID subscriberId) {
        return subscriptionRepository.getSubscriberSubscriptions(subscriberId);
    }

    @Nonnull
    @Override
    public List<Subscription> getProjectSubscriptions(@Nonnull UUID projectId) {
        return subscriptionRepository.getProjectSubscriptions(projectId);
    }

    @Nonnull
    @Override
    public List<Subscription> getEnvironmentSubscriptions(@Nonnull UUID environmentId) {
        return subscriptionRepository.getEnvironmentSubscriptions(environmentId);
    }

    @Nonnull
    @Override
    public List<Subscription> getSystemSubscriptions(@Nonnull UUID systemId) {
        return subscriptionRepository.getSystemSubscriptions(systemId);
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Override
    public Subscription create(Integer subscriptionType,
                               UUID projectId, UUID environmentId, UUID systemId, UUID subscriberId,
                               Integer status, Long lastUpdated) {
        return subscriptionRepository.create(
                subscriptionType, projectId, environmentId, systemId, subscriberId, status,
                dateTimeUtil.timestampAsUtc());
    }

    @Override
    public void update(UUID id, Integer subscriptionType, UUID projectId, UUID environmentId, UUID systemId,
                       UUID subscriberId, Integer status, Long lastUpdated) {
        subscriptionRepository.update(id, subscriptionType, projectId, environmentId, systemId, subscriberId,
                status, dateTimeUtil.timestampAsUtc());
    }

    @Override
    public void delete(UUID subscriptionId) {
        subscriptionRepository.delete(subscriptionId);
    }

    //@Override
    public List<UpdateEvent> getUpdateEvents(UUID subscriptionId) {
        return null;
        //return updateEventRepository.getAllByParentId(subscriptionId);
    }

    @Override
    public List<UUID> getListSubscriptionsByConditions(UUID projectId, UUID environmentId, UUID systemId,
                                                       UUID subscriberId) {
        return subscriptionRepository.getListSubscriptionsByConditions(projectId, environmentId, systemId,
                subscriberId);
    }
}
