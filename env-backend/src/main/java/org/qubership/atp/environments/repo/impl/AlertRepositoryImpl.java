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
import org.qubership.atp.environments.model.impl.AlertImpl;
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
public class AlertRepositoryImpl extends AbstractRepository {

    private final SQLQueryFactory queryFactory;
    private final Provider<SubscriberRepositoryImpl> subscriberRepo;
    private final AlertProjection projection = new AlertProjection(this);

    @Autowired
    public AlertRepositoryImpl(SQLQueryFactory queryFactory,
                               Provider<SubscriberRepositoryImpl> subscriberRepo) {
        this.queryFactory = queryFactory;
        this.subscriberRepo = subscriberRepo;
    }

    @Nullable
    public Alert getById(@Nonnull UUID id) {
        return queryFactory.select(projection).from(ALERTS).where(ALERTS.id.eq(id)).fetchOne();
    }

    public boolean existsById(@Nonnull UUID id) {
        return queryFactory.select(projection).from(ALERTS).where(ALERTS.id.eq(id)).fetchCount() > 0;
    }

    @Nonnull
    public List<Alert> getAll() {
        return queryFactory.select(projection).from(ALERTS).fetch();
    }

    @Nonnull
    public List<Alert> getAllByParentId(@Nonnull UUID subscriberId) {
        return queryFactory.select(projection).from(ALERTS).where(ALERTS.subscriberId.eq(subscriberId)).fetch();
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    public Alert create(String name, String shortDescription, String tagList,
                        String parameters /*!!!!!!JSONB*/, UUID subscriberId, Integer status,
                        Long created) {
        Timestamp createdTimestamp = new Timestamp(created);
        UUID uuid = queryFactory.insert(ALERTS)
                .set(ALERTS.name, name)
                .set(ALERTS.shortDescription, shortDescription)
                .set(ALERTS.tagList, tagList)
                .set(ALERTS.parameters, parameters)
                .set(ALERTS.subscriberId, subscriberId)
                .set(ALERTS.status, status)
                .set(ALERTS.created, createdTimestamp)
                .executeWithKey(ALERTS.id);
        return projection.create(uuid, name, shortDescription, tagList, parameters, subscriberId, status,
                createdTimestamp);
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    public Alert update(@Nonnull UUID alertId, String name, String shortDescription, String tagList,
                        String parameters /*!!!!!!JSONB*/, UUID subscriberId, Integer status,
                        Long created) {
        Timestamp createdTimestamp = new Timestamp(created);
        long update = queryFactory.update(ALERTS)
                .set(ALERTS.name, name)
                .set(ALERTS.shortDescription, shortDescription)
                .set(ALERTS.tagList, tagList)
                .set(ALERTS.parameters, parameters)
                .set(ALERTS.subscriberId, subscriberId)
                .set(ALERTS.status, status)
                .set(ALERTS.created, createdTimestamp)
                .where(ALERTS.id.eq(alertId)).execute();
        Preconditions.checkArgument(update > 0, "Nothing updated");
        return projection.create(alertId, name, shortDescription, tagList, parameters, subscriberId, status,
                createdTimestamp);
    }

    public void delete(UUID alertId) {
        deleteReferenceToTable(alertId, ALERTS, ALERTS.id);
    }

    private void deleteReferenceToTable(UUID id, RelationalPathBase path, SimplePath<UUID> simplePath) {
        queryFactory.delete(path).where(simplePath.eq(id)).execute();
    }

    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    private static class AlertProjection extends MappingProjection<Alert> {

        private static final long serialVersionUID = 42L;
        private final transient AlertRepositoryImpl repo;

        public AlertProjection(AlertRepositoryImpl repo) {
            super(Alert.class, ALERTS.all());
            this.repo = repo;
        }

        @Override
        protected Alert map(Tuple tuple) {
            UUID uuid = tuple.get(ALERTS.id);
            assert uuid != null;
            String name = tuple.get(ALERTS.name);
            assert name != null;
            String shortDescription = tuple.get(ALERTS.shortDescription);
            assert shortDescription != null;
            String tagList = tuple.get(ALERTS.tagList);
            String parameters = (String) tuple.get(ALERTS.parameters); //!!!!!!!!!!!!JSONB
            UUID subscriberId = tuple.get(ALERTS.subscriberId);
            Integer status = tuple.get(ALERTS.status);
            Timestamp created = tuple.get(ALERTS.created);
            assert created != null;
            return create(uuid, name, shortDescription, tagList, parameters, subscriberId, status, created);
        }

        protected AlertImpl create(UUID uuid, String name, String shortDescription, String tagList,
                                   String parameters /*!!!!!!JSONB*/, UUID subscriberId, Integer status,
                                   Timestamp created) {
            //Subscriber subscriberAsObj = Proxies.withId(Subscriber.class, subscriberId, id -> repo.subscriberRepo
            // .get().getById(id));
            //List<Alert> AlertsListId = Proxies.list(() -> repo.updateEventRepo.get().getAllByParentId(id));
            return new AlertImpl(uuid, name, shortDescription, tagList, parameters, subscriberId, status,
                    created.getTime());
        }
    }
}
