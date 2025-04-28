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
import java.util.UUID;

import org.qubership.atp.environments.model.AlertEvent;
import org.qubership.atp.environments.model.impl.AlertEventImpl;
import org.qubership.atp.environments.service.direct.AlertEventService;
import org.qubership.atp.integration.configuration.configuration.AuditAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Preconditions;

@RequestMapping("/api/alertEvents")
@RestController()
public class AlertEventController /*implements AlertControllerApi*/ {

    private final AlertEventService alertEventService;

    @Autowired
    public AlertEventController(AlertEventService service) {
        this.alertEventService = service;
    }

    @GetMapping("/alert/{alertId}/entity/{entityId}")
    @AuditAction(auditAction = "Get alert event {{#alertId.toString()}} entityId {{#entityId.toString()}}")
    public AlertEvent getAlertEvent(@PathVariable("alertId") UUID alertId, @PathVariable("entityId") UUID
    entityId) {
        return alertEventService.get(alertId, entityId);
    }

    @GetMapping
    @AuditAction(auditAction = "Get all alert events")
    public List<AlertEvent> getAll() {
        return alertEventService.getAll();
    }

    @PostMapping // to separate create and update
    @AuditAction(auditAction = "Create alert event {{#alertEvent.alertId}}")
    public AlertEvent create(@RequestBody AlertEventImpl alertEvent) {
        return alertEventService.create(alertEvent.getAlertId(), alertEvent.getEntityId(),
                alertEvent.getTagList(), alertEvent.getStatus());
    }

    /**
     * Update information alert event.
     *
     * @param alertEvent TODO
     */
    @PutMapping("/alert/{alertId}/entity/{entityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AuditAction(auditAction = "Update alert event {{#alert.alertId.toString()}}")
    public void update(@RequestBody AlertEventImpl alertEvent) {
        Preconditions.checkNotNull(alertEvent.getEntityId(), "Entity id can't be empty");
        Preconditions.checkNotNull(alertEvent.getAlertId(), "Alert id can't be empty");
        alertEventService.update(alertEvent.getAlertId(), alertEvent.getEntityId(),
                alertEvent.getTagList(), alertEvent.getStatus());
    }

    @DeleteMapping("/alert/{alertId}/entity/{entityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AuditAction(auditAction = "Delete alert event {{#alertId.toString()}}")
    public void delete(@PathVariable("alertId") UUID alertId, @PathVariable("entityId") UUID entityId) {
        alertEventService.delete(alertId, entityId);
    }
}
