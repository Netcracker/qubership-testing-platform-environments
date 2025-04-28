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

import org.qubership.atp.environments.model.Subscriber;
import org.qubership.atp.environments.model.Subscription;
import org.qubership.atp.environments.repo.impl.SubscriberRepositoryImpl;
import org.qubership.atp.environments.repo.impl.SubscriptionRepositoryImpl;
import org.qubership.atp.environments.service.direct.SubscriberService;
import org.qubership.atp.environments.utils.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("subscriberService")
@SuppressWarnings("CPD-START")
public class SubscriberServiceImpl implements SubscriberService {

    private final SubscriberRepositoryImpl subscriberRepository;
    private final SubscriptionRepositoryImpl subscriptionRepository;
    private final DateTimeUtil dateTimeUtil;

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Autowired
    public SubscriberServiceImpl(SubscriberRepositoryImpl subscriberRepository,
                                 SubscriptionRepositoryImpl subscriptionRepository, DateTimeUtil dateTimeUtil) {
        this.subscriberRepository = subscriberRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.dateTimeUtil = dateTimeUtil;
    }

    @Nullable
    @Override
    public Subscriber get(@Nonnull UUID id) {
        return subscriberRepository.getById(id);
    }

    @Override
    public boolean existsById(@Nonnull UUID id) {
        return subscriberRepository.existsById(id);
    }

    @Nonnull
    @Override
    public List<Subscriber> getAll() {
        return subscriberRepository.getAll();
    }

    @Nonnull
    @Override
    public Subscriber create(Subscriber subscriber) {
        return subscriberRepository.create(subscriber.getName(),
                subscriber.getHost(),
                subscriber.getSubscriberType(),
                subscriber.getSignature(),
                subscriber.getTagList(),
                subscriber.getHostStatus(),
                subscriber.getNotificationURL(),
                dateTimeUtil.timestampAsUtc()
        );
    }

    @Override
    public void update(Subscriber subscriber) {
        subscriberRepository.update(subscriber.getId(),
                subscriber.getName(),
                subscriber.getHost(),
                subscriber.getSubscriberType(),
                subscriber.getSignature(),
                subscriber.getTagList(),
                subscriber.getHostStatus(),
                subscriber.getNotificationURL(),
                dateTimeUtil.timestampAsUtc()
        );
    }

    @Override
    public void delete(UUID subscriberId) {
        subscriberRepository.delete(subscriberId);
    }

    @Override
    public List<Subscription> getSubscriptions(UUID subscriberId) {
        return subscriptionRepository.getAllByParentId(subscriberId);
    }
}
