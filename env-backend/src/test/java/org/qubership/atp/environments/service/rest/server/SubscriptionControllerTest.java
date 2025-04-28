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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.Mockito;
import org.qubership.atp.environments.Main;
import org.qubership.atp.environments.model.Subscription;
import org.qubership.atp.environments.model.impl.SubscriptionImpl;
import org.qubership.atp.environments.service.direct.SubscriptionService;
import org.qubership.atp.environments.service.rest.server.dto.SubscriptionDto;
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
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(SpringExtension.class)
@SpringBootTest()
@AutoConfigureMockMvc()
@ContextConfiguration(classes = Main.class)
@TestPropertySource("classpath:application-test-rest-api.properties")
@EnableWebMvc
@Isolated
public class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private SubscriptionService subscriptionService;

    ObjectMapper objectMapper = new ObjectMapper();

    private List<SubscriptionImpl> subscriptionImpls;

    private List<SubscriptionDto> subscriptionDtoList;

    private List<Subscription> subscriptions;

    private final ResourceAccessor resourceAccessor = new ResourceAccessor(SubscriptionControllerTest.class);

    @BeforeEach
    public void setUp() throws Exception {
        subscriptionImpls = resourceAccessor.readObjectsFromFilePath(SubscriptionImpl.class, "allSubscriptions");
        subscriptionDtoList = resourceAccessor.readObjectsFromFilePath(SubscriptionDto.class, "allSubscriptionDtoList");
        subscriptions = new ArrayList<>();
        subscriptions.add(subscriptionImpls.get(0));
    }

    @Test
    public void getSubscription_PassedRequest_GoodRequest() throws Exception {
        Mockito.when(subscriptionService.get(any(UUID.class))).thenReturn(subscriptionImpls.get(0));
        mockMvc.perform(MockMvcRequestBuilders.
                get("/api/subscriptions/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(subscriptionImpls.get(0).getStatus()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.subscriptionType").value(subscriptionImpls.get(0).getSubscriptionType()));
    }

    @Test
    public void createSubscription_PassedRequest_GoodRequest() throws Exception {
        Mockito.when(subscriptionService.create(any(),any(),any(),any(),any(),any(),any())).thenReturn(subscriptionImpls.get(0));
        this.mockMvc.perform(MockMvcRequestBuilders.
                post("/api/subscriptions")
                .content(objectMapper.writeValueAsString(subscriptionDtoList.get(0)))
                .characterEncoding("utf-8")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(subscriptionImpls.get(0).getStatus()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.subscriptionType").value(subscriptionImpls.get(0).getSubscriptionType()));
    }

    @Test
    public void getAll_PassedRequest_GoodRequest() throws Exception {

        Mockito.when(subscriptionService.getAll())
                .thenReturn(subscriptions);
        mockMvc.perform(MockMvcRequestBuilders.
                get("/api/subscriptions")
                .accept("application/json")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0]").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].status").value(subscriptionImpls.get(0).getStatus()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].subscriptionType").value(subscriptionImpls.get(0).getSubscriptionType()));
    }

    @Test
    public void getSubscriberSubscriptions_PassedRequest_GoodRequest() throws Exception {
        Mockito.when(subscriptionService.getSubscriberSubscriptions(any(UUID.class))).thenReturn(subscriptions);
        mockMvc.perform(MockMvcRequestBuilders.
                get("/api/subscriptions/subscriber/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0]").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].status").value(subscriptionImpls.get(0).getStatus()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].subscriptionType").value(subscriptionImpls.get(0).getSubscriptionType()));
    }

    @Test
    public void getProjectSubscriptions_PassedRequest_GoodRequest() throws Exception {
        Mockito.when(subscriptionService.getProjectSubscriptions(any(UUID.class))).thenReturn(subscriptions);
        mockMvc.perform(MockMvcRequestBuilders.
                get("/api/subscriptions/project/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0]").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].status").value(subscriptionImpls.get(0).getStatus()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].subscriptionType").value(subscriptionImpls.get(0).getSubscriptionType()));
    }

    @Test
    public void getEnvironmentSubscriptions_PassedRequest_GoodRequest() throws Exception {
        Mockito.when(subscriptionService.getEnvironmentSubscriptions(any(UUID.class))).thenReturn(subscriptions);
        mockMvc.perform(MockMvcRequestBuilders.
                get("/api/subscriptions/environment/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0]").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].status").value(subscriptionImpls.get(0).getStatus()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].subscriptionType").value(subscriptionImpls.get(0).getSubscriptionType()));
    }

    @Test
    public void getSystemSubscriptions_PassedRequest_GoodRequest() throws Exception {
        Mockito.when(subscriptionService.getSystemSubscriptions(any(UUID.class))).thenReturn(subscriptions);
        mockMvc.perform(MockMvcRequestBuilders.
                get("/api/subscriptions/system/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0]").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].status").value(subscriptionImpls.get(0).getStatus()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].subscriptionType").value(subscriptionImpls.get(0).getSubscriptionType()));
    }

}
