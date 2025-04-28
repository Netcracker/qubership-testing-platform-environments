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

package org.qubership.atp.environments.service.rest.server.dto;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.qubership.atp.environments.model.impl.AbstractDescribed;

public class SubscriptionDto extends AbstractDescribed {
    static final long serialVersionUID = 42L;
    private Integer subscriptionType;
    private UUID projectId;
    private UUID environmentId;
    private UUID systemId;
    private UUID subscriberId;
    private Integer status;
    private Long lastUpdated;

    public SubscriptionDto() {
    }

    public Integer getSubscriptionType() {
        return subscriptionType;
    }

    public void setSubscriptionType(Integer subscriptionType) {
        this.subscriptionType = subscriptionType;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public UUID getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(UUID environmentId) {
        this.environmentId = environmentId;
    }

    public UUID getSystemId() {
        return systemId;
    }

    public void setSystemId(UUID systemId) {
        this.systemId = systemId;
    }

    public UUID getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(UUID subscriberId) {
        this.subscriberId = subscriberId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Nullable
    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(@Nonnull Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
