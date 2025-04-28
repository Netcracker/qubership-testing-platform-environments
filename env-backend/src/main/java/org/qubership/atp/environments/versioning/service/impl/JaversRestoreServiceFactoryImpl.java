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

import java.util.List;

import org.qubership.atp.environments.versioning.model.entities.AbstractJaversEntity;
import org.qubership.atp.environments.versioning.service.JaversRestoreService;
import org.qubership.atp.environments.versioning.service.JaversRestoreServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JaversRestoreServiceFactoryImpl implements JaversRestoreServiceFactory {

    private List<JaversRestoreService<? extends AbstractJaversEntity>> restoreServices;

    @Autowired
    public JaversRestoreServiceFactoryImpl(List<JaversRestoreService<? extends AbstractJaversEntity>> restoreServices) {
        this.restoreServices = restoreServices;
    }

    @Override
    public JaversRestoreService<? extends AbstractJaversEntity> getRestoreService(
            Class<? extends AbstractJaversEntity> javersClass) {
        return restoreServices
                .stream()
                .filter(service -> javersClass.equals(service.getEntityType()))
                .findFirst()
                .get();
    }
}
