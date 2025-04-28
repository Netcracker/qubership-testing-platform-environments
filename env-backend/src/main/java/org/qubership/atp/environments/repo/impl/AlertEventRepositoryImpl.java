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
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Provider;

import org.qubership.atp.environments.model.Alert;
import org.qubership.atp.environments.model.AlertEvent;
import org.qubership.atp.environments.model.impl.AlertEventImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.google.common.base.Preconditions;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.MappingProjection;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLQueryFactory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Repository
public class AlertEventRepositoryImpl extends AbstractRepository {

    private final SQLQueryFactory queryFactory;
    private final Provider<AlertRepositoryImpl> alertRepo;

    private final AlertEventProjection projection = new AlertEventProjection(this);

    @Autowired
    public AlertEventRepositoryImpl(SQLQueryFactory queryFactory,
                                    Provider<AlertRepositoryImpl> alertRepo) {
        this.queryFactory = queryFactory;
        this.alertRepo = alertRepo;
    }

    @Nonnull
    public List<AlertEvent> getAllByParentId(@Nonnull UUID alertId) {
        return queryFactory.select(projection).from(ALERT_EVENTS).where(ALERT_EVENTS.alertId.eq(alertId)).fetch();
    }

    @Nullable
    public AlertEvent getById(@Nonnull UUID alertId, @Nonnull UUID entityId) {
        return queryFactory.select(projection).from(ALERT_EVENTS)
                .where(ALERT_EVENTS.alertId.eq(alertId).and(ALERT_EVENTS.entityId.eq(entityId))).fetchOne();
    }

    @Nonnull
    public List<AlertEvent> getAll() {
        return queryFactory.select(projection).from(ALERT_EVENTS).fetch();
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    public AlertEvent create(@Nonnull UUID alertId, @Nonnull UUID entityId, String tagList, Integer status,
                             Long lastUpdated) {
        Timestamp lastUpdatedTimestamp = new Timestamp(lastUpdated);
        long create = queryFactory.insert(ALERT_EVENTS)
                .set(ALERT_EVENTS.alertId, alertId)
                .set(ALERT_EVENTS.entityId, entityId)
                .set(ALERT_EVENTS.tagList, tagList)
                .set(ALERT_EVENTS.status, status)
                .set(ALERT_EVENTS.lastUpdated, lastUpdatedTimestamp)
                .execute();
        Preconditions.checkArgument(create > 0, "Nothing create");
        return projection.create(alertId, entityId, tagList, status, lastUpdatedTimestamp);
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    public AlertEvent update(@Nonnull UUID alertId, @Nonnull UUID entityId, String tagList, Integer status,
                             Long lastUpdated) {
        Timestamp lastUpdatedTimestamp = new Timestamp(lastUpdated);
        long update = queryFactory.update(ALERT_EVENTS)
                .set(ALERT_EVENTS.alertId, alertId)
                .set(ALERT_EVENTS.entityId, entityId)
                .set(ALERT_EVENTS.tagList, tagList)
                .set(ALERT_EVENTS.status, status)
                .set(ALERT_EVENTS.lastUpdated, lastUpdatedTimestamp)
                .where(ALERT_EVENTS.alertId.eq(alertId).and(ALERT_EVENTS.entityId.eq(entityId))).execute();
        Preconditions.checkArgument(update > 0, "Nothing updated");
        return projection.create(alertId, entityId, tagList, status, lastUpdatedTimestamp);
    }

    /**
     * TODO Make javadoc documentation for this method.
     *
     * @param alertId  TODO
     * @param entityId TODO
     */
    public void delete(@Nonnull UUID alertId, UUID entityId) {
        deleteReferenceToTable(
                alertId, entityId, ALERT_EVENTS, ALERT_EVENTS.alertId, ALERT_EVENTS.entityId
        );
    }

    private void deleteReferenceToTable(
            @Nonnull UUID alertId, UUID entityId, RelationalPathBase path,
            SimplePath<UUID> simplePathSubscriber, SimplePath<UUID> simplePathEntity
    ) {
        queryFactory.delete(path).where(
                simplePathSubscriber.eq(alertId).and(simplePathEntity.eq(entityId))
        ).execute();
    }

    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    private static class AlertEventProjection extends MappingProjection<AlertEvent> {

        private static final long serialVersionUID = 42L;
        private final transient AlertEventRepositoryImpl repo;

        public AlertEventProjection(AlertEventRepositoryImpl repo) {
            super(AlertEvent.class, ALERT_EVENTS.all());
            this.repo = repo;
        }

        @Override
        protected AlertEvent map(Tuple tuple) {
            UUID alertId = tuple.get(ALERT_EVENTS.alertId);
            assert alertId != null;
            UUID entityId = tuple.get(ALERT_EVENTS.entityId);
            assert entityId != null;
            String tagList = tuple.get(ALERT_EVENTS.tagList);
            Integer status = tuple.get(ALERT_EVENTS.status);
            Timestamp lastUpdated = tuple.get(ALERT_EVENTS.lastUpdated);
            assert lastUpdated != null;
            return create(alertId, entityId, tagList, status, lastUpdated);
        }

        protected AlertEventImpl create(UUID alertId, UUID entityId, String tagList, Integer status,
                                        Timestamp lastUpdated) {
            Alert alertAsObj = repo.alertRepo.get().getById(alertId);
            return new AlertEventImpl(alertAsObj, alertId, entityId, tagList, status, lastUpdated.getTime());
        }
    }
}
