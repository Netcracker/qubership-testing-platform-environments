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

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.Mockito;
import org.qubership.atp.environments.Main;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.service.direct.ConcurrentModificationService;
import org.qubership.atp.environments.service.direct.ConnectionService;
import org.qubership.atp.environments.service.rest.server.dto.ConnectionDto;
import org.qubership.atp.environments.utils.ResourceAccessor;
import org.qubership.atp.environments.utils.TestEntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
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
public class ConnectionControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ConnectionService connectionService;
    @MockBean
    private ConcurrentModificationService concurrentModificationService;

    ObjectMapper objectMapper = new ObjectMapper();

    private final ResourceAccessor resourceAccessor = new ResourceAccessor(ConnectionControllerTest.class);

    List<Connection> connectionList;

    @BeforeEach
    public void setUp() throws Exception {
        connectionList = TestEntityUtils.createConnectionList();
        Mockito.when(connectionService.getAll()).thenReturn(connectionList);
    }

    @Test
    public void onConnectionController_GetConnections_ArrayConnections() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.
                get("/api/connections")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0]").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("connection1"));
    }

    @Test
    public void onConnectionController_GetAllBy_ArrayConnections(TestInfo testInfo) throws Exception {
        Mockito.when(connectionService.getAll(anyList(), any(UUID.class))).thenReturn(Collections.singletonList(connectionList.get(0)));
        this.mockMvc.perform(MockMvcRequestBuilders.
                post("/api/connections/getAllBy")
                .content(resourceAccessor.readStringFromFilePath(testInfo.getTestMethod().get().getName()))
                .characterEncoding("utf-8")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0]").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("connection1"));
    }

    @Test
    public void onConnectionController_GetById_Connection() throws Exception {
        Mockito.when(connectionService.get(any(UUID.class))).thenReturn(connectionList.get(0));
        this.mockMvc.perform(MockMvcRequestBuilders.
                get("/api/connections/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("connection1"));
    }

    @Test
    public void onConnectionController_CreateConnection_Connection(TestInfo testInfo) throws Exception {
        Mockito.when(connectionService.create(any(UUID.class),
                any(String.class),
                any(),
                any(ConnectionParameters.class),
                any(),
                any(UUID.class),
                any(UUID.class),
                anyList())).thenReturn(connectionList.get(0));
        this.mockMvc.perform(MockMvcRequestBuilders.
                post("/api/connections")
                .content(resourceAccessor.readStringFromFilePath(testInfo.getTestMethod().get().getName()))
                .characterEncoding("utf-8")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("connection1"));
    }

    @Test
    public void onConnectionController_UpdateConnection_Connection(TestInfo testInfo) throws Exception {
        Mockito.when(connectionService.update(any(UUID.class),
                any(UUID.class),
                any(String.class),
                any(),
                any(ConnectionParameters.class),
                any(),
                any(UUID.class),
                any(UUID.class),
                anyList())).thenReturn(connectionList.get(0));
        Mockito.when(concurrentModificationService
                .getConcurrentModificationHttpStatus(any(UUID.class), any(), eq(connectionService))).thenReturn(HttpStatus.OK);
        this.mockMvc.perform(MockMvcRequestBuilders.
                put("/api/connections")
                .content(resourceAccessor.readStringFromFilePath(testInfo.getTestMethod().get().getName()))
                .characterEncoding("utf-8")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("connection1"));
    }

    @Test
    public void onConnectionController_UpdateConnectionParameters_ConnectionList(TestInfo testInfo) throws Exception {
        List<ConnectionDto> connectionDtoList = resourceAccessor.readObjectsFromFilePath(ConnectionDto.class,
                testInfo.getTestMethod().get().getName());
        IntStream.range(0, Math.min(connectionDtoList.size(), connectionList.size()))
                .forEach(index -> {
                    Mockito.when(connectionService.get(connectionList.get(index).getId())).thenReturn(connectionList.get(index));
                    connectionDtoList.get(index).setId(connectionList.get(index).getId());
                });
        this.mockMvc.perform(MockMvcRequestBuilders.
                put("/api/connections/parameters")
                .content(objectMapper.writeValueAsString(connectionDtoList))
                .characterEncoding("utf-8")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0]").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("connection1"));
    }
}
