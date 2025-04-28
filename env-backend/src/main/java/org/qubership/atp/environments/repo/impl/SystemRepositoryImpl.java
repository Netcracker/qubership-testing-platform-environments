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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Provider;

import org.qubership.atp.environments.db.Proxies;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.ParametersGettingVersion;
import org.qubership.atp.environments.model.ServerItf;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.SystemCategory;
import org.qubership.atp.environments.model.impl.Context;
import org.qubership.atp.environments.model.impl.EnvironmentImpl;
import org.qubership.atp.environments.model.impl.SystemCategoryImpl;
import org.qubership.atp.environments.model.impl.SystemImpl;
import org.qubership.atp.environments.model.utils.HazelcastMapName;
import org.qubership.atp.environments.model.utils.enums.Status;
import org.qubership.atp.environments.repo.projections.FullSystemProjection;
import org.qubership.atp.environments.repo.projections.LazySystemProjection;
import org.qubership.atp.environments.repo.projections.ShortSystemProjection;
import org.qubership.atp.environments.repo.projections.SystemProjectionWithoutProxy;
import org.qubership.atp.environments.service.direct.KafkaService;
import org.qubership.atp.environments.service.rest.server.dto.EventType;
import org.qubership.atp.environments.versioning.service.CommitEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.support.NoOpCache;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.mysema.commons.lang.CloseableIterator;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.MappingProjection;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@SuppressWarnings("CPD-START")
@SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
public class SystemRepositoryImpl extends AbstractRepository implements ProjectionRepository<System> {

    private static final Expression<?>[] COLUMNS;
    private final ContextRepository contextRepository;

    static {
        ImmutableList<Expression<?>> expressions =
                ImmutableList.<Expression<?>>builder()
                        .add(SYSTEMS.all())
                        .add(SYSTEM_CATEGORIES.all())
                        .add(ENVIRONMENTS.all())
                        .build();
        COLUMNS = expressions.toArray(new Expression[0]);
    }

    protected final SQLQueryFactory queryFactory;
    private final Provider<EnvironmentRepositoryImpl> environmentRepo;
    private final Provider<SystemCategoryRepositoryImpl> systemCategoryRepo;
    private final Provider<ConnectionRepositoryImpl> connectionRepo;
    private final CommitEntityService<System> commitSystemService;
    private final CommitEntityService<Environment> commitEnvironmentService;
    private final KafkaService kafkaService;
    private final FullSystemProjection projection = new FullSystemProjection(this);
    private final ShortSystemProjection shortSystemProjection = new ShortSystemProjection(this);
    private final ShortSystemProjection lazySystemProjection = new LazySystemProjection(this);
    private final SystemProjectionWithoutProxy systemProjectionWithoutProxy = new SystemProjectionWithoutProxy(this);
    private Gson gson = new Gson();
    private final Cache systemCachedMap;
    private final Cache systemsByEnvironmentIdCachedMap;

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Autowired
    public SystemRepositoryImpl(SQLQueryFactory queryFactory,
                                Provider<EnvironmentRepositoryImpl> environmentRepo,
                                Provider<SystemCategoryRepositoryImpl> systemCategoryRepo,
                                Provider<ConnectionRepositoryImpl> connectionRepo,
                                CommitEntityService<System> commitSystemService,
                                CommitEntityService<Environment> commitEnvironmentService,
                                KafkaService kafkaService,
                                CacheManager cacheManager,
                                ContextRepository contextRepository) {
        this.queryFactory = queryFactory;
        this.environmentRepo = environmentRepo;
        this.systemCategoryRepo = systemCategoryRepo;
        this.connectionRepo = connectionRepo;
        this.commitSystemService = commitSystemService;
        this.commitEnvironmentService = commitEnvironmentService;
        this.kafkaService = kafkaService;
        this.systemCachedMap = cacheManager != null && cacheManager.getCache(HazelcastMapName.SYSTEM_VERSION) != null
                ? cacheManager.getCache(HazelcastMapName.SYSTEM_VERSION)
                : new NoOpCache(HazelcastMapName.SYSTEM_VERSION);
        this.systemsByEnvironmentIdCachedMap =
                cacheManager != null && cacheManager.getCache(HazelcastMapName.SYSTEMS_BY_ENVIRONMENT_ID) != null
                        ? cacheManager.getCache(HazelcastMapName.SYSTEMS_BY_ENVIRONMENT_ID)
                        : new NoOpCache(HazelcastMapName.SYSTEMS_BY_ENVIRONMENT_ID);
        this.contextRepository = contextRepository;
    }

    public Provider<EnvironmentRepositoryImpl> getEnvironmentRepo() {
        return environmentRepo;
    }

    public Provider<SystemCategoryRepositoryImpl> getSystemCategoryRepo() {
        return systemCategoryRepo;
    }

    public Provider<ConnectionRepositoryImpl> getConnectionRepo() {
        return connectionRepo;
    }

    @Nullable
    public System getById(@Nonnull UUID id) {
        return queryFactory.select(resolveProjection()).from(SYSTEMS).where(SYSTEMS.id.eq(id)).fetchOne();
    }

    @Nullable
    public System getShortById(UUID id) {
        return queryFactory.select(shortSystemProjection).from(SYSTEMS).where(SYSTEMS.id.eq(id)).fetchOne();
    }

    public boolean existsById(@Nonnull UUID id) {
        return queryFactory.select(shortSystemProjection).from(SYSTEMS).where(SYSTEMS.id.eq(id))
                .fetchCount() > 0;
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    public UUID getProjectId(@Nonnull UUID systemId) {
        log.info("Get projectId for system with id '{}'", systemId);
        return queryFactory.selectDistinct(ENVIRONMENTS.projectId)
                .from(SYSTEMS)
                .leftJoin(ENVIRONMENT_SYSTEMS).on(SYSTEMS.id.eq(ENVIRONMENT_SYSTEMS.systemId))
                .leftJoin(ENVIRONMENTS).on(ENVIRONMENT_SYSTEMS.environmentId.eq(ENVIRONMENTS.id))
                .where(SYSTEMS.id.eq(systemId))
                .fetchOne();
    }

    /**
     * This method get system by id.
     *
     * @param id System id.
     */
    @Nullable
    public System getByIdV2(@Nonnull UUID id) {
        Iterator<System> iterator = getSystems(SYSTEMS.id.eq(id)).values().iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }

    /**
     * Get system in project.
     *
     * @param projectId project Id.
     */
    public List<System> getSystemsByProjectId(UUID projectId) {
        return queryFactory.selectDistinct(resolveProjection())
                .from(SYSTEMS)
                .leftJoin(ENVIRONMENT_SYSTEMS).on(SYSTEMS.id.eq(ENVIRONMENT_SYSTEMS.systemId))
                .leftJoin(ENVIRONMENTS).on(ENVIRONMENT_SYSTEMS.environmentId.eq(ENVIRONMENTS.id))
                .where(ENVIRONMENTS.projectId.eq(projectId))
                .fetch();
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private Map<UUID, System> getSystems(Predicate predicate) {
        SQLQuery<Tuple> sqlQuery = queryFactory.select(COLUMNS).from(SYSTEMS)
                .leftJoin(SYSTEM_CATEGORIES).on(SYSTEMS.categoryId.eq(SYSTEM_CATEGORIES.id))
                .leftJoin(ENVIRONMENT_SYSTEMS).on(SYSTEMS.id.eq(ENVIRONMENT_SYSTEMS.systemId))
                .leftJoin(ENVIRONMENTS).on(ENVIRONMENT_SYSTEMS.environmentId.eq(ENVIRONMENTS.id))
                .where(predicate);
        Map<UUID, System> systems = new HashMap<>();
        Map<UUID, Environment> environments = new HashMap<>();
        Map<UUID, SystemCategory> systemCategories = new HashMap<>();
        try (CloseableIterator<Tuple> iter = sqlQuery.iterate()) {
            while (iter.hasNext()) {
                Tuple row = iter.next();
                UUID id = row.get(SYSTEMS.id);
                UUID envId = row.get(ENVIRONMENTS.id);
                UUID sysCatId = row.get(SYSTEM_CATEGORIES.id);
                System system = systems.computeIfAbsent(id, key -> projection.create(id,
                        row.get(SYSTEMS.name),
                        row.get(SYSTEMS.description),
                        row.get(SYSTEMS.created),
                        row.get(SYSTEMS.createdBy),
                        row.get(SYSTEMS.modified),
                        row.get(SYSTEMS.modifiedBy),
                        null,
                        row.get(SYSTEMS.status),
                        row.get(SYSTEMS.dateOfLastCheck),
                        row.get(SYSTEMS.version),
                        row.get(SYSTEMS.dateOfCheckVersion),
                        row.get(SYSTEMS.parametersGettingVersion),
                        row.get(SYSTEMS.parentSystemId),
                        new ArrayList<>(),
                        row.get(SYSTEMS.serverItf),
                        row.get(SYSTEMS.mergeByName),
                        row.get(SYSTEMS.linkToSystemId),
                        row.get(SYSTEMS.externalId), null,
                        row.get(SYSTEMS.externalName)));
                if (envId != null) {
                    Environment environment = environments.computeIfAbsent(envId, key -> new EnvironmentImpl(key,
                            row.get(ENVIRONMENTS.name), row.get(ENVIRONMENTS.graylogName),
                            row.get(ENVIRONMENTS.description),
                            row.get(ENVIRONMENTS.ssmSolutionAlias),
                            row.get(ENVIRONMENTS.ssmInstanceAlias),
                            row.get(ENVIRONMENTS.consulEgressConfigPath),
                            row.get(ENVIRONMENTS.created).getTime(),
                            row.get(ENVIRONMENTS.createdBy),
                            row.get(ENVIRONMENTS.modified) != null
                                    ? row.get(ENVIRONMENTS.modified).getTime()
                                    : null,
                            row.get(ENVIRONMENTS.modifiedBy),
                            row.get(ENVIRONMENTS.projectId), null,
                            row.get(ENVIRONMENTS.categoryId),
                            row.get(ENVIRONMENTS.sourceId),
                            row.get(ENVIRONMENTS.tags)));
                    system.getEnvironments().add(environment);
                }
                if (sysCatId != null) {
                    SystemCategory systemCategory = systemCategories.computeIfAbsent(sysCatId,
                            key -> new SystemCategoryImpl(key,
                                    row.get(SYSTEM_CATEGORIES.name),
                                    row.get(SYSTEM_CATEGORIES.description),
                                    row.get(SYSTEM_CATEGORIES.created).getTime(),
                                    null));
                    system.setSystemCategory(systemCategory);
                }
            }
        }
        return systems;
    }

    @Nonnull
    public List<System> getAll() {
        return queryFactory.select(resolveProjection()).from(SYSTEMS).orderBy(SYSTEMS.name.asc()).fetch();
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    @Cacheable(value = HazelcastMapName.SYSTEMS_BY_ENVIRONMENT_ID, key = "#environmentId",
            condition = "#environmentId!=null")
    public List<System> getAllByParentId(@Nonnull UUID environmentId) {
        return queryFactory.select(systemProjectionWithoutProxy)
                .from(SYSTEMS)
                .where(systemsUnder(environmentId))
                .orderBy(SYSTEMS.name.asc())
                .fetch();
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    public List<System> getAllByParentId(@Nonnull UUID environmentId, String systemType) {
        UUID categoryId = systemCategoryRepo.get().getByName(systemType).getId();
        return queryFactory.select(resolveProjection())
                .from(SYSTEMS)
                .where(systemsUnder(environmentId).and(SYSTEMS.categoryId.eq(categoryId)))
                .orderBy(SYSTEMS.name.asc())
                .fetch();
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    public List<System> getAllByParentIdAndCategoryId(@Nonnull UUID environmentId, @Nonnull UUID categoryId) {
        return queryFactory.select(resolveProjection())
                .from(SYSTEMS)
                .where(systemsUnder(environmentId).and(SYSTEMS.categoryId.eq(categoryId)))
                .orderBy(SYSTEMS.name.asc())
                .fetch();
    }


    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    public List<System> getAllShortByParentId(@Nonnull UUID environmentId) {
        return queryFactory.select(shortSystemProjection)
                .from(SYSTEMS)
                .where(systemsUnder(environmentId))
                .orderBy(SYSTEMS.name.asc())
                .fetch();
    }

    /**
     * Getting a list of project system names.
     *
     * @param projectId Project identifier
     * @return list of system names
     */
    @Nonnull
    public List<String> getSystemNamesByProjectId(@Nonnull UUID projectId) {
        return queryFactory.selectDistinct(SYSTEMS.name)
                .from(SYSTEMS)
                .where(SYSTEMS.id.in(new SQLQuery<>()
                        .select(ENVIRONMENT_SYSTEMS.systemId)
                        .from(ENVIRONMENT_SYSTEMS)
                        .where(ENVIRONMENT_SYSTEMS.environmentId.in(new SQLQuery<>()
                                .select(ENVIRONMENTS.id)
                                .from(ENVIRONMENTS)
                                .where(ENVIRONMENTS.projectId.eq(projectId))))))
                .orderBy(SYSTEMS.name.asc())
                .fetch();
    }

    /**
     * Getting a list of systems with specified name under environment.
     *
     * @param environmentId Environment identifier
     * @param systemName    name of the specified system
     * @return list of system names
     */
    @Nonnull
    public List<Tuple> checkSystemNameIsUniqueUnderEnvironment(@Nonnull UUID environmentId, String systemName) {
        return queryFactory.select(SYSTEMS.name, SYSTEMS.id)
                .from(SYSTEMS)
                .leftJoin(ENVIRONMENT_SYSTEMS).on(SYSTEMS.id.eq(ENVIRONMENT_SYSTEMS.systemId))
                .leftJoin(ENVIRONMENTS).on(ENVIRONMENT_SYSTEMS.environmentId.eq(ENVIRONMENTS.id))
                .where(ENVIRONMENTS.id.eq(environmentId)
                        .and(SYSTEMS.name.eq(systemName)))
                .fetch();
    }

    /**
     * Getting a list of systems with specified name under environment.
     *
     * @param environmentIds Environment identifiers.
     * @param systemName    name of the specified system.
     * @return list of system names
     */
    @Nonnull
    public List<Tuple> checkSystemNameIsUniqueUnderEnvironments(@Nonnull List<UUID> environmentIds, String systemName) {
        return queryFactory.select(SYSTEMS.name, SYSTEMS.id)
                .from(SYSTEMS)
                .leftJoin(ENVIRONMENT_SYSTEMS).on(SYSTEMS.id.eq(ENVIRONMENT_SYSTEMS.systemId))
                .leftJoin(ENVIRONMENTS).on(ENVIRONMENT_SYSTEMS.environmentId.eq(ENVIRONMENTS.id))
                .where(ENVIRONMENTS.id.in(environmentIds)
                        .and(SYSTEMS.name.eq(systemName)))
                .fetch();
    }

    private BooleanExpression systemsUnder(@Nonnull UUID environmentId) {
        return SYSTEMS.id.in(new SQLQuery<>()
                .select(ENVIRONMENT_SYSTEMS.systemId)
                .from(ENVIRONMENT_SYSTEMS)
                .where(ENVIRONMENT_SYSTEMS.environmentId.eq(environmentId)));
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    public Collection<System> getAllByParentIdV2(@Nonnull UUID environmentId) {
        log.info("Get systems for environment with id '{}'", environmentId);
        return getSystems(systemsUnder(environmentId)).values();
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    public Collection<System> getAllByParentIdV2(@Nonnull UUID environmentId, String systemType) {
        log.info("Get systems for environment with id '{}' and type '{}'", environmentId, systemType);
        UUID categoryId = systemCategoryRepo.get().getByName(systemType).getId();
        log.info("Get systems for environment with id '{}' and category with id '{}'", environmentId, categoryId);
        return getSystems(systemsUnder(environmentId).and(SYSTEMS.categoryId.eq(categoryId))).values();
    }

    /**
     * Creates system with autogenerated {@link UUID} field.
     *
     * @param environmentId            environmentId
     * @param name                     name
     * @param description              description
     * @param created                  created
     * @param createdBy                createdBy
     * @param systemCategoryId         systemCategoryId
     * @param parametersGettingVersion parametersGettingVersion
     * @param parentSystemId           parentSystemId
     * @param serverItf                serverItf
     * @return an instance of {@link System}.
     */
    @Nonnull
    @CacheEvict(value = HazelcastMapName.SYSTEMS_BY_ENVIRONMENT_ID, key = "#environmentId",
            condition = "#environmentId!=null")
    public System create(@Nonnull UUID environmentId,
                         @Nonnull String name,
                         String description,
                         Long created,
                         UUID createdBy,
                         UUID systemCategoryId,
                         ParametersGettingVersion parametersGettingVersion,
                         UUID parentSystemId,
                         ServerItf serverItf,
                         Boolean mergeByName,
                         UUID linkToSystemId,
                         UUID externalId,
                         String externalName) {
        Timestamp createdTimestamp = new Timestamp(created);
        String parameters = parametersGettingVersion != null ? gson.toJson(parametersGettingVersion) : null;
        String parametersToItf = serverItf != null ? gson.toJson(serverItf) : null;
        UUID uuid = queryFactory.insert(SYSTEMS)
                .set(SYSTEMS.name, name)
                .set(SYSTEMS.description, description)
                .set(SYSTEMS.created, createdTimestamp)
                .set(SYSTEMS.createdBy, createdBy)
                .set(SYSTEMS.modified, createdTimestamp)
                .set(SYSTEMS.modifiedBy, createdBy)
                .set(SYSTEMS.categoryId, systemCategoryId)
                .set(SYSTEMS.parametersGettingVersion, parameters)
                .set(SYSTEMS.parentSystemId, parentSystemId)
                .set(SYSTEMS.serverItf, parametersToItf)
                .set(SYSTEMS.mergeByName, mergeByName)
                .set(SYSTEMS.linkToSystemId, linkToSystemId)
                .set(SYSTEMS.externalId, externalId)
                .set(SYSTEMS.externalName, externalName)
                .executeWithKey(SYSTEMS.id);
        queryFactory.insert(ENVIRONMENT_SYSTEMS)
                .set(ENVIRONMENT_SYSTEMS.environmentId, environmentId)
                .set(ENVIRONMENT_SYSTEMS.systemId, uuid)
                .execute();
        environmentRepo.get().updateEnvironmentsModified(environmentId, createdTimestamp, createdBy, null);
        SystemImpl system = projection.create(uuid, name, description,
                createdTimestamp, createdBy, createdTimestamp, createdBy,
                systemCategoryId, null, null, null, null, parameters,
                parentSystemId, parametersToItf, mergeByName, linkToSystemId, externalId, null, externalName, null);
        commitSystemService.commit(system);
        commitEnvironmentService.commit(system.getEnvironments());
        return system;
    }

    /**
     * Creates system with manual created {@link UUID} field.
     *
     * @param environmentId            environmentId
     * @param name                     name
     * @param description              description
     * @param created                  created
     * @param createdBy                createdBy
     * @param systemCategoryId         systemCategoryId
     * @param parametersGettingVersion parametersGettingVersion
     * @param parentSystemId           parentSystemId
     * @param serverItf                serverItf
     * @param sourceId                 sourceId
     * @return an instance of {@link System}.
     */
    @CacheEvict(value = HazelcastMapName.SYSTEMS_BY_ENVIRONMENT_ID, key = "#environmentId",
            condition = "#environmentId!=null")
    public System create(@Nonnull UUID environmentId,
                         @Nonnull UUID systemId,
                         @Nonnull String name,
                         String description,
                         Long created,
                         UUID createdBy,
                         UUID systemCategoryId,
                         ParametersGettingVersion parametersGettingVersion,
                         UUID parentSystemId,
                         ServerItf serverItf,
                         Boolean mergeByName,
                         UUID linkToSystemId,
                         UUID externalId,
                         UUID sourceId,
                         String externalName) {
        Timestamp createdTimestamp = new Timestamp(created);
        String parameters = parametersGettingVersion != null ? gson.toJson(parametersGettingVersion) : null;
        String parametersToItf = serverItf != null ? gson.toJson(serverItf) : null;
        queryFactory.insert(SYSTEMS)
                .set(SYSTEMS.name, name)
                .set(SYSTEMS.description, description)
                .set(SYSTEMS.created, createdTimestamp)
                .set(SYSTEMS.createdBy, createdBy)
                .set(SYSTEMS.modified, createdTimestamp)
                .set(SYSTEMS.modifiedBy, createdBy)
                .set(SYSTEMS.categoryId, systemCategoryId)
                .set(SYSTEMS.parametersGettingVersion, parameters)
                .set(SYSTEMS.parentSystemId, parentSystemId)
                .set(SYSTEMS.serverItf, parametersToItf)
                .set(SYSTEMS.mergeByName, mergeByName)
                .set(SYSTEMS.id, systemId)
                .set(SYSTEMS.linkToSystemId, linkToSystemId)
                .set(SYSTEMS.externalId, externalId)
                .set(SYSTEMS.sourceId, sourceId)
                .set(SYSTEMS.externalName, externalName)
                .execute();
        queryFactory.insert(ENVIRONMENT_SYSTEMS)
                .set(ENVIRONMENT_SYSTEMS.environmentId, environmentId)
                .set(ENVIRONMENT_SYSTEMS.systemId, systemId)
                .execute();
        environmentRepo.get().updateEnvironmentsModified(environmentId, createdTimestamp, createdBy, null);
        SystemImpl system = projection.create(systemId, name, description,
                createdTimestamp, createdBy, createdTimestamp, createdBy,
                systemCategoryId, null, null, null, null,
                parameters, parentSystemId, parametersToItf, mergeByName,
                linkToSystemId, externalId, sourceId, externalName, null);
        commitSystemService.commit(system);
        commitEnvironmentService.commit(system.getEnvironments());
        return system;
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    @CacheEvict(value = HazelcastMapName.SYSTEMS_BY_ENVIRONMENT_ID, key = "#targetEnvironment.getId()",
            condition = "#targetEnvironment.getId()!=null")
    public System share(@Nonnull UUID systemId,
                        @Nonnull Environment targetEnvironment,
                        Long modified,
                        UUID modifiedBy) {
        Timestamp modifiedTimestamp = new Timestamp(modified);
        updateSystemsModified(systemId, modifiedTimestamp, modifiedBy, false, null);
        queryFactory.insert(ENVIRONMENT_SYSTEMS)
                .set(ENVIRONMENT_SYSTEMS.systemId, systemId)
                .set(ENVIRONMENT_SYSTEMS.environmentId, targetEnvironment.getId())
                .execute();
        System system = queryFactory.select(projection).from(SYSTEMS).where(SYSTEMS.id.eq(systemId)).fetchOne();
        environmentRepo.get()
                .updateEnvironmentsModified(targetEnvironment.getId(), modifiedTimestamp, modifiedBy, null);
        commitSystemService.commit(system);
        commitEnvironmentService.commit(targetEnvironment);
        return system;
    }

    /**
     * Stops system's sharing in specified environment.
     *
     * @param systemId      System id.
     * @param environmentId Environment id where this system in use.
     */
    @Nonnull
    @CacheEvict(value = HazelcastMapName.SYSTEMS_BY_ENVIRONMENT_ID, key = "#environmentId",
            condition = "#environmentId!=null")
    public System unShare(@Nonnull UUID systemId, @Nonnull UUID environmentId, Long modified, UUID modifiedBy) {
        Timestamp modifiedTimestamp = new Timestamp(modified);
        updateSystemsModified(systemId, modifiedTimestamp, modifiedBy, false, null);
        queryFactory.delete(ENVIRONMENT_SYSTEMS)
                .where(ENVIRONMENT_SYSTEMS.environmentId.eq(environmentId)
                        .and(ENVIRONMENT_SYSTEMS.systemId.eq(systemId))).execute();
        environmentRepo.get().updateEnvironmentsModified(environmentId, modifiedTimestamp, modifiedBy, null);
        log.info("Shared system id='{}' has been deleted from environment id='{}'", systemId, environmentId);
        return queryFactory.select(projection).from(SYSTEMS).where(SYSTEMS.id.eq(systemId)).fetchOne();
    }

    /**
     * Update system category.
     */
    @Nonnull
    public void updateSystemCategory(@Nonnull UUID id, UUID systemCategoryId, Long modified, UUID modifiedBy) {
        Timestamp modifiedTimestamp = new Timestamp(modified);
        long update = queryFactory.update(SYSTEMS)
                .set(SYSTEMS.categoryId, systemCategoryId)
                .set(SYSTEMS.modified, modifiedTimestamp)
                .set(SYSTEMS.modifiedBy, modifiedBy)
                .where(SYSTEMS.id.eq(id)).execute();
        Preconditions.checkArgument(update > 0, "Information about system category not updated");
        List<Environment> environments = environmentRepo.get().getAllBySystemId(id);
        if (!CollectionUtils.isEmpty(environments)) {
            environments.forEach(environment -> {
                systemsByEnvironmentIdCachedMap.evictIfPresent(environment.getId());
                environmentRepo.get()
                        .updateEnvironmentsModified(environment.getId(),
                                modifiedTimestamp, modifiedBy, environment.getProjectId());
            });
            kafkaService.sendSystemKafkaNotification(id, EventType.UPDATE, environments.get(0).getProjectId());
        }
        commitSystemService.commit(Proxies.withId(System.class, id, this::getById));
    }

    /**
     * TODO Make javadoc documentation for this method.
     */

    @Nonnull
    @CachePut(value = HazelcastMapName.SYSTEM_VERSION, key = "#id",
            condition = "#useProxy && @systemCachedMap.get(#id) != null")
    @CacheEvict(value = HazelcastMapName.CONNECTIONS_BY_SYSTEM_ID, key = "#id", condition = "#id!=null")
    public System update(@Nonnull UUID id,
                         @Nonnull String name,
                         String description,
                         Long modified,
                         UUID modifiedBy,
                         UUID systemCategoryId,
                         Status status,
                         Long dateOfLastCheck,
                         String version,
                         Long dateOfCheckVersion,
                         ParametersGettingVersion parametersGettingVersion,
                         UUID parentSystemId,
                         ServerItf serverItf,
                         Boolean mergeByName,
                         UUID linkToSystemId,
                         UUID externalId,
                         String externalName,
                         boolean useProxy,
                         boolean sendKafkaUpdate) {
        Timestamp modifiedTimestamp = new Timestamp(modified);
        Timestamp dateOfLastCheckTimestamp = dateOfLastCheck != null ? new Timestamp(dateOfLastCheck) : null;
        Timestamp dateOfCheckVersionTimestamp = dateOfCheckVersion != null ? new Timestamp(dateOfCheckVersion) : null;
        String parameters = parametersGettingVersion != null ? gson.toJson(parametersGettingVersion) : null;
        String parametersToItf = serverItf != null ? gson.toJson(serverItf) : null;
        long update = queryFactory.update(SYSTEMS)
                .set(SYSTEMS.name, name)
                .set(SYSTEMS.description, description)
                .set(SYSTEMS.modified, modifiedTimestamp)
                .set(SYSTEMS.modifiedBy, modifiedBy)
                .set(SYSTEMS.categoryId, systemCategoryId)
                .set(SYSTEMS.status, status.toString())
                .set(SYSTEMS.dateOfLastCheck, dateOfLastCheckTimestamp)
                .set(SYSTEMS.version, version)
                .set(SYSTEMS.dateOfCheckVersion, dateOfCheckVersionTimestamp)
                .set(SYSTEMS.parametersGettingVersion, parameters)
                .set(SYSTEMS.parentSystemId, parentSystemId)
                .set(SYSTEMS.serverItf, parametersToItf)
                .set(SYSTEMS.mergeByName, mergeByName)
                .set(SYSTEMS.linkToSystemId, linkToSystemId)
                .set(SYSTEMS.externalId, externalId)
                .set(SYSTEMS.externalName, externalName)
                .where(SYSTEMS.id.eq(id)).execute();
        Tuple tuple = queryFactory.select(SYSTEMS.created, SYSTEMS.createdBy, SYSTEMS.sourceId)
                .from(SYSTEMS)
                .where(SYSTEMS.id.eq(id)).fetchOne();
        Timestamp created = tuple.get(SYSTEMS.created);
        UUID createdBy = tuple.get(SYSTEMS.createdBy);
        UUID sourceId = tuple.get(SYSTEMS.sourceId);
        Preconditions.checkArgument(update > 0, "Information about system not updated");
        SystemImpl system;
        List<Environment> environments = environmentRepo.get().getAllBySystemId(id);
        if (sendKafkaUpdate && !CollectionUtils.isEmpty(environments)) {
            kafkaService.sendSystemKafkaNotification(id, EventType.UPDATE, environments.get(0).getProjectId());
            environments.forEach(environment -> {
                systemsByEnvironmentIdCachedMap.evictIfPresent(environment.getId());
                environmentRepo.get()
                        .updateEnvironmentsModified(environment.getId(), modifiedTimestamp, modifiedBy,
                                environment.getProjectId());
            });
        }
        if (useProxy && systemCachedMap.get(id) == null) {
            system = projection.create(id, name, description, created, createdBy, modifiedTimestamp, modifiedBy,
                    systemCategoryId, status.toString(),
                    dateOfLastCheckTimestamp, version, dateOfCheckVersionTimestamp, parameters,
                    parentSystemId, parametersToItf, mergeByName, linkToSystemId, externalId, sourceId,
                    externalName, environments);
        } else {
            system = projection.createWithoutProxy(id, name, description, created, createdBy,
                    modifiedTimestamp,
                    modifiedBy,
                    systemCategoryId, status.toString(),
                    dateOfLastCheckTimestamp, version, dateOfCheckVersionTimestamp, parameters,
                    parentSystemId, parametersToItf, mergeByName, linkToSystemId, externalId, sourceId,
                    externalName, environments);
        }
        commitSystemService.commit(system);
        return system;
    }

    /**
     * This method delete communication system with the environment
     * if the system is a shared or the whole system, if the system is used in one place.
     *
     * @param id            System id.
     * @param environmentId Environment id where this system in use.
     */
    @Caching(evict = {
            @CacheEvict(value = {HazelcastMapName.SYSTEM_VERSION, HazelcastMapName.CONNECTIONS_BY_SYSTEM_ID,
                    HazelcastMapName.ENVIRONMENTS_BY_SYSTEM_ID}, key = "#id",
                    condition = "#id!=null && #environmentId!=null"),
            @CacheEvict(value = HazelcastMapName.SYSTEMS_BY_ENVIRONMENT_ID, key = "#environmentId",
                    condition = "#environmentId!=null")
    })
    public void delete(UUID id, UUID environmentId, Long modified, UUID modifiedBy,
                       boolean sendKafkaUpdateEnvironmentEvent) {
        Timestamp modifiedTimestamp = new Timestamp(modified);
        if (sendKafkaUpdateEnvironmentEvent) {
            environmentRepo.get().updateEnvironmentsModified(environmentId, modifiedTimestamp, modifiedBy, null);
        }
        long usageCount = queryFactory.select(ENVIRONMENT_SYSTEMS.systemId)
                .from(ENVIRONMENT_SYSTEMS)
                .where(ENVIRONMENT_SYSTEMS.systemId.eq(id)).fetchCount();
        if (usageCount == 1) {
            deleteReferenceToSystem(id, SYSTEMS, SYSTEMS.id);
            commitSystemService.delete(id);
            kafkaService.sendSystemKafkaNotification(id, EventType.DELETE,
                    environmentRepo.get().getProjectId(environmentId));
        } else {
            queryFactory.delete(ENVIRONMENT_SYSTEMS).where(ENVIRONMENT_SYSTEMS.systemId.eq(id)
                    .and(ENVIRONMENT_SYSTEMS.environmentId.eq(environmentId))).execute();
            commitSystemService.commit(Proxies.withId(System.class, id, this::getById));
        }
        commitEnvironmentService.commit(
                Proxies.withId(Environment.class, environmentId, envId -> environmentRepo.get().getById(envId))
        );
    }

    /**
     * Method update "modified" info of system and send kafka notification.
     */
    public void updateSystemsModified(@Nonnull UUID id,
                                      Timestamp modified,
                                      UUID modifiedBy,
                                      boolean sendKafkaNotification,
                                      UUID projectId) {
        queryFactory.update(SYSTEMS)
                .set(SYSTEMS.modified, modified)
                .set(SYSTEMS.modifiedBy, modifiedBy)
                .where(SYSTEMS.id.eq(id))
                .execute();
        if (sendKafkaNotification) {
            kafkaService.sendSystemKafkaNotification(id, EventType.UPDATE, projectId);
        }
    }

    /**
     * Method update "modified" info of system and send kafka notification for system and all depended environments.
     */
    public void updateSystemsModified(@Nonnull UUID id, Timestamp modified, UUID modifiedBy,
                                      List<Environment> environments) {
        Consumer<Environment> consumer = environment -> {
            systemsByEnvironmentIdCachedMap.evictIfPresent(environment.getId());
            environmentRepo.get().updateEnvironmentsModified(environment, modified, modifiedBy);
        };
        if (!CollectionUtils.isEmpty(environments)) {
            updateSystemsModified(id, modified, modifiedBy, true,
                    environments.get(0).getProjectId());
            environments.forEach(consumer);
        } else {
            environmentRepo.get()
                    .getAllBySystemId(id)
                    .forEach(consumer);
        }
    }

    private void deleteReferenceToSystem(UUID id, RelationalPathBase path, SimplePath<UUID> simplePath) {
        queryFactory.delete(path).where(simplePath.eq(id)).execute();
    }

    public List<System> getByListIds(List<UUID> systems) {
        return queryFactory.select(resolveProjection()).from(SYSTEMS).where(SYSTEMS.id.in(systems)).fetch();
    }

    public List<System> getByLinkToSystemId(UUID linkToSystemId) {
        return queryFactory.select(resolveProjection()).from(SYSTEMS)
                .where(SYSTEMS.linkToSystemId.eq(linkToSystemId)).fetch();
    }

    /**
     * Get system by source id and environment id.
     *
     * @param sourceId the source id
     * @return the by name and project id
     */
    public System getBySourceId(UUID sourceId) {
        return queryFactory.select(resolveProjection())
                .from(SYSTEMS)
                .where(SYSTEMS.sourceId.eq(sourceId))
                .fetchFirst();
    }

    /**
     * Get projectIds  by systemIds.
     *
     * @param systemIds Ids of systems
     * @return the list of project ids
     */
    public List<System> getSystemsByIdsAndProjectId(List<UUID> systemIds, UUID projectId) {
        return queryFactory.selectDistinct(resolveProjection())
                .from(SYSTEMS)
                .leftJoin(ENVIRONMENT_SYSTEMS).on(SYSTEMS.id.eq(ENVIRONMENT_SYSTEMS.systemId))
                .leftJoin(ENVIRONMENTS).on(ENVIRONMENT_SYSTEMS.environmentId.eq(ENVIRONMENTS.id))
                .where(SYSTEMS.id.in(systemIds).and(ENVIRONMENTS.projectId.eq(projectId)))
                .fetch();
    }

    /**
     * Get system  by name and environmentId.
     *
     * @param name          name of system
     * @param environmentId id of system
     * @return found system
     */
    public System getSystemByNameAndEnvironmentId(String name, UUID environmentId) {
        return queryFactory.selectDistinct(resolveProjection())
                .from(SYSTEMS)
                .leftJoin(ENVIRONMENT_SYSTEMS).on(SYSTEMS.id.eq(ENVIRONMENT_SYSTEMS.systemId))
                .where(SYSTEMS.name.eq(name.trim()).and(ENVIRONMENT_SYSTEMS.environmentId.eq(environmentId)))
                .fetchOne();
    }

    @Override
    public Context getContext() {
        return contextRepository.getContext();
    }

    @Override
    public MappingProjection<System> getFullProjection() {
        return projection;
    }

    @Override
    public MappingProjection<System> getLazyProjection() {
        return lazySystemProjection;
    }

    @Override
    public MappingProjection<System> getShortProjection() {
        return shortSystemProjection;
    }
}
