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

import java.util.UUID;

import javax.annotation.Nonnull;

import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.impl.ConnectionImpl;
import org.qubership.atp.environments.repo.impl.ConnectionRepositoryImpl;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.MappingProjection;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
public class IdConnectionProjection extends MappingProjection<Connection> {

    static final long serialVersionUID = 42L;
    private final transient ConnectionRepositoryImpl repo;

    public IdConnectionProjection(ConnectionRepositoryImpl repo) {
        super(Connection.class, CONNECTIONS.all());
        this.repo = repo;
    }

    @Override
    protected Connection map(Tuple tuple) {
        UUID uuid = tuple.get(CONNECTIONS.id);
        assert uuid != null;
        String name = tuple.get(CONNECTIONS.name);
        assert name != null;
        return create(uuid, name);
    }

    protected ConnectionImpl create(@Nonnull UUID uuid,
                                    @Nonnull String name) {
        return ConnectionImpl.builder().uuid(uuid).name(name).build();
    }
}
