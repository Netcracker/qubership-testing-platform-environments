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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.qubership.atp.environments.enums.MdcField;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.ParametersGettingVersion;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.impl.SystemImpl;
import org.qubership.atp.environments.model.utils.View;
import org.qubership.atp.environments.model.utils.enums.Status;
import org.qubership.atp.environments.repo.impl.ContextRepository;
import org.qubership.atp.environments.service.direct.ConcurrentModificationService;
import org.qubership.atp.environments.service.direct.SystemService;
import org.qubership.atp.environments.service.rest.server.dto.CreateSystemDto;
import org.qubership.atp.environments.service.rest.server.dto.SharingRequestDto;
import org.qubership.atp.environments.service.rest.server.dto.SystemDto;
import org.qubership.atp.environments.service.rest.server.request.SynchronizeCloudServicesRequest;
import org.qubership.atp.environments.service.rest.server.response.ShortExternalService;
import org.qubership.atp.environments.utils.cloud.KubeClient;
import org.qubership.atp.environments.utils.cloud.OpenshiftClient;
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
import io.swagger.v3.oas.annotations.Operation;

@RequestMapping("/api")
@RestController()
@SuppressWarnings("CPD-START")
public class SystemController /*implements SystemControllerApi*/ {

    private final SystemService systemService;
    private final ConcurrentModificationService concurrentModificationService;
    private final ContextRepository contextRepository;

    /**
     * Constructor.
     */
    @Autowired
    public SystemController(SystemService systemService,
                            ConcurrentModificationService concurrentModificationService,
                            ContextRepository contextRepository) {
        this.systemService = systemService;
        this.concurrentModificationService = concurrentModificationService;
        this.contextRepository = contextRepository;
    }

    @GetMapping("/systems")
    @JsonView({View.FullVer1.class})
    @AuditAction(auditAction = "Get all systems")
    public List<System> getAll() {
        return systemService.getAll();
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @GetMapping("/public/v1/systems/{systemId}/version")
    @JsonView({View.FullVer1.class})
    @AuditAction(auditAction = "(Public V1 API) Check system version by system id {{#id.toString()}}")
    public ResponseEntity getCachedVersion(@PathVariable("systemId") UUID id) {
        return Optional.ofNullable(systemService.getCachedVersionById(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @GetMapping("/systems/{systemId}")
    @JsonView({View.FullVer1.class})
    @AuditAction(auditAction = "Get system with id {{#id.toString()}}")
    public ResponseEntity<System> getSystem(@PathVariable("systemId") UUID id) {
        return Optional.ofNullable(systemService.get(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @GetMapping("/systems/short/{systemId}")
    @JsonView({View.FullVer1.class})
    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).SYSTEM.getName(),"
            + "@environmentService.getProjectIdBySystemId(#id),'READ')")
    public ResponseEntity<System> getShortSystem(@PathVariable("systemId") UUID id) {
        return Optional.ofNullable(systemService.getShortSystem(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @GetMapping("/systems/byName/{environmentId}/{name}")
    @JsonView({View.FullVer1.class})
    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).SYSTEM.getName(),"
            + "@environmentService.getProjectIdByEnvironmentId(#environmentId),'READ')")
    public ResponseEntity<System> getSystemByName(@PathVariable("environmentId") UUID environmentId,
                                                  @PathVariable("name") String name) {
        return Optional.ofNullable(systemService.getSystemByNameAndEnvironmentId(name, environmentId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Getting a system with the ability to expand information on the environments and system categories.
     */
    @GetMapping("/v2/systems/{systemId}")
    @JsonView({View.FullVer2.class})
    @AuditAction(auditAction = "(V2) Get system by system id {{#id.toString()}}")
    public ResponseEntity<System> getSystemV2(@PathVariable("systemId") UUID id) {
        contextRepository.getContext().setFieldsToUnfold("environments", "connections", "systemCategory");
        return Optional.ofNullable(systemService.getV2(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/systems/{systemId}/connections")
    @JsonView({View.FullVer1.class})
    @AuditAction(auditAction = "Get system connections by system id {{#id.toString()}}")
    public List<Connection> getSystemConnections(@PathVariable("systemId") UUID uuid) {
        return systemService.getConnections(uuid);
    }

    /**
     * Endpoint for getting list of systems on all projects in abbreviated form
     * ({"id":"","name":"","environmentIds":[]}).
     */
    @Operation(description = "Endpoint for getting list of systems on all projects ")
    @GetMapping("/systems/short")
    @JsonView({View.Environments.class})
    @AuditAction(auditAction = "Get all short systems with environments id")
    public List<System> getAllSystems() {
        return systemService.getAll();
    }

    /**
     * Creating new system.
     */
    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).SYSTEM.getName(),"
            + "@environmentService.getProjectIdByEnvironmentId(#system.getEnvironmentId()),'CREATE')")
    @PostMapping("/systems")
    @JsonView({View.FullVer1.class})
    @AuditAction(auditAction = "Create system with name {{#system.name}} in environment id  {{#system"
            + ".environmentId.toString()}}")
    public System createSystem(@RequestBody CreateSystemDto system) {
        Preconditions.checkNotNull(system.getName(), "System name can't be null");
        Preconditions.checkArgument(!system.getName().isEmpty(), "System name can't be "
                + "empty");
        Preconditions.checkNotNull(system.getEnvironmentId(), "Environment ID can't be empty");
        MdcUtils.put(MdcField.ENVIRONMENT_ID.name(), system.getEnvironmentId());
        return systemService.create(system.getEnvironmentId(), system.getName(), system.getDescription(),
                system.getSystemCategoryId(), system.getParametersGettingVersion(),
                system.getParentSystemId(),
                system.getServerItf(), system.getMergeByName(), system.getLinkToSystemId(),
                system.getExternalId(),
                system.getExternalName());
    }

    /**
     * Copying the system.
     */
    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).SYSTEM.getName(),"
            + "@environmentService.getProjectIdByEnvironmentId(#system.getEnvironmentId()),'CREATE')")
    @PostMapping("/systems/{systemId}/copy")
    @JsonView({View.FullVer1.class})
    @AuditAction(auditAction = "Copy system with id {{#id.toString()}}")
    public System copy(@PathVariable("systemId") UUID id, @RequestBody CreateSystemDto system) {
        Preconditions.checkNotNull(system.getName(), "System name can't be null");
        Preconditions.checkArgument(!system.getName().isEmpty(), "System name can't be "
                + "empty");
        Preconditions.checkNotNull(system.getEnvironmentId(), "Environment ID can't be empty");
        MdcUtils.put(MdcField.ENVIRONMENT_ID.name(), system.getEnvironmentId());
        return systemService.copy(id, system.getEnvironmentId(), system.getName(), system.getDescription(),
                system.getSystemCategoryId(), system.getParametersGettingVersion(),
                system.getParentSystemId(),
                system.getServerItf(), system.getMergeByName(), system.getLinkToSystemId(),
                system.getExternalId(),
                system.getExternalName());
    }

    /**
     * Sharing the system.
     */
    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).SYSTEM.getName(),"
            + "@environmentService.getProjectIdBySystemId(#id),'UPDATE')")
    @PutMapping("/systems/{systemId}/share")
    @JsonView({View.FullVer2.class})
    @AuditAction(auditAction = "Share system with id {{#id.toString()}}")
    public ResponseEntity<System> share(@PathVariable("systemId") UUID id,
                                        @RequestBody SharingRequestDto sharingRequestDto,
                                        @RequestParam(value = "modified", required = false) Long modified) {
        Preconditions.checkNotNull(sharingRequestDto, "Environment IDs can't be empty");
        contextRepository.getContext().setFieldsToUnfold("environments", "systemCategory");
        HttpStatus status = concurrentModificationService.getConcurrentModificationHttpStatus(
                id, modified, systemService);
        System system = systemService.shareProcessing(id, sharingRequestDto);
        return ResponseEntity.status(status).body(system);
    }

    /**
     * Updating the openshift routes.
     */
    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).SYSTEM.getName(),"
            + "@environmentService.getProjectIdByEnvironmentId(#system.getEnvironmentId()),'UPDATE')")
    @PutMapping("/systems/openshift")
    @JsonView({View.FullVer1.class})
    @AuditAction(auditAction = "Update openshift routes {{#system.name}}")
    public List<Connection> openshiftUpdateRoutes(@RequestBody CreateSystemDto system) {
        Preconditions.checkNotNull(system.getEnvironmentId(), "Environment ID can't be empty");
        MdcUtils.put(MdcField.ENVIRONMENT_ID.name(), system.getEnvironmentId());
        if (system.getId() != null) {
            MdcUtils.put(MdcField.SYSTEM_ID.name(), system.getId());
            return systemService.updateOpenshiftRoute(system.getId(), system.getEnvironmentId());
        } else {
            return systemService.updateOpenshiftRoute(system.getEnvironmentId());
        }
    }

    /**
     * Updating the system.
     */
    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).SYSTEM.getName(),"
            + "@environmentService.getProjectIdBySystemId(#systemDto.getId()),'UPDATE')")
    @PutMapping("/systems")
    @JsonView({View.FullVer1.class})
    @AuditAction(auditAction = "Update system {{#systemDto.getName()}} with system id: {{#systemDto.id"
            + ".toString()}}")
    public ResponseEntity<System> updateSystem(@RequestBody SystemDto systemDto) {
        Preconditions.checkNotNull(systemDto.getId(), "System id can't be empty");
        Preconditions.checkNotNull(systemDto.getName(), "System name can't be null");
        Preconditions.checkArgument(!systemDto.getName().isEmpty(), "System name can't be "
                + "empty");
        contextRepository.getContext().setFieldsToUnfold("connections");
        HttpStatus status = concurrentModificationService.getConcurrentModificationHttpStatus(
                systemDto.getId(), systemDto.getModified(), systemService);
        System updatedSystem = systemService.update(systemDto);
        return ResponseEntity.status(status).body(updatedSystem);
    }

    /**
     * Deleting the system.
     */
    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).SYSTEM.getName(),"
            + "@environmentService.getProjectIdByEnvironmentId(#environmentId),'DELETE')")
    @DeleteMapping("/systems/{systemId}")
    @JsonView({View.FullVer1.class})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AuditAction(auditAction = "Delete system with id  {{#id.toString()}}")
    public void deleteSystem(@PathVariable("systemId") UUID id,
                             @RequestParam("environmentId") UUID environmentId) {
        Preconditions.checkNotNull(id, "system ID can't be empty");
        Preconditions.checkNotNull(environmentId, "Environment ID can't be empty");
        systemService.delete(id, environmentId);
    }

    /**
     * Deleting the system.
     */
    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).SYSTEM.getName(),"
            + "@environmentService.getProjectIdByEnvironmentId(#environmentId),'DELETE')")
    @DeleteMapping("/systems/link/{systemId}/{environmentId}")
    @JsonView({View.FullVer1.class})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AuditAction(auditAction = "Delete linked systems with parent system id {{#systemId.toString()}}")
    public void deleteLinkedSystems(@PathVariable("systemId") UUID systemId,
                                    @PathVariable("environmentId") UUID environmentId) {
        Preconditions.checkNotNull(systemId, "system ID can't be empty");
        Preconditions.checkNotNull(environmentId, "Environment ID can't be empty");
        systemService.deleteLinkedServices(systemId, environmentId);
    }

    public System saveStatusAndDateOfLastCheck(UUID id, Status status) {
        return systemService.saveStatusAndDateOfLastCheck(id, status);
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).SYSTEM.getName(),"
            + "@environmentService.getProjectIdBySystemId(#id),"
            + "'UPDATE')")
    @GetMapping("/systems/{systemId}/version")
    @JsonView({View.FullVer1.class})
    @AuditAction(auditAction = "Check system version by system id {{#systemId.toString()}}")
    public ResponseEntity<System> updateVersion(@PathVariable("systemId") UUID id) {
        return Optional.ofNullable(systemService.updateVersionBySystemId(id, true))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get version with HTML-marking.
     */
    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).SYSTEM.getName(),"
            + "@environmentService.getProjectIdBySystemId(#id),"
            + "'UPDATE')")
    @RequestMapping(value = "/systems/{systemId}/htmlVersion", method = GET, produces = "text/plain")
    @JsonView({View.FullVer1.class})
    @AuditAction(auditAction = "Check system version and convert to html by system id {{#systemId.toString"
            + "()}}")
    public ResponseEntity<String> getHtmlVersion(@PathVariable("systemId") UUID id) {
        return Optional.ofNullable(systemService
                .transformSystemVersionToHtml(systemService.updateVersionBySystemId(id, true))
                .getVersion())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get version with HTML-marking without authorization.
     */
    @RequestMapping(value = "/public/v1/systems/{systemId}/htmlVersion", method = GET, produces = "text"
            + "/plain")
    @JsonView({View.FullVer1.class})
    @AuditAction(auditAction = "(Public V1 API) Check system version and convert to html"
            + " by system id {{#systemId.toString()}}")
    public ResponseEntity<String> getPublicHtmlVersion(@PathVariable("systemId") UUID systemId) {
        return Optional.ofNullable(systemService
                .transformSystemVersionToHtml((SystemImpl) systemService.getCachedVersionById(systemId))
                .getVersion())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).SYSTEM.getName(),"
            + "@environmentService.getProjectIdBySystemId(#systemId),"
            + "'UPDATE')")
    @PutMapping("/systems/{systemId}/parametersGettingVersion")
    @JsonView({View.FullVer1.class})
    @AuditAction(auditAction = "Update parameters getting version for system Id {{#systemId.toString()}}")
    public System updateParametersGettingVersion(@PathVariable("systemId") UUID systemId,
                                                 @RequestBody ParametersGettingVersion parametersGettingVersion) {
        return systemService.updateParametersGettingVersion(systemId, parametersGettingVersion);
    }

    /**
     * synchronizing managed services for Kubernetes.
     */
    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).SYSTEM.getName(),"
            + "@environmentService.getProjectIdByEnvironmentId(#environmentId),'CREATE')")
    @PostMapping("/systems/kubeServices/{environmentId}/{systemId}")
    @JsonView({View.FullVer1.class})
    @AuditAction(auditAction = "Synchronize services from kubernetes by system id {{#systemId.toString()}}")
    public List<System> synchronizeServicesFromKubernetes(@PathVariable("systemId") UUID systemId,
                                                          @PathVariable("environmentId") UUID environmentId,
                                                          @RequestBody SynchronizeCloudServicesRequest request) {
        systemService.deleteSystemsByIds(request.getRemovedServicesSystemIds(), environmentId);
        return systemService.createListFromCloudServer(request.getNewServicesExternalIds(),
                systemId, environmentId, KubeClient.class);
    }

    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).SYSTEM.getName(),"
            + "@environmentService.getProjectIdBySystemId(#systemId),"
            + "'UPDATE')")
    @GetMapping("/systems/kubeServices/{systemId}")
    @JsonView({View.FullVer2.class})
    @AuditAction(auditAction = "Update services from kubernetes by system id {{#systemId.toString()}}")
    public List<System> updateServicesFromKubernetes(@PathVariable("systemId") UUID systemId) {
        return systemService.updateServicesFromCloudServer(systemId, KubeClient.class);
    }

    /**
     * synchronizing managed services for OpenShift.
     */
    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).SYSTEM.getName(),"
            + "@environmentService.getProjectIdByEnvironmentId(#environmentId),'CREATE')")
    @PostMapping("/systems/openshiftServices/{environmentId}/{systemId}")
    @JsonView({View.FullVer1.class})
    @AuditAction(auditAction = "Synchronize services from openshift by system id {{#systemId.toString()}}"
            + " and {{#environmentId.toString()}}")
    public List<System> synchronizeServicesFromOpenShift(@PathVariable("systemId") UUID systemId,
                                                         @PathVariable("environmentId") UUID environmentId,
                                                         @RequestBody SynchronizeCloudServicesRequest request) {
        systemService.deleteSystemsByIds(request.getRemovedServicesSystemIds(), environmentId);
        return systemService.createListFromCloudServer(request.getNewServicesExternalIds(),
                systemId, environmentId, OpenshiftClient.class);
    }

    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).SYSTEM.getName(),"
            + "@environmentService.getProjectIdBySystemId(#systemId),"
            + "'UPDATE')")
    @GetMapping("/systems/openshiftServices/{systemId}")
    @JsonView({View.FullVer2.class})
    @AuditAction(auditAction = "Update services from openshift by system id {{#systemId.toString()}}")
    public List<System> updateServicesFromOpenshift(@PathVariable("systemId") UUID systemId) {
        return systemService.updateServicesFromCloudServer(systemId, OpenshiftClient.class);
    }

    @GetMapping("/systems/shortKubeServices/{systemId}")
    @AuditAction(auditAction = "Get kubernetes service names by system id {{#systemId.toString()}}")
    public List<ShortExternalService> getKubernetesServiceNames(@PathVariable("systemId") UUID systemId) {
        return systemService.getShortExternalServices(systemId, KubeClient.class);
    }

    @GetMapping("/systems/shortOpenshiftServices/{systemId}")
    @AuditAction(auditAction = "Get openshift service names by system id {{#systemId.toString()}}")
    public List<ShortExternalService> getOpenshiftServiceNames(@PathVariable("systemId") UUID systemId) {
        return systemService.getShortExternalServices(systemId, OpenshiftClient.class);
    }

    @GetMapping("/systems/link/{systemId}")
    @JsonView({View.FullVer1.class})
    @AuditAction(auditAction = "Get linked cloud services  by system id {{#systemId.toString()}}")
    public List<System> getLinkedCloudServices(@PathVariable("systemId") UUID systemId) {
        return systemService.getLinkedSystemByParentId(systemId);
    }
}
