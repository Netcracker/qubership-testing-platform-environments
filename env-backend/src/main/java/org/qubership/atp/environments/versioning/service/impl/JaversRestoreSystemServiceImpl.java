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

import static org.qubership.atp.environments.repo.impl.AbstractRepository.CONNECTIONS;
import static org.qubership.atp.environments.repo.impl.AbstractRepository.SYSTEMS;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.validation.Validator;

import org.javers.core.Javers;
import org.javers.core.diff.changetype.container.SetChange;
import org.qubership.atp.auth.springbootstarter.entities.UserInfo;
import org.qubership.atp.auth.springbootstarter.ssl.Provider;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.repo.impl.SystemRepositoryImpl;
import org.qubership.atp.environments.utils.DateTimeUtil;
import org.qubership.atp.environments.versioning.model.entities.SystemJ;
import org.qubership.atp.environments.versioning.model.values.ConnectionJ;
import org.qubership.atp.environments.versioning.service.CommitEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.google.gson.Gson;
import com.querydsl.sql.SQLQueryFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JaversRestoreSystemServiceImpl extends AbstractJaversRestoreService<SystemJ> {

    private final SQLQueryFactory queryFactory;
    private final SystemRepositoryImpl systemRepository;

    private final Gson gson = new Gson();

    /**
     * Constructor.
     */
    @Autowired
    public JaversRestoreSystemServiceImpl(Javers javers,
                                          Validator validator,
                                          CommitEntityService<System> commitEntityService,
                                          DateTimeUtil dateTimeUtil,
                                          Provider<UserInfo> userInfoProvider,
                                          SQLQueryFactory queryFactory,
                                          SystemRepositoryImpl systemRepository) {
        super(javers, validator, commitEntityService, dateTimeUtil, userInfoProvider);
        this.queryFactory = queryFactory;
        this.systemRepository = systemRepository;
    }

    @Override
    void restoreEntity(SystemJ entity, Timestamp modified, UUID modifiedBy) {
        updateSimpleSystemFields(entity, modified, modifiedBy);
        restoreConnectionList(entity, modified, modifiedBy);
        System system = systemRepository.getById(entity.getId());
        commitEntityService.commit(system);
    }

    @Override
    public Class<SystemJ> getEntityType() {
        return SystemJ.class;
    }

    private void updateSimpleSystemFields(SystemJ systemJ, Timestamp modified, UUID modifiedBy) {
        log.debug("Restoring system: updating simple fields: {}", systemJ);
        queryFactory.update(SYSTEMS)
                .set(SYSTEMS.name, systemJ.getName())
                .set(SYSTEMS.description, systemJ.getDescription())
                .set(SYSTEMS.modified, modified)
                .set(SYSTEMS.modifiedBy, modifiedBy)
                .set(SYSTEMS.categoryId, systemJ.getSystemCategoryId())
                .set(SYSTEMS.parametersGettingVersion, gson.toJson(systemJ.getParametersGettingVersion()))
                .set(SYSTEMS.serverItf, gson.toJson(systemJ.getServerItf()))
                .where(SYSTEMS.id.eq(systemJ.getId()))
                .execute();
    }

    private void restoreConnectionList(SystemJ systemJForRestore, Timestamp modified, UUID modifiedBy) {
        UUID systemId = systemJForRestore.getId();
        System system = systemRepository.getById(systemId);
        SystemJ actualSystemJ = new SystemJ(system);

        javers.compare(systemJForRestore, actualSystemJ)
                .getChangesByType(SetChange.class)
                .stream()
                .filter(setChange -> "connections".equals(setChange.getPropertyName()))
                .findFirst()
                .ifPresent(setChange -> {
                    List<?> addedConnections = setChange.getAddedValues();
                    for (Object addedConnection : addedConnections) {
                        UUID connectionId = ((ConnectionJ) addedConnection).getId();
                        log.debug("Restoring system with id = {}: deleting connection with id = {}",
                                systemId, connectionId);
                        deleteConnection(connectionId);
                    }
                    List<?> removedConnections = setChange.getRemovedValues();
                    for (Object removedConnection : removedConnections) {
                        log.debug("Restoring system with id = {}: inserting connection: {}",
                                systemId, removedConnection);
                        insertConnection((ConnectionJ) removedConnection, systemId, modified, modifiedBy);
                    }
                });
    }

    private void deleteConnection(UUID connectionId) {
        queryFactory.delete(CONNECTIONS)
                .where(CONNECTIONS.id.eq(connectionId))
                .execute();
    }

    private void insertConnection(ConnectionJ connectionJ, UUID systemId, Timestamp modified, UUID modifiedBy) {
        queryFactory.insert(CONNECTIONS)
                .set(CONNECTIONS.id, connectionJ.getId())
                .set(CONNECTIONS.name, connectionJ.getName())
                .set(CONNECTIONS.description, connectionJ.getDescription())
                .set(CONNECTIONS.created, modified)
                .set(CONNECTIONS.createdBy, modifiedBy)
                .set(CONNECTIONS.modified, modified)
                .set(CONNECTIONS.modifiedBy, modifiedBy)
                .set(CONNECTIONS.systemId, systemId)
                .set(CONNECTIONS.parameters, gson.toJson(connectionJ.getParameters()))
                .set(CONNECTIONS.connectionType, connectionJ.getConnectionType())
                .set(CONNECTIONS.sourceTemplateId, connectionJ.getSourceTemplateId())
                .set(CONNECTIONS.services, new Gson().toJson(CollectionUtils.isEmpty(connectionJ.getServices())
                        ? Collections.emptyList() : connectionJ.getServices(), List.class))
                .execute();
    }
}
