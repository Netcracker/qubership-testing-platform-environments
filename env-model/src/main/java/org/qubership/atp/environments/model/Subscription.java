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

package org.qubership.atp.environments.model;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id",
        scope = Subscription.class)
public interface Subscription extends Identified {

    @Nonnull
    @JsonProperty("subscriberId")
    Subscriber getSubscriber();

    void setSubscriber(@Nonnull Subscriber subscriber);

    @Nonnull
    Integer getSubscriptionType();

    void setSubscriptionType(Integer subscriptionType);

    @JsonProperty("projectId")
    UUID getProjectId();

    void setProjectId(UUID projectId);

    @JsonProperty("environmentId")
    UUID getEnvironmentId();

    void setEnvironmentId(UUID environmentId);

    @JsonProperty("systemId")
    UUID getSystemId();

    void setSystemId(UUID systemId);

    Integer getStatus();

    void setStatus(@Nonnull Integer status);

    Long getLastUpdated();

    void setLastUpdated(@Nonnull Long lastUpdated);

    List<UpdateEvent> getUpdateEvents();

    void setUpdateEvents(List<UpdateEvent> updateEvents);

    @Override
    default boolean isParent(Identified candidate) {
        return Subscriber.class.isAssignableFrom(candidate.getClass());
    }
}
