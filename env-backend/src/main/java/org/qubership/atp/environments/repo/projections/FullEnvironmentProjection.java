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

package org.qubership.atp.environments.repo.projections;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.qubership.atp.environments.db.Proxies;
import org.qubership.atp.environments.model.Project;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.impl.EnvironmentImpl;
import org.qubership.atp.environments.repo.impl.EnvironmentRepositoryImpl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
@SuppressWarnings("CPD-START")
public class FullEnvironmentProjection extends ShortEnvironmentProjection {

    private static final long serialVersionUID = 42L;

    public FullEnvironmentProjection(EnvironmentRepositoryImpl repo) {
        super(repo);
    }

    /**
     * Create Environment with parameters.
     *
     * @return {@link EnvironmentImpl} instance
     */
    public EnvironmentImpl create(UUID uuid,
                                  String name,
                                  String graylogName,
                                  String description,
                                  String ssmSolutionAlias,
                                  String ssmInstanceAlias,
                                  String consulEgressConfigPath,
                                  Timestamp created,
                                  UUID createdBy,
                                  Timestamp modified,
                                  UUID modifiedBy,
                                  @Nonnull UUID projectId,
                                  @Nonnull UUID categoryId,
                                  UUID sourceId,
                                  List<String> tags) {
        UUID projId = Proxies.withId(Project.class, projectId,
                id -> repo.getProjectRepo().get()
                        .getById(id)).getId();
        List<System> systemsListId = Proxies.list(() -> repo
                .getSystemRepo().get().getAllByParentId(uuid));
        return new EnvironmentImpl(uuid, name, graylogName, description, ssmSolutionAlias, ssmInstanceAlias,
                consulEgressConfigPath, created.getTime(), createdBy, modified == null ? null : modified.getTime(),
                modifiedBy, projId, systemsListId, categoryId, sourceId, tags);
    }
}
