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

import org.qubership.atp.environments.model.Subscription;
import org.qubership.atp.environments.service.direct.SubscriptionService;
import org.qubership.atp.environments.service.rest.server.dto.SubscriptionDto;
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

@RequestMapping("/api/subscriptions")
@RestController()
public class SubscriptionController /*implements SubscriptionControllerApi*/ {

    private final SubscriptionService subscriptionService;

    @Autowired
    public SubscriptionController(SubscriptionService service) {
        this.subscriptionService = service;
    }

    @GetMapping("/{subscriptionId}")
    @AuditAction(auditAction = "Get subscription by subscription id {{#uuid.toString()}}")
    public Subscription getSubscription(@PathVariable("subscriptionId") UUID uuid) {
        return subscriptionService.get(uuid);
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @PostMapping
    @AuditAction(auditAction = "Create subscription for project {{#subscription.projectId.toString()}}"
            + " with subscriber id {{#subscription.subscriberId.toString()}}")
    public Subscription create(@RequestBody SubscriptionDto subscription) {
        Preconditions.checkArgument(subscription.getProjectId() != null
                        ^ subscription.getEnvironmentId() != null
                        ^ subscription.getSystemId() != null,
                "Only one value must be set: projectId | environmentId | systemId");
        List<UUID> listSubscriptionId = subscriptionService.getListSubscriptionsByConditions(
                subscription.getProjectId(),
                subscription.getEnvironmentId(),
                subscription.getSystemId(),
                subscription.getSubscriberId());
        String messageCheck = "";
        if (!listSubscriptionId.isEmpty()) {
            messageCheck = listSubscriptionId.get(0).toString();
        }
        Preconditions.checkArgument(listSubscriptionId.isEmpty(), "This combination (projectId, "
                + "environmentId, systemId, subscriberId) already exists (subscriptionId: " + messageCheck + ")");
        return subscriptionService.create(subscription.getSubscriptionType(),
                subscription.getProjectId(),
                subscription.getEnvironmentId(),
                subscription.getSystemId(),
                subscription.getSubscriberId(),
                subscription.getStatus(),
                subscription.getLastUpdated()
        );
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AuditAction(auditAction = "Update subscription for project {{#subscription.projectId.toString()}}"
            + " with subscriber id {{#subscription.subscriberId.toString()}}")
    public void update(@RequestBody SubscriptionDto subscription) {
        Preconditions.checkNotNull(subscription.getId(), "Subscription id can't be empty");
        Preconditions.checkArgument(subscription.getProjectId() != null
                        ^ subscription.getEnvironmentId() != null
                        ^ subscription.getSystemId() != null,
                "Only one value must be set: projectId | environmentId | systemId");
        subscriptionService.update(subscription.getId(),
                subscription.getSubscriptionType(),
                subscription.getProjectId(),
                subscription.getEnvironmentId(),
                subscription.getSystemId(),
                subscription.getSubscriberId(),
                subscription.getStatus(),
                subscription.getLastUpdated()
        );
    }

    @GetMapping
    @AuditAction(auditAction = "Get all subscription")
    public List<Subscription> getAll() {
        return subscriptionService.getAll();
    }

    @GetMapping("subscriber/{subscriberId}")
    @AuditAction(auditAction = "Get subscriber {{#subscriberId.toString()}} subscriptions")
    public List<Subscription> getSubscriberSubscriptions(@PathVariable("subscriberId") UUID uuid) {
        return subscriptionService.getSubscriberSubscriptions(uuid);
    }

    @GetMapping("project/{projectId}")
    @AuditAction(auditAction = "Get subscriptions by project id")
    public List<Subscription> getProjectSubscriptions(@PathVariable("projectId") UUID uuid) {
        return subscriptionService.getProjectSubscriptions(uuid);
    }

    @GetMapping("environment/{environmentId}")
    @AuditAction(auditAction = "Get subscriptions by environment id: {{#uuid.toString()}}")
    public List<Subscription> getEnvironmentSubscriptions(@PathVariable("environmentId") UUID uuid) {
        return subscriptionService.getEnvironmentSubscriptions(uuid);
    }

    @GetMapping("system/{systemId}")
    @AuditAction(auditAction = "Get system subscriptions for system id {{#uuid.toString()}}")
    public List<Subscription> getSystemSubscriptions(@PathVariable("systemId") UUID uuid) {
        return subscriptionService.getSystemSubscriptions(uuid);
    }

    @DeleteMapping("/{subscriptionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AuditAction(auditAction = "Delete subscription with id {{#subscriptionId.toString()}}")
    public void delete(@PathVariable("subscriptionId") UUID subscriptionId) {
        subscriptionService.delete(subscriptionId);
    }
}
