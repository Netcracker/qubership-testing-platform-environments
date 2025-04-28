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

package org.qubership.atp.environments.service.rest.server.dto;

import org.qubership.atp.environments.enums.TaEngineParamSectionEnum;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TaEngineParam extends TaEngineAbstractParam {

    String defaultValue;
    boolean isMandatory;
    boolean isDisplayed;

    /**
     * Create Ta Engine Param.
     *
     * @param businessName - param business name
     * @param name         - param name
     * @param value        - param value
     * @param defaultValue - default value of param
     * @param isMandatory  - flag that specify whether the parameter is required
     * @param isDisplayed  - flag that specify that the parameter is shown in UI by default
     */
    public TaEngineParam(String businessName, String name, String value, String defaultValue, boolean isMandatory,
                         boolean isDisplayed) {
        super(businessName, name, value, null);
        this.defaultValue = defaultValue;
        this.isMandatory = isMandatory;
        this.isDisplayed = isDisplayed;
    }

    /**
     * Create Ta Engine Param.
     *
     * @param businessName - param business name
     * @param name         - param name
     * @param value        - param value
     * @param defaultValue - default value of param
     * @param isMandatory  - flag that specify whether the parameter is required
     * @param isDisplayed  - flag that specify that the parameter is shown in UI by default
     * @param section      - section name where param is placed
     */
    public TaEngineParam(String businessName, String name, String value, String defaultValue, boolean isMandatory,
                         boolean isDisplayed,
                         TaEngineParamSectionEnum section) {
        super(businessName, name, value, section);
        this.defaultValue = defaultValue;
        this.isMandatory = isMandatory;
        this.isDisplayed = isDisplayed;
    }
}
