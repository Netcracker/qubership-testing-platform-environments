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

import org.qubership.atp.environments.service.rest.server.dto.TaEngineDefaultParam;

public enum TaEngineDefaultParamEnum implements Serializable {

    BROWSER_OPTION_1("Browser Option.1", "chrome.option.1", "--no-sandbox", TaEngineParamSectionEnum.ARG_LINE),
    BROWSER_OPTION_2("Browser Option.2", "chrome.option.2", "--disable-gpu=true", TaEngineParamSectionEnum.ARG_LINE),
    BROWSER_OPTION_3("Browser Option.3", "chrome.option.3", "--ignore-certificate-errors",
            TaEngineParamSectionEnum.ARG_LINE),
    BROWSER_OPTION_4("Browser Option.4", "chrome.option.4", "--reduce-security-for-testing",
            TaEngineParamSectionEnum.ARG_LINE),
    WEBDRIVER_VERSION("Webdriver Version", "webdriver.capabilities.version", "73.0", TaEngineParamSectionEnum.ARG_LINE),
    JAVA_OPTS("JAVA_OPTS", "JAVA_OPTS",
            "-XX:MaxRAM=2G -Xms512m -Xmx1800m -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005",
            TaEngineParamSectionEnum.ENV);

    private TaEngineDefaultParam param;

    TaEngineDefaultParamEnum(String businessName, String name, String value, TaEngineParamSectionEnum section) {
        this.param = new TaEngineDefaultParam(name, value, businessName, section);
    }

    public TaEngineDefaultParam getParam() {
        return param;
    }

    public static List<TaEngineDefaultParam> getAll() {
        return Arrays.stream(values())
                .map(TaEngineDefaultParamEnum::getParam)
                .collect(Collectors.toList());
    }
}
