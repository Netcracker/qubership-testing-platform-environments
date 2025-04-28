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

package org.qubership.atp.environments.mapper;

import org.qubership.atp.environments.service.direct.EnvironmentCategoryService;
import org.qubership.atp.environments.service.direct.ProjectService;
import org.qubership.atp.environments.service.direct.SystemService;
import org.qubership.atp.environments.versioning.model.entities.EnvironmentJ;
import org.qubership.atp.environments.versioning.model.mapper.EnvironmentVersioning;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentVersioningMapper extends AbstractVersioningMapper<EnvironmentJ, EnvironmentVersioning> {

    private final EnvironmentCategoryService environmentCategoryService;
    private final ProjectService projectService;
    private final SystemService systemService;

    /**
     * EnvironmentVersioningMapper constructor.
     * @param environmentCategoryService environmentCategoryService
     * @param projectService             projectService
     * @param systemService              systemService
     */
    @Autowired
    public EnvironmentVersioningMapper(EnvironmentCategoryService environmentCategoryService,
                                       ProjectService projectService,
                                       SystemService systemService) {
        super(EnvironmentJ.class, EnvironmentVersioning.class);
        this.environmentCategoryService = environmentCategoryService;
        this.projectService = projectService;
        this.systemService = systemService;
    }

    @Override
    void mapSpecificFields(EnvironmentJ source, EnvironmentVersioning destination) {
        destination.setCategoryName(getAbstractEntityName(source.getCategoryId(), environmentCategoryService));
        destination.setProjectName(getAbstractEntityName(source.getProjectId(), projectService));
        destination.setSystemNames(getAbstractEntityNames(source.getSystemIds(), systemService));
    }
}
