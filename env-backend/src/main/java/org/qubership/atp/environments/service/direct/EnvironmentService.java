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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.service.rest.server.dto.BaseSearchRequestDto;
import org.qubership.atp.environments.service.rest.server.dto.CreateSystemDto;
import org.qubership.atp.environments.service.rest.server.dto.SystemTemporaryDto;
import org.qubership.atp.environments.service.rest.server.request.EnvironmentsWithFilterRequest;
import org.qubership.atp.environments.service.rest.server.request.ValidateTaToolsRequest;
import org.qubership.atp.environments.service.rest.server.response.GroupedByTagEnvironmentResponse;
import org.qubership.atp.environments.service.rest.server.response.ValidateTaToolsResponse;

public interface EnvironmentService extends IdentifiedService<Environment> {

    @Nullable
    String getEnvironmentNameById(@Nonnull UUID id);

    Optional<Environment> getOrElse(@Nonnull UUID id);

    @Nonnull
    List<Environment> getAll(UUID categoryId);

    @Nonnull
    Environment create(UUID projectId,
                       String name,
                       String graylogName,
                       String description,
                       String ssmSolutionAlias,
                       String ssmInstanceAlias,
                       String consulEgressConfigPath,
                       UUID categoryId,
                       List<String> tags);

    @Nonnull
    System create(UUID environmentId,
                  CreateSystemDto systemDto);

    @Nonnull
    Environment replicate(@Nonnull UUID projectId,
                          @Nonnull UUID environmentId,
                          String name,
                          String graylogName,
                          String description,
                          String ssmSolutionAlias,
                          String ssmInstanceAlias,
                          String consulEgressConfigPath,
                          UUID categoryId,
                          UUID sourceId,
                          List<String> tags);

    @Nonnull
    Environment copy(UUID id,
                     UUID projectId,
                     String name,
                     String graylogName,
                     String description,
                     String ssmSolutionAlias,
                     String ssmInstanceAlias,
                     String consulEgressConfigPath,
                     UUID category,
                     List<String> tags);

    @Nonnull
    void update(UUID id,
                String name,
                String graylogName,
                String description,
                String ssmSolutionAlias,
                String ssmInstanceAlias,
                String consulEgressConfigPath,
                UUID projectId,
                UUID categoryId,
                List<String> tags);

    void update(Environment environment);

    void delete(UUID environmentId);

    List<System> getSystems(UUID environmentId);

    List<System> getSystems(UUID environmentId, String systemType);

    List<System> getShortSystems(UUID environmentId);

    Collection<System> getSystemsV2(UUID environmentId);

    Collection<System> getSystemsV2(UUID environmentId, String systemType);

    System createSystem(UUID environmentId, System system);

    Environment temporary(UUID id, List<SystemTemporaryDto> systemUpdateList);

    UUID getProjectIdBySystemId(@Nonnull UUID systemId);

    UUID getProjectIdByEnvironmentId(@Nonnull UUID environmentId);

    List<Environment> getByProjectId(@Nonnull UUID projectId);

    Environment getByNameAndProjectId(String name, UUID projectId);

    List<Environment> findBySearchRequest(BaseSearchRequestDto searchRequest) throws Exception;

    Environment getBySourceIdAndProjectId(UUID sourceId, UUID projectId);

    List<Environment> getByIds(List<UUID> environmentIds);

    String getHtmlVersionByEnvironments(List<UUID> ids);

    List<Connection> getConnections(UUID environmentId);

    ValidateTaToolsResponse validateTaTools(ValidateTaToolsRequest request);

    List<Environment> getEnvironmentsByFilterRequest(EnvironmentsWithFilterRequest request, Integer page,
                                                     Integer size);

    Collection<GroupedByTagEnvironmentResponse> getGroupedByTagEnvironments(UUID projectId);

    long getEnvironmentsCountByFilter(EnvironmentsWithFilterRequest request);
}
