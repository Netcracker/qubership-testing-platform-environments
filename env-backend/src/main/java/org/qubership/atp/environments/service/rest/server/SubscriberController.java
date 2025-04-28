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

import org.qubership.atp.environments.model.Subscriber;
import org.qubership.atp.environments.model.impl.SubscriberImpl;
import org.qubership.atp.environments.service.direct.SubscriberService;
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

@RequestMapping("/api/subscribers")
@RestController()
public class SubscriberController /*implements SubscriberControllerApi*/ {

    private final SubscriberService subscriberService;

    @Autowired
    public SubscriberController(SubscriberService service) {
        this.subscriberService = service;
    }

    @GetMapping("/{subscriberId}")
    @AuditAction(auditAction = "Get subscriber by id {{#uuid.toString()}}")
    public Subscriber getsubscriber(@PathVariable("subscriberId") UUID uuid) {
        return subscriberService.get(uuid);
    }

    @PostMapping
    @AuditAction(auditAction = "Create subscriber by id {{#subscriber.getId()}}")
    public Subscriber create(@RequestBody SubscriberImpl subscriber) {
        return subscriberService.create(subscriber);
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AuditAction(auditAction = "Update subscriber by id {{#subscriber.getId()}}")
    public void update(@RequestBody SubscriberImpl subscriber) {
        Preconditions.checkNotNull(subscriber.getId(), "Subscriber id can't be empty");
        Preconditions.checkNotNull(subscriber.getName(), "Subscriber name can't be empty");
        subscriberService.update(subscriber);
    }

    @GetMapping
    @AuditAction(auditAction = "Get all subscribers")
    public List<Subscriber> getAll() {
        return subscriberService.getAll();
    }

    @DeleteMapping("/{subscriberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AuditAction(auditAction = "Delete subscriber by id {{#subscriberId.toString()}}")
    public void delete(@PathVariable("subscriberId") UUID subscriberId) {
        subscriberService.delete(subscriberId);
    }
}
