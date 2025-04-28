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

import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.qubership.atp.environments.mocks.JaversEntitiesGenerator.generateCount;
import static org.qubership.atp.environments.mocks.JaversEntitiesGenerator.generateGlobalId;
import static org.qubership.atp.environments.mocks.JaversEntitiesGenerator.getGlobalIdAndCount;
import static org.qubership.atp.environments.mocks.JaversEntitiesGenerator.getOld;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubership.atp.environments.model.response.JaversCountResponse;
import org.qubership.atp.environments.model.response.JaversVersionResponse;
import org.qubership.atp.environments.repo.impl.JaversCommitPropertyRepositoryImpl;
import org.qubership.atp.environments.repo.impl.JaversCommitRepositoryImpl;
import org.qubership.atp.environments.repo.impl.JaversGlobalIdRepositoryImpl;
import org.qubership.atp.environments.repo.impl.JaversSnapshotRepositoryImpl;
import org.qubership.atp.environments.service.direct.JaversSnapshotService;
import org.springframework.test.util.ReflectionTestUtils;

public class JaversSnapshotServiceImplTest {

    private final ThreadLocal<JaversSnapshotRepositoryImpl> javersSnapshotRepository = new ThreadLocal<>();
    private final ThreadLocal<JaversCommitRepositoryImpl> javersCommitRepository = new ThreadLocal<>();
    private final ThreadLocal<JaversCommitPropertyRepositoryImpl> javersCommitPropertyRepository = new ThreadLocal<>();
    private final ThreadLocal<JaversSnapshotService> service = new ThreadLocal<>();

    private static final Integer lastRevisionCount = 200;

    @BeforeEach
    public void setUp() {
        JaversSnapshotRepositoryImpl javersSnapshotRepositoryMock = mock(JaversSnapshotRepositoryImpl.class);
        JaversCommitRepositoryImpl javersCommitRepositoryMock = mock(JaversCommitRepositoryImpl.class);
        JaversCommitPropertyRepositoryImpl javersCommitPropertyRepositoryMock = mock(JaversCommitPropertyRepositoryImpl.class);
        JaversGlobalIdRepositoryImpl javersGlobalIdRepository = mock(JaversGlobalIdRepositoryImpl.class);
        JaversSnapshotServiceImpl serviceMock = new JaversSnapshotServiceImpl(
                javersSnapshotRepositoryMock,
                javersCommitRepositoryMock,
                javersCommitPropertyRepositoryMock,
                javersGlobalIdRepository);
        ReflectionTestUtils.setField(serviceMock, "bulkDeleteCount", 1000);

        javersSnapshotRepository.set(javersSnapshotRepositoryMock);
        javersCommitRepository.set(javersCommitRepositoryMock);
        javersCommitPropertyRepository.set(javersCommitPropertyRepositoryMock);
        service.set(serviceMock);
    }

    @Test
    public void onJaversSnapshotRepositoryImpl_getGlobalIdAndCount() {
        int count = generateCount();
        List<JaversCountResponse> obj = getGlobalIdAndCount(count);
        when(service.get().getGlobalIdAndCount(lastRevisionCount)).thenReturn(obj);
        Assertions.assertNotNull(obj, "Shouldn't be null");
        Assertions.assertEquals(count, obj.size(), "Should be equal");
    }

    @Test
    public void onJaversSnapshotRepositoryImpl_getOld() {
        int count = generateCount();
        List<JaversVersionResponse> responses = getOld(count, generateGlobalId());
        when(service.get().getOld(anyLong(), anyLong())).thenReturn(responses);
        Assertions.assertNotNull(responses, "Shouldn't be null");
        Assertions.assertEquals(count, responses.size(), "Should be equal");
    }

    @Test
    public void onJaversSnapshotRepositoryImpl_deleteOldAndUpdateAsInitial_CountNotEqualsZero() {
        Long count = Long.valueOf(generateCount());
        service.get().deleteOldAndUpdateAsInitial(anyLong(), anyLong(), anyLong());
        verify(javersSnapshotRepository.get()).deleteByVersionAndGlobalIdAndCommitId(anyLong(), anyLong(), anyLong());
        when(javersSnapshotRepository.get().getCountByCommitId(anyLong())).thenReturn(count);
        verify(javersCommitPropertyRepository.get()).deleteByCommitId(anyLong());
        verify(javersCommitRepository.get()).deleteByCommitId(anyLong());
        verify(javersSnapshotRepository.get()).updateAsInitial(anyLong());
    }

    @Test
    public void onJaversSnapshotRepositoryImpl_deleteTerminatedSnapshots() {
        int count = generateCount();
        List<JaversVersionResponse> responses = getOld(count, generateGlobalId());
        service.get().deleteTerminatedSnapshots();
        when(javersSnapshotRepository.get().getTerminatedSnapshots()).thenReturn(responses);
        Assertions.assertNotNull(responses, "Shouldn't be null");
    }
}
