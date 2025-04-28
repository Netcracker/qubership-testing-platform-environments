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

package org.qubership.atp.environments.service.rest.server.dto;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.qubership.atp.environments.model.ParametersGettingVersion;
import org.qubership.atp.environments.model.ServerItf;
import org.qubership.atp.environments.model.utils.enums.Status;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@SuppressWarnings("CPD-START")
public class CreateSystemDto implements Serializable {

    static final long serialVersionUID = 42L;
    private UUID id;
    private String name;
    private String description;
    private UUID environmentId;
    private UUID systemCategoryId;
    private List<ConnectionDto> connections;
    private Status status;
    private Long dateOfLastCheck;
    private String version;
    private Long dateOfCheckVersion;
    private ParametersGettingVersion parametersGettingVersion;
    private UUID parentSystemId;
    @JsonProperty("serverITF")
    private ServerItf serverItf;
    private Boolean mergeByName;
    private UUID linkToSystemId;
    private UUID externalId;
    private String externalName;

    public CreateSystemDto() {
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    public CreateSystemDto(UUID id, String name, UUID environmentId, String description, UUID systemCategoryId,
                           List<ConnectionDto> connections, Status status, Long dateOfLastCheck,
                           String version, Long dateOfCheckVersion, ParametersGettingVersion parametersGettingVersion,
                           UUID parentSystemId, ServerItf serverItf, Boolean mergeByName, UUID linkToSystemId,
                           UUID externalId, String externalName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.environmentId = environmentId;
        this.systemCategoryId = systemCategoryId;
        this.connections = connections;
        this.status = status;
        this.dateOfLastCheck = dateOfLastCheck;
        this.version = version;
        this.dateOfCheckVersion = dateOfCheckVersion;
        this.parametersGettingVersion = parametersGettingVersion;
        this.parentSystemId = parentSystemId;
        this.serverItf = serverItf;
        this.mergeByName = mergeByName;
        this.linkToSystemId = linkToSystemId;
        this.externalId = externalId;
        this.externalName = externalName;
    }
}
