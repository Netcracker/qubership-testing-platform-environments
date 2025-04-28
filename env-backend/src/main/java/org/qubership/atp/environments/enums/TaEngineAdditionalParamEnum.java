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

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.qubership.atp.environments.service.rest.server.dto.TaEngineAdditionalParam;

public enum TaEngineAdditionalParamEnum implements Serializable {

    NEXUS("Nexus", "-nexus", TaEngineParamSectionEnum.ARGS),
    SVN("SVN", "-svn", TaEngineParamSectionEnum.ARGS),
    GIT("Git", "-git", TaEngineParamSectionEnum.ARGS),
    CP("CP", "-cp", TaEngineParamSectionEnum.ARGS),
    WEBDRIVER_NAME("Webdriver Name", "webdriver.capabilities.name", TaEngineParamSectionEnum.ARG_LINE),
    HUB_URL("Hub URL", "webdriver.hub.url", TaEngineParamSectionEnum.ARG_LINE),
    WEBDRIVER_VERSION("Webdriver Version", "webdriver.capabilities.version", TaEngineParamSectionEnum.ARG_LINE),
    WEBDRIVER_TIMEZONE("Webdriver Timezone", "webdriver.capabilities.timeZone", TaEngineParamSectionEnum.ARG_LINE);


    private TaEngineAdditionalParam param;

    TaEngineAdditionalParamEnum(String businessName, String name, TaEngineParamSectionEnum section) {
        this.param = new TaEngineAdditionalParam(name, "", businessName, section);
    }

    public TaEngineAdditionalParam getParam() {
        return param;
    }

    public static List<TaEngineAdditionalParam> getAll() {
        return Arrays.stream(values())
                .map(TaEngineAdditionalParamEnum::getParam)
                .collect(Collectors.toList());
    }
}
