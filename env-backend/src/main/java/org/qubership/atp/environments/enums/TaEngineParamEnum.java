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

import org.qubership.atp.environments.service.rest.server.dto.TaEngineParam;

public enum TaEngineParamEnum {
    image("Image", "image", "", true, true, null),
    name("Name", "name", "", true, true, null),
    version("Version", "-version", "", false, true, TaEngineParamSectionEnum.ARGS);

    private TaEngineParam param;

    TaEngineParamEnum(String businessName, String name, String defaultValue, boolean isMandatory, boolean isDisplayed,
                      TaEngineParamSectionEnum section) {
        this.param = new TaEngineParam(businessName, name, "", defaultValue, isMandatory, isDisplayed, section);
    }

    public TaEngineParam getParam() {
        return param;
    }

    /**
     * Return all {@link TaEngineParam}.
     *
     * @return list of TA Engine Provider params
     */
    public static List<TaEngineParam> getAll() {
        return Arrays.stream(values())
                .map(TaEngineParamEnum::getParam)
                .collect(Collectors.toList());
    }
}
