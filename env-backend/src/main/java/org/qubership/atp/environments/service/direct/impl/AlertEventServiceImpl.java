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

import org.qubership.atp.environments.model.AlertEvent;
import org.qubership.atp.environments.repo.impl.AlertEventRepositoryImpl;
import org.qubership.atp.environments.service.direct.AlertEventService;
import org.qubership.atp.environments.utils.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("alertEventService")
public class AlertEventServiceImpl implements AlertEventService {

    private final AlertEventRepositoryImpl alertEventRepository;
    private final DateTimeUtil dateTimeUtil;

    @Autowired
    public AlertEventServiceImpl(AlertEventRepositoryImpl alertEventRepository, DateTimeUtil dateTimeUtil) {
        this.alertEventRepository = alertEventRepository;
        this.dateTimeUtil = dateTimeUtil;
    }

    @Nullable
    @Override
    public AlertEvent get(@Nonnull UUID alertId, @Nonnull UUID entityId) {
        return alertEventRepository.getById(alertId, entityId);
    }

    @Nonnull
    @Override
    public List<AlertEvent> getAll() {
        return alertEventRepository.getAll();
    }

    @Nonnull
    @Override
    public AlertEvent create(@Nonnull UUID alertId, UUID entityId, String tagList, Integer status) {
        return alertEventRepository.create(alertId, entityId, tagList, status, dateTimeUtil.timestampAsUtc());
    }

    @Override
    public void update(@Nonnull UUID alertId, UUID entityId, String tagList, Integer status) {
        alertEventRepository.update(alertId, entityId, tagList, status, dateTimeUtil.timestampAsUtc());
    }

    @Override
    public void delete(@Nonnull UUID alertId, UUID entityId) {
        alertEventRepository.delete(alertId, entityId);
    }
}
