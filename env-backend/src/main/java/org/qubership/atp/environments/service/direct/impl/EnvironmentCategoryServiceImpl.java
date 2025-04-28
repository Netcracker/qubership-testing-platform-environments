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

import org.qubership.atp.environments.model.EnvironmentCategory;
import org.qubership.atp.environments.repo.impl.EnvironmentCategoryRepositoryImpl;
import org.qubership.atp.environments.service.direct.EnvironmentCategoryService;
import org.qubership.atp.environments.utils.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("environmentCategoryService")
public class EnvironmentCategoryServiceImpl implements EnvironmentCategoryService {

    private final EnvironmentCategoryRepositoryImpl environmentCategoryRepository;
    private final DateTimeUtil dateTimeUtil;

    @Autowired
    public EnvironmentCategoryServiceImpl(EnvironmentCategoryRepositoryImpl environmentCategoryRepository,
                                          DateTimeUtil dateTimeUtil) {
        this.environmentCategoryRepository = environmentCategoryRepository;
        this.dateTimeUtil = dateTimeUtil;
    }

    @Nullable
    @Override
    public EnvironmentCategory get(@Nonnull UUID id) {
        return environmentCategoryRepository.getById(id);
    }

    @Override
    public boolean existsById(@Nonnull UUID id) {
        return environmentCategoryRepository.existsById(id);
    }

    @Nonnull
    @Override
    public List<EnvironmentCategory> getAll() {
        return environmentCategoryRepository.getAll();
    }

    @Nonnull
    @Override
    public EnvironmentCategory create(String name, String description, String tagList) {
        return environmentCategoryRepository.create(name, description, tagList, dateTimeUtil.timestampAsUtc());
    }

    @Nonnull
    @Override
    public EnvironmentCategory update(UUID id, String name, String description, String tagList) {
        return environmentCategoryRepository.update(id, name, description, tagList, dateTimeUtil.timestampAsUtc());
    }

    @Override
    public void delete(UUID id) {
        environmentCategoryRepository.delete(id);
    }

}
