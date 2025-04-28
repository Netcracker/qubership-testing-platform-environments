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

import java.util.UUID;

import javax.annotation.Nonnull;

import org.javers.repository.jql.InstanceIdDTO;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.versioning.model.entities.SystemJ;
import org.qubership.atp.environments.versioning.service.CommitEntityService;
import org.qubership.atp.environments.versioning.service.JaversService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommitSystemServiceImpl implements CommitEntityService<System> {

    private final JaversService javersService;

    @Autowired
    public CommitSystemServiceImpl(JaversService javersService) {
        this.javersService = javersService;
    }

    @Override
    public void commit(@Nonnull System system) {
        javersService.commit(new SystemJ(system));
    }

    @Override
    public void delete(@Nonnull UUID id) {
        javersService.commitShallowDeleteById(InstanceIdDTO.instanceId(id, SystemJ.class));
    }

    @Override
    public Class<System> getEntityType() {
        return System.class;
    }
}
