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

import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.javers.core.Javers;
import org.javers.repository.jql.JqlQuery;
import org.javers.repository.jql.QueryBuilder;
import org.qubership.atp.auth.springbootstarter.entities.UserInfo;
import org.qubership.atp.auth.springbootstarter.ssl.Provider;
import org.qubership.atp.environments.errorhandling.history.EnvironmentHistoryValidationException;
import org.qubership.atp.environments.model.utils.HazelcastMapName;
import org.qubership.atp.environments.utils.DateTimeUtil;
import org.qubership.atp.environments.versioning.exception.EntityIdNotFound;
import org.qubership.atp.environments.versioning.model.entities.AbstractJaversEntity;
import org.qubership.atp.environments.versioning.service.CommitEntityService;
import org.qubership.atp.environments.versioning.service.JaversRestoreService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractJaversRestoreService<E extends AbstractJaversEntity> implements JaversRestoreService<E> {

    protected final Javers javers;
    protected final Validator validator;
    protected final CommitEntityService commitEntityService;
    private final DateTimeUtil dateTimeUtil;
    private final Provider<UserInfo> userInfoProvider;

    protected AbstractJaversRestoreService(Javers javers, Validator validator,
                                           CommitEntityService commitEntityService,
                                           DateTimeUtil dateTimeUtil, Provider<UserInfo> userInfoProvider) {
        this.javers = javers;
        this.validator = validator;
        this.commitEntityService = commitEntityService;
        this.dateTimeUtil = dateTimeUtil;
        this.userInfoProvider = userInfoProvider;
    }

    @Transactional
    @Override
    @CacheEvict(value = HazelcastMapName.CONNECTIONS_BY_SYSTEM_ID, key = "#id", condition = "#id!=null")
    public void restore(Class<E> entityClass, UUID id, Integer revisionId) {
        log.debug("Starting restore {} with id = {} and revision = {}", entityClass.getSimpleName(), id, revisionId);
        E entity = findEntity(entityClass, id, revisionId);
        validateEntity(entity);
        Timestamp modified = new Timestamp(dateTimeUtil.timestampAsUtc());
        UUID userId = userInfoProvider.get().getId();
        restoreEntity(entity, modified, userId);
        log.debug("Restored {} with id = {} and revision = {}", entityClass.getSimpleName(), id, revisionId);
    }

    abstract void restoreEntity(E entity, Timestamp modified, UUID modifiedBy);

    private E findEntity(Class<E> entityClass, UUID id, Integer revisionId) {
        JqlQuery query = QueryBuilder
                .byInstanceId(id, entityClass)
                .withVersion(revisionId)
                .build();
        Object javersEntity = javers.findShadows(query)
                .stream()
                .findFirst()
                .orElseThrow(EntityIdNotFound::new)
                .get();
        return entityClass.cast(javersEntity);
    }

    private void validateEntity(E entity) {
        Set<ConstraintViolation<Object>> violations = validator.validate(entity);
        if (!violations.isEmpty()) {
            log.error("Validation failed for Javers entity: {}. Violations: {}", entity, violations);
            throw new EnvironmentHistoryValidationException();
        }
    }
}
