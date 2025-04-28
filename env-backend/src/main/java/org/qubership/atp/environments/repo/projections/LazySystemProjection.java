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

package org.qubership.atp.environments.repo.projections;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.qubership.atp.environments.db.Proxies;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.ParametersGettingVersion;
import org.qubership.atp.environments.model.ServerItf;
import org.qubership.atp.environments.model.SystemCategory;
import org.qubership.atp.environments.model.impl.SystemImpl;
import org.qubership.atp.environments.model.utils.enums.Status;
import org.qubership.atp.environments.repo.impl.SystemRepositoryImpl;

import com.google.gson.Gson;

@SuppressWarnings("CPD-START")
public class LazySystemProjection extends ShortSystemProjection {

    private static final long serialVersionUID = 42L;

    public LazySystemProjection(SystemRepositoryImpl repo) {
        super(repo);
    }

    /**
     * Create System with parameters but without proxy.
     *
     * @return {@link SystemImpl} instance
     */
    public SystemImpl createWithoutProxy(UUID uuid,
                                         String name,
                                         String description,
                                         Timestamp created,
                                         UUID createdBy,
                                         Timestamp modified,
                                         UUID modifiedBy,
                                         @Nullable UUID systemCategoryId,
                                         String status,
                                         Timestamp dateOfLastCheck,
                                         String version,
                                         Timestamp dateOfCheckVersion,
                                         String parametersGettingVersion,
                                         @Nullable UUID parentSystemId,
                                         String serverItf,
                                         Boolean mergeByName,
                                         UUID linkToSystemId,
                                         UUID externalId,
                                         UUID sourceId,
                                         String externalName,
                                         List<Environment> environments) {
        SystemCategory systemCategory = null;
        if (systemCategoryId != null) {
            systemCategory = repo.getSystemCategoryRepo().get().getById(systemCategoryId);
        }
        List<Connection> connectionsListId = repo.getConnectionRepo().get().getAllShortByParentId(uuid);
        Status systemStatus = status == null ? Status.NOTHING : Status.valueOf(status);
        return new SystemImpl(uuid, name, description,
                created.getTime(),
                createdBy,
                modified == null ? null : modified.getTime(),
                modifiedBy,
                environments == null ? repo.getEnvironmentRepo().get().getAllBySystemId(uuid) : environments,
                systemCategory,
                connectionsListId,
                systemStatus,
                dateOfLastCheck == null ? null : dateOfLastCheck.getTime(),
                version,
                dateOfCheckVersion == null ? null : dateOfCheckVersion.getTime(),
                new Gson().fromJson(parametersGettingVersion, ParametersGettingVersion.class),
                parentSystemId,
                new Gson().fromJson(serverItf, ServerItf.class),
                mergeByName,
                linkToSystemId,
                externalId,
                sourceId,
                externalName);
    }

    @Override
    public SystemImpl create(UUID uuid,
                             String name,
                             String description,
                             Timestamp created,
                             UUID createdBy,
                             Timestamp modified,
                             UUID modifiedBy,
                             @Nullable SystemCategory systemCategory,
                             String status,
                             Timestamp dateOfLastCheck,
                             String version,
                             Timestamp dateOfCheckVersion,
                             String parametersGettingVersion,
                             @Nullable UUID parentSystemId,
                             @Nullable List<Environment> environments,
                             String serverItf,
                             Boolean mergeByName,
                             UUID linkToSystemId,
                             UUID externalId,
                             UUID sourceId,
                             String externalName) {
        if (environments == null) {
            environments = Proxies.list(() -> repo.getEnvironmentRepo()
                    .get().getAllShortBySystemId(uuid));
        }
        List<Connection> connectionsListId = Proxies.list(()
                -> repo.getConnectionRepo().get().getAllShortByParentId(uuid));
        Status systemStatus = status == null ? Status.NOTHING : Status.valueOf(status);
        return new SystemImpl(uuid, name, description,
                created.getTime(),
                createdBy,
                modified == null ? null : modified.getTime(),
                modifiedBy,
                environments,
                systemCategory,
                connectionsListId,
                systemStatus,
                dateOfLastCheck == null ? null : dateOfLastCheck.getTime(),
                version,
                dateOfCheckVersion == null ? null : dateOfCheckVersion.getTime(),
                gson.fromJson(parametersGettingVersion, ParametersGettingVersion.class),
                parentSystemId,
                gson.fromJson(serverItf, ServerItf.class),
                mergeByName,
                linkToSystemId,
                externalId,
                sourceId,
                externalName);
    }
}
