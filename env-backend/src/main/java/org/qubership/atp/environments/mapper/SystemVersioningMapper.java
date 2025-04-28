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

package org.qubership.atp.environments.mapper;

import static java.util.Objects.nonNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.qubership.atp.environments.service.direct.SystemCategoriesService;
import org.qubership.atp.environments.versioning.model.entities.SystemJ;
import org.qubership.atp.environments.versioning.model.mapper.ConnectionVersioning;
import org.qubership.atp.environments.versioning.model.mapper.SystemVersioning;
import org.qubership.atp.environments.versioning.model.values.ConnectionJ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SystemVersioningMapper extends AbstractVersioningMapper<SystemJ, SystemVersioning> {

    private final SystemCategoriesService systemCategoriesService;
    private final ConnectionVersioningMapper connectionVersioningMapper;

    /**
     * SystemVersioningMapper constructor.
     * @param systemCategoriesService    systemCategoriesService
     * @param connectionVersioningMapper connectionVersioningMapper
     */
    @Autowired
    public SystemVersioningMapper(SystemCategoriesService systemCategoriesService,
                                  ConnectionVersioningMapper connectionVersioningMapper) {
        super(SystemJ.class, SystemVersioning.class);
        this.systemCategoriesService = systemCategoriesService;
        this.connectionVersioningMapper = connectionVersioningMapper;
    }

    @Override
    void mapSpecificFields(SystemJ source, SystemVersioning destination) {
        Collection<ConnectionJ> connections = source.getConnections();
        if (nonNull(connections)) {
            List<ConnectionVersioning> connectionVersionings =
                    connections.stream().map(connectionVersioningMapper::map).collect(Collectors.toList());
            destination.setConnections(connectionVersionings);
        }
        destination.setSystemCategoryName(
                getAbstractEntityName(source.getSystemCategoryId(), systemCategoriesService));
    }
}
