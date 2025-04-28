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

package org.qubership.atp.environments.repo.projections;

import static org.qubership.atp.environments.repo.impl.AbstractRepository.CONNECTIONS;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.model.impl.ConnectionImpl;
import org.qubership.atp.environments.repo.impl.ConnectionRepositoryImpl;

import com.google.gson.Gson;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.MappingProjection;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
@SuppressWarnings("CPD-START")
public class FullConnectionProjection extends MappingProjection<Connection> {

    static final long serialVersionUID = 42L;
    private final transient ConnectionRepositoryImpl repo;

    public FullConnectionProjection(ConnectionRepositoryImpl repo) {
        super(Connection.class, CONNECTIONS.all());
        this.repo = repo;
    }

    @Override
    protected Connection map(Tuple tuple) {
        UUID uuid = tuple.get(CONNECTIONS.id);
        assert uuid != null;
        String name = tuple.get(CONNECTIONS.name);
        assert name != null;
        String description = tuple.get(CONNECTIONS.description);
        //String parameters = bytesToJson(mapper, tuple.get(CONNECTIONS.parameters)).asText();
        String parameters = tuple.get(CONNECTIONS.parameters);
        String connectionType = tuple.get(CONNECTIONS.connectionType);
        UUID sourceTemplateId = tuple.get(CONNECTIONS.sourceTemplateId);
        String services = tuple.get(CONNECTIONS.services);
        UUID systemId = tuple.get(CONNECTIONS.systemId);
        assert systemId != null;
        Timestamp created = tuple.get(CONNECTIONS.created);
        assert created != null;
        UUID createdBy = (UUID) tuple.get(CONNECTIONS.createdBy);
        Timestamp modified = tuple.get(CONNECTIONS.modified);
        UUID modifiedBy = (UUID) tuple.get(CONNECTIONS.modifiedBy);
        UUID sourceId = (UUID) tuple.get(CONNECTIONS.sourceId);
        return create(uuid, name, description, new Gson().fromJson(parameters, ConnectionParameters.class),
                created, createdBy, modified, modifiedBy, systemId, connectionType, sourceTemplateId,
                new Gson().fromJson(services, List.class), sourceId);
    }

    /**
     * Create connection with paramenters.
     *
     * @return {@link ConnectionImpl} instance
     */
    public ConnectionImpl create(@Nonnull UUID uuid,
                                 @Nonnull String name,
                                 String description,
                                 @Nonnull ConnectionParameters parameters,
                                 Timestamp created,
                                 UUID createdBy,
                                 Timestamp modified,
                                 UUID modifiedBy,
                                 @Nonnull UUID systemId,
                                 String connectionType,
                                 UUID sourceTemplateId,
                                 List<String> services,
                                 UUID sourceId) {
        return new ConnectionImpl(uuid, name, description, parameters, created.getTime(), createdBy,
                modified == null ? null : modified.getTime(), modifiedBy,
                systemId,
                connectionType,
                sourceTemplateId, services, sourceId);
    }
}
