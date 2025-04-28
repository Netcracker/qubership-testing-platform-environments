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

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import org.qubership.atp.auth.springbootstarter.exceptions.AtpIllegalNullableArgumentException;
import org.qubership.atp.environments.clients.api.healthcheck.dto.StatusDto;
import org.qubership.atp.environments.clients.api.healthcheck.dto.SystemStatusDto;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.utils.enums.Status;
import org.qubership.atp.environments.service.direct.SystemService;
import org.qubership.atp.environments.service.rest.client.HealthcheckFeignClient;
import org.qubership.atp.environments.service.rest.server.dto.SystemDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WebSocketSystemHealthCheckService {

    private final SystemService systemService;
    private final HealthcheckFeignClient hcFeignClient;
    private final ObjectMapper objectMapper;

    /**
     * Creates service which calls ATP Healthcheck and receives server statuses.
     *
     * @param systemService service for saving results of checking.
     * @param hcFeignClient API to go to ATP Healthcheck.
     */
    public WebSocketSystemHealthCheckService(SystemService systemService, HealthcheckFeignClient hcFeignClient) {
        this.systemService = systemService;
        this.hcFeignClient = hcFeignClient;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Checks health of connections in provided system at ATP Healthcheck
     * and saves to system.
     *
     * @param projectId     projectId
     * @param environmentId environmentId
     * @param systemId      systemId
     * @return DTO of saved system.
     * @throws IOException if JSON error during ATP Healthcheck response parsing occur.
     */
    public SystemDto checkHealth(UUID projectId, UUID environmentId, UUID systemId) throws IOException {
        ResponseEntity<SystemStatusDto> systemStatusDtoResponseEntity =
                hcFeignClient.checkSystem(Objects.toString(projectId, null),
                        Objects.toString(environmentId, null), Objects.toString(systemId, null),
                        null, null, null);
        if (systemStatusDtoResponseEntity != null && systemStatusDtoResponseEntity.getBody() != null) {
            SystemStatusDto systemStatusDto = systemStatusDtoResponseEntity.getBody();
            if (systemStatusDto != null && systemStatusDto.getStatus() != null) {
                StatusDto statusDto = systemStatusDto.getStatus();
                if (statusDto != null) {
                    System system = systemService.saveStatusAndDateOfLastCheck(
                            systemId, objectMapper.convertValue(statusDto, Status.class));
                    return SystemDto.convert(system);
                }
            }
        }
        throw new AtpIllegalNullableArgumentException("response", "Healthcheck service check system request");
    }
}
