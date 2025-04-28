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

import static org.mockito.ArgumentMatchers.any;

import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.qubership.atp.environments.model.Subscription;
import org.qubership.atp.environments.model.impl.SubscriptionImpl;
import org.qubership.atp.environments.model.impl.UpdateEventImpl;
import org.qubership.atp.environments.repo.impl.UpdateEventRepositoryImpl;
import org.qubership.atp.environments.utils.DateTimeUtil;

public class UpdateEventServiceImplTest {
    private final ThreadLocal<UpdateEventRepositoryImpl> updateEventRepository = new ThreadLocal<>();
    private final ThreadLocal<UpdateEventServiceImpl> updateEventService = new ThreadLocal<>();

    private static UpdateEventImpl updateEvent;
    private static final UUID entityId = UUID.randomUUID();
    private static final UUID subscriptionId = UUID.randomUUID();

    @BeforeAll
    public static void init() {
        updateEvent = new UpdateEventImpl();
        Subscription subscription = new SubscriptionImpl();
        subscription.setId(subscriptionId);
        updateEvent.setSubscription(subscription);
        updateEvent.setEntityId(entityId);
    }

    @BeforeEach
    public void setUp() {
        UpdateEventRepositoryImpl updateEventRepositoryMock = Mockito.mock(UpdateEventRepositoryImpl.class);
        updateEventRepository.set(updateEventRepositoryMock);
        updateEventService.set(new UpdateEventServiceImpl(updateEventRepositoryMock, new DateTimeUtil()));
    }

    @Test
    public void getUpdateEvent_byEventId_Successful() {
        Mockito.when(updateEventRepository.get().getById(subscriptionId, entityId)).thenReturn(updateEvent);
        Assertions.assertEquals(entityId, updateEventService.get().get(subscriptionId, entityId).getEntityId());
    }

    @Test
    public void getSubscriptionUpdateEvents_bySubscriptionId_Successful() {
        Mockito.when(updateEventRepository.get().getBySubscriptionId(subscriptionId)).thenReturn(updateEvent);
        Assertions.assertEquals(entityId, updateEventService.get().getSubscriptionUpdateEvents(subscriptionId).getEntityId());
    }

    @Test
    public void getEntityUpdateEvents_byEntityId_Successful() {
        Mockito.when(updateEventRepository.get().getByEntityId(entityId)).thenReturn(updateEvent);
        Assertions.assertEquals(entityId, updateEventService.get().getEntityUpdateEvents(entityId).getEntityId());
    }

    @Test
    public void getAll_Successful() {
        Mockito.when(updateEventRepository.get().getAll()).thenReturn(Collections.singletonList(updateEvent));
        Assertions.assertEquals(entityId, updateEventService.get().getAll().get(0).getEntityId());
    }

    @Test
    public void create_ByProperties_Successful() {
        Mockito.when(updateEventRepository.get().create(any(), any(), any(), any(),any(), any()))
                .thenReturn(updateEvent);
        Assertions.assertEquals(entityId, updateEventService.get().create(UUID.randomUUID(), UUID.randomUUID(), "", 1, "")
                .getEntityId());
    }

}
