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

package org.qubership.atp.environments.service.direct.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.qubership.atp.environments.model.response.JaversCountResponse;
import org.qubership.atp.environments.model.response.JaversVersionResponse;
import org.qubership.atp.environments.repo.impl.JaversCommitPropertyRepositoryImpl;
import org.qubership.atp.environments.repo.impl.JaversCommitRepositoryImpl;
import org.qubership.atp.environments.repo.impl.JaversGlobalIdRepositoryImpl;
import org.qubership.atp.environments.repo.impl.JaversSnapshotRepositoryImpl;
import org.qubership.atp.environments.service.direct.JaversSnapshotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Iterators;
import lombok.extern.slf4j.Slf4j;

@Service("javersSnapshotService")
@Slf4j
public class JaversSnapshotServiceImpl implements JaversSnapshotService {

    @Value("${atp-environments.archive.job.bulk-delete-count}")
    private Integer bulkDeleteCount;

    private final JaversSnapshotRepositoryImpl javersSnapshotRepository;
    private final JaversCommitRepositoryImpl javersCommitRepository;
    private final JaversCommitPropertyRepositoryImpl javersCommitPropertyRepository;
    private final JaversGlobalIdRepositoryImpl javersGlobalIdRepository;

    /**
     * Constructor for JaversSnapshotServiceImpl class.
     * @param repository                     javersSnapshot repository.
     * @param javersCommitRepository         javersCommit repository.
     * @param javersCommitPropertyRepository javersCommitProperty repository.
     * @param javersGlobalIdRepository       javersGlobalId repository.
     */
    @Autowired
    public JaversSnapshotServiceImpl(JaversSnapshotRepositoryImpl repository,
                                     JaversCommitRepositoryImpl javersCommitRepository,
                                     JaversCommitPropertyRepositoryImpl javersCommitPropertyRepository,
                                     JaversGlobalIdRepositoryImpl javersGlobalIdRepository) {
        this.javersSnapshotRepository = repository;
        this.javersCommitRepository = javersCommitRepository;
        this.javersCommitPropertyRepository = javersCommitPropertyRepository;
        this.javersGlobalIdRepository = javersGlobalIdRepository;
    }

    /**
     * Get globalId and number of old objects.
     *
     * @return {@link List} of {@link JaversCountResponse}
     */
    @Override
    public List<JaversCountResponse> getGlobalIdAndCount(Integer lastRevisionCount) {
        List<JaversCountResponse> globalIdAndCount = javersSnapshotRepository.getGlobalIdAndCount(lastRevisionCount);
        log.debug("Number of unique globalId '{}'", globalIdAndCount.size());
        return globalIdAndCount;
    }

    /**
     * Get globalId, commitId and version for old objects.
     *
     * @param globalId global id.
     * @param count    number of revisions which must be removed.
     * @return {@link List} of {@link JaversVersionResponse}
     */
    @Override
    public List<JaversVersionResponse> getOld(Long globalId, Long count) {
        List<JaversVersionResponse> snapshots = javersSnapshotRepository.getOld(globalId, count);
        log.debug("Number of old snapshots '{}' for globalId '{}'", snapshots.size(), globalId);
        return snapshots;
    }

    /**
     * Delete old snapshots, commit properties and commits.
     * And update the oldest snapshot as initial.
     *
     * @param version  old version.
     * @param globalId global id.
     * @param commitId commit id.
     */
    @Override
    @Transactional
    public void deleteOldAndUpdateAsInitial(Long version, Long globalId, Long commitId) {
        javersSnapshotRepository.deleteByVersionAndGlobalIdAndCommitId(version, globalId, commitId);
        log.debug("Deleted snapshots with version '{}', globalId '{}' and commitId '{}'.", version, globalId, commitId);
        Long commitCount = javersSnapshotRepository.getCountByCommitId(commitId);
        if (commitCount == 0) {
            javersCommitPropertyRepository.deleteByCommitId(commitId);
            javersCommitRepository.deleteByCommitId(commitId);
            log.debug("Deleted commit properties and commits with commitId '{}'", commitId);
        }
        javersSnapshotRepository.updateAsInitial(globalId);
    }

    /**
     * Delete terminated snapshots, globalIds, commits and commit properties.
     */
    @Override
    @Transactional
    public void deleteTerminatedSnapshots() {
        List<JaversVersionResponse> terminatedSnapshots = javersSnapshotRepository.getTerminatedSnapshots();
        List<Long> globalIds = terminatedSnapshots.stream()
                .map(JaversVersionResponse::getGlobalId)
                .distinct()
                .collect(Collectors.toList());
        List<Long> commitIds = terminatedSnapshots.stream()
                .map(JaversVersionResponse::getCommitId)
                .collect(Collectors.toList());
        log.debug("Number of terminated globalIds '{}', commitIds '{}'", globalIds.size(), commitIds.size());
        Iterators.partition(globalIds.iterator(), bulkDeleteCount)
                .forEachRemaining(javersSnapshotRepository::deleteByGlobalIds);
        log.debug("Terminated snapshots deleted");
        Iterators.partition(globalIds.iterator(), bulkDeleteCount)
                .forEachRemaining(javersGlobalIdRepository::deleteByGlobalIds);
        log.debug("Terminated globalIds deleted");
        Iterators.partition(commitIds.iterator(), bulkDeleteCount)
                .forEachRemaining(javersCommitPropertyRepository::deleteByCommitIds);
        log.debug("Terminated commit properties deleted");
        Iterators.partition(commitIds.iterator(), bulkDeleteCount)
                .forEachRemaining(javersCommitRepository::deleteByCommitIds);
        log.debug("Terminated commits deleted");
    }
}
