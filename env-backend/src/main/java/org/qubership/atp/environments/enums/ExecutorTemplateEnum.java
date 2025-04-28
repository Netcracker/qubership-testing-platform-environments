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

import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import org.qubership.atp.environments.service.rest.server.dto.ExecutorTemplate;
import org.qubership.atp.environments.service.rest.server.dto.TaEngineAbstractParam;

import lombok.Getter;

@Getter
public enum ExecutorTemplateEnum {
    TA_ENGINE_PROVIDER_PARAMS(
            "ta_engine_provider_params",
            "TA Engine Provider Parameters",
            TaEngineProviderParamEnum::getAll
    ),
    TA_ENGINE_PARAMS(
            "ta_engine_params",
            "TA Engine Parameters",
            TaEngineParamEnum::getAll
    ),
    TA_ENGINE_ADDITIONAL_PARAMS(
            "ta_engine_additional_params",
            "TA Engine Additional Parameters",
            TaEngineAdditionalParamEnum::getAll
    ),
    TA_ENGINE_DEFAULT_PARAMS(
            "ta_engine_default_params",
            "TA Engine default Parameters",
            TaEngineDefaultParamEnum::getAll
    );

    private final String name;
    private final Supplier<List<? extends TaEngineAbstractParam>> supplier;
    private final ExecutorTemplate template = new ExecutorTemplate();

    ExecutorTemplateEnum(String name, String displayName, Supplier<List<? extends TaEngineAbstractParam>> supplier) {
        this.name = name.toLowerCase(Locale.ENGLISH);
        this.supplier = supplier;
    }

    public ExecutorTemplate getTemplate() {
        return new ExecutorTemplate(
                ExecutorTemplateEnum.TA_ENGINE_PROVIDER_PARAMS.getSupplier().get(),
                ExecutorTemplateEnum.TA_ENGINE_PARAMS.getSupplier().get(),
                ExecutorTemplateEnum.TA_ENGINE_ADDITIONAL_PARAMS.getSupplier().get(),
                ExecutorTemplateEnum.TA_ENGINE_DEFAULT_PARAMS.getSupplier().get()
        );
    }
}
