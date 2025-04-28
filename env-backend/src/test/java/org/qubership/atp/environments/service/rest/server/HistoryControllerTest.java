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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.Mockito;
import org.qubership.atp.environments.Main;
import org.qubership.atp.environments.service.rest.server.dto.generated.CompareEntityResponseDtoGenerated;
import org.qubership.atp.environments.service.rest.server.dto.generated.HistoryItemResponseDtoGenerated;
import org.qubership.atp.environments.utils.ResourceAccessor;
import org.qubership.atp.environments.versioning.model.entities.SystemJ;
import org.qubership.atp.environments.versioning.service.HistoryServiceFactory;
import org.qubership.atp.environments.versioning.service.JaversHistoryService;
import org.qubership.atp.environments.versioning.service.JaversRestoreServiceFactory;
import org.qubership.atp.environments.versioning.service.impl.SystemVersionHistoryService;
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

@ExtendWith(SpringExtension.class)
@SpringBootTest()
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = Main.class)
@TestPropertySource("classpath:application-test-rest-api.properties")
@Isolated
public class HistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private JaversHistoryService javersHistoryService;
    @MockBean
    private JaversRestoreServiceFactory javersRestoreServiceFactory;
    @MockBean
    private SystemVersionHistoryService service;
    @MockBean
    private HistoryServiceFactory historyServiceFactory;

    private final ResourceAccessor resourceAccessor = new ResourceAccessor(HistoryControllerTest.class);


    @Test
    public void onHistoryController_GetAllSystemHistory_HistoryResponse(TestInfo testInfo) throws Exception {
        Mockito.when(javersHistoryService
                .getAllHistory(any(UUID.class), eq(SystemJ.class), any(), any()))
                .thenReturn(resourceAccessor.readObjectFromFilePath(HistoryItemResponseDtoGenerated.class,
                        testInfo.getTestMethod().get().getName()));
        this.mockMvc.perform(MockMvcRequestBuilders.
                get("/api/history/" + UUID.randomUUID() + "/system/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.pageInfo").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.historyItems").exists());
    }

    @Test
    public void onHistoryController_GetAllSystemHistory_BadRequest() throws Exception {
        javersHistoryService = null;
        this.mockMvc.perform(MockMvcRequestBuilders.
                get("/api/history/" + UUID.randomUUID() + "/system/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    public void onHistoryController_GetEntitiesByVersion_HistoryResponse(TestInfo testInfo) throws Exception {
        Mockito.when(service.getEntitiesByVersion(any(UUID.class), anyList())).thenReturn(resourceAccessor
                .readObjectsFromFilePath(CompareEntityResponseDtoGenerated.class, testInfo.getTestMethod().get().getName()));
        Mockito.when(historyServiceFactory.getHistoryService(any())).thenReturn(Optional.of(service));
        this.mockMvc.perform(MockMvcRequestBuilders.
                get("/api/history/version/" + UUID.randomUUID()
                        + "/system/" + UUID.randomUUID()
                        + "/revision/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].compareEntity").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].revision").exists());
    }

    @Test
    public void onHistoryController_GetEntitiesByVersion_BadRequest() throws Exception {
        Mockito.when(historyServiceFactory.getHistoryService(any())).thenReturn(Optional.empty());
        this.mockMvc.perform(MockMvcRequestBuilders.
                get("/api/history/version/" + UUID.randomUUID()
                        + "/system/" + UUID.randomUUID()
                        + "/revision/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    public void onHistoryController_restoreToRevision_BadRequest() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.
                get("/api/history/version/" + UUID.randomUUID()
                        + "/systemm/" + UUID.randomUUID()
                        + "/revision/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }
}
