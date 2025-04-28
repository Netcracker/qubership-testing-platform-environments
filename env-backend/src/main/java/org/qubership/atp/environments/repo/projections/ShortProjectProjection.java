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
import java.util.UUID;

import org.qubership.atp.environments.model.Project;
import org.qubership.atp.environments.model.impl.ProjectImpl;
import org.qubership.atp.environments.repo.impl.AbstractRepository;
import org.qubership.atp.environments.repo.impl.ProjectRepositoryImpl;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.MappingProjection;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
public class ShortProjectProjection extends MappingProjection<Project> {

    private static final long serialVersionUID = 42L;
    protected final transient ProjectRepositoryImpl repo;

    public ShortProjectProjection(ProjectRepositoryImpl repo) {
        super(Project.class, AbstractRepository.PROJECTS.all());
        this.repo = repo;
    }

    @Override
    protected Project map(Tuple tuple) {
        UUID uuid = tuple.get(AbstractRepository.PROJECTS.id);
        assert uuid != null;
        String name = tuple.get(AbstractRepository.PROJECTS.name);
        assert name != null;
        String shortName = tuple.get(AbstractRepository.PROJECTS.shortName);
        String description = tuple.get(AbstractRepository.PROJECTS.description);
        Timestamp created = tuple.get(AbstractRepository.PROJECTS.created);
        Timestamp modified = tuple.get(AbstractRepository.PROJECTS.modified);
        return create(uuid, name, shortName, description, created, modified);
    }

    public ProjectImpl create(UUID id,
                              String name,
                              String shortName,
                              String description,
                              Timestamp created,
                              Timestamp modified) {
        return new ProjectImpl(id, name, shortName, description, null, created.getTime(),
                modified == null ? null : modified.getTime());
    }
}
