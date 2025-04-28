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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.qubership.atp.environments.clients.api.catalogue.generated.ProjectDto;
import org.qubership.atp.environments.service.direct.ProjectAccessService;
import org.qubership.atp.environments.service.rest.client.CatalogFeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service("projectAccessService")
@RequiredArgsConstructor
@Slf4j
public class ProjectAccessServiceImpl implements ProjectAccessService {

    private final CatalogFeignClient catalogClient;

    @Override
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public List<UUID> getProjectIdsWithAccess() {
        ResponseEntity<List<ProjectDto>> response = catalogClient.getAllShortProjects();
        return response != null && !CollectionUtils.isEmpty(response.getBody())
                ? response.getBody().stream().map(ProjectDto::getUuid).collect(Collectors.toList())
                : Collections.emptyList();
    }
}
