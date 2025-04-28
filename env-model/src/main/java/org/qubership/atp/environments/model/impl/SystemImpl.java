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

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.Identified;
import org.qubership.atp.environments.model.ParametersGettingVersion;
import org.qubership.atp.environments.model.ServerItf;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.SystemCategory;
import org.qubership.atp.environments.model.utils.enums.Status;

import lombok.Builder;

public class SystemImpl extends AbstractCreatedModified implements System, Serializable {

    private static final long serialVersionUID = 1L;

    private List<Environment> environments;
    private SystemCategory systemCategory;
    private List<Connection> connectionsList;
    private Status status;
    private Long dateOfLastCheck;
    private String version;
    private Long dateOfCheckVersion;
    private ParametersGettingVersion parametersGettingVersion;
    private UUID parentSystemId;
    private ServerItf serverItf;
    private Boolean mergeByName;
    private UUID linkToSystemId;
    private UUID externalId;
    private UUID sourceId;
    private String externalName;
    private transient String checkVersionError;

    public SystemImpl() {
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Builder
    public SystemImpl(UUID uuid, String name, String description, Long created, UUID createdBy,
                      Long modified, UUID modifiedBy, @Nonnull List<Environment> environments,
                      SystemCategory systemCategory, List<Connection> connectionsList, Status status,
                      Long dateOfLastCheck, String version, Long dateOfCheckVersion,
                      ParametersGettingVersion parametersGettingVersion,
                      UUID parentSystemId, ServerItf serverItf, Boolean mergeByName, UUID linkToSystemId,
                      UUID externalId, UUID sourceId, String externalName) {
        setId(uuid);
        setName(name);
        setDescription(description);
        setCreated(created);
        setCreatedBy(createdBy);
        setModified(modified);
        setModifiedBy(modifiedBy);
        setEnvironments(environments);
        setSystemCategory(systemCategory);
        setConnections(connectionsList);
        setStatus(status);
        setDateOfLastCheck(dateOfLastCheck);
        setVersion(version);
        setDateOfCheckVersion(dateOfCheckVersion);
        setParametersGettingVersion(parametersGettingVersion);
        setParentSystemId(parentSystemId);
        setServerItf(serverItf);
        setMergeByName(mergeByName);
        setLinkToSystemId(linkToSystemId);
        setExternalId(externalId);
        setSourceId(sourceId);
        setExternalName(externalName);
    }

    @Override
    public List<Environment> getEnvironmentIds() {
        return environments;
    }

    @Override
    public void setEnvironmentIds(@Nonnull List<Environment> environments) {
        setEnvironments(environments);
    }

    @Override
    public List<Environment> getEnvironments() {
        return environments;
    }

    @Override
    public void setEnvironments(@Nonnull List<Environment> environments) {
        this.environments = environments;
    }

    @Override
    public UUID getSystemCategoryId() {
        return Optional.ofNullable(getSystemCategory()).map(Identified::getId).orElse(null);
    }

    @Override
    public SystemCategory getSystemCategory() {
        return systemCategory;
    }

    @Override
    public void setSystemCategory(SystemCategory systemCategory) {
        this.systemCategory = systemCategory;
    }

    @Override
    public Status getStatus() {
        return this.status;
    }

    @Override
    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public List<Connection> getConnections() {
        return connectionsList;
    }

    @Override
    public void setConnections(List<Connection> connectionsList) {
        this.connectionsList = connectionsList;
    }

    @Override
    public Long getDateOfLastCheck() {
        return this.dateOfLastCheck;
    }

    @Override
    public void setDateOfLastCheck(Long dateOfLastCheck) {
        this.dateOfLastCheck = dateOfLastCheck;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    @Override
    public void setVersion(@Nullable String version) {
        this.version = version;
    }

    @Override
    public Long getDateOfCheckVersion() {
        return this.dateOfCheckVersion;
    }

    @Override
    public void setDateOfCheckVersion(Long dateOfCheckVersion) {
        this.dateOfCheckVersion = dateOfCheckVersion;
    }

    @Override
    public ParametersGettingVersion getParametersGettingVersion() {
        return this.parametersGettingVersion;
    }

    @Override
    public void setParametersGettingVersion(@Nonnull ParametersGettingVersion parametersGettingVersion) {
        this.parametersGettingVersion = parametersGettingVersion;
    }

    @Override
    public UUID getParentSystemId() {
        return parentSystemId;
    }

    @Override
    public void setParentSystemId(UUID parentSystemId) {
        this.parentSystemId = parentSystemId;
    }

    @Nullable
    @Override
    public ServerItf getServerItf() {
        return serverItf;
    }

    @Override
    public void setServerItf(@Nonnull ServerItf serverItf) {
        this.serverItf = serverItf;
    }

    @Nullable
    @Override
    public Boolean getMergeByName() {
        return mergeByName;
    }

    @Override
    public void setMergeByName(Boolean mergeByName) {
        this.mergeByName = mergeByName;
    }

    @Nullable
    @Override
    public UUID getLinkToSystemId() {
        return linkToSystemId;
    }

    @Override
    public void setLinkToSystemId(UUID linkToSystemId) {
        this.linkToSystemId = linkToSystemId;
    }

    @Nullable
    @Override
    public UUID getExternalId() {
        return externalId;
    }

    @Override
    public void setExternalId(UUID externalId) {
        this.externalId = externalId;
    }

    @Override
    public String getExternalName() {
        return externalName;
    }

    @Override
    public void setExternalName(String externalName) {
        this.externalName = externalName;
    }

    @Override
    public String getCheckVersionError() {
        return this.checkVersionError;
    }

    @Override
    public void setCheckVersionError(String error) {
        this.checkVersionError = error;
    }

    @Nullable
    @Override
    public UUID getSourceId() {
        return sourceId;
    }

    @Override
    public void setSourceId(@Nullable UUID sourceId) {
        this.sourceId = sourceId;
    }
}
