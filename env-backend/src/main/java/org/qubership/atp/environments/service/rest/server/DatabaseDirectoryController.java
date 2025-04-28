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
import java.util.Locale;
import java.util.Optional;

import org.qubership.atp.environments.model.DatabaseDirectory;
import org.qubership.atp.environments.service.direct.DatabaseDirectoryService;
import org.qubership.atp.integration.configuration.configuration.AuditAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/database-directory")
@RestController()
public class DatabaseDirectoryController /*implements DatabaseDirectoryControllerApi*/ {

    private final DatabaseDirectoryService databaseDirectoryService;

    @Autowired
    public DatabaseDirectoryController(DatabaseDirectoryService databaseDirectoryService) {
        this.databaseDirectoryService = databaseDirectoryService;
    }

    /**
     * Method returns database info.
     */
    @GetMapping("/{name}")
    @AuditAction(auditAction = "Get database directory by name {{#name}}")
    public ResponseEntity<DatabaseDirectory> getName(@PathVariable("name") String name) {
        return Optional.ofNullable(databaseDirectoryService.getName(name.toLowerCase(Locale.ENGLISH)))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @AuditAction(auditAction = "Get all database directories")
    public List<DatabaseDirectory> getAll() {
        return databaseDirectoryService.getAll();
    }
}
