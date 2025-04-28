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

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.qubership.atp.environments.model.Subscriber;
import org.qubership.atp.environments.model.Subscription;
import org.qubership.atp.environments.model.impl.SubscriberImpl;
import org.qubership.atp.environments.model.impl.SubscriptionImpl;
import org.qubership.atp.environments.repo.impl.SubscriptionRepositoryImpl;
import org.qubership.atp.environments.utils.DateTimeUtil;

public class SubscriptionServiceImplTest {

    private final ThreadLocal<SubscriptionRepositoryImpl> subscriptionRepository = new ThreadLocal<>();
    private final ThreadLocal<SubscriptionServiceImpl> service = new ThreadLocal<>();


    private static SubscriptionImpl subscription;
    private static final UUID projectId = UUID.randomUUID();
    private static final UUID environmentId = UUID.randomUUID();
    private static final UUID systemId = UUID.randomUUID();
    private static final UUID subscriptionId = UUID.randomUUID();
    private static final UUID subscriberId = UUID.randomUUID();

    @BeforeAll
    public static void init() {
        subscription = new SubscriptionImpl();
        subscription.setId(subscriptionId);
        subscription.setProjectId(projectId);
        subscription.setEnvironmentId(environmentId);
        subscription.setSystemId(systemId);
        Subscriber subscriber = new SubscriberImpl();
        subscriber.setId(subscriberId);
        subscription.setSubscriber(subscriber);
    }

    @BeforeEach
    public void setUp() {
        SubscriptionRepositoryImpl subscriptionRepositoryMock = Mockito.mock(SubscriptionRepositoryImpl.class);
        subscriptionRepository.set(subscriptionRepositoryMock);
        service.set(new SubscriptionServiceImpl(subscriptionRepositoryMock, new DateTimeUtil()));
    }

    @Test
    public void getSubscription_byId_Successful() {
        Mockito.when(subscriptionRepository.get().getById(subscriptionId)).thenReturn(subscription);
        Assertions.assertEquals(subscriptionId, service.get().get(subscriptionId).getId());
    }

    @Test
    public void subscriptionExists_byId_Successful() {
        Mockito.when(subscriptionRepository.get().existsById(subscriptionId)).thenReturn(true);
        Assertions.assertTrue(service.get().existsById(subscriptionId));
    }

    @Test
    public void getAll_Successful() {
        Mockito.when(subscriptionRepository.get().getAll()).thenReturn(Collections.singletonList(subscription));
        Assertions.assertEquals(subscriptionId, service.get().getAll().get(0).getId());
    }

    @Test
    public void getListIdSubscriptionsForProject_byProjectId_Successful() {
        Mockito.when(subscriptionRepository.get().getListIdSubscriptionsForProject(projectId)).thenReturn(Collections.singletonList(subscriptionId));
        Assertions.assertEquals(subscriptionId, service.get().getListIdSubscriptionsForProject(projectId).get(0));
    }

    @Test
    public void getListIdSubscriptionsForProject_byProjectIdAndCascadeFlag_Successful() {
        Mockito.when(subscriptionRepository.get().getListIdSubscriptionsForProject(eq(projectId), anyBoolean()))
                .thenReturn(Collections.singletonList(subscriptionId));
        Assertions.assertEquals(subscriptionId, service.get().getListIdSubscriptionsForProject(projectId, true).get(0));
    }

    @Test
    public void getListIdSubscriptionsForEnvironment_byEnvironmentId_Successful() {
        Mockito.when(subscriptionRepository.get().getListIdSubscriptionsForEnvironment(environmentId)).thenReturn(Collections.singletonList(subscriptionId));
        Assertions.assertEquals(subscriptionId, service.get().getListIdSubscriptionsForEnvironment(environmentId).get(0));
    }

    @Test
    public void getListIdSubscriptionsForEnvironment_byEnvironmentIdAndCascadeFlag_Successful() {
        Mockito.when(subscriptionRepository.get().getListIdSubscriptionsForEnvironment(eq(environmentId), anyBoolean()))
                .thenReturn(Collections.singletonList(subscriptionId));
        Assertions.assertEquals(subscriptionId, service.get().getListIdSubscriptionsForEnvironment(environmentId, true).get(0));
    }

    @Test
    public void getListIdSubscriptionsForSystem_bySystemId_Successful() {
        Mockito.when(subscriptionRepository.get().getListIdSubscriptionsForSystem(systemId)).thenReturn(Collections.singletonList(subscriptionId));
        Assertions.assertEquals(subscriptionId, service.get().getListIdSubscriptionsForSystem(systemId).get(0));
    }

    @Test
    public void getListIdSubscriptionsForSystem_bySystemIdAndCascadeFlag_Successful() {
        Mockito.when(subscriptionRepository.get().getListIdSubscriptionsForSystem(eq(systemId), anyBoolean()))
                .thenReturn(Collections.singletonList(subscriptionId));
        Assertions.assertEquals(subscriptionId, service.get().getListIdSubscriptionsForSystem(systemId, true).get(0));
    }

    @Test
    public void getSubscriberSubscriptions_bySubscriberId_Successful() {
        Mockito.when(subscriptionRepository.get().getSubscriberSubscriptions(subscriberId)).thenReturn(Collections.singletonList(subscription));
        List<Subscription> subscriptions = service.get().getSubscriberSubscriptions(subscriberId);
        Assertions.assertEquals(subscriptionId, subscriptions.get(0).getId());
        Assertions.assertEquals(subscriberId, subscriptions.get(0).getSubscriber().getId());
    }

    @Test
    public void getProjectSubscriptions_byProjectId_Successful() {
        Mockito.when(subscriptionRepository.get().getProjectSubscriptions(projectId)).thenReturn(Collections.singletonList(subscription));
        List<Subscription> subscriptions = service.get().getProjectSubscriptions(projectId);
        Assertions.assertEquals(subscriptionId, subscriptions.get(0).getId());
        Assertions.assertEquals(projectId, subscriptions.get(0).getProjectId());
    }

    @Test
    public void getEnvironmentSubscriptions_byEnvironmentId_Successful() {
        Mockito.when(subscriptionRepository.get().getEnvironmentSubscriptions(environmentId)).thenReturn(Collections.singletonList(subscription));
        List<Subscription> subscriptions = service.get().getEnvironmentSubscriptions(environmentId);
        Assertions.assertEquals(subscriptionId, subscriptions.get(0).getId());
        Assertions.assertEquals(environmentId, subscriptions.get(0).getEnvironmentId());
    }

    @Test
    public void getSystemSubscriptions_bySystemId_Successful() {
        Mockito.when(subscriptionRepository.get().getSystemSubscriptions(systemId)).thenReturn(Collections.singletonList(subscription));
        List<Subscription> subscriptions = service.get().getSystemSubscriptions(systemId);
        Assertions.assertEquals(subscriptionId, subscriptions.get(0).getId());
        Assertions.assertEquals(systemId, subscriptions.get(0).getSystemId());
    }

    @Test
    public void getListSubscriptionsByConditions_Successful() {
        Mockito.when(subscriptionRepository.get().getListSubscriptionsByConditions(projectId, environmentId, systemId, subscriberId))
                .thenReturn(Collections.singletonList(subscriptionId));
        Assertions.assertEquals(subscriptionId, service.get().getListSubscriptionsByConditions(projectId, environmentId, systemId, subscriberId).get(0));
    }

}
