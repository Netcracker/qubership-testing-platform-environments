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

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.qubership.atp.environments.enums.MdcField;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.utils.Constants;
import org.qubership.atp.environments.model.utils.View;
import org.qubership.atp.environments.repo.impl.ContextRepository;
import org.qubership.atp.environments.service.direct.ConcurrentModificationService;
import org.qubership.atp.environments.service.direct.EnvironmentService;
import org.qubership.atp.environments.service.direct.SystemService;
import org.qubership.atp.environments.service.rest.server.dto.BaseSearchRequestDto;
import org.qubership.atp.environments.service.rest.server.dto.CreateSystemDto;
import org.qubership.atp.environments.service.rest.server.dto.EnvironmentDto;
import org.qubership.atp.environments.service.rest.server.dto.SystemTemporaryDto;
import org.qubership.atp.environments.service.rest.server.request.EnvironmentsWithFilterRequest;
import org.qubership.atp.environments.service.rest.server.response.SystemVersionResponse;
import org.qubership.atp.integration.configuration.configuration.AuditAction;
import org.qubership.atp.integration.configuration.mdc.MdcUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
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
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/api")
@RestController()
@Slf4j
@SuppressWarnings("CPD-START")
public class EnvironmentController /*implements EnvironmentControllerApi*/ {

    private final EnvironmentService environmentService;
    private final SystemService systemService;
    private final ConcurrentModificationService concurrentModificationService;
    private final ContextRepository contextRepository;
    private final UUID categoryId = Constants.Environment.Category.ENVIRONMENT;

    /**
     * Constructor.
     */
    @Autowired
    public EnvironmentController(EnvironmentService service,
                                 SystemService systemService,
                                 ConcurrentModificationService concurrentModificationService,
                                 ContextRepository contextRepository) {
        this.environmentService = service;
        this.systemService = systemService;
        this.concurrentModificationService = concurrentModificationService;
        this.contextRepository = contextRepository;
    }

    @GetMapping("/environments")
    @JsonView({View.FullVer1.class})
    @AuditAction(auditAction = "Get all environment")
    public List<Environment> getAll() {
        return environmentService.getAll(categoryId);
    }

    @PostMapping("/environments/search")
    @JsonView({View.FullVer1.class})
    @AuditAction(auditAction = "Get environments by search request projectId in search request: "
            + "{{#searchRequest"
            + ".projectId.toString()}} ")
    public List<Environment> findBySearchRequest(@RequestBody BaseSearchRequestDto searchRequest) throws Exception {
        return environmentService.findBySearchRequest(searchRequest);
    }

    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).ENVIRONMENT.getName(),"
            + "@environmentService.getProjectIdByEnvironmentId(#environmentId),'READ')")
    @GetMapping("/environments/{environmentId}")
    @JsonView({View.FullVer1.class})
    @AuditAction(auditAction = "Get environment by uuid {{#environmentId.toString()}} ")
    public Environment getEnvironment(@PathVariable("environmentId") UUID environmentId) {
        return environmentService.get(environmentId);
    }

    /**
     * Method returns environment name.
     */
    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).ENVIRONMENT.getName(),"
            + "@environmentService.getProjectIdByEnvironmentId(#environmentId),'READ')")
    @GetMapping("/environments/{environmentId}/name")
    @JsonView({View.FullVer1.class})
    @AuditAction(auditAction = "Get environment name by uuid {{#environmentId.toString()}} ")
    public ResponseEntity<String> getEnvironmentNameById(@PathVariable("environmentId") UUID environmentId) {
        return Optional.ofNullable(environmentService.getEnvironmentNameById(environmentId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).ENVIRONMENT.getName(),"
            + "@environmentService.getProjectIdByEnvironmentId(#environmentId),'UPDATE')")
    @GetMapping("/environments/{environmentId}/systems/update-versions")
    @JsonView({View.FullVer1.class})
    @Operation(description = "Returns all systems with updated versions under environment {{#environmentId"
            + ".toString()}}")
    @AuditAction(auditAction = "Get environment tsg by uuid {{#environmentId.toString()}} ")
    public Environment getEnvironmentTsg3(@Parameter(description = "Id of the environment. Cannot be empty.")
                                          @PathVariable("environmentId") UUID environmentId) {
        systemService.updateVersionByEnvironmentId(environmentId);
        return environmentService.get(environmentId);
    }

    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).ENVIRONMENT.getName(),"
            + "#request.getProjectId(),'READ')")
    @PostMapping("/environments/filter")
    @JsonView({View.FullVer1.class})
    @Operation(description = "Returns all environments with setted specified fields by specified filter")
    public ResponseEntity<List<Environment>> getEnvironmentsByRequest(
            @RequestBody EnvironmentsWithFilterRequest request,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size
    ) {
        return ResponseEntity.ok(environmentService.getEnvironmentsByFilterRequest(request, page, size));
    }


    /**
     * Method returns systems list.
     */
    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).SYSTEM.getName(),"
            + "@environmentService.getProjectIdByEnvironmentId(#environmentId),'READ')")
    @GetMapping("/environments/{environmentId}/systems")
    @JsonView({View.FullVer1.class})
    @AuditAction(auditAction = "Get environment systems by environment uuid {{#environmentId.toString()}} "
            + "and system type {{#systemType}}")
    public List<System> getEnvironmentSystems(@PathVariable("environmentId") UUID environmentId,
                                              @RequestParam(value = "system_type", required = false)
                                                      String systemType) {
        if (systemType != null) {
            return environmentService.getSystems(environmentId, systemType);
        } else {
            return environmentService.getSystems(environmentId);
        }
    }

    /**
     * Method returns systems list.
     */
    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).CONNECTION.getName(),"
            + "@environmentService.getProjectIdByEnvironmentId(#environmentId),'READ')")
    @GetMapping("/environments/{environmentId}/connections")
    @JsonView({View.FullVer1.class})
    public ResponseEntity<List<Connection>> getEnvironmentConnections(
            @PathVariable("environmentId") UUID environmentId
    ) {
        return Optional.ofNullable(environmentService.getConnections(environmentId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Method returns html-tables with system versions.
     */
    @RequestMapping(value = "/public/v1/environments/{environmentIds}/systems/htmlVersions",
            method = GET, produces = "text/plain")
    @AuditAction(auditAction = "Get html of system versions by environment list")
    public ResponseEntity<String> getPublicHtmlVersion(@PathVariable("environmentIds") List<UUID> environmentIds) {
        String htmlResponse = environmentService.getHtmlVersionByEnvironments(environmentIds);
        return StringUtils.isEmpty(htmlResponse) ? ResponseEntity.notFound().build() :
                ResponseEntity.ok(htmlResponse);
    }

    /**
     * Method returns list of systems in abbreviated form ({"id":"","name":""}).
     */
    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).SYSTEM.getName(),"
            + "@environmentService.getProjectIdByEnvironmentId(#environmentId),'READ')")
    @JsonView({View.Name.class})
    @GetMapping("/environments/{environmentId}/systems/short")
    @AuditAction(auditAction = "Get short systems by environment uuid {{#environmentId.toString()}}")
    public List<System> getSystemsShort(@PathVariable("environmentId") UUID environmentId) {
        return environmentService.getShortSystems(environmentId);
    }

    /**
     * Getting a system with the ability to expand information on the environments and system categories.
     */
    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).SYSTEM.getName(),"
            + "@environmentService.getProjectIdByEnvironmentId(#environmentId),'READ')")
    @JsonView({View.FullVer2.class})
    @GetMapping("/v2/environments/{environmentId}/systems")
    @AuditAction(auditAction = "Get system version2 by environment uuid {{#environmentId.toString()}} and "
            + "system type"
            + " {{#systemType}}")
    public Collection<System> getSystemV2(@PathVariable("environmentId") UUID environmentId,
                                          @RequestParam(value = "system_type", required = false) String systemType) {
        log.info("Request to get systems for environment with id '{}' and type '{}'", environmentId,
                systemType);
        contextRepository.getContext().setFieldsToUnfold("environments", "connections", "systemCategory");
        if (systemType != null) {
            return environmentService.getSystemsV2(environmentId, systemType);
        } else {
            return environmentService.getSystemsV2(environmentId);
        }
    }


     /**
     * Method returns systems data as a ZIP archive containing two YAML files:
     * - deployment-parameters.yaml: all connection parameters except encrypted credentials
     * - credentials.yaml: only encrypted parameters (starting with {ENC}) and password/token fields
     */
    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).SYSTEM.getName(),"
            + "@environmentService.getProjectIdByEnvironmentId(#environmentId),'READ')")
    @GetMapping("/v2/environments/{environmentId}/yaml/envgene")
    @AuditAction(auditAction = "Get systems stored in zip archive with two YAML files: "
                + "deployment-parameters.yaml and credentials.yaml in envgene format by environment uuid {{#environmentId.toString()}} "
                + "and system type {{#systemType}}")
    public ResponseEntity<Resource> getSystemsYamlZipArchive(
            @PathVariable("environmentId") UUID environmentId,
            @RequestParam(value = "system_type", required = false) String systemType) {
        log.info("Request to get systems YAML ZIP archive for environment '{}' with system type '{}'", 
                environmentId, systemType);
        contextRepository.getContext().setFieldsToUnfold("environments", "connections", "systemCategory");
        
        byte[] zipBytes = environmentService.getSystemsYamlZipArchive(environmentId, systemType);
        
        // Get environment name for filename
        String environmentName = environmentService.getEnvironmentNameById(environmentId);
        String filename = (environmentName != null ? environmentName : environmentId.toString())
                + "-envgene-configuration.zip";
        
        ByteArrayResource resource = new ByteArrayResource(zipBytes);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(zipBytes.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    /**
     * Create environment.
     */
    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).ENVIRONMENT.getName(),"
            + "#environmentDto.getProjectId(),'CREATE')")
    @PostMapping("/environments")
    @AuditAction(auditAction = "Create environment with name: {{#environmentDto.name}}")
    public Environment createEnvironment(@RequestBody EnvironmentDto environmentDto) {
        return environmentService.create(environmentDto.getProjectId(), environmentDto.getName(),
                environmentDto.getGraylogName(), environmentDto.getDescription(), environmentDto.getSsmSolutionAlias(),
                environmentDto.getSsmInstanceAlias(), environmentDto.getConsulEgressConfigPath(),
                categoryId, environmentDto.getTags());
    }

    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).SYSTEM.getName(),"
            + "@environmentService.getProjectIdByEnvironmentId(#environmentId), 'CREATE')")
    @PostMapping("/environments/{environmentId}")
    @AuditAction(auditAction = "Create system with name {{#createSystemDto.name}} in environment uuid  "
            + "{{#environmentId.toString()}}")
    public System createSystem(@PathVariable("environmentId") UUID environmentId,
                               @RequestBody CreateSystemDto createSystemDto) {
        return environmentService.create(environmentId, createSystemDto);
    }

    /**
     * Copy environment.
     */
    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).ENVIRONMENT.getName(),"
            + "#environmentDto.getProjectId(),'CREATE')")
    @PostMapping("/environments/{environmentId}/copy")
    @AuditAction(auditAction = "Copy environment with environment uuid {{#environmentId.toString()}}")
    public Environment copy(@PathVariable("environmentId") UUID environmentId,
                            @RequestBody EnvironmentDto environmentDto) {
        return environmentService.copy(environmentId, environmentDto.getProjectId(), environmentDto.getName(),
                environmentDto.getGraylogName(), environmentDto.getDescription(), environmentDto.getSsmSolutionAlias(),
                environmentDto.getSsmInstanceAlias(), environmentDto.getConsulEgressConfigPath(),
                categoryId, environmentDto.getTags());
    }

    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).ENVIRONMENT.getName(),"
            + "@environmentService.getProjectIdByEnvironmentId(#environmentId),'CREATE')")
    @JsonView({View.FullVer1.class})
    @PostMapping("/environments/{environmentId}/temporary")
    @AuditAction(auditAction = "Get temporary environment by uuid  {{#environmentId.toString()}}")
    public Environment temporary(@PathVariable("environmentId") UUID environmentId,
                                 @RequestBody List<SystemTemporaryDto> systemUpdateList) {
        contextRepository.getContext().setFieldsToUnfold("systems", "connections");
        return environmentService.temporary(environmentId, systemUpdateList);
    }

    @JsonView({View.FullVer1.class})
    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).SYSTEM.getName(),"
            + "@environmentService.getProjectIdByEnvironmentId(#environmentId),'UPDATE')")
    @GetMapping("/environments/{environmentId}/version")
    @AuditAction(auditAction = "Update system versions in environment by environment uuid  {{#environmentId"
            + ".toString()}}")
    public ResponseEntity<List<System>> updateVersion(@PathVariable("environmentId") UUID environmentId) {
        return ResponseEntity.status(HttpStatus.OK).body(systemService.updateVersionByEnvironmentId(environmentId));
    }

    /**
     * Getting a detailed system version check response with text of check error.
     */
    @JsonView({View.FullVer1.class})
    @PreAuthorize(
            "@entityAccess.checkAccess(@environmentService"
                    + ".getProjectIdByEnvironmentId(#environmentId).toString(),'UPDATE')")
    @GetMapping("/v2/environments/{environmentId}/version")
    @AuditAction(auditAction = "Detailed update all system version in environment"
            + " by environment uuid  {{#environmentId.toString()}}")
    public ResponseEntity<List<SystemVersionResponse>> detailedUpdateVersion(
            @PathVariable("environmentId") UUID environmentId) {
        log.info("Start updating environment version");
        return ResponseEntity.status(HttpStatus.OK).body(systemService
                .updateVersionByEnvironmentId(environmentId)
                .stream()
                .map(system -> new SystemVersionResponse(system, system.getCheckVersionError()))
                .collect(Collectors.toList()));
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).ENVIRONMENT.getName(),"
            + "#environmentDto.getProjectId(),'UPDATE')")
    @PutMapping("/environments")
    @AuditAction(auditAction = "Update environment with id {{#environmentDto.id.toString()}}")
    public ResponseEntity updateEnvironment(@RequestBody EnvironmentDto environmentDto) {
        Preconditions.checkNotNull(environmentDto.getId(), "Environment id can't be empty");
        Preconditions.checkNotNull(environmentDto.getName(), "Environment name can't be empty");
        MdcUtils.put(MdcField.ENVIRONMENT_ID.toString(), environmentDto.getId());
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
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).ENVIRONMENT.getName(),"
            + "@environmentService.getProjectIdByEnvironmentId(#environmentId),'DELETE')")
    @DeleteMapping("/environments/{environmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AuditAction(auditAction = "Delete environment {{#environmentId.toString()}}")
    public void deleteEnvironment(@PathVariable("environmentId") UUID environmentId) {
        environmentService.delete(environmentId);
    }

    /**
     * Returns the total number of environments that match the provided filter criteria.
     */
    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).ENVIRONMENT.getName(),"
            + "#request.getProjectId(),'READ')")
    @PostMapping("/environments/filter/count")
    @Operation(description = "Returns the total count of environments matching the provided filter criteria")
    public ResponseEntity<Map<String, Long>> getEnvironmentsCountByRequest(
            @RequestBody EnvironmentsWithFilterRequest request) {
        long count = environmentService.getEnvironmentsCountByFilter(request);
        return ResponseEntity.ok(Collections.singletonMap("count", count));
    }
}
