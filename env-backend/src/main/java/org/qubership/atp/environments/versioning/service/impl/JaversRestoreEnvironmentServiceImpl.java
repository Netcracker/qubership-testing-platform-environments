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

import static org.qubership.atp.environments.repo.impl.AbstractRepository.ENVIRONMENTS;

import java.sql.Timestamp;
import java.util.UUID;

import javax.validation.Validator;

import org.javers.core.Javers;
import org.qubership.atp.auth.springbootstarter.entities.UserInfo;
import org.qubership.atp.auth.springbootstarter.ssl.Provider;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.repo.impl.EnvironmentRepositoryImpl;
import org.qubership.atp.environments.utils.DateTimeUtil;
import org.qubership.atp.environments.versioning.model.entities.EnvironmentJ;
import org.qubership.atp.environments.versioning.service.CommitEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.querydsl.sql.SQLQueryFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JaversRestoreEnvironmentServiceImpl extends AbstractJaversRestoreService<EnvironmentJ> {

    private final SQLQueryFactory queryFactory;
    private final EnvironmentRepositoryImpl environmentRepository;

    /**
     * Constructor.
     */
    @Autowired
    public JaversRestoreEnvironmentServiceImpl(Javers javers,
                                               Validator validator,
                                               CommitEntityService<Environment> commitEntityService,
                                               DateTimeUtil dateTimeUtil,
                                               Provider<UserInfo> userInfoProvider,
                                               SQLQueryFactory queryFactory,
                                               EnvironmentRepositoryImpl environmentRepository) {
        super(javers, validator, commitEntityService, dateTimeUtil, userInfoProvider);
        this.queryFactory = queryFactory;
        this.environmentRepository = environmentRepository;
    }

    @Override
    void restoreEntity(EnvironmentJ entity, Timestamp modified, UUID modifiedBy) {
        log.debug("Restoring environment: updating simple fields: {}", entity);
        UUID environmentId = entity.getId();
        queryFactory.update(ENVIRONMENTS)
                .set(ENVIRONMENTS.name, entity.getName())
                .set(ENVIRONMENTS.description, entity.getDescription())
                .set(ENVIRONMENTS.modified, modified)
                .set(ENVIRONMENTS.modifiedBy, modifiedBy)
                .set(ENVIRONMENTS.projectId, entity.getProjectId())
                .set(ENVIRONMENTS.categoryId, entity.getCategoryId())
                .set(ENVIRONMENTS.tags, environmentRepository.createTagsPgObject(entity.getTags()))
                .where(ENVIRONMENTS.id.eq(environmentId))
                .execute();
        commitEntityService.commit(environmentRepository.getById(environmentId));
    }

    @Override
    public Class<EnvironmentJ> getEntityType() {
        return EnvironmentJ.class;
    }
}
