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

package api;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.qubership.atp.auth.springbootstarter.config.FeignConfiguration;
import org.qubership.atp.environments.service.rest.client.CatalogFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import au.com.dius.pact.consumer.dsl.PactDslResponse;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.PactProviderRule;
import au.com.dius.pact.consumer.junit.PactVerification;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import configuration.TestAppConfiguration;

@RunWith(SpringRunner.class)
@EnableFeignClients(clients = {CatalogFeignClient.class})
@ContextConfiguration(classes = {TestAppConfiguration.class})
@Import({FeignConfiguration.class, FeignAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class,
        JacksonAutoConfiguration.class})
@TestPropertySource(
        properties = {"feign.atp.catalogue.name=atp-catalogue",
                "feign.atp.catalogue.route=",
                "feign.atp.catalogue.url=http://localhost:8888"
        })

public class ActionsEnvironmentsToCataloguePactUnitTest {

    @Autowired
    CatalogFeignClient catalogFeignClient;

    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("atp-catalogue", "localhost", 8888, this);
    private final UUID actionUUID = UUID.randomUUID();

    @Test
    @PactVerification()
    public void allPass() {
        ResponseEntity<Void> result1 = catalogFeignClient.updateActions(actionUUID);
        Assert.assertEquals(result1.getStatusCode().value(), 200);
    }

    @Pact(consumer = "atp-environments")
    public RequestResponsePact createPact(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        PactDslResponse response = builder
                .given("all ok")
                .uponReceiving("POST /catalog/api/v1/projects/{uuid}/updateActions OK")
                .path("/catalog/api/v1/projects/" + actionUUID + "/updateActions")
                .method("POST")
                .willRespondWith()
                .status(200);

        return response.toPact();
    }
}
