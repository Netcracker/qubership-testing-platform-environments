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

package org.qubership.atp.environments.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.qubership.atp.environments.service.rest.server.dto.TaEngineType;

public enum TaEngineTypeEnum {
    EXECUTOR("Executor", true),
    NEWMAN("Newman", false);

    private TaEngineType engineType;

    TaEngineTypeEnum(String name, boolean isDefault) {
        this.engineType = new TaEngineType(name, isDefault);
    }

    TaEngineTypeEnum(String name) {
        this(name, false);
    }

    public TaEngineType getEngineType() {
        return engineType;
    }

    /**
     * Return all {@link TaEngineType}.
     *
     * @return list of engine type
     */
    public static List<TaEngineType> getAll() {
        return Arrays.stream(values())
                .map(TaEngineTypeEnum::getEngineType)
                .collect(Collectors.toList());
    }
}
