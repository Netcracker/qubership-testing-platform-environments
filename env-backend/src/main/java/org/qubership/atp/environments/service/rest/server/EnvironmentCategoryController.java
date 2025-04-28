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

import org.qubership.atp.environments.model.EnvironmentCategory;
import org.qubership.atp.environments.model.impl.EnvironmentCategoryImpl;
import org.qubership.atp.environments.service.direct.EnvironmentCategoryService;
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

@RequestMapping("/api/environment-categories")
@RestController()
public class EnvironmentCategoryController /*implements EnvironmentCategoryControllerApi*/ {

    private final EnvironmentCategoryService environmentCategoryService;

    @Autowired
    public EnvironmentCategoryController(EnvironmentCategoryService environmentCategoryService) {
        this.environmentCategoryService = environmentCategoryService;
    }

    @GetMapping
    @AuditAction(auditAction = "Get list of environment categories.")
    public List<EnvironmentCategory> getAll() {
        return environmentCategoryService.getAll();
    }

    @GetMapping("/{environmentCategoryId}")
    @AuditAction(auditAction = "Get environment category by id: {{#id.toString()}}")
    public EnvironmentCategory get(@PathVariable("environmentCategoryId") UUID id) {
        return environmentCategoryService.get(id);
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @PostMapping
    @AuditAction(auditAction = "Create environment category with name {{#environmentCategory.name}}")
    public EnvironmentCategory create(@RequestBody EnvironmentCategoryImpl environmentCategory) {
        Preconditions.checkNotNull(environmentCategory.getName(), "Environment category name can't be empty");
        return environmentCategoryService.create(environmentCategory.getName(), environmentCategory.getDescription(),
                environmentCategory.getTagList());
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @PutMapping
    @AuditAction(auditAction = "Update environment category with id {{#environmentCategory.id.toString()}}")
    public EnvironmentCategory update(@RequestBody EnvironmentCategoryImpl environmentCategory) {
        Preconditions.checkNotNull(environmentCategory.getId(), "Environment category "
                + "id can't be empty");
        Preconditions.checkNotNull(environmentCategory.getName(), "Environment category name  can't be empty");
        return environmentCategoryService.update(environmentCategory.getId(), environmentCategory.getName(),
                environmentCategory.getDescription(), environmentCategory.getTagList());
    }

    @DeleteMapping("/{environmentCategoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AuditAction(auditAction = "Delete environment category {{#id.toString()}}")
    public void delete(@PathVariable("environmentCategoryId") UUID id) {
        environmentCategoryService.delete(id);
    }
}
