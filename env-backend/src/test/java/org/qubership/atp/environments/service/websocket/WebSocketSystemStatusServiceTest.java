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

package org.qubership.atp.environments.service.websocket;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.qubership.atp.environments.service.websocket.config.WebSocketSystemStatusServiceRunningConfiguration.ENV_ID;
import static org.qubership.atp.environments.service.websocket.config.WebSocketSystemStatusServiceRunningConfiguration.PROJECT_ID;
import static org.qubership.atp.environments.service.websocket.config.WebSocketSystemStatusServiceRunningConfiguration.SYSTEM_ID;

import java.io.IOException;
import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.qubership.atp.auth.springbootstarter.feign.exception.FeignClientException;
import org.qubership.atp.environments.clients.api.healthcheck.dto.StatusDto;
import org.qubership.atp.environments.clients.api.healthcheck.dto.SystemStatusDto;
import org.qubership.atp.environments.model.utils.enums.Status;
import org.qubership.atp.environments.service.direct.EnvironmentService;
import org.qubership.atp.environments.service.rest.server.dto.SystemDto;
import org.qubership.atp.environments.service.websocket.config.WebSocketSystemStatusServiceRunningConfiguration;
import org.qubership.atp.environments.utils.ResourceAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import com.fasterxml.jackson.databind.ObjectMapper;

public class WebSocketSystemStatusServiceTest {

    private static final ResourceAccessor RESOURCE_ACCESSOR =
            new ResourceAccessor(WebSocketSystemStatusServiceTest.class);

    private final ThreadLocal<EnvironmentService> environmentService = new ThreadLocal<>();
    private final ThreadLocal<SimpMessageSendingOperations> messagingTemplate = new ThreadLocal<>();
    private final ThreadLocal<WebSocketSystemHealthCheckService> healthCheckService = new ThreadLocal<>();
    private final ThreadLocal<WebSocketSystemStatusService> webSocketSystemStatusService = new ThreadLocal<>();

    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        EnvironmentService environmentServiceMock = Mockito.mock(EnvironmentService.class);
        SimpMessageSendingOperations messagingTemplateMock = Mockito.mock(SimpMessageSendingOperations.class);
        WebSocketSystemHealthCheckService healthCheckServiceMock = Mockito.mock(WebSocketSystemHealthCheckService.class);

        environmentService.set(environmentServiceMock);
        messagingTemplate.set(messagingTemplateMock);
        healthCheckService.set(healthCheckServiceMock);
        webSocketSystemStatusService.set(new WebSocketSystemStatusService(environmentServiceMock, messagingTemplateMock, healthCheckServiceMock));
    }

    @Test
    public void checkHealth_shouldShowError_whenHealthcheckReturn500Error() throws IOException {
        final Status expectedStatus = null;
        final String expectedException = String.format("Make sure that at least one connection of the system with "
                + "id [%s] in environment %s has set 'HealthCheck' as 'use in service' field", SYSTEM_ID, ENV_ID);
        WebSocketSystemStatusServiceRunningConfiguration test = WebSocketSystemStatusServiceRunningConfiguration.builder()
                .resourceAccessor(RESOURCE_ACCESSOR)
                .projectId(PROJECT_ID)
                .environmentId(ENV_ID)
                .systemId(SYSTEM_ID)
                .status(expectedStatus)
                .build();
        mockHealthCheck(test.getRequest(), expectedStatus, null, test);
        try {
            webSocketSystemStatusService.get().processRequest(test.getRequest());
            Assertions.fail();
        } catch (FeignClientException e) {
            Assertions.assertEquals(expectedException, e.getErrorMessage());
        }
    }

    @Test
    public void checkHealth_shouldSendSystemWithPass_whenHealthcheckReturnPass() throws IOException {
        Status expectedStatus = Status.PASS;
        WebSocketSystemStatusServiceRunningConfiguration test = WebSocketSystemStatusServiceRunningConfiguration.builder()
                .resourceAccessor(RESOURCE_ACCESSOR)
                .projectId(PROJECT_ID)
                .environmentId(ENV_ID)
                .systemId(SYSTEM_ID)
                .status(expectedStatus)
                .build();
        mockHealthCheck(test.getRequest(), expectedStatus, null, test);
        webSocketSystemStatusService.get().processRequest(test.getRequest());
        verify(messagingTemplate.get()).convertAndSend(WebSocketEventType.SYSTEM_STATUS.getDestinationPrefix(), test.getSystemAsDto());
    }

    @Test
    public void checkHealth_shouldFindSystemFromEnvironmentAndSendSystemWithPass_whenSystemIdNullAndEnvIdNotNull() throws IOException {
        Status expectedStatus = Status.PASS;
        WebSocketSystemStatusServiceRunningConfiguration test = WebSocketSystemStatusServiceRunningConfiguration.builder()
                .resourceAccessor(RESOURCE_ACCESSOR)
                .projectId(PROJECT_ID)
                .environmentId(ENV_ID)
                .systemId(null)
                .status(expectedStatus)
                .build();
        mockHealthCheck(test.getRequest(), expectedStatus, null, test);
        when(environmentService.get().getSystems(test.getRequest().getEnvironmentId()))
                .thenReturn(Collections.singletonList(test.getSystem()));
        webSocketSystemStatusService.get().processRequest(test.getRequest());
        verify(messagingTemplate.get()).convertAndSend(WebSocketEventType.SYSTEM_STATUS.getDestinationPrefix(), test.getSystemAsDto());
    }

    @Test
    public void checkHealth_shouldFindSystemFromEnvironmentAndSendSystemWithPass_whenEnvIdNull() throws IOException {
        Status expectedStatus = Status.PASS;
        WebSocketSystemStatusServiceRunningConfiguration test = WebSocketSystemStatusServiceRunningConfiguration.builder()
                .resourceAccessor(RESOURCE_ACCESSOR)
                .projectId(PROJECT_ID)
                .environmentId(null)
                .systemId(SYSTEM_ID)
                .status(expectedStatus)
                .build();
        mockHealthCheck(test.getRequest(), expectedStatus, null, test);
        when(environmentService.get().getByProjectId(test.getRequest().getProjectId()))
                .thenReturn(test.getSystem().getEnvironments());
        webSocketSystemStatusService.get().processRequest(test.getRequest());
        verify(messagingTemplate.get()).convertAndSend(WebSocketEventType.SYSTEM_STATUS.getDestinationPrefix(), test.getSystemAsDto());
    }

    @Test
    public void checkHealth_shouldShowLogSilently_whenFeignClientReturnedNull() {
        Status expectedStatus = Status.FAIL;
        WebSocketSystemStatusServiceRunningConfiguration test = WebSocketSystemStatusServiceRunningConfiguration.builder()
                .resourceAccessor(RESOURCE_ACCESSOR)
                .projectId(PROJECT_ID)
                .environmentId(ENV_ID)
                .systemId(SYSTEM_ID)
                .status(expectedStatus)
                .build();
        webSocketSystemStatusService.get().processRequest(test.getRequest());
    }

    public void mockHealthCheck(SystemStatusCheckRequest request, Status status, FeignClientException e,
                                WebSocketSystemStatusServiceRunningConfiguration config) throws IOException {
        if (status == null) {
            if (e == null) {
                when(healthCheckService.get().checkHealth(request.getProjectId(), request.getEnvironmentId(), request.getSystemId()))
                        .thenThrow(config.getDefaultFeignException());
            } else {
                when(healthCheckService.get().checkHealth(request.getProjectId(), request.getEnvironmentId(), request.getSystemId()))
                        .thenThrow(e);
            }
        } else {
            SystemStatusDto systemStatusDto = new SystemStatusDto();
            systemStatusDto.setStatus(OBJECT_MAPPER.convertValue(status, StatusDto.class));
            when(healthCheckService.get().checkHealth(request.getProjectId(), request.getEnvironmentId(), request.getSystemId()))
                    .thenReturn(SystemDto.convert(config.getSystem()));
        }
    }
}
