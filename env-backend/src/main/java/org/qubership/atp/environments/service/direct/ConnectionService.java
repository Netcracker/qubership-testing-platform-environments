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

import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.ConnectionParameters;

public interface ConnectionService extends IdentifiedService<Connection> {

    @Nonnull
    Connection create(UUID systemId,
                      String name,
                      String description,
                      ConnectionParameters parameters,
                      String connectionType,
                      UUID sourceTemplateId,
                      UUID projectId,
                      List<String> services);

    @Nonnull
    Connection create(UUID systemId,
                      UUID connectionId,
                      String name,
                      String description,
                      ConnectionParameters parameters,
                      String connectionType,
                      UUID sourceTemplateId,
                      UUID projectId,
                      List<String> services, UUID sourceId);

    @Nonnull
    Connection create(UUID systemId,
                      String name,
                      String description,
                      ConnectionParameters parameters,
                      String connectionType,
                      UUID sourceTemplateId,
                      List<String> services);

    @Nonnull
    Connection replicate(@Nonnull UUID systemId,
                         UUID connectionId,
                         @Nonnull String name,
                         String description,
                         ConnectionParameters parameters,
                         String connectionType,
                         UUID sourceTemplateId,
                         List<String> services,
                         UUID sourceId);

    /**
     * Getting a list of project connections.
     *
     * @param projectId Project identifier
     * @return list of names
     */
    List<Connection> getConnectionsByProjectId(@Nonnull UUID projectId);

    Connection update(UUID id,
                      UUID systemId,
                      String name,
                      String description,
                      ConnectionParameters parameters,
                      String connectionType,
                      UUID sourceTemplateId,
                      UUID projectId,
                      List<String> services,
                      UUID sourceId);

    Connection update(UUID id,
                      UUID systemId,
                      String name,
                      String description,
                      ConnectionParameters parameters,
                      String connectionType,
                      UUID sourceTemplateId,
                      UUID projectId,
                      List<String> services);

    Connection update(UUID id,
                      UUID systemId,
                      String name,
                      String description,
                      ConnectionParameters parameters,
                      String connectionType,
                      UUID sourceTemplateId,
                      List<String> services);

    Connection update(Connection connection);

    List<Connection> getAll(List<UUID> environmentIds, UUID systemCategoryId);

    List<Connection> getConnectionTemplates();

    Connection getConnectionTemplateByName(String name);

    Connection getByParentAndName(UUID systemId, String name);

    List<Connection> getConnectionByHost(String host);

    void delete(UUID id);

    void updateParameters(UUID id, ConnectionParameters parameters, List<String> services);

    void validateTaEngineProviderParameters(UUID sourceTemplateId, ConnectionParameters parameters);

    List<Connection> getByIds(List<UUID> ids);

    UUID getProjectId(UUID connectionId);
}
