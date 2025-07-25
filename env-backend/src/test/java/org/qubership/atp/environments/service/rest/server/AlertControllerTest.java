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
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.qubership.atp.environments.Main;
import org.qubership.atp.environments.model.Alert;
import org.qubership.atp.environments.model.impl.AlertImpl;
import org.qubership.atp.environments.service.direct.AlertService;
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
 * AlertControllerTest - test for{@link AlertController}
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = Main.class)
@TestPropertySource("classpath:application-test-rest-api.properties")
@Isolated
public class AlertControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AlertService alertService;

    private Alert alert;

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
    }

    @Test
    public void getAlert_PassedRequest_GoodRequest() throws Exception {
        when(alertService.get(any(UUID.class))).thenReturn(alert);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/alerts/{alertId}", alert.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(alert.getId().toString()))
                .andExpect(jsonPath("name").value(alert.getName()))
                .andExpect(jsonPath("shortDescription").value(alert.getShortDescription()))
                .andExpect(jsonPath("tagList").value(alert.getTagList()))
                .andExpect(jsonPath("parameters").value(alert.getParameters()))
                .andExpect(jsonPath("status").value(alert.getStatus()))
                .andExpect(jsonPath("created").value(alert.getCreated()))
                .andExpect(jsonPath("subscriberId").value(alert.getSubscriberId().toString()));
    }

    @Test
    public void getAll_PassedRequest_GoodRequest() throws Exception {
        List<Alert> alerts = Collections.singletonList(alert);
        when(alertService.getAll()).thenReturn(alerts);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/alerts/")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(alert.getId().toString()))
                .andExpect(jsonPath("$[0].name").value(alert.getName()))
                .andExpect(jsonPath("$[0].shortDescription").value(alert.getShortDescription()))
                .andExpect(jsonPath("$[0].tagList").value(alert.getTagList()))
                .andExpect(jsonPath("$[0].parameters").value(alert.getParameters()))
                .andExpect(jsonPath("$[0].status").value(alert.getStatus()))
                .andExpect(jsonPath("$[0].created").value(alert.getCreated()))
                .andExpect(jsonPath("$[0].subscriberId").value(alert.getSubscriberId().toString()));
    }

    @Test
    public void create_PassedRequest_GoodRequest() throws Exception {
        when(alertService.create(anyString(), anyString(), anyString(), anyString(), any(UUID.class),
                anyInt())).thenReturn(alert);
        String requestJson = objectMapper.writer().writeValueAsString(alert);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/alerts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(alert.getId().toString()))
                .andExpect(jsonPath("name").value(alert.getName()))
                .andExpect(jsonPath("shortDescription").value(alert.getShortDescription()))
                .andExpect(jsonPath("tagList").value(alert.getTagList()))
                .andExpect(jsonPath("parameters").value(alert.getParameters()))
                .andExpect(jsonPath("status").value(alert.getStatus()))
                .andExpect(jsonPath("created").value(alert.getCreated()))
                .andExpect(jsonPath("subscriberId").value(alert.getSubscriberId().toString()));
    }

    @Test
    public void update_PassedRequest_GoodRequest() throws Exception {
        doNothing().when(alertService).update(any(AlertImpl.class));
        String requestJson = objectMapper.writer().writeValueAsString(alert);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/alerts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNoContent());
    }

    @Test
    public void update_ThrowException_AlertIdEmpty() throws Exception {
        Alert alert = new AlertImpl();
        doNothing().when(alertService).update(any(AlertImpl.class));
        String requestJson = objectMapper.writer().writeValueAsString(alert);

        Exception exception = mockMvc.perform(MockMvcRequestBuilders.put("/api/alerts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().is(500))
                .andReturn()
                .getResolvedException();
        Assertions.assertTrue(exception.getMessage().contains("Alert id can't be empty"));
    }

    @Test
    public void update_ThrowException_SubscriberIdEmpty() throws Exception {
        Alert alert = new AlertImpl();
        alert.setId(UUID.randomUUID());
        doNothing().when(alertService).update(any(AlertImpl.class));
        String requestJson = objectMapper.writer().writeValueAsString(alert);

        Exception exception = mockMvc.perform(MockMvcRequestBuilders.put("/api/alerts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().is(500))
                .andReturn()
                .getResolvedException();
        Assertions.assertTrue(exception.getMessage().contains("Subscriber id can't be empty"));
    }

    @Test
    public void delete_PassedRequest_GoodRequest() throws Exception {
        doNothing().when(alertService).delete(any(UUID.class));

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/alerts/{alertId}", UUID.randomUUID()))
                .andExpect(status().isNoContent());
    }
}
