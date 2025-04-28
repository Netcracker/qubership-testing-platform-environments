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

package org.qubership.atp.environments.versioning.model.entities;

import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.javers.core.metamodel.annotation.TypeName;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.service.direct.SystemCategoriesService;
import org.qubership.atp.environments.versioning.model.values.ConnectionJ;
import org.qubership.atp.environments.versioning.model.values.ParametersGettingVersionJ;
import org.qubership.atp.environments.versioning.model.values.ServerItfJ;
import org.qubership.atp.environments.versioning.validation.ReferenceExists;
import org.springframework.util.CollectionUtils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;

@Getter
@TypeName("System")
public class SystemJ extends AbstractJaversEntity {

    private String name;
    private String description;
    @SuppressFBWarnings("EI_EXPOSE_REP")
    private Date modified;
    @ReferenceExists(service = SystemCategoriesService.class, message = "system category")
    private UUID systemCategoryId;
    @Nullable
    private Set<ConnectionJ> connections;
    private ParametersGettingVersionJ parametersGettingVersion;
    private ServerItfJ serverItf;

    /**
     * The main constructor.
     *
     * @param model - Domain object of {@link System}
     */
    public SystemJ(System model) {
        super(model);
        name = model.getName();
        description = model.getDescription();
        modified = model.getModified() == null
                ? null
                : new Date(model.getModified());
        systemCategoryId = model.getSystemCategoryId();

        if (!CollectionUtils.isEmpty(model.getConnections())) {
            connections = model.getConnections()
                    .stream()
                    .map(ConnectionJ::new)
                    .collect(Collectors.toSet());
        }

        parametersGettingVersion = model.getParametersGettingVersion() == null
                ? null
                : new ParametersGettingVersionJ(model.getParametersGettingVersion());

        serverItf = model.getServerItf() == null
                ? null
                : new ServerItfJ(model.getServerItf());
    }
}
