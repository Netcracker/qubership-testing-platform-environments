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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.javers.core.metamodel.annotation.TypeName;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.System;

import com.google.gson.Gson;
import lombok.Builder;

@TypeName("Environment")
public class EnvironmentImpl extends AbstractCreatedModified implements Environment {

    private UUID projectId;
    private List<System> systemsList;
    private UUID categoryId;
    private String graylogName;
    private UUID sourceId;
    private String ssmSolutionAlias;
    private String ssmInstanceAlias;
    private String consulEgressConfigPath;
    private List<String> tags;

    public EnvironmentImpl() {
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Builder
    public EnvironmentImpl(UUID uuid, String name, String graylogName, String description, String ssmSolutionAlias,
                           String ssmInstanceAlias, String consulEgressConfigPath, Long created,
                           UUID createdBy, Long modified,
                           UUID modifiedBy,
                           @Nonnull UUID projectId,
                           List<System> systemsList,
                           UUID categoryId,
                           UUID sourceId,
                           List<String> tags) {
        setId(uuid);
        setName(name);
        setGraylogName(graylogName);
        setDescription(description);
        setSsmSolutionAlias(ssmSolutionAlias);
        setSsmInstanceAlias(ssmInstanceAlias);
        setConsulEgressConfigPath(consulEgressConfigPath);
        setCreated(created);
        setCreatedBy(createdBy);
        setModified(modified);
        setModifiedBy(modifiedBy);
        setProjectId(projectId);
        setSystems(systemsList);
        setCategoryId(categoryId);
        setSourceId(sourceId);
        setTags(tags);
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    public EnvironmentImpl(UUID uuid, String name, String graylogName, String description, String ssmSolutionAlias,
                           String ssmInstanceAlias, String consulEgressConfigPath, Long created,
                           UUID createdBy, Long modified,
                           UUID modifiedBy,
                           @Nonnull UUID projectId,
                           List<System> systemsList,
                           UUID categoryId,
                           UUID sourceId,
                           Object tags) {
        setId(uuid);
        setName(name);
        setGraylogName(graylogName);
        setDescription(description);
        setSsmSolutionAlias(ssmSolutionAlias);
        setSsmInstanceAlias(ssmInstanceAlias);
        setConsulEgressConfigPath(consulEgressConfigPath);
        setCreated(created);
        setCreatedBy(createdBy);
        setModified(modified);
        setModifiedBy(modifiedBy);
        setProjectId(projectId);
        setSystems(systemsList);
        setCategoryId(categoryId);
        setSourceId(sourceId);
        setTags(tags == null ? Collections.emptyList() : new Gson().fromJson(tags.toString(), List.class));
    }

    @Nonnull
    @Override
    public UUID getProjectId() {
        return projectId;
    }

    @Override
    public void setProjectId(@Nonnull UUID projectId) {
        this.projectId = projectId;
    }

    @Override
    public List<System> getSystems() {
        return systemsList;
    }

    @Override
    public void setSystems(List<System> systemsList) {
        this.systemsList = systemsList;
    }

    @Nonnull
    @Override
    public UUID getCategoryId() {
        return categoryId;
    }

    @Override
    public void setCategoryId(@Nonnull UUID categoryId) {
        this.categoryId = categoryId;
    }

    @Override
    public String getGraylogName() {
        return graylogName;
    }

    @Override
    public void setGraylogName(String graylogName) {
        this.graylogName = graylogName;
    }

    @Override
    public UUID getSourceId() {
        return sourceId;
    }

    @Override
    public void setSourceId(@Nullable UUID sourceId) {
        this.sourceId = sourceId;
    }

    @Override
    public String getSsmSolutionAlias() {
        return ssmSolutionAlias;
    }

    @Override
    public void setSsmSolutionAlias(@Nullable String ssmSolutionAlias) {
        this.ssmSolutionAlias = ssmSolutionAlias;
    }

    @Override
    public String getSsmInstanceAlias() {
        return ssmInstanceAlias;
    }

    @Override
    public void setSsmInstanceAlias(@Nullable String ssmInstanceAlias) {
        this.ssmInstanceAlias = ssmInstanceAlias;
    }

    @Override
    public String getConsulEgressConfigPath() {
        return consulEgressConfigPath;
    }

    @Override
    public void setConsulEgressConfigPath(@Nullable String consulEgressConfigPath) {
        this.consulEgressConfigPath = consulEgressConfigPath;
    }

    @Override
    public List<String> getTags() {
        return tags;
    }

    @Override
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
