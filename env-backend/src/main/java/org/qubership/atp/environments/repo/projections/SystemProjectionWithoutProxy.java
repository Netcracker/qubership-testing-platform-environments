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
import java.util.UUID;

import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.repo.impl.AbstractRepository;
import org.qubership.atp.environments.repo.impl.SystemRepositoryImpl;

import com.querydsl.core.Tuple;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressWarnings("CPD-START")
public class SystemProjectionWithoutProxy extends FullSystemProjection {

    private static final long serialVersionUID = 42L;

    public SystemProjectionWithoutProxy(SystemRepositoryImpl repo) {
        super(repo);
    }

    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
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
        return createWithoutProxy(uuid, name, description, created, createdBy, modified, modifiedBy, systemCategoryId,
                systemStatus, dateOfLastCheck, version, dateOfCheckVersion, parametersGettingVersion,
                parentSystemId, serverItf, mergeByName, linkToSystemId, externalId, sourceId, externalName, null);
    }
}
