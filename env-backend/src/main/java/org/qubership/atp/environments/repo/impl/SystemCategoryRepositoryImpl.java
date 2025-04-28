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
import java.util.Locale;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.qubership.atp.environments.model.SystemCategory;
import org.qubership.atp.environments.model.impl.SystemCategoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.google.common.base.Preconditions;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.MappingProjection;
import com.querydsl.sql.SQLQueryFactory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Repository
public class SystemCategoryRepositoryImpl extends AbstractRepository {

    private final SQLQueryFactory queryFactory;

    private final SystemCategoriesProjection projection = new SystemCategoriesProjection(this);

    @Autowired
    public SystemCategoryRepositoryImpl(SQLQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Nonnull
    public SystemCategory getById(@Nonnull UUID id) {
        return queryFactory.select(projection).from(SYSTEM_CATEGORIES).where(SYSTEM_CATEGORIES.id.eq(id)).fetchOne();
    }

    public boolean existsById(@Nonnull UUID id) {
        return queryFactory.select(projection)
                .from(SYSTEM_CATEGORIES).where(SYSTEM_CATEGORIES.id.eq(id)).fetchCount() > 0;
    }

    /**
     * The method returns a system category by name.
     */
    @Nonnull
    public SystemCategory getByName(@Nonnull String name) {
        SystemCategory systemCategory = queryFactory.select(projection).from(SYSTEM_CATEGORIES)
                .where(SYSTEM_CATEGORIES.name.toLowerCase().eq(name.toLowerCase(Locale.ENGLISH))).fetchOne();
        Preconditions.checkNotNull(systemCategory, "Category not found.");
        return systemCategory;
    }

    @Nonnull
    public List<SystemCategory> getAll() {
        return queryFactory.select(projection).from(SYSTEM_CATEGORIES).fetch();
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    public SystemCategory create(@Nonnull String name,
                                 String description,
                                 Long created) {
        Timestamp createdTimestamp = new Timestamp(created);
        UUID id = queryFactory.insert(SYSTEM_CATEGORIES)
                .set(SYSTEM_CATEGORIES.name, name)
                .set(SYSTEM_CATEGORIES.description, description)
                .set(SYSTEM_CATEGORIES.created, createdTimestamp)
                .executeWithKey(SYSTEM_CATEGORIES.id);
        return projection.create(id, name, description, createdTimestamp, null);
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    public SystemCategory update(@Nonnull UUID id,
                                 @Nonnull String name,
                                 String description,
                                 Long modified) {
        Timestamp modifiedTimestamp = new Timestamp(modified);
        long update = queryFactory.update(SYSTEM_CATEGORIES)
                .set(SYSTEM_CATEGORIES.name, name)
                .set(SYSTEM_CATEGORIES.description, description)
                .set(SYSTEM_CATEGORIES.modified, modifiedTimestamp)
                .where(SYSTEM_CATEGORIES.id.eq(id)).execute();
        Timestamp created = queryFactory.select(SYSTEM_CATEGORIES.created)
                .from(SYSTEM_CATEGORIES)
                .where(SYSTEM_CATEGORIES.id.eq(id)).fetchOne();
        Preconditions.checkArgument(update > 0, "Information about system categories not updated");
        return projection.create(id, name, description, created, modifiedTimestamp);
    }

    public void delete(UUID id) {
        queryFactory.delete(SYSTEM_CATEGORIES).where(SYSTEM_CATEGORIES.id.eq(id)).execute();
    }

    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    private static class SystemCategoriesProjection extends MappingProjection<SystemCategory> {

        static final long serialVersionUID = 42L;

        private SystemCategoriesProjection(SystemCategoryRepositoryImpl repo) {
            super(SystemCategory.class, SYSTEM_CATEGORIES.all());
        }

        @Override
        protected SystemCategory map(Tuple tuple) {
            UUID uuid = tuple.get(SYSTEM_CATEGORIES.id);
            assert uuid != null;
            String name = tuple.get(SYSTEM_CATEGORIES.name);
            assert name != null;
            String description = tuple.get(SYSTEM_CATEGORIES.description);
            Timestamp created = tuple.get(SYSTEM_CATEGORIES.created);
            assert created != null;
            Timestamp modified = tuple.get(SYSTEM_CATEGORIES.modified);
            return create(uuid, name, description, created, modified);
        }

        protected SystemCategoryImpl create(UUID uuid,
                                            String name,
                                            String description,
                                            Timestamp created,
                                            Timestamp modified) {
            return new SystemCategoryImpl(uuid, name, description, created.getTime(),
                    modified == null ? null : modified.getTime());
        }
    }
}
