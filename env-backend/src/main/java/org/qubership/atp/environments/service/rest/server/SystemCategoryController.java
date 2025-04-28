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

import org.qubership.atp.environments.model.SystemCategory;
import org.qubership.atp.environments.model.impl.SystemCategoryImpl;
import org.qubership.atp.environments.model.utils.View;
import org.qubership.atp.environments.service.direct.SystemCategoriesService;
import org.qubership.atp.integration.configuration.configuration.AuditAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.Operation;

@RequestMapping("/api/system-categories")
@RestController()
public class SystemCategoryController /*implements SystemCategoryControllerApi*/ {

    private final SystemCategoriesService systemCategoriesService;

    @Autowired
    public SystemCategoryController(SystemCategoriesService systemCategoriesService) {
        this.systemCategoriesService = systemCategoriesService;
    }

    @GetMapping
    @AuditAction(auditAction = "Get all system categories")
    public List<SystemCategory> getAllSystemCategories() {
        return systemCategoriesService.getAll();
    }

    @Operation(description = "Getting system categories in abbreviated form :{id:\"\",name:\"\"}")
    @GetMapping("/short")
    @JsonView({View.Name.class})
    @AuditAction(auditAction = "Get all short system categories")
    public List<SystemCategory> getShortAll() {
        return systemCategoriesService.getAll();
    }

    @GetMapping("/{systemCategoryId}")
    @AuditAction(auditAction = "Get system category by id {{#id.toString()}}")
    public SystemCategory getSystemCategory(@PathVariable("systemCategoryId") UUID id) {
        return systemCategoriesService.get(id);
    }

    @PostMapping
    @AuditAction(auditAction = "Create system category with name {{#systemCategories.getName()}}")
    public SystemCategory createSystemCategory(@RequestBody SystemCategoryImpl systemCategories) {
        Preconditions.checkNotNull(systemCategories.getName(), "System category name can't be empty");
        return systemCategoriesService.create(systemCategories.getName(), systemCategories.getDescription());
    }

    /**
     * Update system category by id and name.
     */
    @PutMapping
    @AuditAction(auditAction = "Update system category with id {{#systemCategories.getId()}} "
            + "and name {{#systemCategories.getName()}}")
    public SystemCategory updateSystemCategory(@RequestBody SystemCategoryImpl systemCategories) {
        Preconditions.checkNotNull(systemCategories.getId(), "System category id can't be empty");
        Preconditions.checkNotNull(systemCategories.getName(), "System category name  can't be empty");
        return systemCategoriesService.update(systemCategories.getId(), systemCategories.getName(),
                systemCategories.getDescription());
    }

    @DeleteMapping("/{systemCategoryId}")
    @AuditAction(auditAction = "Delete system category by id {{#id.toString()}}")
    public void deleteCategory(@PathVariable("systemCategoryId") UUID id) {
        systemCategoriesService.delete(id);
    }
}
