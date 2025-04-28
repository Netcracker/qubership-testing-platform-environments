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

package org.qubership.atp.environments.versioning.model.values;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.javers.core.metamodel.annotation.TypeName;
import org.javers.core.metamodel.annotation.Value;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.ConnectionParameters;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
@TypeName("Connection")
@Value
public class ConnectionJ {

    private UUID id;
    private String name;
    private String description;
    @EqualsAndHashCode.Exclude
    @SuppressFBWarnings("EI_EXPOSE_REP")
    private Date modified;
    private ConnectionParameters parameters;
    private UUID sourceTemplateId;
    private String connectionType;
    private List<String> services;

    /**
     * The main constructor.
     *
     * @param model - Domain object of {@link Connection}
     */
    public ConnectionJ(Connection model) {
        id = model.getId();
        name = model.getName();
        description = model.getDescription();
        modified = model.getModified() == null
                ? null
                : new Date(model.getModified());
        parameters = model.getParameters();
        sourceTemplateId = model.getSourceTemplateId();
        connectionType = model.getConnectionType();
        services = model.getServices();
    }
}
