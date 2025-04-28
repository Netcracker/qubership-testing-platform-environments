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

package org.qubership.atp.environments.service.rest.server;

import org.qubership.atp.environments.model.Project;
import org.qubership.atp.environments.model.impl.ProjectImpl;
import org.qubership.atp.environments.service.direct.ProjectService;
import org.qubership.atp.integration.configuration.configuration.AuditAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

@RequestMapping("/catalog/api/v1/projects/bulk/create")
@ControllerAdvice
@RestController()
public class CatalogController  /*implements CatalogControllerApi*/ {

    private final ProjectService projectService;

    @Autowired
    public CatalogController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * Endpoint for creating Project via UI.
     * Parse json to Project entity.
     */
    @PostMapping
    @AuditAction(auditAction = "Create project")
    public Project create(@RequestBody JsonNode jsonNode) {
        Project project = new ProjectImpl();
        String name = jsonNode.get("project").get("name").asText();
        project.setName(name);
        project.setShortName(name);
        project.setDescription(name);
        return projectService.create(project);
    }
}
