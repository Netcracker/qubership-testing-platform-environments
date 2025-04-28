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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.qubership.atp.environments.model.impl.AlertEventImpl;
import org.qubership.atp.environments.repo.impl.AlertEventRepositoryImpl;
import org.qubership.atp.environments.utils.DateTimeUtil;

public class AlertEventServiceImplTest {
    private final ThreadLocal<AlertEventRepositoryImpl> alertEventRepository = new ThreadLocal<>();
    private final ThreadLocal<AlertEventServiceImpl> alertEventService = new ThreadLocal<>();

    private static final UUID entityId = UUID.randomUUID();
    private static final UUID alertId = UUID.randomUUID();
    private AlertEventImpl alertEvent;

    @BeforeEach
    public void setUp() {
        DateTimeUtil timeUtil = Mockito.mock(DateTimeUtil.class);
        AlertEventRepositoryImpl alertEventRepositoryMock = Mockito.mock(AlertEventRepositoryImpl.class);
        alertEventRepository.set(alertEventRepositoryMock);
        alertEventService.set(new AlertEventServiceImpl(alertEventRepositoryMock, timeUtil));
        alertEvent = new AlertEventImpl();
        alertEvent.setAlertId(alertId);
        alertEvent.setEntityId(entityId);
    }

    @Test
    public void getAll_Successful() {
        Mockito.when(alertEventRepository.get().getAll()).thenReturn(Collections.singletonList(alertEvent));
        Assertions.assertEquals(alertId, alertEventService.get().getAll().get(0).getAlertId());
    }

    @Test
    public void getAlertEvent_byId_Successful() {
        Mockito.when(alertEventRepository.get().getById(alertId, entityId)).thenReturn(alertEvent);
        Assertions.assertEquals(alertId, alertEventService.get().get(alertId, entityId).getAlertId());
    }

    @Test
    public void create_ByProperties_Successful() {
        Mockito.when(alertEventRepository.get().create(any(), any(), any(), any(),any()))
                .thenReturn(alertEvent);
        Assertions.assertEquals(alertId, alertEventService.get().create(UUID.randomUUID(), UUID.randomUUID(), "", 1)
                .getAlertId());
    }

}
