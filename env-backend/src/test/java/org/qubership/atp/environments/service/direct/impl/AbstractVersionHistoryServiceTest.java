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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.javers.core.Javers;
import org.javers.core.commit.CommitId;
import org.javers.shadow.Shadow;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubership.atp.environments.errorhandling.history.EnvironmentHistoryRevisionNotFoundException;
import org.qubership.atp.environments.mapper.EnvironmentVersioningMapper;
import org.qubership.atp.environments.mocks.EntitiesGenerator;
import org.qubership.atp.environments.mocks.JaversEntitiesGenerator;
import org.qubership.atp.environments.service.rest.server.dto.generated.CompareEntityResponseDtoGenerated;
import org.qubership.atp.environments.versioning.model.entities.EnvironmentJ;
import org.qubership.atp.environments.versioning.model.mapper.EnvironmentVersioning;
import org.qubership.atp.environments.versioning.service.impl.EnvironmentVersionHistoryService;

public class AbstractVersionHistoryServiceTest {

    private final ThreadLocal<Javers> javers = new ThreadLocal<>();
    private final ThreadLocal<EnvironmentVersioningMapper> environmentVersioningMapper = new ThreadLocal<>();
    private final ThreadLocal<EnvironmentVersionHistoryService> versionHistoryService = new ThreadLocal<>();

    @BeforeEach
    public void setUp() {
        Javers javersMock = mock(Javers.class);
        EnvironmentVersioningMapper environmentVersioningMapperMock = mock(EnvironmentVersioningMapper.class);
        javers.set(javersMock);
        environmentVersioningMapper.set(environmentVersioningMapperMock);
        versionHistoryService.set(new EnvironmentVersionHistoryService(javersMock, environmentVersioningMapperMock));
    }

    @Test
    public void onAbstractVersionHistoryService_whenGetEntitiesByInvalidRevisionId_ExceptionIsThrown() {
        when(javers.get().findShadows(any())).thenReturn(Collections.EMPTY_LIST);
        Assertions.assertThrows(EnvironmentHistoryRevisionNotFoundException.class,
                () -> versionHistoryService.get().getEntitiesByVersion(UUID.randomUUID(), Collections.singletonList("1")));
    }

    @Test
    public void onAbstractVersionHistoryService_whenGetEntities_expectedVersionsReturned()
    {
        List<String> revisionIds = Arrays.asList("1", "2");
        when(environmentVersioningMapper.get().map(any())).thenReturn(new EnvironmentVersioning());

        List<Shadow<Object>> collect = revisionIds
                .stream()
                .map(revisionId -> {
                            Shadow<Object> shadow = mock(Shadow.class);
                            EnvironmentJ environmentJ =
                                    new EnvironmentJ(EntitiesGenerator.generateEnvironment("Environment" + revisionId));
                            when(shadow.get()).thenReturn(environmentJ);
                            when(shadow.getCommitMetadata()).
                                    thenReturn(JaversEntitiesGenerator.generateCommitMetaData("author", CommitId.valueOf("1.0")));
                            when(javers.get().findShadows(any())).thenReturn(Arrays.asList(shadow));
                            return shadow;
                        }
                )
                .collect(Collectors.toList());

        List<CompareEntityResponseDtoGenerated> entitiesByVersion =
                versionHistoryService.get().getEntitiesByVersion(UUID.randomUUID(), revisionIds);

        Assertions.assertNotNull(entitiesByVersion);
        Assertions.assertEquals(revisionIds.size(), entitiesByVersion.size());

        entitiesByVersion.forEach(compareEntityResponse -> {
            Assertions.assertNotNull(compareEntityResponse.getCompareEntity());
            Assertions.assertNotNull(compareEntityResponse.getRevision());
        });

        List<String> actualRevisionIds = entitiesByVersion
                .stream()
                .map(CompareEntityResponseDtoGenerated::getRevision)
                .collect(Collectors.toList());

        Assertions.assertEquals(revisionIds, actualRevisionIds);
    }

    @Test
    public void onAbstractVersionHistoryService_whenGetEntities_expectedModifiedByReturned()
    {
        String expectedRevision = "1";
        String expectedAuthor = "Author";
        when(environmentVersioningMapper.get().map(any())).thenReturn(new EnvironmentVersioning());

        Shadow<Object> shadow = mock(Shadow.class);
        EnvironmentJ environmentJ =
                new EnvironmentJ(EntitiesGenerator.generateEnvironment("Environment" + expectedRevision));
        when(shadow.get()).thenReturn(environmentJ);
        when(shadow.getCommitMetadata()).
                thenReturn(JaversEntitiesGenerator.generateCommitMetaData(expectedAuthor, CommitId.valueOf("1.0")));
        when(javers.get().findShadows(any())).thenReturn(Arrays.asList(shadow));

        List<CompareEntityResponseDtoGenerated> entitiesByVersion =
                versionHistoryService.get().getEntitiesByVersion(UUID.randomUUID(), Arrays.asList(expectedRevision));

        Assertions.assertNotNull(entitiesByVersion);
        CompareEntityResponseDtoGenerated compareEntityResponseDto = entitiesByVersion.iterator().next();
        Assertions.assertNotNull(compareEntityResponseDto);

        EnvironmentVersioning environmentVersioning = (EnvironmentVersioning)compareEntityResponseDto.getCompareEntity();
        Assertions.assertEquals(expectedAuthor, environmentVersioning.getModifiedBy());
    }
}
