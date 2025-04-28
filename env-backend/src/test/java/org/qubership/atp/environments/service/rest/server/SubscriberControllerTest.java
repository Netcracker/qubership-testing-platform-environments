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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.qubership.atp.environments.Main;
import org.qubership.atp.environments.model.Subscriber;
import org.qubership.atp.environments.model.Subscription;
import org.qubership.atp.environments.model.impl.SubscriberImpl;
import org.qubership.atp.environments.model.impl.SubscriptionImpl;
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
public class SubscriberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubscriberController subscriberController;

    private Subscriber subscriberTest;
    private List<Subscription> subscriptionsList = new ArrayList<>();
    private Subscription subscriptionTest;

    @BeforeEach
    public void setUp() {
        subscriberTest = new SubscriberImpl(UUID.randomUUID(), "Test subscriber", "host", 1, "signature", "tagList",
                1, "notificationURL", null, subscriptionsList);
        subscriptionTest = new SubscriptionImpl(UUID.randomUUID(), 1, UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), subscriberTest, 1, null, null);
        subscriptionsList.add(subscriptionTest);
    }

    @Test
    public void onSubscriberController_GetSubscriberByFullParam() throws Exception {
        when(subscriberController.getsubscriber(subscriberTest.getId())).thenReturn(subscriberTest);
        this.mockMvc.perform(MockMvcRequestBuilders.
                get("/api/subscribers/" + subscriberTest.getId()).param("full","")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.subscriptions[0]").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.subscriptions[0].projectId").exists());
    }

    @Test
    public void onSubscriberController_GetSubscriber_ShortViewArraySubscriptions() throws Exception {
        when(subscriberController.getsubscriber(subscriberTest.getId())).thenReturn(subscriberTest);
        this.mockMvc.perform(MockMvcRequestBuilders.
                get("/api/subscribers/" + subscriberTest.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.subscriptions[0]").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.subscriptions[0].projectId").doesNotExist());
    }
}
