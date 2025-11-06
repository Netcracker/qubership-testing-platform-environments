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

package org.qubership.atp.environments.repo.impl;

import static java.util.Objects.nonNull;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Provider;

import org.postgresql.util.PGobject;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.impl.Context;
import org.qubership.atp.environments.model.impl.EnvironmentImpl;
import org.qubership.atp.environments.model.utils.HazelcastMapName;
import org.qubership.atp.environments.repo.mapper.EnvironmentMapper;
import org.qubership.atp.environments.repo.projections.FullEnvironmentProjection;
import org.qubership.atp.environments.repo.projections.GenericEnvironmentProjection;
import org.qubership.atp.environments.repo.projections.LazyEnvironmentProjection;
import org.qubership.atp.environments.repo.projections.ShortEnvironmentProjection;
import org.qubership.atp.environments.service.direct.KafkaService;
import org.qubership.atp.environments.service.rest.server.dto.BaseSearchRequestDto;
import org.qubership.atp.environments.service.rest.server.dto.EventType;
import org.qubership.atp.environments.service.rest.server.request.EnvironmentsWithFilterRequest;
import org.qubership.atp.environments.versioning.service.CommitEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.MappingProjection;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
@SuppressWarnings("CPD-START")
public class EnvironmentRepositoryImpl extends AbstractRepository implements ProjectionRepository<Environment> {

    private final SQLQueryFactory queryFactory;
    private final Provider<ProjectRepositoryImpl> projectRepo;
    private final Provider<SystemRepositoryImpl> systemRepo;
    private final Provider<ConnectionRepositoryImpl> connectionRepo;
    private final Provider<SystemCategoryRepositoryImpl> systemCategoryRepo;
    private final CommitEntityService<Environment> commitEntityService;
    private final KafkaService kafkaService;
    private final ContextRepository contextRepository;

    private final FullEnvironmentProjection projection = new FullEnvironmentProjection(this);
    private final LazyEnvironmentProjection lazyProjection = new LazyEnvironmentProjection(this);
    private final ShortEnvironmentProjection shortProjection = new ShortEnvironmentProjection(this);
    private final EnvironmentMapper environmentMapper;

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Autowired
    public EnvironmentRepositoryImpl(SQLQueryFactory queryFactory,
                                     Provider<ProjectRepositoryImpl> projectRepo,
                                     Provider<SystemRepositoryImpl> systemRepo,
                                     Provider<ConnectionRepositoryImpl> connectionRepo,
                                     Provider<SystemCategoryRepositoryImpl> systemCategoryRepo,
                                     CommitEntityService<Environment> commitEntityService,
                                     KafkaService kafkaService, ContextRepository contextRepository,
                                     EnvironmentMapper mapper) {
        this.queryFactory = queryFactory;
        this.projectRepo = projectRepo;
        this.systemRepo = systemRepo;
        this.connectionRepo = connectionRepo;
        this.systemCategoryRepo = systemCategoryRepo;
        this.commitEntityService = commitEntityService;
        this.kafkaService = kafkaService;
        this.contextRepository = contextRepository;
        this.environmentMapper = mapper;
    }

    public Provider<ProjectRepositoryImpl> getProjectRepo() {
        return projectRepo;
    }

    public Provider<SystemRepositoryImpl> getSystemRepo() {
        return systemRepo;
    }

    /**
     * TODO.
     */
    @Nullable
    public Environment getById(@Nonnull UUID id) {
        return queryFactory.select(resolveProjection()).from(ENVIRONMENTS).where(ENVIRONMENTS.id.eq(id)).fetchOne();
    }

    @Nullable
    public String getNameById(@Nonnull UUID id) {
        return queryFactory.select(ENVIRONMENTS.name).from(ENVIRONMENTS).where(ENVIRONMENTS.id.eq(id)).fetchOne();
    }

    public boolean existsById(@Nonnull UUID id) {
        return queryFactory.select(shortProjection).from(ENVIRONMENTS).where(ENVIRONMENTS.id.eq(id)).fetchCount() > 0;
    }

    @Nonnull
    public List<Environment> getAll() {
        return queryFactory.select(resolveProjection()).from(ENVIRONMENTS).orderBy(ENVIRONMENTS.name.asc()).fetch();
    }

    /**
     * Returns environments by categoryId.
     */
    @Nonnull
    public List<Environment> getAll(UUID categoryId) {
        return queryFactory.select(resolveProjection())
                .from(ENVIRONMENTS)
                .orderBy(ENVIRONMENTS.name.asc())
                .where(ENVIRONMENTS.categoryId.eq(categoryId)).fetch();
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nullable
    public List<Environment> getAllByParentId(@Nonnull UUID projectId) {
        return queryFactory.select(resolveProjection())
                .from(ENVIRONMENTS)
                .orderBy(ENVIRONMENTS.name.asc())
                .where(ENVIRONMENTS.projectId.eq(projectId)).fetch();
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nullable
    public List<Environment> getAllByParentId(@Nonnull UUID projectId, @Nonnull UUID... categoryIds) {
        return queryFactory.select(resolveProjection())
                .from(ENVIRONMENTS)
                .orderBy(ENVIRONMENTS.name.asc())
                .where(ENVIRONMENTS.projectId.eq(projectId).and(ENVIRONMENTS.categoryId.in(categoryIds))).fetch();
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nullable
    public List<Environment> getAllShortByParentId(@Nonnull UUID projectId, @Nonnull UUID... categoryIds) {
        return queryFactory.select(shortProjection)
                .from(ENVIRONMENTS)
                .orderBy(ENVIRONMENTS.name.asc())
                .where(ENVIRONMENTS.projectId.eq(projectId).and(ENVIRONMENTS.categoryId.in(categoryIds))).fetch();
    }

    /**
     * Getting a list of project system names.
     *
     * @param projectId Project identifier
     * @return list of system names
     */
    public List<String> getSystemNamesByProjectId(@Nonnull UUID projectId) {
        List<String> systemNames = new ArrayList<>();
        systemNames.addAll(systemRepo.get().getSystemNamesByProjectId(projectId));
        return systemNames;
    }

    /**
     * Getting a list of project connection names.
     *
     * @param projectId Project identifier
     * @return list of connection names
     */
    public List<String> getConnectionNamesByProjectId(UUID projectId) {
        List<String> connectionNames = new ArrayList<>();
        connectionNames.addAll(connectionRepo.get().getConnectionNameByProjectId(projectId));
        return connectionNames;
    }

    /**
     * Creates an environment with manual {@link UUID}.
     *
     * @param environmentId environmentId
     * @param name          name
     * @param description   description
     * @param created       created
     * @param createdBy     createdBy
     * @param projectId     projectId
     * @param categoryId    categoryId
     * @param sourceId      sourceId
     * @return an istance of {@link Environment}
     */
    @Nonnull
    public Environment create(@Nonnull UUID environmentId,
                              @Nonnull String name,
                              @Nonnull String graylogName,
                              String description,
                              String ssmSolutionAlias,
                              String ssmInstanceAlias,
                              String consulEgressConfigPath,
                              @Nonnull Long created,
                              @Nonnull UUID createdBy,
                              @Nonnull UUID projectId,
                              @Nonnull UUID categoryId,
                              UUID sourceId,
                              List<String> tags) {
        Timestamp createdTimestamp = new Timestamp(created);
        queryFactory.insert(ENVIRONMENTS)
                .set(ENVIRONMENTS.name, name)
                .set(ENVIRONMENTS.graylogName, graylogName)
                .set(ENVIRONMENTS.description, description)
                .set(ENVIRONMENTS.ssmSolutionAlias, ssmSolutionAlias)
                .set(ENVIRONMENTS.ssmInstanceAlias, ssmInstanceAlias)
                .set(ENVIRONMENTS.consulEgressConfigPath, consulEgressConfigPath)
                .set(ENVIRONMENTS.created, createdTimestamp)
                .set(ENVIRONMENTS.createdBy, createdBy)
                .set(ENVIRONMENTS.modified, createdTimestamp)
                .set(ENVIRONMENTS.modifiedBy, createdBy)
                .set(ENVIRONMENTS.projectId, projectId)
                .set(ENVIRONMENTS.categoryId, categoryId)
                .set(ENVIRONMENTS.id, environmentId)
                .set(ENVIRONMENTS.sourceId, sourceId)
                .set(ENVIRONMENTS.tags, createTagsPgObject(tags))
                .execute();
        updateProjects(projectId, createdTimestamp);
        EnvironmentImpl environment = projection.create(environmentId, name, graylogName, description, ssmSolutionAlias,
                ssmInstanceAlias, consulEgressConfigPath, createdTimestamp, createdBy, createdTimestamp,
                createdBy, projectId,
                categoryId,
                sourceId, tags);
        commitEntityService.commit(environment);
        return environment;
    }

    /**
     * Creates an environment with autogenerated {@link UUID}.
     *
     * @param name        name
     * @param description description
     * @param created     created
     * @param createdBy   createdBy
     * @param projectId   projectId
     * @param categoryId  categoryId
     * @return an istance of {@link Environment}
     */
    @Nonnull
    public Environment create(@Nonnull String name,
                              @Nonnull String graylogName,
                              String description,
                              String ssmSolutionAlias,
                              String ssmInstanceAlias,
                              String consulEgressConfigPath,
                              @Nonnull Long created,
                              @Nonnull UUID createdBy,
                              @Nonnull UUID projectId,
                              @Nonnull UUID categoryId,
                              List<String> tags) {
        Timestamp createdTimestamp = new Timestamp(created);
        UUID uuid = queryFactory.insert(ENVIRONMENTS)
                .set(ENVIRONMENTS.name, name)
                .set(ENVIRONMENTS.graylogName, graylogName)
                .set(ENVIRONMENTS.description, description)
                .set(ENVIRONMENTS.ssmSolutionAlias, ssmSolutionAlias)
                .set(ENVIRONMENTS.ssmInstanceAlias, ssmInstanceAlias)
                .set(ENVIRONMENTS.consulEgressConfigPath, consulEgressConfigPath)
                .set(ENVIRONMENTS.created, createdTimestamp)
                .set(ENVIRONMENTS.createdBy, createdBy)
                .set(ENVIRONMENTS.modified, createdTimestamp)
                .set(ENVIRONMENTS.modifiedBy, createdBy)
                .set(ENVIRONMENTS.projectId, projectId)
                .set(ENVIRONMENTS.categoryId, categoryId)
                .set(ENVIRONMENTS.tags, createTagsPgObject(tags))
                .executeWithKey(ENVIRONMENTS.id);
        updateProjects(projectId, createdTimestamp);
        EnvironmentImpl environment = projection.create(uuid, name, graylogName, description, ssmSolutionAlias,
                ssmInstanceAlias, consulEgressConfigPath, createdTimestamp, createdBy, createdTimestamp,
                createdBy, projectId,
                categoryId,
                null, tags);
        commitEntityService.commit(environment);
        return environment;
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    public Environment update(@Nonnull UUID environmentId,
                              @Nonnull String name,
                              @Nonnull String graylogName,
                              String description,
                              String ssmSolutionAlias,
                              String ssmInstanceAlias,
                              String consulEgressConfigPath,
                              @Nonnull Long modified,
                              @Nonnull UUID modifiedBy,
                              @Nonnull UUID projectId,
                              @Nonnull UUID categoryId,
                              List<String> tags) {
        Timestamp modifiedTimestamp = new Timestamp(modified);
        long update = queryFactory.update(ENVIRONMENTS)
                .set(ENVIRONMENTS.name, name)
                .set(ENVIRONMENTS.graylogName, graylogName)
                .set(ENVIRONMENTS.description, description)
                .set(ENVIRONMENTS.ssmSolutionAlias, ssmSolutionAlias)
                .set(ENVIRONMENTS.ssmInstanceAlias, ssmInstanceAlias)
                .set(ENVIRONMENTS.consulEgressConfigPath, consulEgressConfigPath)
                .set(ENVIRONMENTS.modified, modifiedTimestamp)
                .set(ENVIRONMENTS.modifiedBy, modifiedBy)
                .set(ENVIRONMENTS.projectId, projectId)
                .set(ENVIRONMENTS.categoryId, categoryId)
                .set(ENVIRONMENTS.tags, createTagsPgObject(tags))
                .where(ENVIRONMENTS.id.eq(environmentId)).execute();
        Tuple tuple = queryFactory.select(ENVIRONMENTS.created, ENVIRONMENTS.createdBy, ENVIRONMENTS.sourceId)
                .from(ENVIRONMENTS)
                .where(ENVIRONMENTS.id.eq(environmentId)).fetchOne();
        Timestamp created = tuple.get(ENVIRONMENTS.created);
        UUID createdBy = tuple.get(ENVIRONMENTS.createdBy);
        UUID sourceId = tuple.get(ENVIRONMENTS.sourceId);
        Preconditions.checkArgument(update > 0, "Nothing updated");
        EnvironmentImpl environment = projection.create(environmentId, name, graylogName, description, ssmSolutionAlias,
                ssmInstanceAlias, consulEgressConfigPath, created, createdBy, modifiedTimestamp, modifiedBy,
                projectId, categoryId, sourceId, tags);
        updateEnvironmentsModified(environmentId, modifiedTimestamp, modifiedBy, projectId);
        commitEntityService.commit(environment);
        return environment;
    }

    /**
     * Delete environment.
     */
    public void delete(UUID environmentId, Long modified, UUID modifiedBy) {
        Timestamp modifiedTimestamp = new Timestamp(modified);
        UUID projectId = queryFactory.select(ENVIRONMENTS.projectId)
                .from(ENVIRONMENTS)
                .where(ENVIRONMENTS.id.eq(environmentId)).fetchOne();
        updateProjects(projectId, modifiedTimestamp);
        queryFactory.select(ENVIRONMENT_SYSTEMS.systemId)
                .from(ENVIRONMENT_SYSTEMS)
                .where(ENVIRONMENT_SYSTEMS.environmentId.eq(environmentId))
                .fetch()
                .forEach(systemId -> systemRepo.get().delete(systemId, environmentId, modified, modifiedBy, false));
        kafkaService.sendEnvironmentKafkaNotification(environmentId, EventType.DELETE, projectId);
        deleteReferenceToEnvironment(environmentId, ENVIRONMENTS, ENVIRONMENTS.id);
        commitEntityService.delete(environmentId);
    }

    private void deleteReferenceToEnvironment(UUID id, RelationalPathBase path, SimplePath<UUID> simplePath) {
        queryFactory.delete(path).where(simplePath.eq(id)).execute();
    }

    public void updateEnvironmentsModified(@Nonnull Environment environment, Timestamp modified, UUID modifiedBy) {
        updateEnvironmentsModified(environment.getId(), modified, modifiedBy, environment.getProjectId());
    }

    /**
     * Method for updating environment modified info.
     */
    public void updateEnvironmentsModified(@Nonnull UUID environmentId, Timestamp modified, UUID modifiedBy,
                                           UUID projectId) {
        queryFactory.update(ENVIRONMENTS)
                .set(ENVIRONMENTS.modified, modified)
                .set(ENVIRONMENTS.modifiedBy, modifiedBy)
                .where(ENVIRONMENTS.id.eq(environmentId))
                .execute();
        kafkaService.sendEnvironmentKafkaNotification(environmentId,
                EventType.UPDATE,
                projectId == null ? getProjectId(environmentId) :
                        projectId);
    }

    /**
     * Method for getting projectId by environmentId.
     */
    public UUID getProjectId(UUID environmentId) {
        return queryFactory.select(ENVIRONMENTS.projectId)
                .from(ENVIRONMENTS)
                .where(ENVIRONMENTS.id.eq(environmentId))
                .fetchOne();
    }

    private void updateProjects(@Nonnull UUID id, Timestamp modified) {
        queryFactory.update(PROJECTS)
                .set(PROJECTS.modified, modified)
                .where(PROJECTS.id.eq(id))
                .execute();
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Cacheable(value = HazelcastMapName.ENVIRONMENTS_BY_SYSTEM_ID, key = "#systemId", condition = "#systemId!=null")
    public List<Environment> getAllBySystemId(UUID systemId) {
        List<UUID> environmentIds = queryFactory
                .select(ENVIRONMENT_SYSTEMS.environmentId)
                .from(ENVIRONMENT_SYSTEMS)
                .where(ENVIRONMENT_SYSTEMS.systemId.eq(systemId))
                .fetch();
        return getByIds(environmentIds);
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    public List<Environment> getAllShortBySystemId(UUID systemId) {
        List<UUID> environmentIds = queryFactory
                .select(ENVIRONMENT_SYSTEMS.environmentId)
                .from(ENVIRONMENT_SYSTEMS)
                .where(ENVIRONMENT_SYSTEMS.systemId.eq(systemId))
                .fetch();
        return geShortByIds(environmentIds);
    }

    /**
     * Gets environments id by list of id.
     */
    public List<Environment> getByIds(List<UUID> uuids) {
        List<Environment> result =
                queryFactory.select(resolveProjection()).from(ENVIRONMENTS).where(ENVIRONMENTS.id.in(uuids)).fetch();
        return CollectionUtils.isEmpty(result) ? Collections.emptyList() : result;
    }

    /**
     * Gets environments id by list of id.
     */
    public List<Environment> geShortByIds(List<UUID> uuids) {
        List<Environment> result =
                queryFactory.select(shortProjection).from(ENVIRONMENTS).where(ENVIRONMENTS.id.in(uuids)).fetch();
        return CollectionUtils.isEmpty(result) ? Collections.emptyList() : result;
    }

    /**
     * Gets project id by system id.
     */
    public UUID getProjectIdBySystemId(@Nonnull UUID systemId) {
        UUID environmentId = queryFactory.select(ENVIRONMENT_SYSTEMS.environmentId)
                .from(ENVIRONMENT_SYSTEMS)
                .where(ENVIRONMENT_SYSTEMS.systemId.eq(systemId))
                .fetchFirst();
        return getProjectIdByEnvironmentId(environmentId);
    }

    /**
     * Get project ID by environment ID.
     *
     * @param environmentId environment ID
     * @return project ID
     */
    public UUID getProjectIdByEnvironmentId(@Nonnull UUID environmentId) {
        return queryFactory.select(ENVIRONMENTS.projectId)
                .from(ENVIRONMENTS)
                .where(ENVIRONMENTS.id.eq(environmentId))
                .fetchOne();
    }

    /**
     * Gets by name and project id.
     *
     * @param name      the name
     * @param projectId the project id
     * @return the by name and project id
     */
    public Environment getByNameAndProjectId(String name, UUID projectId) {
        return queryFactory.select(resolveProjection())
                .from(ENVIRONMENTS)
                .where(ENVIRONMENTS.projectId.eq(projectId).and(ENVIRONMENTS.name.eq(name))).fetchFirst();
    }

    /**
     * Get environment by source id and project id.
     *
     * @param sourceId  the source id
     * @param projectId the project id
     * @return the by name and project id
     */
    public Environment getBySourceIdAndProjectId(UUID sourceId, UUID projectId) {
        return queryFactory.select(resolveProjection())
                .from(ENVIRONMENTS)
                .where(ENVIRONMENTS.projectId.eq(projectId).and(ENVIRONMENTS.sourceId.eq(sourceId)))
                .fetchFirst();
    }

    /**
     * Search environments by request with criteria.
     *
     * @param searchRequest search request
     * @return found environments
     */
    public List<Environment> findBySearchRequest(BaseSearchRequestDto searchRequest,
                                                 List<UUID> projectIdsWithAccess) throws Exception {
        SQLQuery<Environment> sqlQuery = queryFactory.select(resolveProjection()).from(ENVIRONMENTS);
        List<BooleanExpression> searchExpressions = new ArrayList<>();
        UUID projectId = searchRequest.getProjectId();
        if (nonNull(projectId)) {
            if (CollectionUtils.isEmpty(projectIdsWithAccess)
                    || !projectIdsWithAccess.contains(searchRequest.getProjectId())) {
                return Collections.emptyList();
            }
            sqlQuery = sqlQuery.where(ENVIRONMENTS.projectId.eq(projectId));
            searchExpressions.add(ENVIRONMENTS.projectId.eq(projectId));
        } else if (!CollectionUtils.isEmpty(projectIdsWithAccess)) {
            sqlQuery = sqlQuery.where(ENVIRONMENTS.projectId.in(projectIdsWithAccess));
            searchExpressions.add(ENVIRONMENTS.projectId.in(projectIdsWithAccess));
        }
        Set<UUID> ids = searchRequest.getIds();
        if (!CollectionUtils.isEmpty(ids)) {
            sqlQuery = sqlQuery.where(ENVIRONMENTS.id.in(ids));
            searchExpressions.add(ENVIRONMENTS.id.in(ids));
        }
        Set<String> names = searchRequest.getNames();
        if (!CollectionUtils.isEmpty(names)) {
            BooleanExpression nameContainExpression = names.stream()
                    .map(ENVIRONMENTS.name::containsIgnoreCase)
                    .reduce(BooleanExpression::or)
                    .orElseThrow(() -> new IllegalStateException("Failed to construct environment search query: "
                            + "invalid contain name params"));
            sqlQuery = sqlQuery.where(nameContainExpression);
            searchExpressions.add(nameContainExpression);
        }
        if (!searchExpressions.isEmpty()) {
            sqlQuery.where(searchExpressions.toArray(new BooleanExpression[0]));
            return sqlQuery.fetch();
        }
        return Collections.emptyList();
    }

    @Override
    public Context getContext() {
        return contextRepository.getContext();
    }

    @Override
    public MappingProjection<Environment> getFullProjection() {
        return projection;
    }

    @Override
    public MappingProjection<Environment> getLazyProjection() {
        return lazyProjection;
    }

    @Override
    public MappingProjection<Environment> getShortProjection() {
        return shortProjection;
    }

    /**
     * Search environments by project id and systemCategoryId.
     *
     * @param request EnvironmentsWithFilterRequest.
     * @param size size of the requested list.
     * @param offset start position for search.
     * @return found environments
     */
    public List<Environment> getEnvironmentsByFilterPaged(EnvironmentsWithFilterRequest request,
                                                          Integer size,
                                                          Integer offset) {
        MappingProjection<Environment> projection = getLazyProjection();
        if (!CollectionUtils.isEmpty(request.getFields())) {
            projection = new GenericEnvironmentProjection(this, request.getFields(), environmentMapper);
        }
        SQLQuery<Environment> sqlQuery = queryFactory.select(projection)
                        .from(ENVIRONMENTS)
                        .groupBy(ENVIRONMENTS.id)
                        .orderBy(ENVIRONMENTS.name.asc());
        BooleanExpression whereExpression = ENVIRONMENTS.projectId.eq(request.getProjectId());
        if (!CollectionUtils.isEmpty(request.getFilter())) {
            whereExpression = environmentMapper.mapFilter(sqlQuery, whereExpression,
                    request.getFilter());
        }
        sqlQuery = sqlQuery.where(whereExpression);
        if (size != null && offset != null) {
            sqlQuery = sqlQuery.offset(offset).limit(size);
        }
        return sqlQuery.fetch();
    }

    /**
     * Create PgObject for tags setting in environments table.
     *
     * @param tags list of the tags.
     * @return PgObject with "jsonb" type.
     */
    public PGobject createTagsPgObject(List<String> tags) {
        PGobject tagsPgObject = new PGobject();
        tagsPgObject.setType("jsonb");
        try {
            tagsPgObject.setValue(new Gson().toJson(tags == null ? Collections.emptyList() : tags));
        } catch (SQLException e) {
            log.error("Error occurred while \"tags\" field processing in Environment", e);
            throw new RuntimeException();
        }
        return tagsPgObject;
    }

    /**
     * Returns count of environments matching given filter (without pagination).
     *
     * @param request EnvironmentsWithFilterRequest containing filters
     * @return count of matching environments
     */
    public long getEnvironmentsCountByFilter(EnvironmentsWithFilterRequest request) {
        BooleanExpression whereExpression = ENVIRONMENTS.projectId.eq(request.getProjectId());
        if (!CollectionUtils.isEmpty(request.getFilter())) {
            SQLQuery<Environment> filterBuilderQuery = queryFactory.select(getLazyProjection())
                    .from(ENVIRONMENTS);
            whereExpression = environmentMapper.mapFilter(filterBuilderQuery, whereExpression, request.getFilter());
        }
        SQLQuery<Long> countQuery = queryFactory
                .select(ENVIRONMENTS.id.countDistinct())
                .from(ENVIRONMENTS);
        boolean hasSystemsFilter = request.getFilter() != null && request.getFilter().stream()
                .anyMatch(f -> f != null && f.getName() != null && f.getName().contains("systems"));
        if (hasSystemsFilter) {
            countQuery.leftJoin(ENVIRONMENT_SYSTEMS)
                    .on(ENVIRONMENTS.id.eq(ENVIRONMENT_SYSTEMS.environmentId))
                    .leftJoin(SYSTEMS)
                    .on(ENVIRONMENT_SYSTEMS.systemId.eq(SYSTEMS.id));
        }
        if (whereExpression != null) {
            countQuery.where(whereExpression);
        }
        Long result = countQuery.fetchOne();
        return result != null ? result : 0L;
    }
}
