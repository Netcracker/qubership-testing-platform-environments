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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import javax.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.repo.impl.AbstractRepository;
import org.qubership.atp.environments.repo.impl.EnvironmentRepositoryImpl;
import org.qubership.atp.environments.repo.impl.SystemRepositoryImpl;
import org.qubership.atp.environments.repo.mapper.EnvironmentMapper;

import com.querydsl.core.Tuple;

public class GenericEnvironmentProjectionTest {

    private final ThreadLocal<EnvironmentRepositoryImpl> repo = new ThreadLocal<>();

    private final ThreadLocal<SystemRepositoryImpl> systemRepo = new ThreadLocal<>();

    private final ThreadLocal<Tuple> tuple = new ThreadLocal<>();

    private final ThreadLocal<GenericEnvironmentProjection> genericEnvironmentProjection = new ThreadLocal<>();

    @BeforeEach
    public void setUp() throws Exception {
        Provider systemRepositoryProvider = mock(Provider.class);
        SystemRepositoryImpl systemRepoMock = mock(SystemRepositoryImpl.class);
        EnvironmentRepositoryImpl repoMock = mock(EnvironmentRepositoryImpl.class);

        when(systemRepositoryProvider.get()).thenReturn(systemRepoMock);
        when(repoMock.getSystemRepo()).thenReturn(systemRepositoryProvider);

        repo.set(repoMock);
        systemRepo.set(systemRepoMock);
        tuple.set(mock(Tuple.class));
        genericEnvironmentProjection.set(new GenericEnvironmentProjection(repoMock,
                Arrays.asList("id", "name", "graylogName", "description", "ssmSolutionAlias",
                        "ssmInstanceAlias","consulEgressConfigPath", "projectId", "created", "createdBy",
                        "modified", "modifiedBy",
                        "categoryId", "sourceId"),
                new EnvironmentMapper()));
    }

    @Test
    public void test_map_successful() {
        UUID uuid = UUID.randomUUID();
        String name = "environment";
        String graylogName = "graylog";
        String description = "desc";
        String ssmSolutionAlias = "ssSolution";
        String ssmInstanceAlias = "ssmInstance";
        String consulEgressConfigPath = "consulEgressConfigPath";
        UUID projectId = UUID.randomUUID();
        Timestamp created = new Timestamp(System.currentTimeMillis());
        UUID createdBy = UUID.randomUUID();
        Timestamp modified = new Timestamp(System.currentTimeMillis());
        UUID modifiedBy = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID sourceId = UUID.randomUUID();
        when(tuple.get().get(AbstractRepository.ENVIRONMENTS.id)).thenReturn(uuid);
        when(tuple.get().get(AbstractRepository.ENVIRONMENTS.name)).thenReturn(name);
        when(tuple.get().get(AbstractRepository.ENVIRONMENTS.graylogName)).thenReturn(graylogName);
        when(tuple.get().get(AbstractRepository.ENVIRONMENTS.description)).thenReturn(description);
        when(tuple.get().get(AbstractRepository.ENVIRONMENTS.ssmSolutionAlias)).thenReturn(ssmSolutionAlias);
        when(tuple.get().get(AbstractRepository.ENVIRONMENTS.ssmInstanceAlias)).thenReturn(ssmInstanceAlias);
        when(tuple.get().get(AbstractRepository.ENVIRONMENTS.consulEgressConfigPath)).thenReturn(consulEgressConfigPath);
        when(tuple.get().get(AbstractRepository.ENVIRONMENTS.projectId)).thenReturn(projectId);
        when(tuple.get().get(AbstractRepository.ENVIRONMENTS.created)).thenReturn(created);
        when(tuple.get().get(AbstractRepository.ENVIRONMENTS.createdBy)).thenReturn(createdBy);
        when(tuple.get().get(AbstractRepository.ENVIRONMENTS.modified)).thenReturn(modified);
        when(tuple.get().get(AbstractRepository.ENVIRONMENTS.modifiedBy)).thenReturn(modifiedBy);
        when(tuple.get().get(AbstractRepository.ENVIRONMENTS.categoryId)).thenReturn(categoryId);
        when(tuple.get().get(AbstractRepository.ENVIRONMENTS.sourceId)).thenReturn(sourceId);
        when(repo.get().getSystemRepo().get()).thenReturn(systemRepo.get());
        when(systemRepo.get().getAllByParentIdAndCategoryId(any(UUID.class), any(UUID.class)))
                .thenReturn(Collections.emptyList());
        Environment environment = genericEnvironmentProjection.get().map(tuple.get());
        assertNotNull(environment);
        assertEquals(uuid, environment.getId());
        assertEquals(name, environment.getName());
        assertEquals(graylogName, environment.getGraylogName());
        assertEquals(description, environment.getDescription());
        assertEquals(ssmSolutionAlias, environment.getSsmSolutionAlias());
        assertEquals(ssmInstanceAlias, environment.getSsmInstanceAlias());
        assertEquals(consulEgressConfigPath, environment.getConsulEgressConfigPath());
        assertEquals(created.getTime(), environment.getCreated().longValue());
        assertEquals(createdBy, environment.getCreatedBy());
        assertEquals(modified.getTime(), environment.getModified().longValue());
        assertEquals(modifiedBy, environment.getModifiedBy());
        assertEquals(projectId, environment.getProjectId());
        assertEquals(categoryId, environment.getCategoryId());
        assertEquals(sourceId, environment.getSourceId());
    }
}
