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

package org.qubership.atp.environments.db.modification;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;

public enum TrackedMethod {
    CREATE("create", DbNotificationStatus.UPDATED),
    UPDATE("update", DbNotificationStatus.UPDATED),
    DELETE("delete", DbNotificationStatus.DELETED);

    private static final ImmutableMap<String, TrackedMethod> BY_NAME = new ImmutableMap
            .Builder<String, TrackedMethod>()
            .putAll(Arrays.stream(TrackedMethod.values())
                    .map(type -> new AbstractMap.SimpleEntry<>(type.name, type))
                    .collect(Collectors.toList())).build();
    public final String name;
    public final DbNotificationStatus dbNotificationStatus;

    TrackedMethod(String name, DbNotificationStatus dbNotificationStatus) {
        this.name = name;
        this.dbNotificationStatus = dbNotificationStatus;
    }

    public static Optional<TrackedMethod> getByName(String name) {
        return Optional.ofNullable(BY_NAME.get(name));
    }

    public String getName() {
        return name;
    }
}
