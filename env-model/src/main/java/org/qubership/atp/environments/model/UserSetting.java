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

package org.qubership.atp.environments.model;

import java.util.UUID;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public interface UserSetting {

    @Nonnull
    @JsonIgnore
    UUID getUserId();

    void setUserId(@Nonnull UUID userId);

    @Nonnull
    @JsonProperty("view")
    String getView();

    void setView(@Nonnull String view);

    @RequiredArgsConstructor
    @Getter
    enum ViewType {
        TAGS("TAGS"),
        LIST("LIST");
        private final String name;

        public static ViewType findByName(String name) {
            for (ViewType type : values()) {
                if (type.getName().equals(name)) {
                    return type;
                }
            }
            throw new RuntimeException(String.format("There is no %s type of environments view", name));
        }
    }

}
