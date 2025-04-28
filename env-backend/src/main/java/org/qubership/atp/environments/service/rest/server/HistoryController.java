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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;

import org.qubership.atp.environments.errorhandling.history.EnvironmentIllegalHistoryEntityTypeArgumentException;
import org.qubership.atp.environments.service.rest.server.dto.generated.CompareEntityResponseDtoGenerated;
import org.qubership.atp.environments.service.rest.server.dto.generated.HistoryItemResponseDtoGenerated;
import org.qubership.atp.environments.service.rest.server.dto.generated.HistoryItemTypeDtoGenerated;
import org.qubership.atp.environments.service.rest.server.generated.HistoryControllerApi;
import org.qubership.atp.environments.versioning.model.entities.AbstractJaversEntity;
import org.qubership.atp.environments.versioning.model.entities.EnvironmentJ;
import org.qubership.atp.environments.versioning.model.entities.SystemJ;
import org.qubership.atp.environments.versioning.service.HistoryServiceFactory;
import org.qubership.atp.environments.versioning.service.JaversHistoryService;
import org.qubership.atp.environments.versioning.service.JaversRestoreServiceFactory;
import org.qubership.atp.environments.versioning.service.VersionHistoryService;
import org.qubership.atp.integration.configuration.configuration.AuditAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class HistoryController implements HistoryControllerApi {

    private final JaversHistoryService javersHistoryService;
    private final JaversRestoreServiceFactory javersRestoreServiceFactory;
    private HistoryServiceFactory historyServiceFactory;
    private final Map<String, Class<? extends AbstractJaversEntity>> javersClasses = ImmutableMap.of(
            HistoryItemTypeDtoGenerated.ENVIRONMENT.toString(), EnvironmentJ.class,
            HistoryItemTypeDtoGenerated.TATOOL.toString(), EnvironmentJ.class,
            HistoryItemTypeDtoGenerated.SYSTEM.toString(), SystemJ.class
    );

    /**
     * Constructor.
     */
    @Autowired
    public HistoryController(JaversHistoryService javersHistoryService,
                             JaversRestoreServiceFactory javersRestoreServiceFactory,
                             HistoryServiceFactory historyServiceFactory) {
        this.javersHistoryService = javersHistoryService;
        this.javersRestoreServiceFactory = javersRestoreServiceFactory;
        this.historyServiceFactory = historyServiceFactory;
    }

    @PreAuthorize("@entityAccess.checkAccess(#projectId,'READ')")
    @Override
    @AuditAction(auditAction = "Get all history with item type: {{#itemType}}")
    public ResponseEntity<HistoryItemResponseDtoGenerated> getAllHistory(
            @PathVariable("projectId") UUID projectId,
            @PathVariable("itemType") String itemType,
            @PathVariable("id") UUID entityId,
            @Valid @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
            @Valid @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit) {
        Class<? extends AbstractJaversEntity> javersClass = javersClasses.get(itemType);
        if (javersClass != null) {
            HistoryItemResponseDtoGenerated response = javersHistoryService.getAllHistory(
                    entityId, javersClass, offset, limit);
            response.getHistoryItems().forEach(item -> item.setType(HistoryItemTypeDtoGenerated.fromValue(itemType)));
            return ResponseEntity.ok(response);
        } else {
            log.error("History for Entity type '{}' is not supported", itemType);
            throw new EnvironmentIllegalHistoryEntityTypeArgumentException(itemType);
        }
    }

    @PreAuthorize("@entityAccess.checkAccess(#projectId,'READ')")
    @Override
    @AuditAction(auditAction = "Get entities by version projectId: {{#projectId.toString()}}"
            + " item type: {{#itemType}}")
    public ResponseEntity<List<CompareEntityResponseDtoGenerated>> getEntitiesByVersion(
            @PathVariable("projectId") UUID projectId,
            @PathVariable("itemType") String itemType,
            @PathVariable("id") UUID entityId,
            @PathVariable("revisionIds") List<String> revisionIds) {
        Optional<VersionHistoryService> historyServiceOptional = historyServiceFactory.getHistoryService(itemType);

        if (historyServiceOptional.isPresent()) {
            VersionHistoryService versionHistoryService = historyServiceOptional.get();
            List<CompareEntityResponseDtoGenerated> response =
                    versionHistoryService.getEntitiesByVersion(entityId, revisionIds);
            return ResponseEntity.ok(response);
        } else {
            log.error("History for Entity type '{}' is not supported", itemType);
            throw new EnvironmentIllegalHistoryEntityTypeArgumentException(itemType);
        }
    }

    @PreAuthorize("@entityAccess.checkAccess(#projectId,'UPDATE')")
    @Override
    @AuditAction(auditAction = "Restore to revision projectId: {{#projectId.toString()}}"
            + " item type: {{#itemType}}")
    public ResponseEntity<Void> restoreToRevision(@PathVariable("projectId") UUID projectId,
                                                  @PathVariable("itemType") String itemType,
                                                  @PathVariable("id") UUID entityId,
                                                  @PathVariable("revisionId") Integer revisionId) {
        Class<? extends AbstractJaversEntity> javersClass = javersClasses.get(itemType);
        if (javersClass != null) {
            javersRestoreServiceFactory.getRestoreService(javersClass)
                    .restore(javersClass, entityId, revisionId);
            return ResponseEntity.ok().build();
        } else {
            log.error("History for Entity type '{}' is not supported", itemType);
            throw new EnvironmentIllegalHistoryEntityTypeArgumentException(itemType);
        }
    }
}
