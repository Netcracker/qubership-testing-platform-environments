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

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.Mockito;
import org.qubership.atp.environments.Main;
import org.qubership.atp.environments.model.SystemCategory;
import org.qubership.atp.environments.service.direct.SystemCategoriesService;
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
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = Main.class)
@TestPropertySource("classpath:application-test-rest-api.properties")
@EnableWebMvc
@Isolated
public class SystemCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private SystemCategoriesService systemCategoriesService;

    ObjectMapper objectMapper = new ObjectMapper();

    private List<SystemCategory> systemCategories;

    private final ResourceAccessor resourceAccessor = new ResourceAccessor(SystemCategoryControllerTest.class);
    @BeforeEach
    public void setUp() throws Exception {
        systemCategories = resourceAccessor.readObjectsFromFilePath(SystemCategory.class, "allCategories");
    }

    @Test
    public void getAll_PassedRequest_GoodRequest() throws Exception {
        Mockito.when(systemCategoriesService.getAll()).thenReturn(systemCategories);
        mockMvc.perform(MockMvcRequestBuilders.
                get("/api/system-categories")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0]").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].created").value(systemCategories.get(0).getCreated()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(systemCategories.get(0).getName()));
    }

    @Test
    public void getAllShort_PassedRequest_GoodRequest() throws Exception {
        Mockito.when(systemCategoriesService.getAll()).thenReturn(systemCategories);
        mockMvc.perform(MockMvcRequestBuilders.
                get("/api/system-categories/short")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0]").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(systemCategories.get(0).getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].created").doesNotExist());
    }

    @Test
    public void getCategory_PassedRequest_GoodRequest() throws Exception {
        Mockito.when(systemCategoriesService.get(any(UUID.class))).thenReturn(systemCategories.get(0));
        mockMvc.perform(MockMvcRequestBuilders.
                get("/api/system-categories/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(systemCategories.get(0).getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.created").value(systemCategories.get(0).getCreated()));
    }

    @Test
    public void createCategory_PassedRequest_GoodRequest() throws Exception {
        Mockito.when(systemCategoriesService.create(any(),any())).thenReturn(systemCategories.get(0));
        this.mockMvc.perform(MockMvcRequestBuilders.
                post("/api/system-categories")
                .content(objectMapper.writeValueAsString(systemCategories.get(0)))
                .characterEncoding("utf-8")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(systemCategories.get(0).getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.created").value(systemCategories.get(0).getCreated()));
    }

    @Test
    public void updateCategory_PassedRequest_GoodRequest() throws Exception {
        Mockito.when(systemCategoriesService.update(any(UUID.class),
                any(),any())).thenReturn(systemCategories.get(0));
        mockMvc.perform(MockMvcRequestBuilders.
                put("/api/system-categories")
                .content(objectMapper.writeValueAsString(systemCategories.get(0)))
                .accept("application/json")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(systemCategories.get(0).getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.created").value(systemCategories.get(0).getCreated()));
    }

}
