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

import org.qubership.atp.environments.errorhandling.internal.EnvironmentJsonParseException;
import org.qubership.atp.environments.errorhandling.project.EnvironmentIllegalProjectEventTypeException;
import org.qubership.atp.environments.model.Project;
import org.qubership.atp.environments.model.impl.ProjectImpl;
import org.qubership.atp.environments.service.direct.ProjectService;
import org.qubership.atp.environments.service.rest.server.dto.EventType;
import org.qubership.atp.environments.service.rest.server.dto.ProjectEvent;
import org.springframework.kafka.annotation.KafkaListener;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProjectEventKafkaListener implements ProjectEventListener {
    private final ProjectService projectService;

    public ProjectEventKafkaListener(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    @KafkaListener(topics = "${kafka.catalogue.topic:catalog_notification_topic}")
    public void listen(String event) {
        ProjectEvent projectEvent;
        try {
            projectEvent = new Gson().fromJson(event, ProjectEvent.class);
        } catch (Exception e) {
            log.error("Failed to parse json and get project event entity", e);
            throw new EnvironmentJsonParseException("Failed to parse JSON and get project event entity");
        }
        EventType projectEventType = projectEvent.getType();
        switch (projectEventType) {
            case CREATE: {
                Project project = new ProjectImpl();
                project.setId(projectEvent.getProjectId());
                project.setShortName(projectEvent.getProjectName());
                project.setName(projectEvent.getProjectName());
                projectService.createAsIs(project);
                break;
            }
            case UPDATE: {
                Project project = new ProjectImpl();
                project.setId(projectEvent.getProjectId());
                project.setShortName(projectEvent.getProjectName());
                project.setName(projectEvent.getProjectName());
                projectService.update(
                        projectEvent.getProjectId(),
                        projectEvent.getProjectName(),
                        projectEvent.getProjectName());
                break;
            }
            case DELETE: {
                projectService.delete(projectEvent.getProjectId());
                break;
            }
            default: {
                log.error("Failed to find project event by specified type: {}", projectEventType);
                throw new EnvironmentIllegalProjectEventTypeException(projectEventType.name());
            }

        }
    }
}
