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

import java.util.Locale;

import org.qubership.atp.environments.enums.ExecutorTemplateEnum;
import org.qubership.atp.environments.enums.TaEngineTypeEnum;
import org.qubership.atp.environments.errorhandling.taengine.EnvironmentIllegalTaEngineTemplateException;
import org.qubership.atp.environments.service.direct.TaEngineProviderService;
import org.qubership.atp.environments.service.rest.server.dto.NewmanTemplate;
import org.qubership.atp.environments.service.rest.server.dto.TaEngineTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("taEngineProviderService")
public class TaEngineProviderServiceImpl implements TaEngineProviderService {

    @Override
    public TaEngineTemplate getTemplate(String engineType) {
        switch (TaEngineTypeEnum.valueOf(engineType.toUpperCase(Locale.ENGLISH))) {
            case NEWMAN:
                return new NewmanTemplate();
            case EXECUTOR:
                return ExecutorTemplateEnum.TA_ENGINE_PARAMS.getTemplate();
            default:
                log.error("Failed to find TA Engine Template by specified type: {}", engineType);
                throw new EnvironmentIllegalTaEngineTemplateException(engineType);
        }
    }
}
