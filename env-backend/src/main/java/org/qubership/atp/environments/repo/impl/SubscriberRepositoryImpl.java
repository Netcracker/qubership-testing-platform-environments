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
import org.qubership.atp.environments.model.impl.SubscriberImpl;
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
public class SubscriberRepositoryImpl extends AbstractRepository {

    private final SQLQueryFactory queryFactory;
    private final Provider<SubscriptionRepositoryImpl> subscriptionRepo;

    private final SubscriberProjection projection = new SubscriberProjection(this);

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Autowired
    public SubscriberRepositoryImpl(SQLQueryFactory queryFactory,
                                    Provider<SubscriptionRepositoryImpl> subscriptionRepo) {
        this.queryFactory = queryFactory;
        this.subscriptionRepo = subscriptionRepo;
    }

    @Nullable
    public Subscriber getById(@Nonnull UUID id) {
        return queryFactory.select(projection).from(SUBSCRIBERS).where(SUBSCRIBERS.id.eq(id)).fetchOne();
    }

    public boolean existsById(@Nonnull UUID id) {
        return queryFactory.select(projection).from(SUBSCRIBERS).where(SUBSCRIBERS.id.eq(id))
                .fetchCount() > 0;
    }

    @Nonnull
    public List<Subscriber> getAll() {
        return queryFactory.select(projection).from(SUBSCRIBERS).fetch();
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    public Subscriber create(@Nonnull String name,
                             String host, Integer subscriberType, String signature,
                             String tagList, Integer hostStatus, String notificationURL,
                             Long registrationDate) {
        Timestamp registrationDateTimestamp = new Timestamp(registrationDate);
        UUID uuid = queryFactory.insert(SUBSCRIBERS)
                .set(SUBSCRIBERS.name, name)
                .set(SUBSCRIBERS.host, host)
                .set(SUBSCRIBERS.subscriberType, subscriberType)
                .set(SUBSCRIBERS.signature, signature)
                .set(SUBSCRIBERS.tagList, tagList)
                .set(SUBSCRIBERS.hostStatus, hostStatus)
                .set(SUBSCRIBERS.notificationUrl, notificationURL)
                .set(SUBSCRIBERS.registrationDate, registrationDateTimestamp)
                .executeWithKey(SUBSCRIBERS.id);
        return projection.create(uuid, name, host, subscriberType, signature, tagList, hostStatus, notificationURL,
                registrationDateTimestamp);
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    public Subscriber update(@Nonnull UUID subscriberId,
                             @Nonnull String name,
                             String host, Integer subscriberType, String signature,
                             String tagList, Integer hostStatus, @Nonnull String notificationURL,
                             Long registrationDate) {
        Timestamp registrationDateTimestamp = new Timestamp(registrationDate);
        long update = queryFactory.update(SUBSCRIBERS)
                .set(SUBSCRIBERS.name, name)
                .set(SUBSCRIBERS.host, host)
                .set(SUBSCRIBERS.subscriberType, subscriberType)
                .set(SUBSCRIBERS.signature, signature)
                .set(SUBSCRIBERS.tagList, tagList)
                .set(SUBSCRIBERS.hostStatus, hostStatus)
                .set(SUBSCRIBERS.notificationUrl, notificationURL)
                .set(SUBSCRIBERS.registrationDate, registrationDateTimestamp)
                .where(SUBSCRIBERS.id.eq(subscriberId)).execute();
        Preconditions.checkArgument(update > 0, "Nothing updated");
        return projection.create(subscriberId, name, host, subscriberType, signature, tagList, hostStatus,
                notificationURL, registrationDateTimestamp);
    }

    public void delete(UUID subscriberId) {
        deleteReferenceToTable(subscriberId, SUBSCRIBERS, SUBSCRIBERS.id);
    }

    private void deleteReferenceToTable(UUID subscriberId, RelationalPathBase path, SimplePath<UUID> simplePath) {
        queryFactory.delete(path).where(simplePath.eq(subscriberId)).execute();
    }

    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    private static class SubscriberProjection extends MappingProjection<Subscriber> {

        static final long serialVersionUID = 42L;
        private final transient SubscriberRepositoryImpl repo;

        public SubscriberProjection(SubscriberRepositoryImpl repo) {
            super(Subscriber.class, SUBSCRIBERS.all());
            this.repo = repo;
        }

        @Override
        protected Subscriber map(Tuple tuple) {
            UUID uuid = tuple.get(SUBSCRIBERS.id);
            assert uuid != null;
            String name = tuple.get(SUBSCRIBERS.name);
            assert name != null;
            String host = tuple.get(SUBSCRIBERS.host);
            Integer subscriberType = tuple.get(SUBSCRIBERS.subscriberType);
            String signature = tuple.get(SUBSCRIBERS.signature);
            String tagList = tuple.get(SUBSCRIBERS.tagList);
            Integer hostStatus = tuple.get(SUBSCRIBERS.hostStatus);
            String notificationURL = tuple.get(SUBSCRIBERS.notificationUrl);
            Timestamp registrationDate = tuple.get(SUBSCRIBERS.registrationDate);
            assert registrationDate != null;
            return create(uuid, name, host, subscriberType, signature,
                    tagList, hostStatus, notificationURL, registrationDate);
        }

        protected SubscriberImpl create(UUID id, String name,
                                        String host, Integer subscriberType, String signature,
                                        String tagList, Integer hostStatus, String notificationURL,
                                        Timestamp registrationDate) {
            List<Subscription> subscriptionsListId =
                    Proxies.list(() -> repo.subscriptionRepo.get().getAllByParentId(id));
            return new SubscriberImpl(id, name, host, subscriberType, signature, tagList,
                    hostStatus, notificationURL, registrationDate.getTime(), subscriptionsListId);
        }
    }
}
