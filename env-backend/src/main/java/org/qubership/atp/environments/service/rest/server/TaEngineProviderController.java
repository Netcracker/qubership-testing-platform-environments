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

package org.qubership.atp.environments.service.rest.server;

import java.util.List;

import org.qubership.atp.environments.enums.TaEngineTypeEnum;
import org.qubership.atp.environments.service.direct.TaEngineProviderService;
import org.qubership.atp.environments.service.rest.server.dto.TaEngineTemplate;
import org.qubership.atp.environments.service.rest.server.dto.TaEngineType;
import org.qubership.atp.integration.configuration.configuration.AuditAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/ta-engine-provider")
@RestController
public class TaEngineProviderController {

    private final TaEngineProviderService taEngineProviderService;

    @Autowired
    public TaEngineProviderController(TaEngineProviderService taEngineProviderService) {
        this.taEngineProviderService = taEngineProviderService;
    }

    @GetMapping("/engine-type")
    @AuditAction(auditAction = "Get all TA engine type")
    public List<TaEngineType> getAll() {
        return TaEngineTypeEnum.getAll();
    }

    @GetMapping("/template/{engineType}")
    @AuditAction(auditAction = "Get TA engine template by engine type {{#engineType}}")
    public TaEngineTemplate getTemplate(@PathVariable("engineType") String engineType) {
        return taEngineProviderService.getTemplate(engineType);
    }
}
