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

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id",
        scope = Subscriber.class)
public interface Subscriber extends Identified, Named {

    //@Nonnull
    String getHost();

    void setHost(@Nonnull String host);

    @Nonnull
    Integer getSubscriberType();

    void setSubscriberType(Integer subscriberType);

    @Nonnull
    String getSignature();

    void setSignature(@Nonnull String signature);

    String getTagList();

    void setTagList(@Nonnull String tagList);

    Integer getHostStatus();

    void setHostStatus(@Nonnull Integer hostStatus);

    @Nonnull
    String getNotificationURL();

    void setNotificationURL(@Nonnull String notificationURL);

    @Nonnull
    Long getRegistrationDate();

    void setRegistrationDate(@Nonnull Long registrationDate);

    List<Subscription> getSubscriptions();

    void setSubscriptions(List<Subscription> subscriptionsList);
}
