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

import java.util.UUID;

import org.qubership.atp.auth.springbootstarter.feign.exception.FeignClientException;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.service.direct.EnvironmentService;
import org.qubership.atp.environments.service.rest.server.dto.SystemDto;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressFBWarnings("RV_RETURN_VALUE_IGNORED")
@AllArgsConstructor
@Service
public class WebSocketSystemStatusService {

    private final EnvironmentService environmentService;
    private final SimpMessageSendingOperations messagingTemplate;
    private final WebSocketSystemHealthCheckService healthCheckService;

    /**
     * Process get system status websocket request.
     *
     * @param request - get status websocket request.
     */
    public void processRequest(SystemStatusCheckRequest request) {
        String nullError = "Aborting sending request to Healthcheck service. %s is null. Request: %s";
        final UUID projectId = Preconditions.checkNotNull(request.getProjectId(),
                String.format(nullError, "Project Id", request));
        if (request.getSystemId() == null && request.getEnvironmentId() != null) {
            for (System system : environmentService.getSystems(request.getEnvironmentId())) {
                processRequest(projectId, request.getEnvironmentId(), system.getId());
            }
        } else if (request.getEnvironmentId() == null) {
            for (Environment environment : environmentService.getByProjectId(projectId)) {
                if (environment.getSystems() != null) {
                    for (System system : environment.getSystems()) {
                        processRequest(projectId, environment.getId(), system.getId());
                    }
                }
            }
        } else {
            processRequest(projectId, request.getEnvironmentId(), request.getSystemId());
        }
    }

    private void processRequest(UUID projectId, UUID environmentId, UUID systemId) {
        try {
            SystemDto system = healthCheckService.checkHealth(projectId, environmentId, systemId);
            messagingTemplate.convertAndSend(WebSocketEventType.SYSTEM_STATUS.getDestinationPrefix(), system);
        } catch (FeignClientException e) {
            final String env = extractEnvName(environmentId);
            final String errTemplate = "Make sure that at least one connection of the system with id [%s]"
                    + " in environment %s has set 'HealthCheck' as 'use in service' field";
            log.error(e.getErrorMessage());
            throw new FeignClientException(e.getStatus(), String.format(errTemplate, systemId, env),
                    e.getHttpMethod(), e.getHeaders(), e.getRequest());
        } catch (Exception e) {
            log.error("Error get healthcheck status: ", e);
        }
    }

    private String extractEnvName(UUID environmentId) {
        return environmentService.getOrElse(environmentId)
                .map(e -> e.getName() + " (" + environmentId + ")")
                .orElse(environmentId.toString());
    }
}
