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

import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.Identified;
import org.qubership.atp.environments.model.Project;
import org.qubership.atp.environments.model.System;

import com.google.common.collect.ImmutableMap;

public enum TrackedType {
    PROJECT("Project", Project.class, "project"),
    ENVIRONMENT("Environment", Environment.class, "environment"),
    SYSTEM("System", System.class, "system"),
    CONNECTION("Connection", Connection.class, "system");

    private static final ImmutableMap<String, TrackedType> BY_NAME = new ImmutableMap
            .Builder<String, TrackedType>()
            .putAll(Arrays.stream(TrackedType.values())
                    .map(type -> new AbstractMap.SimpleEntry<>(type.name, type))
                    .collect(Collectors.toList())).build();
    public final String name;
    public final Class<? extends Identified> entityType;
    public final String entityForNotice;

    TrackedType(String name, Class<? extends Identified> entityType, String entityForNotice) {
        this.name = name;
        this.entityType = entityType;
        this.entityForNotice = entityForNotice;
    }

    public static Optional<TrackedType> getByName(String name) {
        return Optional.ofNullable(BY_NAME.get(name));
    }
}
