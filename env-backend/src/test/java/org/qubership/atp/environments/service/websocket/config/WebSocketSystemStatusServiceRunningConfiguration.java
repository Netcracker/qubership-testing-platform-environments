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

package org.qubership.atp.environments.service.websocket.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.qubership.atp.auth.springbootstarter.feign.exception.FeignClientException;
import org.qubership.atp.environments.model.impl.EnvironmentImpl;
import org.qubership.atp.environments.model.impl.SystemImpl;
import org.qubership.atp.environments.model.utils.enums.Status;
import org.qubership.atp.environments.service.rest.server.dto.SystemDto;
import org.qubership.atp.environments.service.websocket.SystemStatusCheckRequest;
import org.qubership.atp.environments.utils.ResourceAccessor;

import feign.Request;
import lombok.Builder;
import lombok.Getter;
import net.sf.json.JSONObject;

public class WebSocketSystemStatusServiceRunningConfiguration {

    public static final UUID PROJECT_ID = UUID.fromString("dfea6d54-ef2f-4a4b-9793-5712ba3e736e");
    public static final UUID ENV_ID = UUID.fromString("02456511-bcc9-467c-99c8-a6a97f968d6e");
    public static final UUID SYSTEM_ID = UUID.fromString("73d55763-c12f-4f93-9812-d8ad5707d869");

    @Getter
    private final SystemStatusCheckRequest request;
    @Getter
    private final SystemImpl system;

    private final ResourceAccessor resourceAccessor;
    private final Map<Status, String> statusMapper = new HashMap<Status, String>() {{
        put(Status.PASS, "passStatusFeignJsonStatus.json");
        put(Status.FAIL, "failStatusFeignJsonStatus.json");
        put(Status.WARN, "warnStatusFeignJsonStatus.json");
        put(Status.NOTHING, "nothingStatusFeignJsonStatus.json");
        put(null, "errorStatusFeignJsonStatus.json");
    }};

    @Builder
    public WebSocketSystemStatusServiceRunningConfiguration(UUID projectId, UUID environmentId, UUID systemId,
                                                            Status status, ResourceAccessor resourceAccessor,
                                                            FeignClientException e) {
        if (resourceAccessor == null) {
            throw new RuntimeException("Please set resourceAccessor!");
        }
        this.resourceAccessor = resourceAccessor;
        this.request = new SystemStatusCheckRequest(projectId, environmentId, systemId);
        SystemImpl system = SystemImpl.builder()
                .uuid(request.getSystemId())
                .connectionsList(new ArrayList<>())
                .status(status)
                .build();
        system.setEnvironments(Collections.singletonList(EnvironmentImpl.builder()
                .systemsList(Collections.singletonList(system))
                .build()));
        this.system = system;
    }

    public FeignClientException getDefaultFeignException() {
        final String filepath = statusMapper.get(null);
        final JSONObject jsonError = resourceAccessor.readObjectFromFilePath(JSONObject.class, filepath);
        int status = jsonError.getInt("status");
        Request request = Request.create(Request.HttpMethod.POST,"testurl", Collections.emptyMap(),null,
                null,null);
        return new FeignClientException(
                status,
                jsonError.getString("message"),
                request.httpMethod(),
                Collections.emptyMap(),
                request);
    }

    public JSONObject getJsonAccordingStatus(Status status) {
        return resourceAccessor.readObjectFromFilePath(JSONObject.class, statusMapper.get(status));
    }

    public SystemDto getSystemAsDto() {
        return SystemDto.convert(system);
    }
}
