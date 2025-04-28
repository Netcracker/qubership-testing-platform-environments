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

package org.qubership.atp.environments.mapper;

import static java.util.Objects.isNull;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.qubership.atp.environments.model.Identified;
import org.qubership.atp.environments.model.Named;
import org.qubership.atp.environments.service.direct.IdentifiedService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractVersioningMapper<S, D> extends AbstractMapper<S, D> {

    private static final String UNDEFINED = "Undefined name";

    public AbstractVersioningMapper(Class<S> sourceClass, Class<D> destinationClass) {
        super(sourceClass, destinationClass);
    }

    @PostConstruct
    public void init() {
        mapper.createTypeMap(sourceClass, destinationClass).setPostConverter(mapConverter());
    }

    protected <T extends Named & Identified> String getAbstractEntityName(UUID uuid, IdentifiedService<T> service) {
        if (isNull(uuid)) {
            return null;
        }
        return Stream.of(uuid)
                .map(id -> getEntityById(id, service))
                .filter(Objects::nonNull)
                .map(Named::getName)
                .filter(name -> !name.isEmpty())
                .findFirst()
                .orElse(UNDEFINED);
    }

    protected <T extends Named & Identified> List<String> getAbstractEntityNames(Collection<UUID> uuids,
                                                                                 IdentifiedService<T> service) {
        if (isEmpty(uuids)) {
            return null;
        }
        return uuids.stream()
                .map(uuid -> getEntityById(uuid, service))
                .filter(Objects::nonNull)
                .map(Named::getName)
                .map(name -> !name.isEmpty() ? name : UNDEFINED)
                .collect(Collectors.toList());
    }

    private <T extends Named & Identified> T getEntityById(UUID uuid, IdentifiedService<T> service) {
        T entity;
        try {
            entity = service.get(uuid);
        } catch (Exception e) {
            log.error("Can't get entity. Cause: {}", e.getMessage());
            return null;
        }
        return entity;
    }
}
