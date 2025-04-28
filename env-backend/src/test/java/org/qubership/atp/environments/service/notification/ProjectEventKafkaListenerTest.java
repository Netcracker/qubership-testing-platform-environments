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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.qubership.atp.environments.model.Project;
import org.qubership.atp.environments.model.impl.EnvironmentImpl;
import org.qubership.atp.environments.model.impl.ProjectImpl;
import org.qubership.atp.environments.service.direct.ProjectService;
import org.qubership.atp.environments.service.rest.server.dto.EventType;
import org.qubership.atp.environments.service.rest.server.dto.ProjectEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * ProjectEventKafkaListenerTest - test for {@link ProjectEventKafkaListener}
 */
public class ProjectEventKafkaListenerTest {

    private final ThreadLocal<ProjectService> projectService = new ThreadLocal<>();
    private final ThreadLocal<ProjectEventKafkaListener> projectEventKafkaListener = new ThreadLocal<>();

    private Project project;
    private ProjectEvent projectEvent;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() throws Exception {
        ProjectService projectServiceMock = Mockito.mock(ProjectService.class);
        projectService.set(projectServiceMock);
        projectEventKafkaListener.set(new ProjectEventKafkaListener(projectServiceMock));
        project = new ProjectImpl(UUID.randomUUID(), "Test Project", "Test Project", "Test Description",
                Collections.singletonList(new EnvironmentImpl()), 1635189430738L, 1635189430738L);
        projectEvent = new ProjectEvent(project.getId(), project.getName(), EventType.CREATE);
        objectMapper = new ObjectMapper();
    }

    @Test
    public void listen_CreateSuccessful_ProjectTypeCreate() throws JsonProcessingException {
        projectEvent.setType(EventType.CREATE);
        String requestJson = objectMapper.writer().writeValueAsString(projectEvent);

        projectEventKafkaListener.get().listen(requestJson);

        verify(projectService.get(), times(1)).createAsIs(eq(project));
    }

    @Test
    public void listen_UpdateSuccessful_ProjectTypeUpdate() throws JsonProcessingException {
        projectEvent.setType(EventType.UPDATE);
        String requestJson = objectMapper.writer().writeValueAsString(projectEvent);

        projectEventKafkaListener.get().listen(requestJson);

        verify(projectService.get(), times(1)).update(
                eq(projectEvent.getProjectId()),
                eq(projectEvent.getProjectName()),
                eq(projectEvent.getProjectName()));
    }

    @Test
    public void listen_DeleteSuccessful_ProjectTypeDelete() throws JsonProcessingException {
        projectEvent.setType(EventType.DELETE);
        String requestJson = objectMapper.writer().writeValueAsString(projectEvent);

        projectEventKafkaListener.get().listen(requestJson);

        verify(projectService.get(), times(1)).delete(eq(projectEvent.getProjectId()));
    }
}
