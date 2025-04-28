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

package org.qubership.atp.environments.model.impl;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.qubership.atp.environments.model.Subscriber;
import org.qubership.atp.environments.model.Subscription;
import org.qubership.atp.environments.model.UpdateEvent;

public class SubscriptionImpl extends AbstractIdentified implements Subscription {

    private List<UpdateEvent> updateEvents;
    private Integer subscriptionType;
    private UUID projectId;
    private UUID environmentId;
    private UUID systemId;
    private Subscriber subscriber;
    private Integer status;
    private Long lastUpdated;

    public SubscriptionImpl() {
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    public SubscriptionImpl(UUID uuid, Integer subscriptionType,
                            UUID projectId, UUID environmentId, UUID systemId, Subscriber subscriber,
                            Integer status, Long lastUpdated, List<UpdateEvent> updateEvents) {
        setId(uuid);
        setSubscriptionType(subscriptionType);
        setProjectId(projectId);
        setEnvironmentId(environmentId);
        setSystemId(systemId);
        setSubscriber(subscriber);
        setStatus(status);
        setLastUpdated(lastUpdated);
        setUpdateEvents(updateEvents);
    }

    @Override
    @Nonnull
    public Integer getSubscriptionType() {
        return subscriptionType;
    }

    @Override
    public void setSubscriptionType(@Nonnull Integer subscriptionType) {
        this.subscriptionType = subscriptionType;
    }

    @Override
    public UUID getProjectId() {
        return projectId;
    }

    @Override
    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    @Override
    public UUID getEnvironmentId() {
        return environmentId;
    }

    @Override
    public void setEnvironmentId(UUID environmentId) {
        this.environmentId = environmentId;
    }

    @Override
    public UUID getSystemId() {
        return systemId;
    }

    @Override
    public void setSystemId(UUID systemId) {
        this.systemId = systemId;
    }

    @Nonnull
    @Override
    public Subscriber getSubscriber() {
        return subscriber;
    }

    @Override
    public void setSubscriber(@Nonnull Subscriber subscriber) {
        this.subscriber = subscriber;
    }

    @Override
    public Integer getStatus() {
        return status;
    }

    @Override
    public void setStatus(@Nonnull Integer status) {
        this.status = status;
    }

    @Override
    @Nullable
    public Long getLastUpdated() {
        return lastUpdated;
    }

    @Override
    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public List<UpdateEvent> getUpdateEvents() {
        return updateEvents;
    }

    @Override
    public void setUpdateEvents(List<UpdateEvent> updateEvents) {
        this.updateEvents = updateEvents;
    }
}
