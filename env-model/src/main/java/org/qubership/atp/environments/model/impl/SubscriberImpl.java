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

public class SubscriberImpl extends AbstractNamed implements Subscriber {

    private List<Subscription> subscriptions;
    private String host;
    private Integer subscriberType;
    private String signature;
    private String tagList;
    private Integer hostStatus;
    private String notificationURL;
    private Long registrationDate;

    public SubscriberImpl() {
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    public SubscriberImpl(UUID uuid, String name, String host, Integer subscriberType, String signature,
                          String tagList, Integer hostStatus, String notificationURL, Long registrationDate,
                          List<Subscription> subscriptions) {
        setId(uuid);
        setName(name);
        setHost(host);
        setSubscriberType(subscriberType);
        setSignature(signature);
        setTagList(tagList);
        setHostStatus(hostStatus);
        setNotificationURL(notificationURL);
        setRegistrationDate(registrationDate);
        setSubscriptions(subscriptions);
    }

    @Nonnull
    @Override
    public String getHost() {
        return host;
    }

    @Override
    public void setHost(@Nonnull String host) {
        this.host = host;
    }

    @Override
    public Integer getSubscriberType() {
        return subscriberType;
    }

    @Override
    public void setSubscriberType(Integer subscriberType) {
        this.subscriberType = subscriberType;
    }

    @Override
    public String getSignature() {
        return signature;
    }

    @Override
    public void setSignature(@Nonnull String signature) {
        this.signature = signature;
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
    public Integer getHostStatus() {
        return hostStatus;
    }

    @Override
    public void setHostStatus(@Nonnull Integer hostStatus) {
        this.hostStatus = hostStatus;
    }

    @Override
    public String getNotificationURL() {
        return notificationURL;
    }

    @Override
    public void setNotificationURL(@Nonnull String notificationURL) {
        this.notificationURL = notificationURL;
    }

    @Override
    public Long getRegistrationDate() {
        return registrationDate;
    }

    @Override
    public void setRegistrationDate(Long registrationDate) {
        this.registrationDate = registrationDate;
    }

    @Nullable
    @Override
    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    @Override
    public void setSubscriptions(List<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }
}
