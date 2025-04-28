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

import org.qubership.atp.environments.db.Proxies;
import org.qubership.atp.environments.model.Subscriber;
import org.qubership.atp.environments.model.Subscription;
import org.qubership.atp.environments.model.impl.SubscriptionImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.google.common.base.Preconditions;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.MappingProjection;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLQueryFactory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Repository
@SuppressWarnings("CPD-START")
public class SubscriptionRepositoryImpl extends AbstractRepository {

    public static final Integer CASCADE_SUSBSCRIPTION_TYPE = 22;

    private final SQLQueryFactory queryFactory;

    private final Provider<ProjectRepositoryImpl> projectRepo;
    private final Provider<EnvironmentRepositoryImpl> environmentRepo;
    private final Provider<SystemRepositoryImpl> systemRepo;
    private final Provider<SubscriberRepositoryImpl> subscriberRepo;
    //private final Provider<UpdateEventRepositoryImpl> updateEventRepo;
    private final Provider<SubscriptionRepositoryImpl> subscriptionRepo;

    private final SubscriptionProjection projection = new SubscriptionProjection(this);

    private final BooleanExpression cascadeSubscription =
            SUBSCRIPTIONS.subscriptionType.eq(CASCADE_SUSBSCRIPTION_TYPE);
    private final BooleanExpression simpleSubscription =
            SUBSCRIPTIONS.subscriptionType.ne(CASCADE_SUSBSCRIPTION_TYPE);

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Autowired
    public SubscriptionRepositoryImpl(SQLQueryFactory queryFactory,
                                      Provider<ProjectRepositoryImpl> projectRepo,
                                      Provider<EnvironmentRepositoryImpl> environmentRepo,
                                      Provider<SystemRepositoryImpl> systemRepo,
                                      Provider<SubscriberRepositoryImpl> subscriberRepo,
                                      Provider<SubscriptionRepositoryImpl> subscriptionRepo/*,
                                      Provider<UpdateEventRepositoryImpl> updateEventRepo*/) {
        this.queryFactory = queryFactory;
        this.projectRepo = projectRepo;
        this.environmentRepo = environmentRepo;
        this.systemRepo = systemRepo;
        this.subscriberRepo = subscriberRepo;
        //this.updateEventRepo = updateEventRepo;
        this.subscriptionRepo = subscriptionRepo;
    }

    @Nullable
    public Subscription getById(@Nonnull UUID id) {
        return queryFactory.select(projection).from(SUBSCRIPTIONS).where(SUBSCRIPTIONS.id.eq(id)).fetchOne();
    }

    public boolean existsById(@Nonnull UUID id) {
        return queryFactory.select(projection).from(SUBSCRIPTIONS).where(SUBSCRIPTIONS.id.eq(id))
                .fetchCount() > 0;
    }

    @Nonnull
    public List<Subscription> getAll() {
        return queryFactory.select(projection).from(SUBSCRIPTIONS).fetch();
    }

    @Nonnull
    public List<Subscription> getAllByParentId(@Nonnull UUID subscriberId) {
        return queryFactory.select(projection).from(SUBSCRIPTIONS)
                .where(SUBSCRIPTIONS.subscriberId.eq(subscriberId)).fetch();
    }

    @Nonnull
    public List<UUID> getListIdSubscriptionsForProject(@Nonnull UUID projectId) {
        return queryFactory.select(SUBSCRIPTIONS.id).from(SUBSCRIPTIONS)
                .where(SUBSCRIPTIONS.projectId.eq(projectId)).fetch();
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    public List<UUID> getListIdSubscriptionsForProject(@Nonnull UUID projectId, boolean cascade) {
        BooleanExpression condition = cascade ? cascadeSubscription : simpleSubscription;
        return queryFactory.select(SUBSCRIPTIONS.id).from(SUBSCRIPTIONS)
                .where(SUBSCRIPTIONS.projectId.eq(projectId).and(condition)).fetch();
    }

    @Nonnull
    public List<UUID> getListIdSubscriptionsForEnvironment(@Nonnull UUID environmentId) {
        return queryFactory.select(SUBSCRIPTIONS.id).from(SUBSCRIPTIONS)
                .where(SUBSCRIPTIONS.environmentId.eq(environmentId)).fetch();
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    public List<UUID> getListIdSubscriptionsForEnvironment(@Nonnull UUID environmentId, boolean cascade) {
        BooleanExpression condition = cascade ? cascadeSubscription : simpleSubscription;
        return queryFactory.select(SUBSCRIPTIONS.id).from(SUBSCRIPTIONS)
                .where(SUBSCRIPTIONS.environmentId.eq(environmentId).and(condition)).fetch();
    }

    @Nonnull
    public List<UUID> getListIdSubscriptionsForSystem(@Nonnull UUID systemId) {
        return queryFactory.select(SUBSCRIPTIONS.id).from(SUBSCRIPTIONS)
                .where(SUBSCRIPTIONS.systemId.eq(systemId)).fetch();
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    public List<UUID> getListIdSubscriptionsForSystem(@Nonnull UUID systemId, boolean cascade) {
        BooleanExpression condition = cascade ? cascadeSubscription : simpleSubscription;
        return queryFactory.select(SUBSCRIPTIONS.id).from(SUBSCRIPTIONS)
                .where(SUBSCRIPTIONS.systemId.eq(systemId).and(condition)).fetch();
    }

    @Nonnull
    public List<Subscription> getSubscriberSubscriptions(@Nonnull UUID subscriberId) {
        return queryFactory.select(projection).from(SUBSCRIPTIONS)
                .where(SUBSCRIPTIONS.subscriberId.eq(subscriberId)).fetch();
    }

    @Nonnull
    public List<Subscription> getProjectSubscriptions(@Nonnull UUID projectId) {
        return queryFactory.select(projection).from(SUBSCRIPTIONS)
                .where(SUBSCRIPTIONS.projectId.eq(projectId)).fetch();
    }

    @Nonnull
    public List<Subscription> getEnvironmentSubscriptions(@Nonnull UUID environmentId) {
        return queryFactory.select(projection).from(SUBSCRIPTIONS)
                .where(SUBSCRIPTIONS.environmentId.eq(environmentId)).fetch();
    }

    @Nonnull
    public List<Subscription> getSystemSubscriptions(@Nonnull UUID systemId) {
        return queryFactory.select(projection).from(SUBSCRIPTIONS)
                .where(SUBSCRIPTIONS.systemId.eq(systemId)).fetch();
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    public List<UUID> getListSubscriptionsByConditions(UUID projectId, UUID environmentId, UUID systemId,
                                                       @Nonnull UUID subscriberId) {
        List<UUID> listSubscriptionId;
        if (projectId != null) {
            listSubscriptionId = queryFactory.select(SUBSCRIPTIONS.id).from(SUBSCRIPTIONS)
                    .where(SUBSCRIPTIONS.projectId.eq(projectId).and(SUBSCRIPTIONS.subscriberId.eq(subscriberId)))
                    .fetch();
        } else if (environmentId != null) {
            listSubscriptionId = queryFactory.select(SUBSCRIPTIONS.id).from(SUBSCRIPTIONS)
                    .where(SUBSCRIPTIONS.environmentId.eq(environmentId).and(SUBSCRIPTIONS.subscriberId
                            .eq(subscriberId))).fetch();
        } else {
            listSubscriptionId = queryFactory.select(SUBSCRIPTIONS.id).from(SUBSCRIPTIONS)
                    .where(SUBSCRIPTIONS.systemId.eq(systemId).and(SUBSCRIPTIONS.subscriberId.eq(subscriberId)))
                    .fetch();
        }
        return listSubscriptionId;
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    public Subscription create(Integer subscriptionType,
                               UUID projectId, UUID environmentId, UUID systemId, UUID subscriberId,
                               Integer status, Long lastUpdated) {
        Timestamp lastUpdatedTimestamp = new Timestamp(lastUpdated);
        UUID uuid = queryFactory.insert(SUBSCRIPTIONS)
                .set(SUBSCRIPTIONS.subscriptionType, subscriptionType)
                .set(SUBSCRIPTIONS.projectId, projectId)
                .set(SUBSCRIPTIONS.environmentId, environmentId)
                .set(SUBSCRIPTIONS.systemId, systemId)
                .set(SUBSCRIPTIONS.subscriberId, subscriberId)
                .set(SUBSCRIPTIONS.status, status)
                .set(SUBSCRIPTIONS.lastUpdated, lastUpdatedTimestamp)
                .executeWithKey(SUBSCRIPTIONS.id);
        return projection.create(uuid, subscriptionType, projectId, environmentId, systemId, subscriberId, status,
                lastUpdatedTimestamp);
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    public Subscription update(@Nonnull UUID subscriptionId, Integer subscriptionType,
                               UUID projectId, UUID environmentId, UUID systemId, UUID subscriberId,
                               Integer status, Long lastUpdated) {
        Timestamp lastUpdatedTimestamp = new Timestamp(lastUpdated);
        long update = queryFactory.update(SUBSCRIPTIONS)
                .set(SUBSCRIPTIONS.subscriptionType, subscriptionType)
                .set(SUBSCRIPTIONS.projectId, projectId)
                .set(SUBSCRIPTIONS.environmentId, environmentId)
                .set(SUBSCRIPTIONS.systemId, systemId)
                .set(SUBSCRIPTIONS.subscriberId, subscriberId)
                .set(SUBSCRIPTIONS.status, status)
                .set(SUBSCRIPTIONS.lastUpdated, lastUpdatedTimestamp)
                .where(SUBSCRIPTIONS.id.eq(subscriptionId)).execute();
        Preconditions.checkArgument(update > 0, "Nothing updated");
        return projection.create(subscriptionId, subscriptionType, projectId, environmentId, systemId, subscriberId,
                status, lastUpdatedTimestamp);
    }

    public void delete(UUID subscriptionId) {
        deleteReferenceToTable(subscriptionId, SUBSCRIPTIONS, SUBSCRIPTIONS.id);
    }

    private void deleteReferenceToTable(UUID id, RelationalPathBase path, SimplePath<UUID> simplePath) {
        queryFactory.delete(path).where(simplePath.eq(id)).execute();
    }

    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    private static class SubscriptionProjection extends MappingProjection<Subscription> {

        static final long serialVersionUID = 42L;
        private final transient SubscriptionRepositoryImpl repo;

        public SubscriptionProjection(SubscriptionRepositoryImpl repo) {
            super(Subscription.class, SUBSCRIPTIONS.all());
            this.repo = repo;
        }

        @Override
        protected Subscription map(Tuple tuple) {
            UUID uuid = tuple.get(SUBSCRIPTIONS.id);
            assert uuid != null;
            Integer subscriptionType = tuple.get(SUBSCRIPTIONS.subscriptionType);
            UUID projectId = tuple.get(SUBSCRIPTIONS.projectId);
            UUID environmentId = tuple.get(SUBSCRIPTIONS.environmentId);
            UUID systemId = tuple.get(SUBSCRIPTIONS.systemId);
            UUID subscriberId = tuple.get(SUBSCRIPTIONS.subscriberId);
            Integer status = tuple.get(SUBSCRIPTIONS.status);
            Timestamp lastUpdated = tuple.get(SUBSCRIPTIONS.lastUpdated);
            assert lastUpdated != null;
            return create(uuid, subscriptionType, projectId, environmentId, systemId, subscriberId, status,
                    lastUpdated);
        }

        protected SubscriptionImpl create(UUID uuid, Integer subscriptionType,
                                          UUID projectId, UUID environmentId,
                                          UUID systemId, UUID subscriberId, Integer status,
                                          Timestamp lastUpdated) {
            Subscriber subscriberAsObj = Proxies.withId(Subscriber.class, subscriberId,
                    id -> repo.subscriberRepo.get().getById(id));
            return new SubscriptionImpl(uuid, subscriptionType, projectId, environmentId, systemId,
                    subscriberAsObj, status, lastUpdated.getTime(), null/*updateEventsListId*/);
        }
    }
}
