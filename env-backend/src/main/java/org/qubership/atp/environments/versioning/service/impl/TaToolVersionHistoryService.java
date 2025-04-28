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

package org.qubership.atp.environments.versioning.service.impl;

import org.javers.core.Javers;
import org.qubership.atp.environments.mapper.AbstractMapper;
import org.qubership.atp.environments.service.rest.server.dto.generated.HistoryItemTypeDtoGenerated;
import org.qubership.atp.environments.versioning.model.entities.EnvironmentJ;
import org.qubership.atp.environments.versioning.model.mapper.EnvironmentVersioning;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaToolVersionHistoryService extends AbstractVersionHistoryService<EnvironmentVersioning, EnvironmentJ> {

    @Autowired
    public TaToolVersionHistoryService(Javers javers,
                                       AbstractMapper<EnvironmentJ,
                                               EnvironmentVersioning> environmentVersioningMapper) {
        super(environmentVersioningMapper, javers);
    }

    @Override
    public HistoryItemTypeDtoGenerated getItemType() {
        return HistoryItemTypeDtoGenerated.TATOOL;
    }

    @Override
    public Class<EnvironmentJ> getEntityClass() {
        return EnvironmentJ.class;
    }
}
