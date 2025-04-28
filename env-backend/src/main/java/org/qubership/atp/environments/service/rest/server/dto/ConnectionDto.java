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

import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.model.impl.AbstractCreatedModified;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ConnectionDto extends AbstractCreatedModified implements Serializable {
    static final long serialVersionUID = 42L;
    private UUID systemId;
    private ConnectionParameters parameters;
    private UUID sourceTemplateId;
    private String connectionType;
    private UUID projectId;
    private List<String> services;


    /**
     * TODO Make javadoc documentation for this method.
     */
    public ConnectionDto(UUID id,
                         String name,
                         UUID systemId,
                         ConnectionParameters parameters,
                         UUID sourceTemplateId,
                         String connectionType,
                         Long created,
                         Long modified,
                         List<String> services) {
        this.id = id;
        this.name = name;
        this.systemId = systemId;
        this.parameters = parameters;
        this.sourceTemplateId = sourceTemplateId;
        this.connectionType = connectionType;
        this.created = created;
        this.modified = modified;
        this.services = services;
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    public static ConnectionDto convert(Connection connection) {
        return new ConnectionDto(connection.getId(),
                connection.getName(), connection.getSystemId(), connection.getParameters(),
                connection.getSourceTemplateId(), connection.getConnectionType(), connection.getCreated(),
                connection.getModified(),connection.getServices());
    }
}
