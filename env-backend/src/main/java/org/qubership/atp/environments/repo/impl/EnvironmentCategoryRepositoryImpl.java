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

import org.qubership.atp.environments.model.EnvironmentCategory;
import org.qubership.atp.environments.model.impl.EnvironmentCategoryImpl;
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
public class EnvironmentCategoryRepositoryImpl extends AbstractRepository {

    private final SQLQueryFactory queryFactory;

    private final EnvironmentCategoryProjection projection = new EnvironmentCategoryProjection(this);

    @Autowired
    public EnvironmentCategoryRepositoryImpl(SQLQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Nonnull
    public EnvironmentCategory getById(@Nonnull UUID id) {
        return queryFactory.select(projection).from(ENVIRONMENT_CATEGORIES).where(ENVIRONMENT_CATEGORIES.id.eq(id))
                .fetchOne();
    }

    public boolean existsById(@Nonnull UUID id) {
        return queryFactory.select(projection).from(ENVIRONMENT_CATEGORIES)
                .where(ENVIRONMENT_CATEGORIES.id.eq(id)).fetchCount() > 0;
    }

    @Nonnull
    public List<EnvironmentCategory> getAll() {
        return queryFactory.select(projection).from(ENVIRONMENT_CATEGORIES).fetch();
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    public EnvironmentCategory create(@Nonnull String name, String description, String tagList, Long created) {
        Timestamp createdTimestamp = new Timestamp(created);
        UUID id = queryFactory.insert(ENVIRONMENT_CATEGORIES)
                .set(ENVIRONMENT_CATEGORIES.name, name)
                .set(ENVIRONMENT_CATEGORIES.description, description)
                .set(ENVIRONMENT_CATEGORIES.tagList, tagList)
                .set(ENVIRONMENT_CATEGORIES.created, createdTimestamp)
                .executeWithKey(ENVIRONMENT_CATEGORIES.id);
        return projection.create(id, name, description, tagList, createdTimestamp, null);
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    public EnvironmentCategory update(@Nonnull UUID id, @Nonnull String name, String description, String tagList,
                                      Long modified) {
        Timestamp modifiedTimestamp = new Timestamp(modified);
        long update = queryFactory.update(ENVIRONMENT_CATEGORIES)
                .set(ENVIRONMENT_CATEGORIES.name, name)
                .set(ENVIRONMENT_CATEGORIES.description, description)
                .set(ENVIRONMENT_CATEGORIES.tagList, tagList)
                .set(ENVIRONMENT_CATEGORIES.modified, modifiedTimestamp)
                .where(ENVIRONMENT_CATEGORIES.id.eq(id)).execute();
        Timestamp created = queryFactory.select(ENVIRONMENT_CATEGORIES.created)
                .from(ENVIRONMENT_CATEGORIES)
                .where(ENVIRONMENT_CATEGORIES.id.eq(id)).fetchOne();
        Preconditions.checkArgument(update > 0, "Nothing updated");
        return projection.create(id, name, description, tagList, created, modifiedTimestamp);
    }

    public void delete(UUID id) {
        deleteReferenceToTable(id, ENVIRONMENT_CATEGORIES, ENVIRONMENT_CATEGORIES.id);
    }

    private void deleteReferenceToTable(UUID id, RelationalPathBase path, SimplePath<UUID> simplePath) {
        queryFactory.delete(path).where(simplePath.eq(id)).execute();
    }

    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    private static class EnvironmentCategoryProjection extends MappingProjection<EnvironmentCategory> {

        static final long serialVersionUID = 42L;
        private final transient EnvironmentCategoryRepositoryImpl repo;

        private EnvironmentCategoryProjection(EnvironmentCategoryRepositoryImpl repo) {
            super(EnvironmentCategory.class, ENVIRONMENT_CATEGORIES.all());
            this.repo = repo;
        }

        @Override
        protected EnvironmentCategory map(Tuple tuple) {
            UUID uuid = tuple.get(ENVIRONMENT_CATEGORIES.id);
            assert uuid != null;
            String name = tuple.get(ENVIRONMENT_CATEGORIES.name);
            assert name != null;
            String description = tuple.get(ENVIRONMENT_CATEGORIES.description);
            String tagList = tuple.get(ENVIRONMENT_CATEGORIES.tagList);
            Timestamp created = tuple.get(ENVIRONMENT_CATEGORIES.created);
            assert created != null;
            Timestamp modified = tuple.get(ENVIRONMENT_CATEGORIES.modified);
            return create(uuid, name, description, tagList, created, modified);
        }

        protected EnvironmentCategoryImpl create(UUID uuid,
                                                 String name,
                                                 String description,
                                                 String tagList,
                                                 Timestamp created,
                                                 Timestamp modified) {
            return new EnvironmentCategoryImpl(uuid, name, description, tagList, created.getTime(),
                    modified == null ? null : modified.getTime());
        }
    }
}
