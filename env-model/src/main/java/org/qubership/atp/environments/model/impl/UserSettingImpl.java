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

package org.qubership.atp.environments.model.impl;

import java.util.UUID;

import org.qubership.atp.environments.model.UserSetting;

public class UserSettingImpl implements UserSetting {

    private UUID userId;
    private String view;

    /**
     * TODO Make javadoc documentation for this method.
     */
    public UserSettingImpl(UUID userId, String view) {
        setUserId(userId);
        setView(view);
    }

    @Override
    public UUID getUserId() {
        return userId;
    }

    @Override
    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    @Override
    public String getView() {
        return view;
    }

    @Override
    public void setView(String view) {
        this.view = view;
    }
}
