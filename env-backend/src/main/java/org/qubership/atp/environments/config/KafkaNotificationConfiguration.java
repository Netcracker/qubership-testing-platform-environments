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

package org.qubership.atp.environments.config;

import java.util.UUID;

import org.qubership.atp.environments.service.direct.ProjectService;
import org.qubership.atp.environments.service.notification.EventNotificationService;
import org.qubership.atp.environments.service.notification.KafkaEventNotificationService;
import org.qubership.atp.environments.service.notification.ProjectEventKafkaListener;
import org.qubership.atp.environments.service.notification.ProjectEventListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;


@Configuration
public class KafkaNotificationConfiguration {

    @Value("${kafka.environments.topic}")
    public String kafkaEnvironmentsProducerTopic;

    @Value("${kafka.systems.topic}")
    public String kafkaSystemsProducerTopic;

    @Value("${kafka.connections.topic}")
    private String kafkaConnectionProducerTopic;

    @Value("${kafka.enable:false}")
    private boolean kafkaEnable;


    /**
     * Creates project event listener.
     *
     * @return ProjectEventListener
     */
    @Bean
    public ProjectEventListener projectEventListener(ProjectService projectService) {
        if (kafkaEnable) {
            return new ProjectEventKafkaListener(projectService);
        } else {
            return event -> {
            };
        }
    }


    /**
     * Creates EnvironmentEventNotificationService.
     *
     * @param kafkaTemplate - kafka template
     * @return - ProjectEventNotificationService.
     */
    @Bean
    public EventNotificationService environmentEventNotificationService(KafkaTemplate<UUID, String> kafkaTemplate) {
        if (kafkaEnable) {
            return new KafkaEventNotificationService(kafkaTemplate, kafkaEnvironmentsProducerTopic);
        } else {
            return environmentEvent -> {
            };
        }
    }

    /**
     * Creates SystemEventNotificationService.
     *
     * @param kafkaTemplate - kafka template
     * @return - ProjectEventNotificationService.
     */
    @Bean
    public EventNotificationService systemEventNotificationService(KafkaTemplate<UUID, String> kafkaTemplate) {
        if (kafkaEnable) {
            return new KafkaEventNotificationService(kafkaTemplate, kafkaSystemsProducerTopic);
        } else {
            return systemEvent -> {
            };
        }
    }

    /**
     * Creates ConnectionEventNotificationService.
     *
     * @param kafkaTemplate - kafka template
     * @return - ProjectEventNotificationService.
     */
    @Bean
    public EventNotificationService connectionEventNotificationService(KafkaTemplate<UUID, String> kafkaTemplate) {
        if (kafkaEnable) {
            return new KafkaEventNotificationService(kafkaTemplate, kafkaConnectionProducerTopic);
        } else {
            return systemEvent -> {
            };
        }
    }
}
