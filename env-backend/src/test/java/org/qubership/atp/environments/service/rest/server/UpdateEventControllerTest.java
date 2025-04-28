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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.qubership.atp.environments.Main;
import org.qubership.atp.environments.model.UpdateEvent;
import org.qubership.atp.environments.model.impl.UpdateEventImpl;
import org.qubership.atp.environments.service.direct.UpdateEventService;
import org.qubership.atp.environments.utils.TestEntityUtils;
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

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(SpringExtension.class)
@SpringBootTest()
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = Main.class)
@TestPropertySource("classpath:application-test-rest-api.properties")
@Isolated
public class UpdateEventControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UpdateEventService updateEventService;

    private List<UpdateEvent> updateEventList;

    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        updateEventList = TestEntityUtils.createUpdateEventList();
    }

    @Test
    public void getEventByEntityIdAndSubscriptionId_PassedRequest_GoodRequest() throws Exception {
        when(updateEventService.get(any(UUID.class), any(UUID.class))).thenReturn(updateEventList.get(0));
        mockMvc.perform(MockMvcRequestBuilders.
                get("/api/updateEvents/subscription/" + UUID.randomUUID() + "/entity/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.entityId").value(updateEventList.get(0).getEntityId().toString().trim()))
                .andExpect(jsonPath("$.subscription.id").value(updateEventList.get(0).getSubscriptionId().toString().trim()));
    }

    @Test
    public void getEventBySubscriptionId_PassedRequest_GoodRequest() throws Exception {
        when(updateEventService.getSubscriptionUpdateEvents(any(UUID.class))).thenReturn(updateEventList.get(0));
        mockMvc.perform(MockMvcRequestBuilders.
                get("/api/updateEvents/subscription/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.entityId").value(updateEventList.get(0).getEntityId().toString().trim()))
                .andExpect(jsonPath("$.subscription.id").value(updateEventList.get(0).getSubscriptionId().toString().trim()));
    }

    @Test
    public void getEventByEntityId_PassedRequest_GoodRequest() throws Exception {
        when(updateEventService.getEntityUpdateEvents(any(UUID.class))).thenReturn(updateEventList.get(0));
        mockMvc.perform(MockMvcRequestBuilders.
                get("/api/updateEvents/entity/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.entityId").value(updateEventList.get(0).getEntityId().toString().trim()))
                .andExpect(jsonPath("$.subscription.id").value(updateEventList.get(0).getSubscriptionId().toString().trim()));
    }

    @Test
    public void getAllEvents_PassedRequest_GoodRequest() throws Exception {
        when(updateEventService.getAll()).thenReturn(updateEventList);
        mockMvc.perform(MockMvcRequestBuilders.
                get("/api/updateEvents")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[0].entityId").value(updateEventList.get(0).getEntityId().toString().trim()))
                .andExpect(jsonPath("$[0].subscription.id").value(updateEventList.get(0).getSubscriptionId().toString().trim()));
    }

    @Test
    public void createEvent_PassedRequest_GoodRequest() throws Exception {
        when(updateEventService.create(any(UUID.class), any(UUID.class),
                anyString(), anyInt(), anyString())).thenReturn(updateEventList.get(0));
        ((UpdateEventImpl)updateEventList.get(0)).setSubscription(null);
        mockMvc.perform(MockMvcRequestBuilders.
                post("/api/updateEvents")
                .content(objectMapper.writeValueAsString(updateEventList.get(0)))
                .characterEncoding("utf-8")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void updateEvent_PassedRequest_GoodRequest() throws Exception {
        ((UpdateEventImpl)updateEventList.get(0)).setSubscription(null);
        mockMvc.perform(MockMvcRequestBuilders.
                put("/api/updateEvents/subscription/" + UUID.randomUUID() + "/entity/" + UUID.randomUUID())
                .content(objectMapper.writeValueAsString(updateEventList.get(0)))
                .characterEncoding("utf-8")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());
    }
}
