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

package org.qubership.atp.environments.service.direct.impl;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.qubership.atp.environments.model.Alert;
import org.qubership.atp.environments.repo.impl.AlertRepositoryImpl;
import org.qubership.atp.environments.service.direct.AlertService;
import org.qubership.atp.environments.utils.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("alertService")
public class AlertServiceImpl implements AlertService {

    private final AlertRepositoryImpl alertRepository;
    private final DateTimeUtil dateTimeUtil;

    @Autowired
    public AlertServiceImpl(AlertRepositoryImpl alertRepository, DateTimeUtil dateTimeUtil) {
        this.alertRepository = alertRepository;
        this.dateTimeUtil = dateTimeUtil;
    }

    @Nullable
    @Override
    public Alert get(@Nonnull UUID id) {
        return alertRepository.getById(id);
    }

    @Override
    public boolean existsById(@Nonnull UUID id) {
        return alertRepository.existsById(id);
    }

    @Nonnull
    @Override
    public List<Alert> getAll() {
        return alertRepository.getAll();
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Override
    public Alert create(String name, String shortDescription, String tagList,
                        String parameters /*!!!!!!JSONB*/, UUID subscriberId, Integer status) {
        return alertRepository.create(name, shortDescription, tagList,
                parameters /*!!!!!!JSONB*/, subscriberId, status,
                dateTimeUtil.timestampAsUtc());
    }

    @Override
    public void update(Alert alert) {
        alertRepository.update(alert.getId(),
                alert.getName(),
                alert.getShortDescription(),
                alert.getTagList(),
                alert.getParameters(),
                alert.getSubscriberId(),
                alert.getStatus(),
                dateTimeUtil.timestampAsUtc());
    }

    @Override
    public void delete(UUID alertId) {
        alertRepository.delete(alertId);
    }
}
