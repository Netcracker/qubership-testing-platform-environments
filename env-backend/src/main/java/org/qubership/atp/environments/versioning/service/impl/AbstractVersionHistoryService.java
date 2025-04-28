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

package org.qubership.atp.environments.versioning.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.javers.core.Javers;
import org.javers.repository.jql.JqlQuery;
import org.javers.repository.jql.QueryBuilder;
import org.javers.shadow.Shadow;
import org.qubership.atp.environments.errorhandling.history.EnvironmentHistoryRevisionNotFoundException;
import org.qubership.atp.environments.mapper.AbstractMapper;
import org.qubership.atp.environments.service.rest.server.dto.generated.CompareEntityResponseDtoGenerated;
import org.qubership.atp.environments.versioning.model.mapper.DateAuditorEntityVersioning;
import org.qubership.atp.environments.versioning.service.VersionHistoryService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractVersionHistoryService<D extends DateAuditorEntityVersioning,S>
        implements VersionHistoryService<D,S> {

    private AbstractMapper<S, D> abstractMapper;
    private Javers javers;

    public AbstractVersionHistoryService(AbstractMapper<S, D> abstractMapper, Javers javers) {
        this.abstractMapper = abstractMapper;
        this.javers = javers;
    }

    /**
     * Returns collection of entities with requested revision numbers.
     * @param id uuid of entity in DB.
     * @param versions collection of requested revision numbers.
     * @return collection of CompareEntityResponseDtoGenerated.
     */
    public List<CompareEntityResponseDtoGenerated> getEntitiesByVersion(UUID id, List<String> versions) {

        log.debug("id={}, versions={}", id, versions);

        return versions
                .stream()
                .map(version -> getEntityByRevision(id, version))
                .collect(Collectors.toList());
    }

    private CompareEntityResponseDtoGenerated getEntityByRevision(UUID id, String version) {

        JqlQuery query = QueryBuilder.byInstanceId(id, getEntityClass())
                .withNewObjectChanges()
                .withVersion(Long.parseLong(version))
                .build();

        List<Shadow<Object>> shadows = javers.findShadows(query);
        log.debug("Shadows found : {}", shadows);

        Optional<Shadow<Object>> entity = shadows.stream().findFirst();

        if (entity.isPresent()) {
            return createCompareEntityResponse(version, entity);
        } else {
            log.error("Failed to find history revision");
            throw new EnvironmentHistoryRevisionNotFoundException();
        }
    }

    protected CompareEntityResponseDtoGenerated createCompareEntityResponse(String version,
                                                                            Optional<Shadow<Object>> entity) {

        log.debug("version={}, entity={}", version, entity);
        CompareEntityResponseDtoGenerated compareEntityResponseDto =
                new CompareEntityResponseDtoGenerated();
        compareEntityResponseDto.setRevision(version);
        Shadow<Object> objectShadow = entity.get();
        D resolvedEntity = mapToResolvedEntity((S) objectShadow.get());
        resolvedEntity.setModifiedBy(objectShadow.getCommitMetadata().getAuthor());
        compareEntityResponseDto.setCompareEntity(resolvedEntity);

        log.debug("compareEntityResponseDto={}", compareEntityResponseDto);
        return compareEntityResponseDto;
    }

    protected D mapToResolvedEntity(S entityJ) {
        return abstractMapper.map(entityJ);
    }
}
