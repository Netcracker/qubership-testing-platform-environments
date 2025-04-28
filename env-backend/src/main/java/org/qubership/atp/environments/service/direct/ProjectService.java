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

package org.qubership.atp.environments.service.direct;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.qubership.atp.auth.springbootstarter.exceptions.AtpException;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.Project;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.service.rest.server.dto.EnvironmentDto;
import org.qubership.atp.environments.service.rest.server.request.ProjectSearchRequest;

public interface ProjectService extends IdentifiedService<Project> {

    @Nullable
    Project getShort(@Nonnull UUID id);

    @Nonnull
    List<Project> getAllShort();

    @Nonnull
    Project create(Project project);

    @Nonnull
    Environment create(UUID projectId, EnvironmentDto environment, UUID categoryId);

    @Nonnull
    Project replicate(@Nonnull UUID projectId, @Nonnull String name, String shortName, String description, Long created)
            throws AtpException;

    @Nonnull
    Project createAsIs(Project project);

    @Nonnull
    Project copy(UUID id, String name, String shortName, String description);

    @Nonnull
    void update(UUID id, String name, String shortName, String description);

    @Nonnull
    void update(UUID id, String name, String shortName);

    void delete(UUID projectId);

    List<Environment> getEnvironments(UUID projectId);

    List<Environment> getShortEnvironments(UUID projectId);

    List<Environment> getTemporaryEnvironments(UUID projectId);

    List<Environment> getAllEnvironments(UUID projectId);

    List<Environment> getTools(UUID projectId);

    List<Environment> getShortTools(UUID projectId);

    List<System> getSystemsByProjectId(UUID projectId);

    List<System> getSystemsByProjectIdAndCategoryName(UUID projectId, String categoryName);

    List<Connection> getConnections(UUID projectId);

    List<String> getSystemNames(UUID projectId);

    List<String> getConnectionNames(UUID projectId);

    List<Project> getProjectsByHost(String host);

    Project getProjectWithSpecifiedEnvironments(UUID projectId, List<UUID> environmentIds);

    Project getShortByName(String name);

    List<Project> getProjectsByRequest(ProjectSearchRequest request);
}
