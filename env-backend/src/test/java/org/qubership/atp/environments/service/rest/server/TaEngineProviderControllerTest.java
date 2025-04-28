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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.qubership.atp.environments.Main;
import org.qubership.atp.environments.enums.TaEngineTypeEnum;
import org.qubership.atp.environments.service.direct.TaEngineProviderService;
import org.qubership.atp.environments.service.rest.server.dto.ExecutorTemplate;
import org.qubership.atp.environments.utils.ResourceAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(SpringExtension.class)
@SpringBootTest()
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = Main.class)
@TestPropertySource("classpath:application-test-rest-api.properties")
@Isolated
public class TaEngineProviderControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private TaEngineProviderService taEngineProviderService;

    ObjectMapper objectMapper = new ObjectMapper();

    private final ResourceAccessor resourceAccessor = new ResourceAccessor(TaEngineProviderControllerTest.class);

    @Test
    public void getAll_PassedRequest_GoodRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.
                get("/api/ta-engine-provider/engine-type")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(TaEngineTypeEnum.getAll())));
    }

    @Test
    public void getTemplate_PassedRequest_GoodRequest(TestInfo testInfo) throws Exception {
        ExecutorTemplate executorTemplate = resourceAccessor
                        .readObjectFromFilePath(ExecutorTemplate.class, testInfo.getTestMethod().get().getName());
        when(taEngineProviderService.getTemplate(eq("Executor"))).thenReturn(executorTemplate);
        mockMvc.perform(MockMvcRequestBuilders.
                get("/api/ta-engine-provider/template/Executor")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(objectMapper.writeValueAsString(executorTemplate)));

    }


}
