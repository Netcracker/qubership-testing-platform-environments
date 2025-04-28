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

import org.qubership.atp.environments.db.Proxies;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.impl.ProjectImpl;
import org.qubership.atp.environments.model.utils.Constants;
import org.qubership.atp.environments.repo.impl.ProjectRepositoryImpl;

@SuppressWarnings("CPD-START")
public class LazyProjectProjection extends ShortProjectProjection {

    private static final long serialVersionUID = 42L;

    public LazyProjectProjection(ProjectRepositoryImpl repo) {
        super(repo);
    }

    @Override
    public ProjectImpl create(UUID id,
                              String name,
                              String shortName,
                              String description,
                              Timestamp created,
                              Timestamp modified) {
        List<Environment> environmentsListId = Proxies.list(() ->
                repo.getEnvironmentRepo().get().getAllShortByParentId(id, Constants.Environment.Category.ENVIRONMENT));
        return new ProjectImpl(id, name, shortName, description, environmentsListId, created.getTime(),
                modified == null ? null : modified.getTime());
    }
}
