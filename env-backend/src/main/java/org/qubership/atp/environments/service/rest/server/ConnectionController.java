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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.utils.View;
import org.qubership.atp.environments.service.direct.ConcurrentModificationService;
import org.qubership.atp.environments.service.direct.ConnectionService;
import org.qubership.atp.environments.service.rest.server.dto.ConnectionByCategoryDto;
import org.qubership.atp.environments.service.rest.server.dto.ConnectionDto;
import org.qubership.atp.integration.configuration.configuration.AuditAction;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.Operation;

@RequestMapping("/api/connections")
@RestController()
public class ConnectionController /*implements ConnectionControllerApi*/  {

    private final ConnectionService connectionService;
    private final ConcurrentModificationService concurrentModificationService;

    @Autowired
    public ConnectionController(ConnectionService connectionService,
                                ConcurrentModificationService concurrentModificationService) {
        this.connectionService = connectionService;
        this.concurrentModificationService = concurrentModificationService;
    }

    @GetMapping
    @AuditAction(auditAction = "Get all connections")
    public List<Connection> getConnectionsAll() {
        return connectionService.getAll();
    }

    @PostMapping("/getAllBy")
    @AuditAction(auditAction = "Get all connections by system category id {{#connectionByCategory.systemCategoryId}}")
    public List<Connection> getConnectionsAllBy(@RequestBody ConnectionByCategoryDto connectionByCategory) {
        return connectionService.getAll(
                connectionByCategory.getEnvironmentIds(), connectionByCategory.getSystemCategoryId());
    }

    @GetMapping("/{connectionId}")
    @AuditAction(auditAction = "Get connection by id {{#id.toString()}}")
    public Connection getConnection(@PathVariable("connectionId") UUID id) {
        return connectionService.get(id);
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).CONNECTION.getName(),"
            + "#connection.getProjectId(),'CREATE')")
    @PostMapping
    @AuditAction(auditAction = "Create connection. System id: {{#connection.systemId.toString()}}")
    public Connection createConnection(@RequestBody ConnectionDto connection) {
        Preconditions.checkNotNull(connection.getName(), "Connection name can't be empty");
        Preconditions.checkNotNull(connection.getSystemId(), "System ID can't be empty");
        return connectionService.create(connection.getSystemId(),
                connection.getName(),
                connection.getDescription(),
                connection.getParameters(),
                connection.getConnectionType(),
                connection.getSourceTemplateId(),
                connection.getProjectId(),
                connection.getServices());
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).CONNECTION.getName(),"
            + "#connectionDto.getProjectId(),'UPDATE')")
    @PutMapping
    @AuditAction(auditAction = "Update connection. System id: {{#connection.systemId.toString()}}"
            + " and connection id: {{#connection.id.toString()}}")
    public ResponseEntity<Connection> updateConnection(@RequestBody ConnectionDto connectionDto) {
        Preconditions.checkNotNull(connectionDto.getId(), "Connection id can't be empty");
        Preconditions.checkNotNull(connectionDto.getName(), "Connection name can't be empty");
        Preconditions.checkNotNull(connectionDto.getSystemId(), "System ID can't be empty");
        HttpStatus status = concurrentModificationService.getConcurrentModificationHttpStatus(
                connectionDto.getId(), connectionDto.getModified(), connectionService);
        Connection updatedConnection = connectionService.update(connectionDto.getId(),
                connectionDto.getSystemId(),
                connectionDto.getName(),
                connectionDto.getDescription(),
                connectionDto.getParameters(),
                connectionDto.getConnectionType(),
                connectionDto.getSourceTemplateId(),
                connectionDto.getProjectId(),
                connectionDto.getServices());
        return ResponseEntity.status(status).body(updatedConnection);
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @PutMapping("/parameters")
    @AuditAction(auditAction = "Update connection parameters")
    public List<Connection> updateParameters(@RequestBody List<ConnectionDto> connections) {
        List<Connection> updatedConnections = new ArrayList<>();
        for (ConnectionDto connection : connections) {
            Preconditions.checkNotNull(connection.getId(), "Connection id can't be empty");
            Preconditions.checkNotNull(connection.getName(), "Connection name can't be empty");
            Preconditions.checkNotNull(connection.getSystemId(), "System ID can't be empty");
            connectionService.updateParameters(connection.getId(), connection.getParameters(),
                    connection.getServices());
            updatedConnections.add(connectionService.get(connection.getId()));
        }
        return updatedConnections;
    }

    @PreAuthorize("@entityAccess.checkAccess("
            + "T(org.qubership.atp.environments.enums.UserManagementEntities).CONNECTION.getName(),"
            + "@connectionService.getProjectId(#id),'DELETE')")
    @DeleteMapping("/{connectionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AuditAction(auditAction = "Delete connection by uuid {{#id.toString()}}")
    public void deleteConnection(@PathVariable("connectionId") UUID id) {
        connectionService.delete(id);
    }

    @GetMapping("/templates")
    @AuditAction(auditAction = "Get connection templates.")
    public List<Connection> getConnectionTemplates() {
        return connectionService.getConnectionTemplates();
    }

    @Operation(description = "Getting connection templates in abbreviated form :{id:\"\",name:\"\"}")
    @GetMapping("/templates/short")
    @JsonView({View.Name.class})
    @AuditAction(auditAction = "Get connection templates short.")
    public List<Connection> getConnectionTemplatesShort() {
        return connectionService.getConnectionTemplates();
    }
}
