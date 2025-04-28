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

import org.javers.core.metamodel.object.SnapshotType;
import org.qubership.atp.environments.model.response.JaversCountResponse;
import org.qubership.atp.environments.model.response.JaversVersionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.sql.SQLQueryFactory;

@Repository
@SuppressWarnings("CPD-START")
public class JaversSnapshotRepositoryImpl extends AbstractRepository {

    private static final String GLOBAL_ID = "globalId";
    private static final String COMMIT_ID = "commitId";
    private static final String COUNT = "count";
    private static final String VERSION = "version";

    private final SQLQueryFactory queryFactory;

    @Autowired
    public JaversSnapshotRepositoryImpl(SQLQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * Get terminated snapshots.
     *
     * @return {@link List} of {@link JaversVersionResponse}.
     */
    public List<JaversVersionResponse> getTerminatedSnapshots() {
        return queryFactory
                .select(Projections.fields(
                        JaversVersionResponse.class,
                        JV_SNAPSHOT.globalIdFk.as(GLOBAL_ID),
                        JV_SNAPSHOT.commitFk.as(COMMIT_ID),
                        JV_SNAPSHOT.version.as(VERSION)))
                .from(JV_SNAPSHOT)
                .where(JV_SNAPSHOT.globalIdFk.in(getTerminalGlobalIds()))
                .fetch();
    }

    /**
     * Get globalId and number of snapshots.
     *
     * @param count number of last revision.
     * @return {@link List} of {@link JaversCountResponse}
     */
    public List<JaversCountResponse> getGlobalIdAndCount(Integer count) {
        return queryFactory
                .select(Projections.fields(
                        JaversCountResponse.class,
                        JV_SNAPSHOT.globalIdFk.as(GLOBAL_ID),
                        JV_SNAPSHOT.globalIdFk.count().as(COUNT)))
                .from(JV_SNAPSHOT)
                .groupBy(JV_SNAPSHOT.globalIdFk)
                .having(JV_SNAPSHOT.globalIdFk.count().gt(count))
                .fetch();
    }

    /**
     * Get globalId, commitId and version for old objects.
     *
     * @param globalId global id.
     * @param count    number of revisions which must be removed.
     * @return {@link List} of {@link JaversVersionResponse}
     */
    public List<JaversVersionResponse> getOld(Long globalId, Long count) {
        return queryFactory
                .select(Projections.fields(
                        JaversVersionResponse.class,
                        JV_SNAPSHOT.globalIdFk.as(GLOBAL_ID),
                        JV_SNAPSHOT.commitFk.as(COMMIT_ID),
                        JV_SNAPSHOT.version.as(VERSION)))
                .from(JV_SNAPSHOT)
                .where(JV_SNAPSHOT.globalIdFk.eq(globalId))
                .orderBy(JV_SNAPSHOT.version.asc())
                .limit(count)
                .fetch();
    }

    /**
     * Delete snapshots by version, globalId and commitId.
     *
     * @param version  old version.
     * @param globalId global id.
     * @param commitId commit id.
     */
    public void deleteByVersionAndGlobalIdAndCommitId(Long version, Long globalId, Long commitId) {
        queryFactory
                .delete(JV_SNAPSHOT)
                .where(JV_SNAPSHOT.version.eq(version),
                        JV_SNAPSHOT.globalIdFk.eq(globalId),
                        JV_SNAPSHOT.commitFk.eq(commitId))
                .execute();
    }

    /**
     * Delete snapshots by globalIds.
     *
     * @param globalIds globalIds.
     */
    public void deleteByGlobalIds(List<Long> globalIds) {
        queryFactory.delete(JV_SNAPSHOT).where(JV_SNAPSHOT.globalIdFk.in(globalIds)).execute();
    }

    /**
     * Get number of snapshots.
     *
     * @param commitId commit id.
     * @return {@link Long} number of snapshots by commit id.
     */
    public Long getCountByCommitId(Long commitId) {
        return queryFactory.from(JV_SNAPSHOT).where(JV_SNAPSHOT.commitFk.eq(commitId)).fetchCount();
    }

    /**
     * Update snapshot as initial.
     *
     * @param globalId global id.
     */
    public void updateAsInitial(Long globalId) {
        Long minVersion = queryFactory
                .select(JV_SNAPSHOT.version.min())
                .from(JV_SNAPSHOT)
                .where(JV_SNAPSHOT.globalIdFk.eq(globalId))
                .fetchFirst();
        queryFactory
                .update(JV_SNAPSHOT)
                .set(JV_SNAPSHOT.type, SnapshotType.INITIAL.name())
                .where(JV_SNAPSHOT.globalIdFk.eq(globalId),
                        JV_SNAPSHOT.version.eq(minVersion))
                .execute();
    }

    /**
     * Get globalIds of terminated snapshots.
     *
     * @return {@link List} of {@link Long} with globalIds.
     */
    private List<Long> getTerminalGlobalIds() {
        return queryFactory
                .select(Projections.constructor(Long.class, JV_SNAPSHOT.globalIdFk.as(GLOBAL_ID)))
                .from(JV_SNAPSHOT)
                .where(JV_SNAPSHOT.type.eq(SnapshotType.TERMINAL.name()))
                .fetch();
    }
}
