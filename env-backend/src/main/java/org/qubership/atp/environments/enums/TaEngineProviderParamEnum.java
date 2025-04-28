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

public enum TaEngineProviderParamEnum {
    PROVIDER_URL("Provider_URL", "http://atp-dealer:8080", true, false),
    PROVIDER_ENGINE_TYPE("Provider_Engine_Type", "NTT", true, false),
    ACQUIRE_CREATE_TOOL_PATH("Acquire_Create_Tool_Path", "/ntt", true, false),
    ACQUIRE_CREATE_TOOL_HTTP_METHOD("Acquire_Create_Tool_HTTP_Method", "Post", true, false),
    ACQUIRE_CONTENT_TYPE("Acquire_Content_Type", "application/json", true, false),
    ACQUIRE_CREATE_TOOL_REQUEST_BODY("Acquire_Create_Tool_Request_Body", "", true, false),
    ENGINE_SERVICE_URL_JSON_PATH("Engine_Service_URL_JSON_Path", "$.url", true, false),
    RELEASE_TOOL_PATH("Release_Tool_Path", "/${$.id}", false, false),
    RELEASE_TOOL_HTTP_METHOD("Release_Tool_HTTP_Method", "Delete", false, false);

    private TaEngineParam param;

    TaEngineProviderParamEnum(String name, String defaultValue, boolean isMandatory, boolean isDisplayed) {
        this.param = new TaEngineParam(name, name, "", defaultValue, isMandatory, isDisplayed);
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
                .map(TaEngineProviderParamEnum::getParam)
                .collect(Collectors.toList());
    }
}
