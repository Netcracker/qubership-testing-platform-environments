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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.qubership.atp.environments.Main;
import org.qubership.atp.environments.model.AlertEvent;
import org.qubership.atp.environments.model.impl.AlertEventImpl;
import org.qubership.atp.environments.model.impl.AlertImpl;
import org.qubership.atp.environments.service.direct.AlertEventService;
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

/**
 * AlertEventControllerTest - test for{@link AlertEventController}
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = Main.class)
@TestPropertySource("classpath:application-test-rest-api.properties")
@Isolated
public class AlertEventControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AlertEventService alertEventService;

    private AlertEvent alertEvent;
    private AlertImpl alert;

    @BeforeEach
    public void setUp() {
        alert = new AlertImpl(UUID.randomUUID(),
                "TestAlert",
                "Test Entity",
                "string",
                "string",
                UUID.randomUUID(),
                1,
                1635189430738L);
        alertEvent = new AlertEventImpl(
                alert,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "testTagList",
                1,
                1635189430738L);
    }

    @Test
    public void getAlertEvent_RequestPassed_GoodRequest() throws Exception {
        when(alertEventService.get(any(UUID.class), any(UUID.class))).thenReturn(alertEvent);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/alertEvents/alert/{alertId}/entity/{entityId}",
                                UUID.randomUUID(),
                                UUID.randomUUID())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entityId").value(alertEvent.getEntityId().toString()))
                .andExpect(jsonPath("$.tagList").value(alertEvent.getTagList()))
                .andExpect(jsonPath("$.status").value(alertEvent.getStatus()))
                .andExpect(jsonPath("$.lastUpdated").value(alertEvent.getLastUpdated()))
                .andExpect(jsonPath("$.alertId").value(alertEvent.getAlertId().toString()))
                .andExpect(jsonPath("$.alert.id").value((alert.getId().toString())))
                .andExpect(jsonPath("$.alert.name").value(alert.getName()))
                .andExpect(jsonPath("$.alert.shortDescription").value(alert.getShortDescription()))
                .andExpect(jsonPath("$.alert.tagList").value(alert.getTagList()))
                .andExpect(jsonPath("$.alert.parameters").value(alert.getParameters()))
                .andExpect(jsonPath("$.alert.status").value(alert.getStatus()))
                .andExpect(jsonPath("$.alert.created").value(alert.getCreated()))
                .andExpect(jsonPath("$.alert.subscriberId").value(alert.getSubscriberId().toString()));
    }

    @Test
    public void getAll_RequestPassed_GoodRequest() throws Exception {
        when(alertEventService.getAll()).thenReturn(Collections.singletonList(alertEvent));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/alertEvents")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].entityId").value(alertEvent.getEntityId().toString()))
                .andExpect(jsonPath("$[0].tagList").value(alertEvent.getTagList()))
                .andExpect(jsonPath("$[0].status").value(alertEvent.getStatus()))
                .andExpect(jsonPath("$[0].lastUpdated").value(alertEvent.getLastUpdated()))
                .andExpect(jsonPath("$[0].alertId").value(alertEvent.getAlertId().toString()))
                .andExpect(jsonPath("$[0].alert.id").value((alert.getId().toString())))
                .andExpect(jsonPath("$[0].alert.name").value(alert.getName()))
                .andExpect(jsonPath("$[0].alert.shortDescription").value(alert.getShortDescription()))
                .andExpect(jsonPath("$[0].alert.tagList").value(alert.getTagList()))
                .andExpect(jsonPath("$[0].alert.parameters").value(alert.getParameters()))
                .andExpect(jsonPath("$[0].alert.status").value(alert.getStatus()))
                .andExpect(jsonPath("$[0].alert.created").value(alert.getCreated()))
                .andExpect(jsonPath("$[0].alert.subscriberId").value(alert.getSubscriberId().toString()));
    }

    @Test
    public void create_RequestPassed_GoodRequest() throws Exception {
        AlertEvent alertEventToCreate = new AlertEventImpl();
        alertEventToCreate.setAlertId(UUID.randomUUID());
        alertEventToCreate.setEntityId(UUID.randomUUID());
        alertEventToCreate.setTagList("testTagList");
        alertEventToCreate.setStatus(1);
        alertEventToCreate.setLastUpdated(1635189430738L);
        when(alertEventService.create(any(UUID.class), any(UUID.class), anyString(), anyInt())).thenReturn(alertEvent);
        String requestJson = objectMapper.writer().writeValueAsString(alertEventToCreate);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/alertEvents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entityId").value(alertEvent.getEntityId().toString()))
                .andExpect(jsonPath("$.tagList").value(alertEvent.getTagList()))
                .andExpect(jsonPath("$.status").value(alertEvent.getStatus()))
                .andExpect(jsonPath("$.lastUpdated").value(alertEvent.getLastUpdated()))
                .andExpect(jsonPath("$.alertId").value(alertEvent.getAlertId().toString()))
                .andExpect(jsonPath("$.alert.id").value((alert.getId().toString())))
                .andExpect(jsonPath("$.alert.name").value(alert.getName()))
                .andExpect(jsonPath("$.alert.shortDescription").value(alert.getShortDescription()))
                .andExpect(jsonPath("$.alert.tagList").value(alert.getTagList()))
                .andExpect(jsonPath("$.alert.parameters").value(alert.getParameters()))
                .andExpect(jsonPath("$.alert.status").value(alert.getStatus()))
                .andExpect(jsonPath("$.alert.created").value(alert.getCreated()))
                .andExpect(jsonPath("$.alert.subscriberId").value(alert.getSubscriberId().toString()));
    }

    @Test
    public void update_RequestPassed_GoodRequest() throws Exception {
        AlertEvent alertEventToCreate = new AlertEventImpl();
        alertEventToCreate.setAlertId(UUID.randomUUID());
        alertEventToCreate.setEntityId(UUID.randomUUID());
        alertEventToCreate.setTagList("testTagList");
        alertEventToCreate.setStatus(1);
        alertEventToCreate.setLastUpdated(1635189430738L);
        doNothing().when(alertEventService).update(any(UUID.class), any(UUID.class), anyString(), anyInt());
        String requestJson = objectMapper.writer().writeValueAsString(alertEventToCreate);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/alertEvents/alert/{alertId}/entity/{entityId}",
                                UUID.randomUUID(),
                                UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNoContent());
    }

    @Test
    public void update_ThrowException_EntityIdEmpty() throws Exception {
        AlertEvent alertEventToCreate = new AlertEventImpl();
        doNothing().when(alertEventService).update(any(UUID.class), any(UUID.class), anyString(), anyInt());
        String requestJson = objectMapper.writer().writeValueAsString(alertEventToCreate);

        Exception exception =
                mockMvc.perform(MockMvcRequestBuilders.put("/api/alertEvents/alert/{alertId}/entity/{entityId}",
                                        UUID.randomUUID(),
                                        UUID.randomUUID())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                        .andExpect(status().is(500))
                        .andReturn()
                        .getResolvedException();
        Assertions.assertTrue(exception.getMessage().contains("Entity id can't be empty"));
    }

    @Test
    public void update_ThrowException_AlertIdEmpty() throws Exception {
        AlertEvent alertEventToCreate = new AlertEventImpl();
        alertEventToCreate.setEntityId(UUID.randomUUID());
        doNothing().when(alertEventService).update(any(UUID.class), any(UUID.class), anyString(), anyInt());
        String requestJson = objectMapper.writer().writeValueAsString(alertEventToCreate);

        Exception exception =
                mockMvc.perform(MockMvcRequestBuilders.put("/api/alertEvents/alert/{alertId}/entity/{entityId}",
                                        UUID.randomUUID(),
                                        UUID.randomUUID())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                        .andExpect(status().is(500))
                        .andReturn()
                        .getResolvedException();
        Assertions.assertTrue(exception.getMessage().contains("Alert id can't be empty"));
    }

    @Test
    public void delete_RequestPassed_GoodRequest() throws Exception {
        doNothing().when(alertEventService).delete(any(UUID.class), any(UUID.class));

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/alertEvents/alert/{alertId}/entity/{entityId}",
                        UUID.randomUUID(),
                        UUID.randomUUID()))
                .andExpect(status().isNoContent());
    }
}
