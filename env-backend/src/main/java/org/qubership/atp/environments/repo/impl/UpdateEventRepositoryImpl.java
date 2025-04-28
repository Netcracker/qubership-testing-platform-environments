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

import org.qubership.atp.environments.model.Subscription;
import org.qubership.atp.environments.model.UpdateEvent;
import org.qubership.atp.environments.model.impl.UpdateEventImpl;
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
@SuppressWarnings("CPD-START")
public class UpdateEventRepositoryImpl extends AbstractRepository {

    private final SQLQueryFactory queryFactory;
    private final Provider<SubscriptionRepositoryImpl> subscriptionRepo;

    private final UpdateEventProjection projection = new UpdateEventProjection(this);

    @Autowired
    public UpdateEventRepositoryImpl(SQLQueryFactory queryFactory,
                                     Provider<SubscriptionRepositoryImpl> subscriptionRepo) {
        this.queryFactory = queryFactory;
        this.subscriptionRepo = subscriptionRepo;
    }

    @Nonnull
    public List<UpdateEvent> getAllByParentId(@Nonnull UUID subscriptionId) {
        return queryFactory.select(projection).from(UPDATE_EVENTS).where(UPDATE_EVENTS.subscriptionId
                .eq(subscriptionId)).fetch();
    }

    @Nullable
    public UpdateEvent getById(@Nonnull UUID subscriptionId, @Nonnull UUID entityId) {
        return queryFactory.select(projection).from(UPDATE_EVENTS).where(UPDATE_EVENTS.subscriptionId
                .eq(subscriptionId).and(UPDATE_EVENTS.entityId.eq(entityId))).fetchOne();
    }

    @Nullable
    public UpdateEvent getBySubscriptionId(@Nonnull UUID subscriptionId) {
        return queryFactory.select(projection).from(UPDATE_EVENTS)
                .where(UPDATE_EVENTS.subscriptionId.eq(subscriptionId)).fetchOne();
    }

    @Nullable
    public UpdateEvent getByEntityId(@Nonnull UUID entityId) {
        return queryFactory.select(projection).from(UPDATE_EVENTS)
                .where(UPDATE_EVENTS.entityId.eq(entityId)).fetchOne();
    }

    @Nonnull
    public List<UpdateEvent> getAll() {
        return queryFactory.select(projection).from(UPDATE_EVENTS).fetch();
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    public UpdateEvent create(@Nonnull UUID subscriptionId, @Nonnull UUID entityId, String tagList, Integer status,
                              Long lastEventDate, String entityType) {
        Timestamp lastEventDateTimestamp = new Timestamp(lastEventDate);
        long create = queryFactory.insert(UPDATE_EVENTS)
                .set(UPDATE_EVENTS.subscriptionId, subscriptionId)
                .set(UPDATE_EVENTS.entityId, entityId)
                .set(UPDATE_EVENTS.tagList, tagList)
                .set(UPDATE_EVENTS.status, status)
                .set(UPDATE_EVENTS.lastEventDate, lastEventDateTimestamp)
                .set(UPDATE_EVENTS.entityType, entityType)
                .execute();
        Preconditions.checkArgument(create > 0, "Nothing created");
        return projection.create(subscriptionId, entityId, tagList, status, lastEventDateTimestamp, entityType);
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    public UpdateEvent update(@Nonnull UUID subscriptionId, @Nonnull UUID entityId, String tagList, Integer status,
                              Long lastEventDate, String entityType) {
        Timestamp lastEventDateTimestamp = new Timestamp(lastEventDate);
        long update = queryFactory.update(UPDATE_EVENTS)
                .set(UPDATE_EVENTS.subscriptionId, subscriptionId)
                .set(UPDATE_EVENTS.entityId, entityId)
                .set(UPDATE_EVENTS.tagList, tagList)
                .set(UPDATE_EVENTS.status, status)
                .set(UPDATE_EVENTS.lastEventDate, lastEventDateTimestamp)
                .set(UPDATE_EVENTS.entityType, entityType)
                .where(UPDATE_EVENTS.subscriptionId.eq(subscriptionId).and(UPDATE_EVENTS.entityId.eq(entityId)))
                .execute();
        Preconditions.checkArgument(update > 0, "Nothing updated");
        return projection.create(subscriptionId, entityId, tagList, status, lastEventDateTimestamp, entityType);
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    public void delete(UUID subscriptionId, UUID entityId) {
        deleteReferenceToTable(
                subscriptionId, entityId, UPDATE_EVENTS, UPDATE_EVENTS.subscriptionId, UPDATE_EVENTS.entityId
        );
    }

    private void deleteReferenceToTable(
            UUID subscriberId, UUID entityId, RelationalPathBase path,
            SimplePath<UUID> simplePathSubscriber, SimplePath<UUID> simplePathEntity
    ) {
        queryFactory.delete(path).where(
                simplePathSubscriber.eq(subscriberId).and(simplePathEntity.eq(entityId))
        ).execute();
    }

    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    private static class UpdateEventProjection extends MappingProjection<UpdateEvent> {

        static final long serialVersionUID = 42L;
        private final transient UpdateEventRepositoryImpl repo;

        public UpdateEventProjection(UpdateEventRepositoryImpl repo) {
            super(UpdateEvent.class, UPDATE_EVENTS.all());
            this.repo = repo;
        }

        @Override
        protected UpdateEvent map(Tuple tuple) {
            UUID subscriptionId = tuple.get(UPDATE_EVENTS.subscriptionId);
            assert subscriptionId != null;
            UUID entityId = tuple.get(UPDATE_EVENTS.entityId);
            assert entityId != null;
            String tagList = tuple.get(UPDATE_EVENTS.tagList);
            Integer status = tuple.get(UPDATE_EVENTS.status);
            Timestamp lastEventDate = tuple.get(UPDATE_EVENTS.lastEventDate);
            assert lastEventDate != null;
            String entityType = tuple.get(UPDATE_EVENTS.entityType);
            return create(subscriptionId, entityId, tagList, status, lastEventDate, entityType);
        }

        protected UpdateEventImpl create(UUID subscriptionId, UUID entityId, String tagList, Integer status,
                                         Timestamp lastEventDate, String entityType) {
            Subscription subscriptionAsObj = repo.subscriptionRepo.get().getById(subscriptionId);
            return new UpdateEventImpl(subscriptionAsObj, subscriptionId, entityId, tagList, status,
                    lastEventDate.getTime(), entityType);
        }
    }
}
