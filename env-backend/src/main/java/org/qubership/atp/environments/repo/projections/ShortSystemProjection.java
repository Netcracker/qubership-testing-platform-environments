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
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.ParametersGettingVersion;
import org.qubership.atp.environments.model.ServerItf;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.SystemCategory;
import org.qubership.atp.environments.model.impl.SystemImpl;
import org.qubership.atp.environments.model.utils.enums.Status;
import org.qubership.atp.environments.repo.impl.AbstractRepository;
import org.qubership.atp.environments.repo.impl.SystemRepositoryImpl;

import com.google.gson.Gson;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.MappingProjection;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressWarnings("CPD-START")
public class ShortSystemProjection extends MappingProjection<System> {

    private static final long serialVersionUID = 42L;
    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    protected final transient SystemRepositoryImpl repo;
    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    protected transient Gson gson = new Gson();

    public ShortSystemProjection(SystemRepositoryImpl repo) {
        super(System.class, AbstractRepository.SYSTEMS.all());
        this.repo = repo;
    }

    @Override
    protected System map(Tuple tuple) {
        UUID uuid = tuple.get(AbstractRepository.SYSTEMS.id);
        assert uuid != null;
        String name = tuple.get(AbstractRepository.SYSTEMS.name);
        assert name != null;
        String description = tuple.get(AbstractRepository.SYSTEMS.description);
        Timestamp created = tuple.get(AbstractRepository.SYSTEMS.created);
        UUID createdBy = tuple.get(AbstractRepository.SYSTEMS.createdBy);
        Timestamp modified = tuple.get(AbstractRepository.SYSTEMS.modified);
        UUID modifiedBy = tuple.get(AbstractRepository.SYSTEMS.modifiedBy);
        UUID systemCategoryId = tuple.get(AbstractRepository.SYSTEMS.categoryId);
        String systemStatus = tuple.get(AbstractRepository.SYSTEMS.status);
        Timestamp dateOfLastCheck = tuple.get(AbstractRepository.SYSTEMS.dateOfLastCheck);
        String version = tuple.get(AbstractRepository.SYSTEMS.version);
        Timestamp dateOfCheckVersion = tuple.get(AbstractRepository.SYSTEMS.dateOfCheckVersion);
        String parametersGettingVersion = tuple.get(AbstractRepository.SYSTEMS.parametersGettingVersion);
        UUID parentSystemId = tuple.get(AbstractRepository.SYSTEMS.parentSystemId);
        String serverItf = tuple.get(AbstractRepository.SYSTEMS.serverItf);
        Boolean mergeByName = tuple.get(AbstractRepository.SYSTEMS.mergeByName);
        UUID linkToSystemId = tuple.get(AbstractRepository.SYSTEMS.linkToSystemId);
        UUID externalId = tuple.get(AbstractRepository.SYSTEMS.externalId);
        UUID sourceId = tuple.get(AbstractRepository.SYSTEMS.sourceId);
        String externalName = tuple.get(AbstractRepository.SYSTEMS.externalName);
        return create(uuid, name, description, created, createdBy, modified, modifiedBy, systemCategoryId,
                systemStatus, dateOfLastCheck, version, dateOfCheckVersion, parametersGettingVersion,
                parentSystemId, serverItf, mergeByName, linkToSystemId, externalId, sourceId, externalName, null);
    }

    /**
     * Create System with parameters.
     *
     * @return {@link SystemImpl} instance
     */
    public SystemImpl create(UUID uuid,
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
            systemCategory = Proxies.withId(SystemCategory.class, systemCategoryId,
                    id -> repo.getSystemCategoryRepo().get()
                            .getById(id));
        }
        return create(uuid, name, description, created, createdBy, modified, modifiedBy, systemCategory, status,
                dateOfLastCheck, version, dateOfCheckVersion, parametersGettingVersion, parentSystemId,
                environments,
                serverItf, mergeByName, linkToSystemId, externalId, sourceId, externalName);
    }

    /**
     * Create System with parameters.
     *
     * @return {@link SystemImpl} instance
     */
    @SuppressFBWarnings("NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE")
    public SystemImpl create(UUID uuid,
                             String name,
                             String description,
                             Timestamp created,
                             UUID createdBy,
                             Timestamp modified,
                             UUID modifiedBy,
                             @Nullable SystemCategory systemCategory,
                             @Nullable String status,
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
        return new SystemImpl(
                uuid,
                name,
                description,
                created.getTime(),
                createdBy,
                modified == null ? null : modified.getTime(),
                modifiedBy,
                environments,
                systemCategory,
                null,
                status == null ? Status.NOTHING : Status.valueOf(status),
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
