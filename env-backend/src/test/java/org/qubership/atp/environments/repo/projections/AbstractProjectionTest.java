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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.qubership.atp.environments.repo.impl.AbstractRepository.CONNECTIONS;
import static org.qubership.atp.environments.repo.impl.AbstractRepository.ENVIRONMENTS;
import static org.qubership.atp.environments.repo.impl.AbstractRepository.PROJECTS;
import static org.qubership.atp.environments.repo.impl.AbstractRepository.SYSTEMS;

import java.sql.Timestamp;
import java.util.Objects;

import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.Project;
import org.qubership.atp.environments.model.System;

import com.google.gson.Gson;
import com.querydsl.core.Tuple;

public class AbstractProjectionTest {

    protected Tuple mockTupleForConnection(Connection connection) {
        Tuple tuple = mock(Tuple.class);
        when(tuple.get(CONNECTIONS.id)).thenReturn(connection.getId());
        when(tuple.get(CONNECTIONS.name)).thenReturn(connection.getName());
        when(tuple.get(CONNECTIONS.description)).thenReturn(connection.getDescription());
        when(tuple.get(CONNECTIONS.parameters)).thenReturn(new Gson().toJson(connection.getParameters()));
        when(tuple.get(CONNECTIONS.connectionType)).thenReturn(connection.getConnectionType());
        when(tuple.get(CONNECTIONS.sourceTemplateId)).thenReturn(connection.getSourceTemplateId());
        when(tuple.get(CONNECTIONS.services)).thenReturn(new Gson().toJson(connection.getServices()));
        when(tuple.get(CONNECTIONS.systemId)).thenReturn(connection.getSystemId());
        when(tuple.get(CONNECTIONS.created)).thenReturn(new Timestamp(connection.getCreated()));
        when(tuple.get(CONNECTIONS.createdBy)).thenReturn(connection.getCreatedBy());
        when(tuple.get(CONNECTIONS.modified)).thenReturn(new Timestamp(connection.getModified() == null ? 0 : connection.getModified()));
        when(tuple.get(CONNECTIONS.modifiedBy)).thenReturn(connection.getModifiedBy());
        when(tuple.get(CONNECTIONS.sourceId)).thenReturn(connection.getSourceId());
        return tuple;
    }

    protected Tuple mockTupleForSystem(System system) {
        Tuple tuple = mock(Tuple.class);
        when(tuple.get(SYSTEMS.id)).thenReturn(system.getId());
        when(tuple.get(SYSTEMS.name)).thenReturn(system.getName());
        when(tuple.get(SYSTEMS.description)).thenReturn(system.getDescription());
        when(tuple.get(SYSTEMS.created)).thenReturn(new Timestamp(system.getCreated()));
        when(tuple.get(SYSTEMS.createdBy)).thenReturn(system.getCreatedBy());
        when(tuple.get(SYSTEMS.modified)).thenReturn(new Timestamp(system.getModified() == null ? 0 : system.getModified()));
        when(tuple.get(SYSTEMS.modifiedBy)).thenReturn(system.getModifiedBy());
        when(tuple.get(SYSTEMS.sourceId)).thenReturn(system.getSourceId());
        when(tuple.get(SYSTEMS.categoryId)).thenReturn(system.getSystemCategoryId());
        when(tuple.get(SYSTEMS.status)).thenReturn(Objects.requireNonNull(system.getStatus()).toString());
        when(tuple.get(SYSTEMS.dateOfLastCheck)).thenReturn(system.getDateOfLastCheck() != null ? new Timestamp(system.getDateOfLastCheck()) : null);
        when(tuple.get(SYSTEMS.version)).thenReturn(system.getVersion());
        when(tuple.get(SYSTEMS.dateOfCheckVersion)).thenReturn(system.getDateOfCheckVersion() != null ? new Timestamp(system.getDateOfCheckVersion()) : null);
        when(tuple.get(SYSTEMS.parametersGettingVersion)).thenReturn(new Gson().toJson(system.getParametersGettingVersion()));
        when(tuple.get(SYSTEMS.parentSystemId)).thenReturn(system.getParentSystemId());
        when(tuple.get(SYSTEMS.serverItf)).thenReturn(new Gson().toJson(system.getServerItf()));
        when(tuple.get(SYSTEMS.mergeByName)).thenReturn(system.getMergeByName());
        when(tuple.get(SYSTEMS.linkToSystemId)).thenReturn(system.getLinkToSystemId());
        when(tuple.get(SYSTEMS.externalId)).thenReturn(system.getExternalId());
        when(tuple.get(SYSTEMS.externalName)).thenReturn(system.getExternalName());
        return tuple;
    }

    protected Tuple mockTupleForEnvironment(Environment environment) {
        Tuple tuple = mock(Tuple.class);
        when(tuple.get(ENVIRONMENTS.id)).thenReturn(environment.getId());
        when(tuple.get(ENVIRONMENTS.name)).thenReturn(environment.getName());
        when(tuple.get(ENVIRONMENTS.description)).thenReturn(environment.getDescription());
        when(tuple.get(ENVIRONMENTS.graylogName)).thenReturn(environment.getGraylogName());
        when(tuple.get(ENVIRONMENTS.ssmSolutionAlias)).thenReturn(environment.getSsmSolutionAlias());
        when(tuple.get(ENVIRONMENTS.ssmInstanceAlias)).thenReturn(environment.getSsmInstanceAlias());
        when(tuple.get(ENVIRONMENTS.projectId)).thenReturn(environment.getProjectId());
        when(tuple.get(ENVIRONMENTS.created)).thenReturn(new Timestamp(environment.getCreated()));
        when(tuple.get(ENVIRONMENTS.createdBy)).thenReturn(environment.getCreatedBy());
        when(tuple.get(ENVIRONMENTS.modified)).thenReturn(new Timestamp(environment.getModified() == null ? 0 : environment.getModified()));
        when(tuple.get(ENVIRONMENTS.modifiedBy)).thenReturn(environment.getModifiedBy());
        when(tuple.get(ENVIRONMENTS.sourceId)).thenReturn(environment.getSourceId());
        when(tuple.get(ENVIRONMENTS.categoryId)).thenReturn(environment.getCategoryId());
        return tuple;
    }

    protected Tuple mockTupleForProject(Project project) {
        Tuple tuple = mock(Tuple.class);
        when(tuple.get(PROJECTS.id)).thenReturn(project.getId());
        when(tuple.get(PROJECTS.name)).thenReturn(project.getName());
        when(tuple.get(PROJECTS.shortName)).thenReturn(project.getShortName());
        when(tuple.get(PROJECTS.description)).thenReturn(project.getDescription());
        when(tuple.get(PROJECTS.created)).thenReturn(new Timestamp(0L));
        when(tuple.get(PROJECTS.modified)).thenReturn(new Timestamp(project.getModified() == null ? 0 : project.getModified()));
        return tuple;
    }

}
