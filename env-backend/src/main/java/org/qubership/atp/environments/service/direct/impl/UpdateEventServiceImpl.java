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

import org.qubership.atp.environments.model.UpdateEvent;
import org.qubership.atp.environments.repo.impl.UpdateEventRepositoryImpl;
import org.qubership.atp.environments.service.direct.UpdateEventService;
import org.qubership.atp.environments.utils.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("updateEventService")
public class UpdateEventServiceImpl implements UpdateEventService {

    private final UpdateEventRepositoryImpl updateEventRepository;
    private final DateTimeUtil dateTimeUtil;

    @Autowired
    public UpdateEventServiceImpl(UpdateEventRepositoryImpl updateEventRepository, DateTimeUtil dateTimeUtil) {
        this.updateEventRepository = updateEventRepository;
        this.dateTimeUtil = dateTimeUtil;
    }

    @Nullable
    @Override
    public UpdateEvent get(@Nonnull UUID subscriptionId, @Nonnull UUID entityId) {
        return updateEventRepository.getById(subscriptionId, entityId);
    }

    @Nullable
    @Override
    public UpdateEvent getSubscriptionUpdateEvents(@Nonnull UUID subscriptionId) {
        return updateEventRepository.getBySubscriptionId(subscriptionId);
    }

    @Nullable
    @Override
    public UpdateEvent getEntityUpdateEvents(@Nonnull UUID entityId) {
        return updateEventRepository.getByEntityId(entityId);
    }

    @Nonnull
    @Override
    public List<UpdateEvent> getAll() {
        return updateEventRepository.getAll();
    }

    @Nonnull
    @Override
    public UpdateEvent create(@Nonnull UUID subscriptionId, UUID entityId, String tagList, Integer status,
                              String entityType) {
        return updateEventRepository.create(subscriptionId, entityId, tagList, status,
                dateTimeUtil.timestampAsUtc(), entityType);
    }

    @Override
    public void update(UUID subscriptionId, UUID entityId, String tagList, Integer status, String entityType) {
        updateEventRepository.update(subscriptionId, entityId, tagList, status, dateTimeUtil.timestampAsUtc(),
                entityType);
    }

    @Override
    public void delete(UUID subscriptionId, UUID entityId) {
        updateEventRepository.delete(subscriptionId, entityId);
    }
}
