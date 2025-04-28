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

import org.qubership.atp.environments.model.UpdateEvent;
import org.qubership.atp.environments.model.impl.UpdateEventImpl;
import org.qubership.atp.environments.service.direct.UpdateEventService;
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

@RequestMapping("/api/updateEvents")
@RestController()
public class UpdateEventController /*implements UpdateEventControllerApi*/ {

    private final UpdateEventService updateEventService;

    @Autowired
    public UpdateEventController(UpdateEventService service) {
        this.updateEventService = service;
    }

    @GetMapping("/subscription/{subscriptionId}/entity/{entityId}")
    @AuditAction(auditAction = "Get update event by subscription id {{#subscriptionId.toString()}}")
    public UpdateEvent getUpdateEvent(@PathVariable("subscriptionId") UUID subscriptionId,
                                      @PathVariable("entityId") UUID entityId) {
        return updateEventService.get(subscriptionId, entityId);
    }

    @GetMapping("/subscription/{subscriptionId}")
    @AuditAction(auditAction = "Get subscription update event by subscription id {{#subscriptionId.toString()}}")
    public UpdateEvent getSubscriptionUpdateEvents(@PathVariable("subscriptionId") UUID subscriptionId) {
        return updateEventService.getSubscriptionUpdateEvents(subscriptionId);
    }

    @GetMapping("/entity/{entityId}")
    @AuditAction(auditAction = "Get entity events by entity id {{#entityId.toString()}}")
    public UpdateEvent getEntityEvents(@PathVariable("entityId") UUID entityId) {
        return updateEventService.getEntityUpdateEvents(entityId);
    }

    @GetMapping
    @AuditAction(auditAction = "Get all update events")
    public List<UpdateEvent> getAll() {
        return updateEventService.getAll();
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @PostMapping // to separate create and update
    @AuditAction(auditAction = "Create entity event with subscription id {{#updateEvent.getSubscriptionId()}}")
    public UpdateEvent create(@RequestBody UpdateEventImpl updateEvent) {
        return updateEventService.create(updateEvent.getSubscriptionId(), updateEvent.getEntityId(),
                updateEvent.getTagList(), updateEvent.getStatus(), updateEvent.getEntityType());
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @PutMapping("/subscription/{subscriptionId}/entity/{entityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AuditAction(auditAction = "Update entity event with subscription id {{#updateEvent.getSubscriptionId()}}")
    public void update(@RequestBody UpdateEventImpl updateEvent) {
        Preconditions.checkNotNull(updateEvent.getEntityId(), "Entity id can't be empty");
        Preconditions.checkNotNull(updateEvent.getSubscriptionId(), "Subscription id can't be empty");
        updateEventService.update(updateEvent.getSubscriptionId(), updateEvent.getEntityId(),
                updateEvent.getTagList(), updateEvent.getStatus(), updateEvent.getEntityType());
    }

    @DeleteMapping("/subscription/{subscriptionId}/entity/{entityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AuditAction(auditAction = "Delete entity event with subscription id {{#subscriptionId.toString()}}")
    public void delete(@PathVariable("subscriptionId") UUID subscriptionId, @PathVariable("entityId") UUID entityId) {
        updateEventService.delete(subscriptionId, entityId);
    }
}
