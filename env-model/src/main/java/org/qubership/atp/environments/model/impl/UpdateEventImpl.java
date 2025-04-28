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

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.qubership.atp.environments.model.Subscription;
import org.qubership.atp.environments.model.UpdateEvent;

public class UpdateEventImpl implements UpdateEvent {

    private Subscription subscription;
    private UUID subscriptionId;
    private UUID entityId;
    private String tagList;
    private Integer status;
    private Long lastEventDate;
    private String entityType;

    public UpdateEventImpl() {
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    public UpdateEventImpl(Subscription subscription, UUID subscriptionId,
                           UUID entityId, String tagList, Integer status, Long lastEventDate, String entityType) {
        setSubscriptionId(subscriptionId);
        setSubscription(subscription);
        setEntityId(entityId);
        setTagList(tagList);
        setStatus(status);
        setLastEventDate(lastEventDate);
        setEntityType(entityType);
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    public UpdateEventImpl(UUID subscriptionId,
                           UUID entityId, String tagList, Integer status, Long lastEventDate, String entityType) {
        setSubscriptionId(subscriptionId);
        setEntityId(entityId);
        setTagList(tagList);
        setStatus(status);
        setLastEventDate(lastEventDate);
        setEntityType(entityType);
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    @Override
    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    @Override
    public void setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    @Override
    public UUID getEntityId() {
        return entityId;
    }

    @Override
    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    @Override
    public String getTagList() {
        return tagList;
    }

    @Override
    public void setTagList(@Nonnull String tagList) {
        this.tagList = tagList;
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
    public Long getLastEventDate() {
        return lastEventDate;
    }

    @Override
    public void setLastEventDate(Long lastEventDate) {
        this.lastEventDate = lastEventDate;
    }

    @Override
    public String getEntityType() {
        return entityType;
    }

    @Override
    public void setEntityType(@Nonnull String entityType) {
        this.entityType = entityType;
    }

}