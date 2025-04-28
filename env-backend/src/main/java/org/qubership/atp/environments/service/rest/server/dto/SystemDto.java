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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.qubership.atp.environments.model.Identified;
import org.qubership.atp.environments.model.ParametersGettingVersion;
import org.qubership.atp.environments.model.ServerItf;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.impl.AbstractCreatedModified;
import org.qubership.atp.environments.model.utils.enums.Status;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
@SuppressWarnings("CPD-START")
public class SystemDto extends AbstractCreatedModified {

    static final long serialVersionUID = 42L;
    protected List<UUID> environmentIds;
    protected UUID systemCategoryId;
    protected List<ConnectionDto> connections;
    protected Status status;
    protected Long dateOfLastCheck;
    protected String version;
    protected Long dateOfCheckVersion;
    protected ParametersGettingVersion parametersGettingVersion;
    protected UUID parentSystemId;
    @JsonProperty("serverITF")
    protected ServerItf serverItf;
    protected Boolean mergeByName;
    protected UUID linkToSystemId;
    protected UUID externalId;
    protected String externalName;

    public SystemDto() {
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    private SystemDto(UUID id,
                      String name,
                      List<ConnectionDto> connections,
                      UUID systemCategoryId,
                      List<UUID> environmentIds,
                      Long created,
                      Long modified,
                      Status status,
                      Long dateOfLastCheck,
                      String version,
                      Long dateOfCheckVersion,
                      ParametersGettingVersion parametersGettingVersion,
                      UUID parentSystemId,
                      ServerItf serverItf,
                      Boolean mergeByName,
                      UUID linkToSystemId,
                      UUID externalId,
                      String externalName) {
        this.id = id;
        this.name = name;
        this.connections = connections;
        this.systemCategoryId = systemCategoryId;
        this.environmentIds = environmentIds;
        this.created = created;
        this.modified = modified;
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

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    public static SystemDto convert(@Nonnull System system) {
        return new SystemDto(system.getId(),
                system.getName(),
                system.getConnections().stream().map(ConnectionDto::convert).collect(Collectors.toList()),
                system.getSystemCategoryId(),
                system.getEnvironments().stream().map(Identified::getId).collect(Collectors.toList()),
                system.getCreated(),
                system.getModified(),
                system.getStatus(),
                system.getDateOfLastCheck(),
                system.getVersion(),
                system.getDateOfCheckVersion(),
                system.getParametersGettingVersion(),
                system.getParentSystemId(),
                system.getServerItf(),
                system.getMergeByName(),
                system.getLinkToSystemId(),
                system.getExternalId(),
                system.getExternalName());
    }
}
