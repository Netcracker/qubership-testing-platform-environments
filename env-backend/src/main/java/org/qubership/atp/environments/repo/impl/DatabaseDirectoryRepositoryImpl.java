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

import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.qubership.atp.environments.model.DatabaseDirectory;
import org.qubership.atp.environments.model.impl.DatabaseDirectoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.MappingProjection;
import com.querydsl.sql.SQLQueryFactory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Repository
public class DatabaseDirectoryRepositoryImpl extends AbstractRepository {

    private final SQLQueryFactory queryFactory;

    private final DatabaseDirectoryProjection projection = new DatabaseDirectoryProjection(this);

    @Autowired
    public DatabaseDirectoryRepositoryImpl(SQLQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * The method returns a database by name.
     */
    @Nullable
    public DatabaseDirectory getByName(@Nonnull String name) {
        return queryFactory.select(projection).from(DATABASE_DIRECTORY)
                .where(DATABASE_DIRECTORY.name.toLowerCase().eq(name.toLowerCase(Locale.ENGLISH))).fetchOne();
    }

    @Nonnull
    public List<DatabaseDirectory> getAll() {
        return queryFactory.select(projection).from(DATABASE_DIRECTORY).fetch();
    }


    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    private static class DatabaseDirectoryProjection extends MappingProjection<DatabaseDirectory> {

        static final long serialVersionUID = 42L;

        private DatabaseDirectoryProjection(DatabaseDirectoryRepositoryImpl repo) {
            super(DatabaseDirectory.class, DATABASE_DIRECTORY.all());
        }

        @Override
        protected DatabaseDirectory map(Tuple tuple) {
            String name = tuple.get(DATABASE_DIRECTORY.name);
            assert name != null;
            String urlFormat = tuple.get(DATABASE_DIRECTORY.urlFormat);
            return create(name, urlFormat);
        }

        protected DatabaseDirectoryImpl create(String name,
                                               String urlFormat) {
            return new DatabaseDirectoryImpl(name, urlFormat);
        }
    }
}
