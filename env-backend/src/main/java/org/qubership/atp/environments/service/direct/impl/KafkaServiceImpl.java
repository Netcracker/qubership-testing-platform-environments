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

import java.util.UUID;

import org.qubership.atp.environments.service.direct.KafkaService;
import org.qubership.atp.environments.service.notification.EventNotificationService;
import org.qubership.atp.environments.service.rest.server.dto.EventType;
import org.qubership.atp.environments.service.rest.server.dto.ProducerNotificationEvent;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class KafkaServiceImpl implements KafkaService {

    private final EventNotificationService systemEventNotificationService;

    private final EventNotificationService environmentEventNotificationService;

    private final EventNotificationService connectionEventNotificationService;

    @Override
    public void sendConnectionKafkaNotification(UUID connectionId, EventType connectionEventType, UUID projectId) {

        connectionEventNotificationService.notify(new ProducerNotificationEvent(connectionId,
                connectionEventType, projectId));
    }

    @Override
    public void sendSystemKafkaNotification(UUID systemId,
                                            EventType systemEventType, UUID projectId) {
        systemEventNotificationService.notify(new ProducerNotificationEvent(systemId,
                systemEventType, projectId));
    }

    @Override
    public void sendEnvironmentKafkaNotification(UUID environmentId,
                                                 EventType environmentEventType,
                                                 UUID projectId) {
        environmentEventNotificationService
                .notify(new ProducerNotificationEvent(environmentId,
                        environmentEventType, projectId));
    }

}
