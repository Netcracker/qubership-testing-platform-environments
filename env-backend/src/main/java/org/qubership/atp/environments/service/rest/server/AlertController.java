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

import org.qubership.atp.environments.model.Alert;
import org.qubership.atp.environments.model.impl.AlertImpl;
import org.qubership.atp.environments.service.direct.AlertService;
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

@RequestMapping("/api/alerts")
@RestController()
public class AlertController /*implements AlertControllerApi*/ {

    private final AlertService alertService;

    @Autowired
    public AlertController(AlertService service) {
        this.alertService = service;
    }

    @GetMapping("/{alertId}")
    @AuditAction(auditAction = "Get alert by {{#uuid}}")
    public Alert getAlert(@PathVariable("alertId") UUID uuid) {
        return alertService.get(uuid);
    }

    @GetMapping
    @AuditAction(auditAction = "Get all alerts.")
    public List<Alert> getAll() {
        return alertService.getAll();
    }

    /**
     * Create new Alert.
     */
    @PostMapping(value = "/create")
    @AuditAction(auditAction = "Create new alert {{#alert.shortDescription}}")
    public Alert create(@RequestBody AlertImpl alert) {
        return alertService.create(alert.getName(),
                alert.getShortDescription(),
                alert.getTagList(),
                alert.getParameters(),
                alert.getSubscriberId(),
                alert.getStatus()
        );
    }

    /**
     * Update alert information.
     *
     * @param alert TODO
     */
    @PutMapping(value = "/create")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AuditAction(auditAction = "Update alert {{#alert.shortDescription}}")
    public void update(@RequestBody AlertImpl alert) {
        Preconditions.checkNotNull(alert.getId(), "Alert id can't be empty");
        Preconditions.checkNotNull(alert.getSubscriberId(), "Subscriber id can't be empty");
        alertService.update(alert);
    }

    @DeleteMapping("/{alertId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AuditAction(auditAction = "Delete alert {{#alert.shortDescription}}")
    public void delete(@PathVariable("alertId") UUID alertId) {
        alertService.delete(alertId);
    }
}
