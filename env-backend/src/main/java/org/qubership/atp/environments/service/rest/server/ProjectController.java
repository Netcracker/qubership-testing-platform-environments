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

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.Project;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.impl.ProjectImpl;
import org.qubership.atp.environments.model.utils.Constants;
import org.qubership.atp.environments.model.utils.View;
import org.qubership.atp.environments.service.direct.EnvironmentService;
import org.qubership.atp.environments.service.direct.ProjectService;
import org.qubership.atp.environments.service.direct.SystemService;
import org.qubership.atp.environments.service.rest.server.dto.EnvironmentDto;
import org.qubership.atp.environments.service.rest.server.dto.ProjectDto;
import org.qubership.atp.environments.service.rest.server.dto.StatusDto;
import org.qubership.atp.environments.service.rest.server.request.ProjectSearchRequest;
import org.qubership.atp.environments.service.rest.server.response.GroupedByTagEnvironmentResponse;
import org.qubership.atp.integration.configuration.configuration.AuditAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RequestMapping("/api")
@RestController()
public class ProjectController /*implements ProjectControllerApi*/ {

    private final ProjectService projectService;
    private final EnvironmentService environmentService;
    private final SystemService systemService;

    /**
     * ProjectController constructor.
     */
    @Autowired
    public ProjectController(ProjectService projectService,
                             EnvironmentService environmentService,
                             SystemService systemService) {
        this.projectService = projectService;
        this.environmentService = environmentService;
        this.systemService = systemService;
    }

    /**
     * Endpoint for getting list of projects.
     */
    @GetMapping("/projects")
    @JsonView({View.FullVer2.class})
    @AuditAction(auditAction = "Get all projects")
    public List<Project> getAllProjects(@RequestParam(value = "host", required = false) String host) {
        if (StringUtils.isNotBlank(host)) {
            return projectService.getProjectsByHost(host);
        } else {
            return projectService.getAll();
        }
    }

    /**
     * Endpoint for getting list of projects in abbreviated form ({"id":"","name":""}).
     */
    @JsonView({View.Name.class})
    @GetMapping("/projects/short")
    @AuditAction(auditAction = "Get all short projects")
    public List<Project> getAllShort() {
        return projectService.getAllShort();
    }

    /**
     * Endpoint for getting list of projects for which user have an access in abbreviated form ({"id":"",
     * "name":""}).
     */
    @JsonView({View.Name.class})
    @AuditAction(auditAction = "Get all short projects with access")
    @GetMapping("/projects/shortWithAccess")
    @Deprecated
    public List<Project> getAllShortWithUserAccess() {
        return projectService.getAllShort();
    }

    @GetMapping("/projects/{projectId}")
    @JsonView({View.FullVer1.class})
    @AuditAction(auditAction = "Get project with uuid {{#id.toString()}}")
    public Project getProject(@PathVariable("projectId") UUID id) {
        return projectService.get(id);
    }

    @GetMapping("/projects/short/{projectId}")
    @PreAuthorize("@entityAccess.checkAccess(#id,'READ')")
    @JsonView({View.FullVer1.class})
    public Project getShortProject(@PathVariable("projectId") UUID id) {
        return projectService.getShort(id);
    }

    @GetMapping("/projects/short/name/{projectName}")
    @JsonView({View.FullVer1.class})
    public Project getShortProjectByName(@PathVariable("projectName") String name) {
        return projectService.getShortByName(name);
    }

    /**
     * Getting a project with specified environments.
     */
    @GetMapping("/projects/{projectId}/{environmentIds}")
    @JsonView({View.FullVer1.class})
    @AuditAction(auditAction = "Get project with specified environments for project {{#projectId.toString()"
            + "}}")
    public ResponseEntity<Project> getProjectWithSpecifiedEnvironments(
            @PathVariable("projectId") UUID projectId,
            @PathVariable("environmentIds") List<UUID> environmentIds) {
        Project project = projectService.getProjectWithSpecifiedEnvironments(projectId, environmentIds);
        return project != null ? ResponseEntity.ok(project) : ResponseEntity.notFound().build();
    }

    /**
     * Endpoint for getting list of systems on project in abbreviated form ({"id":"","name":"",
     * "environmentIds":[]}).
     */
    @GetMapping("/projects/{projectId}/environments/systems/short")
    @JsonView({View.Environments.class})
    @Operation(description = "Endpoint for getting list of systems on project by projectId")
    @AuditAction(auditAction = "Get all short systems by project id {{#id.toString()}}")
    public List<System> getAllShortSystemsOnProject(@PathVariable("projectId")
                                                    @Parameter(description = "Project id for getting "
                                                            + "system") UUID id) {
        return projectService.getSystemsByProjectId(id);
    }

    @GetMapping("/projects/{projectId}/environments")
    @AuditAction(auditAction = "Get all environments by project id {{#id.toString()}}")
    public List<Environment> getEnvironments(@PathVariable("projectId") UUID id) {
        return projectService.getEnvironments(id);
    }

    @GetMapping("/projects/{projectId}/environments/groupedByTags")
    public Collection<GroupedByTagEnvironmentResponse> getEnvironmentsGroupedByTags(
            @PathVariable("projectId") UUID id
    ) {
        return environmentService.getGroupedByTagEnvironments(id);
    }

    @PreAuthorize("@entityAccess.isAuthenticated()")
    @PostMapping("/projects/search")
    @JsonView({View.FullVer1.class})
    public List<Project> getProjectsByRequest(@RequestBody ProjectSearchRequest request) {
        return projectService.getProjectsByRequest(request);
    }

    /**
     * Endpoint for update system statuses by statuses list.
     */
    @PreAuthorize("@entityAccess.checkAccess(#projectId,'UPDATE')")
    @PutMapping("projects/{projectId}/systems/status")
    @JsonView({View.FullVer2.class})
    @AuditAction(auditAction = "Update system statuses by project id {{#projectId.toString()}} and system "
            + "statuses list")
    public ResponseEntity<Object> updateSystemStatuses(@RequestBody List<StatusDto> statuses,
                                                       @PathVariable UUID projectId) {
        return ResponseEntity.ok(systemService.saveStatusesAndDateOfLastCheck(statuses, projectId));
    }

    /**
     * Endpoint for getting list of environments in abbreviated form ({"id":"","name":""}).
     */
    @JsonView({View.Name.class})
    @GetMapping("/projects/{projectId}/environments/short")
    @AuditAction(auditAction = "Get short environments by project id {{#id.toString()}}")
    public List<Environment> getEnvironmentsShort(@PathVariable("projectId") UUID id) {
        return projectService.getShortEnvironments(id);
    }

    /**
     * Endpoint for getting list of temporary environments.
     */
    @GetMapping("/projects/{projectId}/temporary/environments")
    @AuditAction(auditAction = "Get temporary environments by project id {{#id.toString()}}")
    public List<Environment> getTemporaryEnvironments(@PathVariable("projectId") UUID id) {
        return projectService.getTemporaryEnvironments(id);
    }

    /**
     * Endpoint for getting list of temporary and not temporary environments.
     */
    @GetMapping("/projects/{projectId}/environments/all")
    @AuditAction(auditAction = "Get all environments (including temporary)  by project id {{#id.toString()}}")
    public List<Environment> getAllEnvironments(@PathVariable("projectId") UUID id) {
        return projectService.getAllEnvironments(id);
    }

    @GetMapping("/projects/{projectId}/tools")
    @AuditAction(auditAction = "Get TA Tools by project id {{#id.toString()}}")
    public List<Environment> getTools(@PathVariable("projectId") UUID id) {
        return projectService.getTools(id);
    }

    /**
     * Endpoint for getting list of TaToolGroups in abbreviated form ({"id":"","name":""}).
     */
    @JsonView({View.Name.class})
    @GetMapping("/projects/{projectId}/tools/short")
    @AuditAction(auditAction = "Get short TA Tools by project id {{#id.toString()}}")
    public List<Environment> getToolsShort(@PathVariable("projectId") UUID id) {
        return projectService.getShortTools(id);
    }

    /**
     * Endpoint for getting systems list.
     */
    @JsonView({View.FullVer2.class})
    @GetMapping("/projects/{projectId}/environments/systems")
    @AuditAction(auditAction = "Get project systems by project id {{#id.toString()}}"
            + " and categoryName {{#categoryName}}")
    public List<System> getProjectSystems(@PathVariable("projectId") UUID id,
                                          @RequestParam(value = "category", required = false) String categoryName) {
        if (StringUtils.isNotBlank(categoryName)) {
            return projectService.getSystemsByProjectIdAndCategoryName(id, categoryName);
        } else {
            return projectService.getSystemsByProjectId(id);
        }
    }

    /**
     * Endpoint for getting distinct list of system names by project.
     */
    @GetMapping("/projects/{projectId}/environments/systems/name")
    @AuditAction(auditAction = "Get system names by project id {{#id.toString()}}")
    public List<String> getSystemsName(@PathVariable("projectId") UUID id) {
        return projectService.getSystemNames(id);
    }

    /**
     * Endpoint for getting connection list.
     */
    @JsonView({View.FullVer2.class})
    @GetMapping("/projects/{projectId}/environments/connections")
    @AuditAction(auditAction = "Get connections by project id {{#id.toString()}}")
    public List<Connection> getConnections(@PathVariable("projectId") UUID id) {
        return projectService.getConnections(id);
    }

    /**
     * Endpoint for getting distinct list of connection names by project.
     */
    @GetMapping("/projects/{projectId}/environments/connections/name")
    @AuditAction(auditAction = "Get connection names by project id {{#id.toString()}}")
    public List<String> getConnectionsName(@PathVariable("projectId") UUID id) {
        return projectService.getConnectionNames(id);
    }

    @PreAuthorize("@entityAccess.checkAccess(#id.toString(),'CREATE')")
    @PostMapping("/projects/{projectId}/environments")
    @AuditAction(auditAction = "Create environment with name {{#environment.name}} by project id {{#id"
            + ".toString()}}")
    public Environment createEnvironment(@PathVariable("projectId") UUID id,
                                         @RequestBody EnvironmentDto environment) {
        return projectService.create(id, environment, Constants.Environment.Category.ENVIRONMENT);
    }

    @PreAuthorize("@entityAccess.checkAccess(#id.toString(),'CREATE')")
    @PostMapping("/projects/{projectId}/tools")
    @AuditAction(auditAction = "Create tool with name {{#environment.name}} by project id {{#id.toString()"
            + "}}}")
    public Environment createTool(@PathVariable("projectId") UUID id,
                                  @RequestBody EnvironmentDto environment) {
        return projectService.create(id, environment, Constants.Environment.Category.TOOL);
    }

    @PreAuthorize("@entityAccess.checkAccess(#project.getId().toString(),'CREATE')")
    @PostMapping("/projects")
    @AuditAction(auditAction = "Create Project with name {{#project.shortName}}")
    public Project createProject(@RequestBody ProjectImpl project) {
        return projectService.create(project);
    }

    @PreAuthorize("@entityAccess.checkAccess(#project.getId().toString(),'CREATE')")
    @PostMapping("/projects/{projectId}/copy")
    @AuditAction(auditAction = "Copy {{#project.shortName}} with id {{#id.toString()}}")
    public Project copy(@PathVariable("projectId") UUID id,
                        @RequestBody ProjectDto project) {
        return projectService.copy(id, project.getName(), project.getShortName(), project.getDescription());
    }

    /**
     * Endpoint for update project.
     */
    @PreAuthorize("@entityAccess.checkAccess(#project.getId().toString(),'UPDATE')")
    @PutMapping("/projects")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AuditAction(auditAction = "Update Project {{#project.id}}.")
    public void updateProject(@RequestBody ProjectDto project) {
        Preconditions.checkNotNull(project.getId(), "Project id can't be empty");
        Preconditions.checkNotNull(project.getName(), "Project name can't be empty");
        Preconditions.checkNotNull(project.getShortName(), "Project short name can't be empty");
        projectService.update(project.getId(), project.getName(), project.getShortName(),
                project.getDescription());
    }

    @PreAuthorize("@entityAccess.checkAccess(#id.toString(),'DELETE')")
    @DeleteMapping("/projects/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AuditAction(auditAction = "Delete project by id {{#id.toString()}}")
    public void deleteProject(@PathVariable("projectId") UUID id) {
        projectService.delete(id);
    }
}
