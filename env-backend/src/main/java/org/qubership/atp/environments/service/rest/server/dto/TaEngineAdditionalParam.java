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

import java.io.Serializable;

import org.qubership.atp.environments.enums.TaEngineParamSectionEnum;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TaEngineAdditionalParam extends TaEngineAbstractParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Create additional engine param.
     *
     * @param name         - param name
     * @param value        - param value
     * @param businessName - business name of param
     * @param section      - section name where param is placed
     */
    public TaEngineAdditionalParam(String name, String value, String businessName, TaEngineParamSectionEnum section) {
        super(businessName, name, value, section);
    }
}
