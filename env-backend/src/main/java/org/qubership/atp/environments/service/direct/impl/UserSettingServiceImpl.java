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

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.qubership.atp.environments.model.UserSetting;
import org.qubership.atp.environments.repo.impl.UserSettingRepositoryImpl;
import org.qubership.atp.environments.service.direct.UserSettingService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserSettingServiceImpl implements UserSettingService {

    private final UserSettingRepositoryImpl userSettingRepository;

    @Nullable
    @Override
    public UserSetting get(@Nonnull UUID userId) {
        return userSettingRepository.getByUserId(userId);
    }

    @Override
    public UserSetting create(@Nonnull UUID userId, @Nonnull UserSetting.ViewType view) {
        return userSettingRepository.create(userId, view.getName());
    }

    @Override
    public UserSetting update(@Nonnull UUID userId, @Nonnull UserSetting.ViewType view) {
        return userSettingRepository.update(userId, view.getName());
    }

}
