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

package org.qubership.atp.environments.service.notification;

import java.util.UUID;

import org.qubership.atp.environments.service.rest.server.dto.ProducerNotificationEvent;
import org.springframework.kafka.core.KafkaTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaEventNotificationService implements EventNotificationService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String topicName;

    private final KafkaTemplate<UUID, String> kafkaTemplate;

    public KafkaEventNotificationService(KafkaTemplate<UUID, String> kafkaTemplate, String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    @Override
    public void notify(ProducerNotificationEvent event) {
        try {
            kafkaTemplate.send(topicName, event.getId(), objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            log.error("NotificationService can't write to kafka", e);
        }
    }
    }
