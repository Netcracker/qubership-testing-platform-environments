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
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.ParametersGettingVersion;
import org.qubership.atp.environments.model.ServerItf;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.utils.enums.Status;
import org.qubership.atp.environments.service.rest.server.dto.ConnectionDto;
import org.qubership.atp.environments.service.rest.server.dto.CreateSystemDto;
import org.qubership.atp.environments.service.rest.server.dto.SharingRequestDto;
import org.qubership.atp.environments.service.rest.server.dto.StatusDto;
import org.qubership.atp.environments.service.rest.server.dto.SystemDto;
import org.qubership.atp.environments.service.rest.server.response.ShortExternalService;
import org.qubership.atp.environments.utils.cloud.ExternalCloudClient;
import org.springframework.transaction.annotation.Transactional;

public interface SystemService extends IdentifiedService<System> {

    @Nullable
    System getV2(UUID id);

    @Nonnull
    System replicate(@Nonnull UUID environmentId,
                     @Nonnull UUID systemId,
                     @Nonnull String name,
                     String description,
                     UUID systemCategoryId,
                     ParametersGettingVersion parametersGettingVersion,
                     UUID parentSystemId,
                     ServerItf serverItf,
                     UUID linkToSystemId,
                     UUID externalId,
                     UUID sourceId,
                     String externalName);

    @Nonnull
    System create(UUID environmentId,
                  String name,
                  String description,
                  UUID systemCategoryId,
                  ParametersGettingVersion parametersGettingVersion,
                  UUID parentSystemId,
                  ServerItf serverItf,
                  Boolean mergeByName,
                  UUID linkToSystemId,
                  UUID externalId,
                  String externalName);

    @Nonnull
    System create(UUID environmentId,
                  CreateSystemDto systemDto);

    @Nonnull
    Connection create(UUID systemId,
                      ConnectionDto connection);

    @Nonnull
    System copy(UUID systemId,
                UUID environmentId,
                String name,
                String description,
                UUID systemCategoryId,
                ParametersGettingVersion parametersGettingVersion,
                UUID parentSystemId,
                ServerItf serverItf,
                Boolean mergeByName,
                UUID linkToSystemId,
                UUID externalId,
                String externalName);

    @Nonnull
    System shareProcessing(UUID systemId, SharingRequestDto sharingRequestDto);

    @Nonnull
    System share(System system, List<UUID> environmentId);

    @Transactional
    System unShare(UUID systemId, List<UUID>  environmentIds);

    List<Connection> updateOpenshiftRoute(UUID environmentId);

    List<Connection> updateOpenshiftRoute(UUID systemId, UUID environmentId);

    System update(UUID id,
                  String name,
                  String description,
                  UUID systemCategoryId,
                  ParametersGettingVersion getParametersGettingVersion,
                  UUID parentSystemId,
                  ServerItf serverItf,
                  Boolean mergeByName,
                  UUID linkToSystemId,
                  UUID externalId,
                  String externalName);

    System update(System system);

    System update(SystemDto systemDto);

    System getCachedVersionBySystem(System system);

    void delete(UUID id, UUID environmentId);

    void deleteLinkedServices(UUID parentSystemId, UUID environmentId);

    void deleteSystemsByIds(List<UUID> systemIds, UUID environmentId);

    List<Connection> getConnections(UUID systemId);

    Connection getConnectionBySystemIdAndConnectionType(UUID systemId, UUID connectionType);

    System saveStatusAndDateOfLastCheck(UUID id, Status status);

    List<System> saveStatusesAndDateOfLastCheck(List<StatusDto> statuses, UUID projectId);

    System saveVersionAndDateOfLastCheck(System system, String version);

    Object getCachedVersionById(UUID id);

    System updateVersionBySystemId(UUID systemId, boolean updateCache);

    System updateParametersGettingVersion(UUID systemId, ParametersGettingVersion parametersGettingVersion);

    System transformSystemVersionToHtml(System system);

    List<System> updateVersionByEnvironmentId(UUID environmentId);

    List<System> getByIds(List<UUID> systems);

    List<System> createListFromCloudServer(List<UUID> serviceIds, UUID id, UUID environmentId,
                                           Class<? extends ExternalCloudClient> clientClass);

    List<System> updateServicesFromCloudServer(UUID id, Class<? extends ExternalCloudClient> clientClass);

    List<ShortExternalService> getShortExternalServices(UUID systemId,
                                                        Class<? extends ExternalCloudClient> clientClass);

    List<System> getLinkedSystemByParentId(UUID systemId);

    System getBySourceId(UUID sourceId);

    List<System> getSystemsByProjectId(UUID id);

    List<System> getSystemsByProjectIdAndCategoryName(UUID id, String name);

    System getShortSystem(UUID id);

    System getSystemByNameAndEnvironmentId(String name, UUID id);

    /**
     * Generates YAML strings for systems in envgene format.
     * 
     * @param systems collection of systems to process
     * @return array with [deploymentParamsYaml, credentialsYaml]
     */
    @Nonnull
    String[] generateSystemsYaml(@Nonnull Collection<System> systems);
}
