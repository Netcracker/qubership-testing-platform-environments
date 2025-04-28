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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.impl.EnvironmentImpl;
import org.qubership.atp.environments.repo.impl.AbstractRepository;
import org.qubership.atp.environments.repo.impl.EnvironmentRepositoryImpl;

import com.google.gson.Gson;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.MappingProjection;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
@SuppressWarnings("CPD-START")
public class ShortEnvironmentProjection extends MappingProjection<Environment> {

    private static final long serialVersionUID = 42L;
    protected final transient EnvironmentRepositoryImpl repo;

    public ShortEnvironmentProjection(EnvironmentRepositoryImpl repo) {
        super(Environment.class, AbstractRepository.ENVIRONMENTS.all());
        this.repo = repo;
    }

    @Override
    protected Environment map(Tuple tuple) {
        UUID uuid = tuple.get(AbstractRepository.ENVIRONMENTS.id);
        assert uuid != null;
        String name = tuple.get(AbstractRepository.ENVIRONMENTS.name);
        assert name != null;
        String graylogName = tuple.get(AbstractRepository.ENVIRONMENTS.graylogName);
        String description = tuple.get(AbstractRepository.ENVIRONMENTS.description);
        String ssmSolutionAlias = tuple.get(AbstractRepository.ENVIRONMENTS.ssmSolutionAlias);
        String ssmInstanceAlias = tuple.get(AbstractRepository.ENVIRONMENTS.ssmInstanceAlias);
        String consulEgressConfigPath = tuple.get(AbstractRepository.ENVIRONMENTS.consulEgressConfigPath);
        UUID projectId = tuple.get(AbstractRepository.ENVIRONMENTS.projectId);
        assert projectId != null;
        Timestamp created = tuple.get(AbstractRepository.ENVIRONMENTS.created);
        UUID createdBy = tuple.get(AbstractRepository.ENVIRONMENTS.createdBy);
        Timestamp modified = tuple.get(AbstractRepository.ENVIRONMENTS.modified);
        UUID modifiedBy = tuple.get(AbstractRepository.ENVIRONMENTS.modifiedBy);
        UUID categoryId = tuple.get(AbstractRepository.ENVIRONMENTS.categoryId);
        UUID sourceId = tuple.get(AbstractRepository.ENVIRONMENTS.sourceId);
        Object tags = tuple.get(AbstractRepository.ENVIRONMENTS.tags);
        assert categoryId != null;
        return create(uuid, name, graylogName, description, ssmSolutionAlias, ssmInstanceAlias,
                consulEgressConfigPath, created, createdBy, modified, modifiedBy, projectId, categoryId,
                sourceId,  tags == null ? Collections.emptyList() : new Gson().fromJson(tags.toString(), List.class));
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
        return new EnvironmentImpl(uuid, name, graylogName, description, ssmSolutionAlias, ssmInstanceAlias,
                consulEgressConfigPath, created.getTime(), createdBy, modified == null ? null :
                modified.getTime(), modifiedBy, projectId,
                null, categoryId, sourceId, tags);
    }
}
