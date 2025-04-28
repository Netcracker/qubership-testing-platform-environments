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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Provider;

import org.qubership.atp.environments.db.Proxies;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.impl.ConnectionImpl;
import org.qubership.atp.environments.model.utils.Constants;
import org.qubership.atp.environments.model.utils.HazelcastMapName;
import org.qubership.atp.environments.repo.projections.FullConnectionProjection;
import org.qubership.atp.environments.repo.projections.IdConnectionProjection;
import org.qubership.atp.environments.service.direct.EncryptorService;
import org.qubership.atp.environments.service.direct.KafkaService;
import org.qubership.atp.environments.service.rest.server.dto.EventType;
import org.qubership.atp.environments.versioning.service.CommitEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.SQLUpdateClause;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Repository
@SuppressWarnings("CPD-START")
public class ConnectionRepositoryImpl extends AbstractRepository {

    private final SQLQueryFactory queryFactory;
    private final Provider<ConnectionRepositoryImpl> connectionRepo;
    private final EncryptorService encryptorService;
    private final Provider<SystemRepositoryImpl> systemRepo;
    private final CommitEntityService<System> commitEntityService;
    private final FullConnectionProjection projection = new FullConnectionProjection(this);
    private final IdConnectionProjection idConnectionProjection = new IdConnectionProjection(this);
    private final List<String> allServices = Arrays.asList("HealthCheck", "LogCollector", "MIA", "TDM", "TSG");
    private final KafkaService kafkaService;

    /**
     * Constructor of Connection Repository.
     */
    @Autowired
    public ConnectionRepositoryImpl(SQLQueryFactory queryFactory,
                                    Provider<ConnectionRepositoryImpl> connectionRepo,
                                    EncryptorService encryptorService,
                                    Provider<SystemRepositoryImpl> systemRepo,
                                    CommitEntityService<System> commitEntityService,
                                    KafkaService kafkaService) {
        this.queryFactory = queryFactory;
        this.connectionRepo = connectionRepo;
        this.encryptorService = encryptorService;
        this.systemRepo = systemRepo;
        this.commitEntityService = commitEntityService;
        this.kafkaService = kafkaService;
    }

    private boolean validateServices(List<String> services) {
        if (services != null) {
            for (String service : services) {
                if (allServices.stream().noneMatch(service::equals)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Nullable
    public Connection getById(@Nonnull UUID id) {
        return queryFactory.select(projection).from(CONNECTIONS).where(CONNECTIONS.id.eq(id)).fetchOne();
    }

    public boolean existsById(@Nonnull UUID id) {
        return queryFactory.select(projection).from(CONNECTIONS).where(CONNECTIONS.id.eq(id)).fetchCount() > 0;
    }

    @Nonnull
    public List<Connection> getAll() {
        return queryFactory.select(projection).orderBy(CONNECTIONS.name.asc()).from(CONNECTIONS).fetch();
    }

    /**
     * Returns connections by environmentId and sourceTemplateId.
     */
    @Nonnull
    public List<Connection> getAll(@Nonnull UUID environmentId,
                                   @Nonnull UUID sourceTemplateId) {
        return getConnectionsJoinedToSystems(ENVIRONMENT_SYSTEMS.environmentId.eq(environmentId)
                .and(CONNECTIONS.sourceTemplateId.eq(sourceTemplateId)));
    }

    /**
     * Returns connections by environmentIds and systemCategoryId.
     */
    @Nonnull
    public List<Connection> getAll(@Nonnull List<UUID> environmentIds,
                                   @Nonnull UUID systemCategoryId) {
        return getConnectionsJoinedToEnvironments(ENVIRONMENTS.id.in(environmentIds)
                .and(SYSTEMS.categoryId.eq(systemCategoryId)));
    }

    /**
     * Returns connections by environmentId, sourceTemplateId and systemCategoryId.
     */
    @Nonnull
    public List<Connection> getAll(@Nonnull UUID environmentId,
                                   @Nonnull UUID sourceTemplateId,
                                   @Nonnull UUID systemCategoryId) {
        return getConnectionsJoinedToSystems(ENVIRONMENT_SYSTEMS.environmentId.eq(environmentId)
                .and(CONNECTIONS.sourceTemplateId.eq(sourceTemplateId))
                .and(SYSTEMS.categoryId.eq(systemCategoryId)));
    }

    /**
     * Returns connections by environmentId, sourceTemplateId, systemCategoryId and systemId.
     */
    @Nonnull
    public List<Connection> getAll(@Nonnull UUID environmentId,
                                   @Nonnull UUID sourceTemplateId,
                                   @Nonnull UUID systemCategoryId,
                                   @Nonnull UUID systemId) {
        return getConnectionsJoinedToSystems(ENVIRONMENT_SYSTEMS.environmentId.eq(environmentId)
                .and(CONNECTIONS.sourceTemplateId.eq(sourceTemplateId))
                .and(SYSTEMS.categoryId.eq(systemCategoryId))
                .and(CONNECTIONS.systemId.eq(systemId)));
    }

    /**
     * Returns connections by source_template_id.
     */
    public List<Connection> getAllByTemplateId(@Nonnull UUID templateId) {
        return queryFactory.select(projection)
                .where(CONNECTIONS.sourceTemplateId.eq(templateId))
                .orderBy(CONNECTIONS.name.asc())
                .from(CONNECTIONS).fetch();
    }

    /**
     * Returns connections by host string.
     */
    public List<Connection> getConnectionsByHost(@Nonnull String host) {
        return queryFactory.select(projection)
                .from(CONNECTIONS)
                .where(CONNECTIONS.parameters.like("%" + host + "%")).fetch();
    }

    /**
     * Returns connections by environmentId and sourceTemplateId.
     */
    @Nonnull
    public List<Connection> getConnectionsJoinedToSystems(Predicate predicate) {
        return queryFactory.select(projection)
                .from(CONNECTIONS)
                .innerJoin(SYSTEMS)
                .on(CONNECTIONS.systemId.eq(SYSTEMS.id))
                .innerJoin(ENVIRONMENT_SYSTEMS)
                .on(SYSTEMS.id.eq(ENVIRONMENT_SYSTEMS.systemId))
                .where(predicate)
                .orderBy(CONNECTIONS.name.asc())
                .fetch();
    }

    /**
     * Returns connections by environmentId and sourceTemplateId.
     */
    @Nonnull
    public List<Connection> getConnectionsJoinedToEnvironments(Predicate predicate) {
        return queryFactory.select(projection)
                .from(CONNECTIONS)
                .innerJoin(SYSTEMS)
                .on(CONNECTIONS.systemId.eq(SYSTEMS.id))
                .innerJoin(ENVIRONMENT_SYSTEMS)
                .on(SYSTEMS.id.eq(ENVIRONMENT_SYSTEMS.systemId))
                .innerJoin(ENVIRONMENTS)
                .on(ENVIRONMENTS.id.eq(ENVIRONMENT_SYSTEMS.environmentId))
                .where(predicate)
                .orderBy(CONNECTIONS.name.asc())
                .fetch();
    }

    /**
     * Returns template connections.
     */
    @Nonnull
    public List<Connection> getConnectionTemplates() {
        return queryFactory.select(projection)
                .from(CONNECTIONS)
                .where(CONNECTIONS.sourceTemplateId.isNull(),
                        CONNECTIONS.systemId.eq(Constants.Environment.System.DEFAULT))
                .orderBy(CONNECTIONS.name.asc())
                .fetch();
    }

    /**
     * Returns template connection by name.
     */
    @Nonnull
    public Connection getConnectionTemplateByName(String name) {
        return queryFactory.select(projection)
                .from(CONNECTIONS)
                .where(CONNECTIONS.sourceTemplateId.isNull(),
                        CONNECTIONS.name.eq(name),
                        CONNECTIONS.systemId.eq(Constants.Environment.System.DEFAULT)).fetchOne();
    }

    /**
     * Returns connections by systemId.
     */
    @Nonnull
    @Cacheable(value = HazelcastMapName.CONNECTIONS_BY_SYSTEM_ID, key = "#systemId", condition = "#systemId!=null")
    public List<Connection> getAllByParentId(@Nonnull UUID systemId) {
        return queryFactory.select(projection)
                .from(CONNECTIONS)
                .where(CONNECTIONS.systemId.eq(systemId))
                .orderBy(CONNECTIONS.name.asc())
                .fetch();
    }

    /**
     * Returns connections by systemId.
     */
    @Nonnull
    public List<Connection> getAllShortByParentId(@Nonnull UUID systemId) {
        return queryFactory.select(idConnectionProjection)
                .from(CONNECTIONS)
                .where(CONNECTIONS.systemId.eq(systemId))
                .orderBy(CONNECTIONS.name.asc())
                .fetch();
    }

    /**
     * Returns connection by systemId and name.
     */
    @Nonnull
    public Connection getByParentIdAndName(@Nonnull UUID systemId, @Nonnull String name) {
        return queryFactory.select(projection)
                .from(CONNECTIONS)
                .where(CONNECTIONS.systemId.eq(systemId).and(CONNECTIONS.name.eq(name)))
                .fetchOne();
    }

    /**
     * Returns connections by systemId and connectionType.
     */
    @Nonnull
    public List<Connection> getAllByParentIdAndConnectionType(@Nonnull UUID systemId, @Nonnull UUID connectionType) {
        return queryFactory.select(projection)
                .from(CONNECTIONS)
                .where(CONNECTIONS.systemId.eq(systemId), CONNECTIONS.sourceTemplateId.eq(connectionType))
                .fetch();
    }

    /**
     * Returns systemId by connectionId.
     */
    @Nonnull
    public UUID getSystemId(@Nonnull UUID connectionId) {
        return queryFactory.select(CONNECTIONS.systemId)
                .from(CONNECTIONS)
                .where(CONNECTIONS.id.eq(connectionId))
                .fetchOne();
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    @CacheEvict(value = HazelcastMapName.CONNECTIONS_BY_SYSTEM_ID, key = "#systemId", condition = "#systemId!=null")
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public Connection create(@Nonnull UUID systemId,
                             @Nonnull String name,
                             String description,
                             ConnectionParameters parameters,
                             Long created,
                             UUID createdBy,
                             String connectionType,
                             UUID sourceTemplateId,
                             List<String> services,
                             UUID sourceId) {
        Preconditions.checkArgument(validateServices(services),
                "Cannot create connection with name \"%s\" under system \"%s\". Invalid services",
                name,
                systemId);
        Timestamp createdTimestamp = new Timestamp(created);
        parameters = encryptorService.encryptParameters(parameters);
        UUID uuid = queryFactory.insert(CONNECTIONS)
                .set(CONNECTIONS.name, name)
                .set(CONNECTIONS.description, description)
                .set(CONNECTIONS.parameters, new Gson().toJson(parameters))
                .set(CONNECTIONS.services, new Gson().toJson(CollectionUtils.isEmpty(services)
                        ? Collections.emptyList() : services))
                .set(CONNECTIONS.created, createdTimestamp)
                .set(CONNECTIONS.createdBy, createdBy)
                .set(CONNECTIONS.modified, createdTimestamp)
                .set(CONNECTIONS.modifiedBy, createdBy)
                .set(CONNECTIONS.systemId, systemId)
                .set(CONNECTIONS.connectionType, connectionType)
                .set(CONNECTIONS.sourceTemplateId, sourceTemplateId)
                .set(CONNECTIONS.sourceId, sourceId)
                .executeWithKey(CONNECTIONS.id);
        ConnectionImpl connection = projection.create(uuid, name, description, parameters,
                createdTimestamp, createdBy, createdTimestamp, createdBy,
                systemId, connectionType, sourceTemplateId, services, sourceId);
        System system = Proxies.withId(System.class, systemId, id -> systemRepo.get().getById(id));
        updateSystems(systemId, createdTimestamp, createdBy, system.getEnvironments());
        commitEntityService.commit(system);
        return connection;
    }

    /**
     * Create connection.
     *
     * @param systemId         the system id
     * @param connectionId     the connection id
     * @param name             the name
     * @param description      the description
     * @param parameters       the parameters
     * @param created          the created
     * @param connectionType   the connection type
     * @param sourceTemplateId the source template id
     * @param sourceId         the source id
     * @return the connection
     */
    @Nonnull
    @CacheEvict(value = HazelcastMapName.CONNECTIONS_BY_SYSTEM_ID, key = "#systemId", condition = "#systemId!=null")
    public Connection create(@Nonnull UUID systemId,
                             @Nonnull UUID connectionId,
                             @Nonnull String name,
                             String description,
                             ConnectionParameters parameters,
                             Long created,
                             UUID createdBy,
                             String connectionType,
                             UUID sourceTemplateId,
                             List<String> services,
                             UUID sourceId) {
        Preconditions.checkArgument(validateServices(services),
                "Cannot create connection with name \"%s\" under system \"%s\". Invalid services",
                name,
                systemId);
        Timestamp createdTimestamp = new Timestamp(created);
        parameters = encryptorService.encryptParameters(parameters);
        queryFactory.insert(CONNECTIONS)
                .set(CONNECTIONS.name, name)
                .set(CONNECTIONS.description, description)
                .set(CONNECTIONS.parameters, new Gson().toJson(parameters))
                .set(CONNECTIONS.services, new Gson().toJson(CollectionUtils.isEmpty(services)
                        ? Collections.emptyList() : services))
                .set(CONNECTIONS.created, createdTimestamp)
                .set(CONNECTIONS.systemId, systemId)
                .set(CONNECTIONS.connectionType, connectionType)
                .set(CONNECTIONS.sourceTemplateId, sourceTemplateId)
                .set(CONNECTIONS.id, connectionId)
                .set(CONNECTIONS.sourceId, sourceId)
                .execute();
        updateSystems(systemId, createdTimestamp, createdBy, Collections.emptyList());
        return projection
                .create(connectionId, name, description, parameters, createdTimestamp, createdBy, createdTimestamp,
                        createdBy, systemId, connectionType, sourceTemplateId, services, sourceId);
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    @CacheEvict(value = HazelcastMapName.CONNECTIONS_BY_SYSTEM_ID, key = "#systemId", condition = "#systemId!=null")
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public Connection update(@Nonnull UUID id,
                             @Nonnull UUID systemId,
                             @Nonnull String name,
                             String description,
                             ConnectionParameters parameters,
                             Long modified,
                             UUID modifiedBy,
                             String connectionType,
                             UUID sourceTemplateId,
                             List<String> services, UUID sourceId) {
        Preconditions.checkArgument(validateServices(services),
                "Cannot update connection [Name: \"%s\",ID:\"%s\"] under system \"%s\". Invalid services",
                name,
                id,
                systemId);
        Timestamp modifiedTimestamp = new Timestamp(modified);
        parameters = encryptorService.encryptParameters(parameters);
        long update = queryFactory.update(CONNECTIONS)
                .set(CONNECTIONS.name, name)
                .set(CONNECTIONS.description, description)
                .set(CONNECTIONS.parameters, new Gson().toJson(parameters))
                .set(CONNECTIONS.services, new Gson().toJson(CollectionUtils.isEmpty(services)
                        ? Collections.emptyList() : services, List.class))
                .set(CONNECTIONS.modified, modifiedTimestamp)
                .set(CONNECTIONS.modifiedBy, modifiedBy)
                .set(CONNECTIONS.systemId, systemId)
                .set(CONNECTIONS.connectionType, connectionType)
                .set(CONNECTIONS.sourceTemplateId, sourceTemplateId)
                .set(CONNECTIONS.sourceId, sourceId)
                .where(CONNECTIONS.id.eq(id)).execute();
        Tuple tuple = queryFactory.select(CONNECTIONS.created, CONNECTIONS.createdBy)
                .from(CONNECTIONS)
                .where(CONNECTIONS.id.eq(id)).fetchOne();
        Timestamp created = tuple.get(CONNECTIONS.created);
        UUID createdBy = (UUID) tuple.get(CONNECTIONS.createdBy);
        Preconditions.checkArgument(update > 0, "Information about connection not updated");
        System system = Proxies.withId(System.class, systemId, sysId -> systemRepo.get().getById(sysId));
        sendKafkaNotification(id, system, EventType.UPDATE);
        ConnectionImpl connection = projection.create(id, name, description, parameters,
                created, createdBy, modifiedTimestamp, modifiedBy,
                systemId, connectionType, sourceTemplateId, services, sourceId);
        updateSystems(systemId, modifiedTimestamp, modifiedBy, system.getEnvironments());
        commitEntityService.commit(system);
        return connection;
    }

    /**
     * Update connection without updating source id.
     */
    @Nonnull
    @CacheEvict(value = HazelcastMapName.CONNECTIONS_BY_SYSTEM_ID, key = "#systemId", condition = "#systemId!=null")
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public Connection update(@Nonnull UUID id,
                             @Nonnull UUID systemId,
                             @Nonnull String name,
                             String description,
                             ConnectionParameters parameters,
                             Long modified,
                             UUID modifiedBy,
                             String connectionType,
                             UUID sourceTemplateId,
                             List<String> services) {
        Preconditions.checkArgument(validateServices(services),
                "Cannot update connection [Name: \"%s\",ID:\"%s\"] under system \"%s\". Invalid services",
                name,
                id,
                systemId);
        Timestamp modifiedTimestamp = new Timestamp(modified);
        parameters = encryptorService.encryptParameters(parameters);
        long update = queryFactory.update(CONNECTIONS)
                .set(CONNECTIONS.name, name)
                .set(CONNECTIONS.description, description)
                .set(CONNECTIONS.parameters, new Gson().toJson(parameters))
                .set(CONNECTIONS.services, new Gson().toJson(CollectionUtils.isEmpty(services)
                        ? Collections.emptyList() : services, List.class))
                .set(CONNECTIONS.modified, modifiedTimestamp)
                .set(CONNECTIONS.modifiedBy, modifiedBy)
                .set(CONNECTIONS.systemId, systemId)
                .set(CONNECTIONS.connectionType, connectionType)
                .set(CONNECTIONS.sourceTemplateId, sourceTemplateId)
                .where(CONNECTIONS.id.eq(id)).execute();
        Tuple tuple = queryFactory.select(CONNECTIONS.created, CONNECTIONS.createdBy, CONNECTIONS.sourceId)
                .from(CONNECTIONS)
                .where(CONNECTIONS.id.eq(id)).fetchOne();
        Timestamp created = tuple.get(CONNECTIONS.created);
        UUID createdBy = (UUID) tuple.get(CONNECTIONS.createdBy);
        UUID sourceId = (UUID) tuple.get(CONNECTIONS.sourceId);
        Preconditions.checkArgument(update > 0, "Information about connection not updated");
        System system = Proxies.withId(System.class, systemId, sysId -> systemRepo.get().getById(sysId));
        sendKafkaNotification(id, system, EventType.UPDATE);
        ConnectionImpl connection = projection.create(id, name, description, parameters,
                created, createdBy, modifiedTimestamp, modifiedBy,
                systemId, connectionType, sourceTemplateId, services, sourceId);
        updateSystems(systemId, modifiedTimestamp, modifiedBy, system.getEnvironments());
        commitEntityService.commit(system);
        return connection;
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    @CacheEvict(value = HazelcastMapName.CONNECTIONS_BY_SYSTEM_ID, key = "#systemId", condition = "#systemId!=null")
    public void updateParameters(UUID systemId, @Nonnull UUID id,
                                 ConnectionParameters parameters,
                                 Long modified,
                                 UUID modifiedBy,
                                 List<String> services) {
        parameters = encryptorService.encryptParameters(parameters);
        Timestamp modifiedTimestamp = modified != null
                ? new Timestamp(modified)
                : new Timestamp(new Date().getTime());
        SQLUpdateClause query = getUpdateParametersQuery(id, parameters);
        query.set(CONNECTIONS.modified, modifiedTimestamp);
        query.set(CONNECTIONS.modifiedBy, modifiedBy);
        if (!CollectionUtils.isEmpty(services)) {
            query.set(CONNECTIONS.services, new Gson().toJson(services, List.class));
        }
        long update = query.execute();
        Preconditions.checkArgument(update > 0, "Information about connection not updated");
        System system = Proxies.withId(System.class, systemId, sysId -> systemRepo.get().getById(sysId));
        sendKafkaNotification(id, system, EventType.UPDATE);
        updateSystems(systemId, modifiedTimestamp, modifiedBy, system.getEnvironments());
        commitEntityService.commit(system);
    }

    /**
     * Query for updating connection parameters.
     */
    public SQLUpdateClause getUpdateParametersQuery(@Nonnull UUID id,
                                                    ConnectionParameters parameters) {
        return queryFactory.update(CONNECTIONS)
                .set(CONNECTIONS.parameters, new Gson().toJson(parameters))
                .where(CONNECTIONS.id.eq(id));
    }

    /**
     * Delete connections.
     */
    @CacheEvict(value = HazelcastMapName.CONNECTIONS_BY_SYSTEM_ID, key = "#systemId", condition = "#systemId!=null")
    public void delete(UUID systemId, UUID id, Long modified, UUID modifiedBy) {
        Timestamp modifiedTimestamp = new Timestamp(modified);
        queryFactory.delete(CONNECTIONS).where(CONNECTIONS.id.eq(id)).execute();
        System system = Proxies.withId(System.class, systemId, sysId -> systemRepo.get().getById(sysId));
        sendKafkaNotification(id, system, EventType.DELETE);
        updateSystems(systemId, modifiedTimestamp, modifiedBy, system.getEnvironments());
        commitEntityService.commit(system);
    }

    private void sendKafkaNotification(UUID id, System system, EventType eventType) {
        UUID projectId = !CollectionUtils.isEmpty(system.getEnvironments())
                ? system.getEnvironments().get(0).getProjectId() : null;
        kafkaService.sendConnectionKafkaNotification(id, eventType, projectId);
    }

    private void updateSystems(@Nonnull UUID id, Timestamp modified, UUID modifiedBy, List<Environment> environments) {
        systemRepo.get().updateSystemsModified(id, modified, modifiedBy, environments);
    }

    /**
     * Getting a list of project connection names.
     *
     * @param projectId Project identifier
     * @return list of names
     */
    public List<String> getConnectionNameByProjectId(UUID projectId) {
        return queryFactory.selectDistinct(CONNECTIONS.name)
                .from(CONNECTIONS)
                .where(CONNECTIONS.systemId.in(new SQLQuery<>()
                        .select(SYSTEMS.id)
                        .from(SYSTEMS)
                        .where(SYSTEMS.id.in(new SQLQuery<>()
                                .select(ENVIRONMENT_SYSTEMS.systemId)
                                .from(ENVIRONMENT_SYSTEMS)
                                .where(ENVIRONMENT_SYSTEMS.environmentId.in(new SQLQuery<>()
                                        .select(ENVIRONMENTS.id)
                                        .from(ENVIRONMENTS)
                                        .where(ENVIRONMENTS.projectId.eq(projectId)))
                                ))
                        ))
                )
                .orderBy(CONNECTIONS.name.asc())
                .fetch();
    }

    /**
     * Getting a list of project connection.
     *
     * @param projectId Project identifier
     * @return list of connections
     */
    public List<Connection> getConnectionsByProjectId(UUID projectId) {
        return queryFactory.selectDistinct(projection)
                .from(CONNECTIONS)
                .leftJoin(SYSTEMS).on(CONNECTIONS.systemId.eq(SYSTEMS.id))
                .leftJoin(ENVIRONMENT_SYSTEMS).on(SYSTEMS.id.eq(ENVIRONMENT_SYSTEMS.systemId))
                .leftJoin(ENVIRONMENTS).on(ENVIRONMENT_SYSTEMS.environmentId.eq(ENVIRONMENTS.id))
                .where(ENVIRONMENTS.projectId.eq(projectId))
                .fetch();
    }

    /**
     * Get connections by IDs.
     *
     * @param ids IDs
     * @return list of Connection
     */
    public List<Connection> getByIds(List<UUID> ids) {
        return queryFactory.select(projection).from(CONNECTIONS).where(CONNECTIONS.id.in(ids)).fetch();
    }

    /**
     * Get project ID by connection ID.
     *
     * @param connectionId connection ID
     * @return project ID
     */
    public UUID getProjectId(UUID connectionId) {
        return queryFactory.select(ENVIRONMENTS.projectId)
                .from(CONNECTIONS)
                .leftJoin(SYSTEMS).on(CONNECTIONS.systemId.eq(SYSTEMS.id))
                .leftJoin(ENVIRONMENT_SYSTEMS).on(SYSTEMS.id.eq(ENVIRONMENT_SYSTEMS.systemId))
                .leftJoin(ENVIRONMENTS).on(ENVIRONMENT_SYSTEMS.environmentId.eq(ENVIRONMENTS.id))
                .where(CONNECTIONS.id.eq(connectionId))
                .limit(1)
                .fetchOne();
    }

    /**
     * Get all connections in environment by environment id.
     *
     * @param environmentId environment ID
     * @return list of the connections
     */
    @Nullable
    public List<Connection> getAllByEnvironmentId(UUID environmentId) {
        return queryFactory.selectDistinct(projection)
                .from(CONNECTIONS)
                .leftJoin(SYSTEMS).on(CONNECTIONS.systemId.eq(SYSTEMS.id))
                .leftJoin(ENVIRONMENT_SYSTEMS).on(SYSTEMS.id.eq(ENVIRONMENT_SYSTEMS.systemId))
                .where(ENVIRONMENT_SYSTEMS.environmentId.eq(environmentId)).fetch();
    }
}
