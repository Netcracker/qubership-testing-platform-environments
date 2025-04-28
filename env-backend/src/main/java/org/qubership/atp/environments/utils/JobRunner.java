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

package org.qubership.atp.environments.utils;

import org.qubership.atp.environments.service.direct.JaversSnapshotService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Component
public class JobRunner {

    private static final String UTC_TIMEZONE = "UTC";

    private final JaversSnapshotService javersSnapshotService;
    private final ThreadPoolTaskExecutor archiveJobExecutor;

    @Value("${atp-environments.last.revision.count}")
    private Integer lastRevisionCount;

    public JobRunner(JaversSnapshotService javersSnapshotService,
                     ThreadPoolTaskExecutor archiveJobExecutor) {
        this.javersSnapshotService = javersSnapshotService;
        this.archiveJobExecutor = archiveJobExecutor;
    }

    /**
     * Job that removes irrelevant data from the change history.
     */
    @Scheduled(cron = "${atp-environments.archive.cron.expression}", zone = UTC_TIMEZONE)
    @SchedulerLock(name = "${atp-environments.archive.job.name}", lockAtMostFor = "12h", lockAtLeastFor = "1h")
    public void run() {
        javersSnapshotService.deleteTerminatedSnapshots();
        javersSnapshotService.getGlobalIdAndCount(lastRevisionCount).stream()
                .<Runnable>map(response ->
                        () -> javersSnapshotService.getOld(response.getGlobalId(),
                                response.getCount() - lastRevisionCount)
                                .forEach(snapshot -> javersSnapshotService.deleteOldAndUpdateAsInitial(
                                        snapshot.getVersion(), snapshot.getGlobalId(), snapshot.getCommitId())))
                .forEach(archiveJobExecutor::execute);
    }
}
