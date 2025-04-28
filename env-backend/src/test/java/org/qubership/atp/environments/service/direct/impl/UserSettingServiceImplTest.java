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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubership.atp.environments.model.UserSetting;
import org.qubership.atp.environments.model.impl.UserSettingImpl;
import org.qubership.atp.environments.repo.impl.UserSettingRepositoryImpl;
import org.qubership.atp.environments.service.direct.UserSettingService;

class UserSettingServiceImplTest {

    private UserSettingRepositoryImpl userSettingRepository;
    private UserSettingService userSettingService;

    @BeforeEach
    public void setUp() {
        userSettingRepository = mock(UserSettingRepositoryImpl.class);
        userSettingService = new UserSettingServiceImpl(userSettingRepository);
    }

    @Test
    void get_withExistingUserId_gotInfo() {
        UserSetting userSetting = new UserSettingImpl(UUID.randomUUID(), "TAGS");
        when(userSettingRepository.getByUserId(any())).thenReturn(userSetting);
        Assertions.assertEquals("TAGS", userSettingService.get(UUID.randomUUID()).getView());
    }

    @Test
    void get_userIdNotFound_gotNull() {
        when(userSettingRepository.getByUserId(any())).thenReturn(null);
        UserSetting userSetting = userSettingService.get(UUID.randomUUID());
        Assertions.assertNull(userSetting);
    }

    @Test
    void create_gotFilledRequest_createdUserSettings() {
        UserSetting userSetting = new UserSettingImpl(UUID.randomUUID(), "TAGS");
        when(userSettingRepository.create(any(), anyString())).thenReturn(userSetting);
        Assertions.assertEquals("TAGS", userSettingService.create(UUID.randomUUID(), UserSetting.ViewType.TAGS).getView());
    }

    @Test
    void update_gotFilledRequest_updatedUserSettings() {
        UserSetting userSetting = new UserSettingImpl(UUID.randomUUID(), "TAGS");
        when(userSettingRepository.update(any(), anyString())).thenReturn(userSetting);
        Assertions.assertEquals("TAGS",
                userSettingService.update(UUID.randomUUID(), UserSetting.ViewType.TAGS).getView());
    }
}
