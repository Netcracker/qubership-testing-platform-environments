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

package org.qubership.atp.environments.model.impl;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.ConnectionParameters;

import lombok.Builder;

public class ConnectionImpl extends AbstractCreatedModified implements Connection {

    private UUID systemId;
    private ConnectionParameters parameters;
    private UUID sourceTemplateId;
    private String connectionType;
    private List<String> services;
    private UUID sourceId;

    public ConnectionImpl() {
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Builder
    public ConnectionImpl(UUID uuid,
                          String name,
                          String description,
                          @Nonnull ConnectionParameters parameters,
                          Long created,
                          UUID createdBy,
                          Long modified,
                          UUID modifiedBy,
                          @Nonnull UUID systemId,
                          String connectionType,
                          @Nonnull UUID sourceTemplateId,
                          List<String> services,
                          UUID sourceId) {
        setId(uuid);
        setName(name);
        setDescription(description);
        setParameters(parameters);
        setCreated(created);
        setCreatedBy(createdBy);
        setModified(modified);
        setModifiedBy(modifiedBy);
        setSystemId(systemId);
        setConnectionType(connectionType);
        setSourceTemplateId(sourceTemplateId);
        setServices(services);
        setSourceId(sourceId);
    }

    @Nonnull
    @Override
    public UUID getSystemId() {
        return systemId;
    }

    @Override
    public void setSystemId(@Nonnull UUID systemId) {
        this.systemId = systemId;
    }

    @Nonnull
    @Override
    public ConnectionParameters getParameters() {
        return parameters;
    }

    @Override
    public void setParameters(@Nonnull ConnectionParameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public List<String> getServices() {
        return services;
    }

    @Override
    public void setServices(List<String> services) {
        this.services = services;
    }

    @Nullable
    @Override
    public UUID getSourceTemplateId() {
        return sourceTemplateId;
    }

    @Override
    public void setSourceTemplateId(@Nullable UUID sourceTemplateId) {
        this.sourceTemplateId = sourceTemplateId;
    }

    @Nullable
    @Override
    public String getConnectionType() {
        return connectionType;
    }

    @Override
    public void setConnectionType(@Nullable String connectionType) {
        this.connectionType = connectionType;
    }

    @Override
    public UUID getSourceId() {
        return sourceId;
    }

    @Override
    public void setSourceId(UUID sourceId) {
        this.sourceId = sourceId;
    }
}
