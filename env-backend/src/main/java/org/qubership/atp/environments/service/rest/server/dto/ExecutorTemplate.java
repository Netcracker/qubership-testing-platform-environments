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

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExecutorTemplate implements TaEngineTemplate {

    List<TaEngineParam> taEngineProviderParams;
    List<TaEngineParam> taEngineParams;
    List<TaEngineAdditionalParam> additionalParams;
    List<TaEngineDefaultParam> defaultParams;

    /**
     * Create Executor Template.
     *
     * @param taEngineProviderParams - Ta Engine Provider params
     * @param taEngineParams         - Ta Engine params
     * @param additionalParams       - Additional Engine params
     * @param defaultParams          - Default Engine params
     */
    public ExecutorTemplate(List<? extends TaEngineAbstractParam> taEngineProviderParams,
                            List<? extends TaEngineAbstractParam> taEngineParams,
                            List<? extends TaEngineAbstractParam> additionalParams,
                            List<? extends TaEngineAbstractParam> defaultParams) {
        this.taEngineProviderParams = (List<TaEngineParam>) taEngineProviderParams;
        this.taEngineParams = (List<TaEngineParam>) taEngineParams;
        this.additionalParams = (List<TaEngineAdditionalParam>) additionalParams;
        this.defaultParams = (List<TaEngineDefaultParam>) defaultParams;
    }

    public List<TaEngineParam> getTaEngineProviderParams() {
        return taEngineProviderParams;
    }

    public void setTaEngineProviderParams(List<TaEngineParam> taEngineProviderParams) {
        this.taEngineProviderParams = taEngineProviderParams;
    }

    public List<TaEngineParam> getTaEngineParams() {
        return taEngineParams;
    }

    public void setTaEngineParams(List<TaEngineParam> taEngineParams) {
        this.taEngineParams = taEngineParams;
    }

    public List<TaEngineAdditionalParam> getAdditionalParams() {
        return additionalParams;
    }

    public void setAdditionalParams(List<TaEngineAdditionalParam> additionalParams) {
        this.additionalParams = additionalParams;
    }
}
