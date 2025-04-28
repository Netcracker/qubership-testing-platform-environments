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

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.qubership.atp.environments.errorhandling.database.UserSettingsAlreadyExistsException;
import org.qubership.atp.environments.model.UserSetting;
import org.qubership.atp.environments.model.impl.UserSettingImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import com.google.common.base.Preconditions;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.MappingProjection;
import com.querydsl.sql.SQLQueryFactory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
@SuppressWarnings("CPD-START")
public class UserSettingRepositoryImpl extends AbstractRepository {

    private final SQLQueryFactory queryFactory;

    private final UserSettingProjection projection = new UserSettingProjection(this);

    @Autowired
    public UserSettingRepositoryImpl(SQLQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Nullable
    public UserSetting getByUserId(@Nonnull UUID userId) {
        return queryFactory.select(projection).from(USER_SETTINGS)
                .where(USER_SETTINGS.userId.eq(userId)).fetchOne();
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    public UserSetting create(@Nonnull UUID userId, @Nonnull String view) {
        try {
            long create = queryFactory.insert(USER_SETTINGS)
                    .set(USER_SETTINGS.userId, userId)
                    .set(USER_SETTINGS.view, view)
                    .execute();
            Preconditions.checkArgument(create > 0, "Nothing created");
        } catch (DataIntegrityViolationException e) {
            log.error(String.format("Error occurred while creating new user settings."
                    + " User settings for user with id %s already exists", userId));
            throw new UserSettingsAlreadyExistsException(userId);
        }
        return projection.create(userId, view);
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    public UserSetting update(@Nonnull UUID userId, @Nonnull String view) {
        long update = queryFactory.update(USER_SETTINGS)
                .set(USER_SETTINGS.view, view)
                .where(USER_SETTINGS.userId.eq(userId))
                .execute();
        Preconditions.checkArgument(update > 0, "Nothing updated");
        return projection.create(userId, view);
    }

    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    private static class UserSettingProjection extends MappingProjection<UserSetting> {

        static final long serialVersionUID = 42L;
        private final transient UserSettingRepositoryImpl repo;

        public UserSettingProjection(UserSettingRepositoryImpl repo) {
            super(UserSetting.class, USER_SETTINGS.all());
            this.repo = repo;
        }

        @Override
        protected UserSetting map(Tuple tuple) {
            UUID userId = tuple.get(USER_SETTINGS.userId);
            assert userId != null;
            String view = tuple.get(USER_SETTINGS.view);
            return create(userId, view);
        }

        protected UserSettingImpl create(UUID userId, String view) {
            return new UserSettingImpl(userId, view);
        }
    }
}
