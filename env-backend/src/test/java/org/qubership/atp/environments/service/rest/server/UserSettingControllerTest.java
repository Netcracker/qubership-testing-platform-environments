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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.qubership.atp.auth.springbootstarter.entities.UserInfo;
import org.qubership.atp.auth.springbootstarter.ssl.Provider;
import org.qubership.atp.environments.Main;
import org.qubership.atp.environments.model.UserSetting;
import org.qubership.atp.environments.model.impl.UserSettingImpl;
import org.qubership.atp.environments.service.direct.UserSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * UserSettingControllerTest - test for {@link UserSettingController}
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = Main.class)
@TestPropertySource("classpath:application-test-rest-api.properties")
@Isolated
class UserSettingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserSettingService userSettingService;
    @MockBean
    private Provider<UserInfo> userInfoProvider;

    UserInfo userInfo;

    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
            userInfo = new UserInfo();
            userInfo.setId(UUID.randomUUID());
            userInfo.setUsername("Username");
            userInfo.setFirstName("Firstname");
            userInfo.setLastName("Lastname");
            userInfo.setEmail("Email@some-domain.com");
            when(userInfoProvider.get()).thenReturn(userInfo);
    }

    @Test
    void get_GetUserIdFromToken_gotInfo() throws Exception {
        when(userSettingService.get(any())).thenReturn(new UserSettingImpl(UUID.randomUUID(), "TAGS"));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/usersettings")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.view").value("TAGS"));
    }

    @Test
    void create_WithFilledBody_createdInfo() throws Exception {
        mockMvcPerform("TAGS", "/api/usersettings", "POST");
    }

    @Test
    void update_WithFilledBody_updatedInfo() throws Exception {
        mockMvcPerform("TAGS", "/api/usersettings", "PUT");
    }

    private void mockMvcPerform(String view, String endpoint, String requestMethod) throws Exception {
        UserSetting userSetting = new UserSettingImpl(UUID.randomUUID(), view);
        MockHttpServletRequestBuilder requestBuilder;
        if ("POST".equals(requestMethod)) {
            when(userSettingService.create(any(UUID.class), any())).thenReturn(userSetting);
            requestBuilder = MockMvcRequestBuilders.post(endpoint);
        } else {
            when(userSettingService.update(any(UUID.class), any())).thenReturn(userSetting);
            requestBuilder = MockMvcRequestBuilders.put(endpoint);
        }
        requestBuilder
                .content(objectMapper.writeValueAsString(userSetting))
                .characterEncoding("utf-8")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.view").value(view));
    }
}
