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

import org.qubership.atp.environments.model.SystemCategory;
import org.qubership.atp.environments.repo.impl.SystemCategoryRepositoryImpl;
import org.qubership.atp.environments.service.direct.SystemCategoriesService;
import org.qubership.atp.environments.utils.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("systemCategoriesService")
public class SystemCategoriesServiceImpl implements SystemCategoriesService {

    private final SystemCategoryRepositoryImpl systemCategoriesRepository;
    private final DateTimeUtil dateTimeUtil;

    @Autowired
    public SystemCategoriesServiceImpl(SystemCategoryRepositoryImpl systemCategoriesRepository,
                                       DateTimeUtil dateTimeUtil) {
        this.systemCategoriesRepository = systemCategoriesRepository;
        this.dateTimeUtil = dateTimeUtil;
    }

    @Nullable
    @Override
    public SystemCategory get(@Nonnull UUID id) {
        return systemCategoriesRepository.getById(id);
    }

    @Override
    public boolean existsById(@Nonnull UUID id) {
        return systemCategoriesRepository.existsById(id);
    }

    @Nonnull
    @Override
    public List<SystemCategory> getAll() {
        return systemCategoriesRepository.getAll();
    }

    @Nonnull
    @Override
    public SystemCategory create(String name, String description) {
        return systemCategoriesRepository.create(name, description, dateTimeUtil.timestampAsUtc());
    }

    @Nonnull
    @Override
    public SystemCategory update(UUID id, String name, String description) {
        return systemCategoriesRepository.update(id, name, description, dateTimeUtil.timestampAsUtc());
    }

    @Override
    public void delete(UUID id) {
    }

    @Override
    public SystemCategory getByName(String name) {
        return systemCategoriesRepository.getByName(name);
    }
}
