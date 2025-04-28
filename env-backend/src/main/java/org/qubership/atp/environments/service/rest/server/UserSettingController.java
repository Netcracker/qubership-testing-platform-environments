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

package org.qubership.atp.environments.service.rest.server;

import org.qubership.atp.auth.springbootstarter.entities.UserInfo;
import org.qubership.atp.auth.springbootstarter.ssl.Provider;
import org.qubership.atp.environments.model.UserSetting;
import org.qubership.atp.environments.service.direct.UserSettingService;
import org.qubership.atp.environments.service.rest.server.dto.UserSettingsDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RequestMapping("/api/usersettings")
@RestController
@RequiredArgsConstructor
public class UserSettingController {

    private final UserSettingService userSettingService;
    private final Provider<UserInfo> userInfoProvider;

    @GetMapping
    public UserSetting get() {
        return userSettingService.get(userInfoProvider.get().getId());
    }

    /**
     * API for creating new user setting.
     * @param userSetting request body.
     * @return userSetting
     */
    @PostMapping
    public UserSetting create(@RequestBody UserSettingsDto userSetting) {
        return userSettingService.create(
                userInfoProvider.get().getId(),
                UserSetting.ViewType.findByName(userSetting.getView())
        );
    }

    /**
     * API for updating user setting.
     * @param userSetting request body.
     * @return userSetting
     */
    @PutMapping
    public UserSetting update(@RequestBody UserSettingsDto userSetting) {
        return userSettingService.update(
                userInfoProvider.get().getId(),
                UserSetting.ViewType.findByName(userSetting.getView())
        );
    }
}
