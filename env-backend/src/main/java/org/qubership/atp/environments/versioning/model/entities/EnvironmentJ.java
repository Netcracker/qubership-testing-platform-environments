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

package org.qubership.atp.environments.versioning.model.entities;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.javers.core.metamodel.annotation.TypeName;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.Identified;
import org.qubership.atp.environments.service.direct.EnvironmentCategoryService;
import org.qubership.atp.environments.service.direct.ProjectService;
import org.qubership.atp.environments.versioning.validation.ReferenceExists;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;

@Getter
@TypeName("Environment")
public class EnvironmentJ extends AbstractJaversEntity {

    private String name;
    private String description;
    @SuppressFBWarnings("EI_EXPOSE_REP")
    private Date modified;
    @ReferenceExists(service = ProjectService.class, message = "project")
    private UUID projectId;
    @ReferenceExists(service = EnvironmentCategoryService.class, message = "environment category")
    private UUID categoryId;
    private Set<UUID> systemIds;
    private List<String> tags;

    /**
     * The main constructor.
     *
     * @param model - Domain object of {@link Environment}
     */
    public EnvironmentJ(Environment model) {
        super(model);
        name = model.getName();
        description = model.getDescription();
        modified = model.getModified() == null
                ? null
                : new Date(model.getModified());
        projectId = model.getProjectId();
        categoryId = model.getCategoryId();
        tags = model.getTags();
        if (!CollectionUtils.isEmpty(model.getSystems())) {
            systemIds = model.getSystems()
                    .stream()
                    .map(Identified::getId)
                    .collect(Collectors.toSet());
        }
    }
}
