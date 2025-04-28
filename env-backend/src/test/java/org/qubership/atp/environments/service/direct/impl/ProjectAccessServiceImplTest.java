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

import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.qubership.atp.environments.clients.api.catalogue.generated.ProjectDto;
import org.qubership.atp.environments.service.rest.client.CatalogFeignClient;
import org.springframework.http.ResponseEntity;

class ProjectAccessServiceImplTest {

    private final ThreadLocal<CatalogFeignClient> catalogFeignClient = new ThreadLocal<>();

    private ProjectAccessServiceImpl projectAccessService;


    @BeforeEach
    public void setUp() {
        CatalogFeignClient catalogFeignClientMock = Mockito.mock(CatalogFeignClient.class);
        catalogFeignClient.set(catalogFeignClientMock);
        projectAccessService = new ProjectAccessServiceImpl(catalogFeignClient.get());
    }

    @Test
    public void test_getProjectIdsWithAccess_gotProjectIds() {
        UUID projectId = UUID.randomUUID();
        ProjectDto projectDto =
                new ProjectDto();
        projectDto.setUuid(projectId);
        Mockito.when(catalogFeignClient.get().getAllShortProjects()).thenReturn(ResponseEntity.ok(Collections.singletonList(projectDto)));
        Assertions.assertEquals(projectAccessService.getProjectIdsWithAccess().get(0), projectId);
    }


}
