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

import org.qubership.atp.environments.enums.MdcField;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.utils.Constants;
import org.qubership.atp.environments.model.utils.View;
import org.qubership.atp.environments.repo.impl.ContextRepository;
import org.qubership.atp.environments.service.direct.ConcurrentModificationService;
import org.qubership.atp.environments.service.direct.EnvironmentService;
import org.qubership.atp.environments.service.direct.SystemService;
import org.qubership.atp.environments.service.rest.server.dto.EnvironmentDto;
import org.qubership.atp.environments.service.rest.server.request.ValidateTaToolsRequest;
import org.qubership.atp.environments.service.rest.server.response.ValidateTaToolsResponse;
import org.qubership.atp.integration.configuration.configuration.AuditAction;
import org.qubership.atp.integration.configuration.mdc.MdcUtils;
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

@RequestMapping("/api")
@RestController()
@SuppressWarnings("CPD-START")
public class ToolController /*implements ToolControllerApi*/ {

    private final EnvironmentService environmentService;
    private final SystemService systemService;
    private final ConcurrentModificationService concurrentModificationService;
    private final ContextRepository contextRepository;
    private final UUID categoryId = Constants.Environment.Category.TOOL;

    /**
     * Constructor.
     */
    @Autowired
    public ToolController(EnvironmentService service,
                          SystemService systemService,
                          ConcurrentModificationService concurrentModificationService,
                          ContextRepository contextRepository) {
        this.environmentService = service;
        this.systemService = systemService;
        this.concurrentModificationService = concurrentModificationService;
        this.contextRepository = contextRepository;
    }

    @GetMapping("/tools")
    @AuditAction(auditAction = "Get all TA Tools")
    public List<Environment> getAll() {
        return environmentService.getAll(categoryId);
    }

    @GetMapping("/tools/{environmentId}")
    @AuditAction(auditAction = "Get TA Tool by tool id {{#environmentId.toString()}}")
    public Environment getEnvironment(@PathVariable UUID environmentId) {
        return environmentService.get(environmentId);
    }

    /**
     * Method returns systems list.
     */
    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).SYSTEM.getName(),"
            + "@environmentService.getProjectIdByEnvironmentId(#environmentId),'READ')")
    @GetMapping("/tools/{environmentId}/systems")
    @JsonView({View.FullVer1.class})
    @AuditAction(auditAction = "Get tool systems by tool id {{#environmentId.toString()}}")
    public List<System> getToolSystems(@PathVariable("environmentId") UUID environmentId,
                                       @RequestParam(value = "system_type", required = false) String systemType) {
        MdcUtils.put(MdcField.ENVIRONMENT_ID.toString(), environmentId);
        if (systemType != null) {
            return environmentService.getSystems(environmentId, systemType);
        } else {
            return environmentService.getSystems(environmentId);
        }
    }

    /**
     * Getting a system with the ability to expand information on the environments and system categories.
     */
    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).SYSTEM.getName(),"
            + "@environmentService.getProjectIdByEnvironmentId(#environmentId),'READ')")
    @GetMapping("/v2/tools/{environmentId}/systems")
    @JsonView({View.FullVer2.class})
    @AuditAction(auditAction = "(V2 API) Get TA tool system by tool id {{#environmentId.toString()}}")
    public Collection<System> getSystemV2(@PathVariable("environmentId") UUID environmentId,
                                          @RequestParam(value = "system_type", required = false) String systemType) {
        MdcUtils.put(MdcField.ENVIRONMENT_ID.toString(), environmentId);
        contextRepository.getContext().setFieldsToUnfold("environments", "connections", "systemCategory");
        if (systemType != null) {
            return environmentService.getSystemsV2(environmentId, systemType);
        } else {
            return environmentService.getSystemsV2(environmentId);
        }
    }

    /**
     * Create tool.
     */
    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).TA_TOOL.getName(),"
            + "#environmentDto.getProjectId(),'CREATE')")
    @PostMapping("/tools")
    @AuditAction(auditAction = "Create tool with id {{#environmentId.toString()}}")
    public Environment createTool(@RequestBody EnvironmentDto environmentDto) {
        return environmentService.create(environmentDto.getProjectId(), environmentDto.getName(),
                environmentDto.getGraylogName(), environmentDto.getDescription(), environmentDto.getSsmSolutionAlias(),
                environmentDto.getSsmInstanceAlias(), environmentDto.getConsulEgressConfigPath(),
                categoryId, environmentDto.getTags());
    }

    /**
     * Copy tool.
     */
    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).TA_TOOL.getName(),"
            + "#environmentDto.getProjectId(),'CREATE')")
    @PostMapping("/tools/{environmentId}/copy")
    @AuditAction(auditAction = "Copy tool with id {{#environmentId.toString()}}")
    public Environment copyTool(@PathVariable("environmentId") UUID environmentId,
                                @RequestBody EnvironmentDto environmentDto) {
        return environmentService.copy(environmentId, environmentDto.getProjectId(), environmentDto.getName(),
                environmentDto.getGraylogName(), environmentDto.getDescription(), environmentDto.getSsmSolutionAlias(),
                environmentDto.getSsmInstanceAlias(), environmentDto.getConsulEgressConfigPath(),
                categoryId, environmentDto.getTags());
    }

    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).TA_TOOL.getName(),"
            + "@environmentService.getProjectIdByEnvironmentId(#environmentId),'UPDATE')")
    @GetMapping("/tools/{environmentId}/version")
    @AuditAction(auditAction = "Update all system versions in TA Tool by tool id {{#environmentId.toString()}}")
    public ResponseEntity<List<System>> updateVersion(@PathVariable("environmentId") UUID environmentId) {
        return ResponseEntity.status(HttpStatus.OK).body(systemService.updateVersionByEnvironmentId(environmentId));
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).TA_TOOL.getName(),"
            + "#environmentDto.getProjectId(),'UPDATE')")
    @PutMapping("/tools")
    @AuditAction(auditAction = "Update tool by tool id {{#environmentDto.id.toString()}}")
    public ResponseEntity updateTool(@RequestBody EnvironmentDto environmentDto) {
        Preconditions.checkNotNull(environmentDto.getId(), "Tool group id can't be empty");
        MdcUtils.put(MdcField.ENVIRONMENT_ID.toString(), environmentDto.getId());
        Preconditions.checkNotNull(environmentDto.getName(), "Tool group name can't be empty");
        HttpStatus status = concurrentModificationService.getConcurrentModificationHttpStatus(
                environmentDto.getId(), environmentDto.getModified(), environmentService);
        environmentService.update(environmentDto.getId(), environmentDto.getName(), environmentDto.getGraylogName(),
                environmentDto.getDescription(), environmentDto.getSsmSolutionAlias(),
                environmentDto.getSsmInstanceAlias(), environmentDto.getConsulEgressConfigPath(),
                environmentDto.getProjectId(),
                categoryId, environmentDto.getTags());
        return ResponseEntity.status(status).body("");
    }

    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).TA_TOOL.getName(),"
            + "#validateTaToolsRequest.getProjectId(),'READ')")
    @PostMapping("/tools/validate")
    public ResponseEntity<ValidateTaToolsResponse> validateTaTools(
            @RequestBody ValidateTaToolsRequest validateTaToolsRequest) {
        return ResponseEntity.ok(environmentService.validateTaTools(validateTaToolsRequest));
    }

    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).TA_TOOL.getName(),"
            + "@environmentService.getProjectIdByEnvironmentId(#environmentId),'DELETE')")
    @DeleteMapping("/tools/{environmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AuditAction(auditAction = "Delete tool by tool id {{#environmentId.toString()}}")
    public void deleteTool(@PathVariable("environmentId") UUID environmentId) {
        environmentService.delete(environmentId);
    }
}
