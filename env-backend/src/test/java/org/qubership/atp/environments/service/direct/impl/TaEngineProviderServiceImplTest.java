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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.qubership.atp.environments.service.rest.server.dto.ExecutorTemplate;
import org.qubership.atp.environments.service.rest.server.dto.NewmanTemplate;

public class TaEngineProviderServiceImplTest {

    TaEngineProviderServiceImpl taEngineProviderService = new TaEngineProviderServiceImpl();

    @Test
    public void getTemplate_NewmanGot() {
        Assertions.assertEquals(taEngineProviderService.getTemplate("newman").getClass(), NewmanTemplate.class);
    }

    @Test
    public void getTemplate_ExecutorGot() {
        Assertions.assertEquals(taEngineProviderService.getTemplate("executor").getClass(), ExecutorTemplate.class);
    }

    @Test
    public void getTemplate_catchException() {
        Assertions.assertThrows(RuntimeException.class, () -> taEngineProviderService.getTemplate("exekutor"));
    }


}
