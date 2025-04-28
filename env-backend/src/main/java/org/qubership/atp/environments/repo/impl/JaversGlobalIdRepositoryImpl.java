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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.querydsl.sql.SQLQueryFactory;

@Repository
public class JaversGlobalIdRepositoryImpl extends AbstractRepository {

    private final SQLQueryFactory queryFactory;

    @Autowired
    public JaversGlobalIdRepositoryImpl(SQLQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * Delete globalIds by ids.
     *
     * @param globalIds globalIds.
     */
    public void deleteByGlobalIds(List<Long> globalIds) {
        queryFactory.delete(JV_GLOBAL_ID).where(JV_GLOBAL_ID.globalIdPk.in(globalIds)).execute();
    }
}
