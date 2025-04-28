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
import org.qubership.atp.environments.clients.api.healthcheck.dto.SystemStatusDto;
import org.qubership.atp.environments.service.rest.client.HealthcheckFeignClient;
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

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslResponse;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.PactProviderRule;
import au.com.dius.pact.consumer.junit.PactVerification;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import configuration.TestAppConfiguration;

@RunWith(SpringRunner.class)
@EnableFeignClients(clients = {HealthcheckFeignClient.class})
@ContextConfiguration(classes = {TestAppConfiguration.class})
@Import({JacksonAutoConfiguration.class,
        HttpMessageConvertersAutoConfiguration.class,
        FeignConfiguration.class,
        FeignAutoConfiguration.class})
@TestPropertySource(
        properties = {"feign.atp.healthcheck.name=atp-healthcheck",
                "feign.atp.healthcheck.route=",
                "feign.atp.healthcheck.url=http://localhost:8888"
        })
public class HealthcheckStatusFeignClientPactUnitTest {
    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("atp-healthcheck", "localhost", 8888, this);
    @Autowired
    HealthcheckFeignClient healthcheckFeignClient;

    @Test
    @PactVerification()
    public void allPass() {
        UUID projectId = UUID.fromString("c2737427-05e4-4c17-8032-455539deaa01");
        UUID environmentId = UUID.fromString("c2737427-05e4-4c17-8032-455539deaa02");
        UUID systemId = UUID.fromString("c2737427-05e4-4c17-8032-455539deaa03");

        ResponseEntity<SystemStatusDto> expectedSystemStatusDto = healthcheckFeignClient
                .checkSystem(projectId.toString(), environmentId.toString(), systemId.toString(), null, null, null);
        Assert.assertEquals(expectedSystemStatusDto.getStatusCode().value(), 200);
        Assert.assertTrue(expectedSystemStatusDto.getHeaders().get("Content-Type").contains("application/json"));
    }

    @Pact(consumer = "atp-enviroments")
    public RequestResponsePact createPact(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        DslPart resultDataSetStorage = new PactDslJsonBody()
                .uuid("systemId")
                .stringType("name")
                .minArrayLike("processStatuses", 0, new PactDslJsonBody()
                        .stringType("process")
                        .stringType("actual")
                        .stringType("expected")
                        .booleanType("isHtml")
                        .booleanType("notAffectStatus")
                        .booleanType("isMandatory")
                        .array("attachments").closeArray())
                .minArrayLike("configurationStatuses", 0, new PactDslJsonBody()
                        .stringType("process")
                        .stringType("actual")
                        .stringType("expected")
                        .booleanType("isHtml")
                        .booleanType("notAffectStatus")
                        .booleanType("isMandatory")
                        .array("attachments").closeArray())
                .minArrayLike("connectionStatuses", 0, new PactDslJsonBody()
                        .stringType("connectionId")
                        .stringType("name")
                        .stringType("host")
                        .minArrayLike("processStatuses", 0, new PactDslJsonBody()
                                .stringType("process")
                                .stringType("actual")
                                .stringType("expected")
                                .booleanType("isHtml")
                                .booleanType("notAffectStatus")
                                .booleanType("isMandatory")
                                .array("attachments").closeArray())
                        .minArrayLike("configurationStatuses", 0, new PactDslJsonBody()
                                .stringType("process")
                                .stringType("actual")
                                .stringType("expected")
                                .booleanType("isHtml")
                                .booleanType("notAffectStatus")
                                .booleanType("isMandatory")
                                .array("attachments").closeArray()))
                ;

        PactDslResponse response = builder
                .given("all ok")
                .uponReceiving("GET /rest/status/system/{projectId}/{environmentId}/{systemId} OK")
                .path("/rest/status/system/c2737427-05e4-4c17-8032-455539deaa01/c2737427-05e4-4c17-8032-455539deaa02/c2737427-05e4-4c17-8032-455539deaa03")
                .method("GET")
                .willRespondWith()
                .status(200)
                .headers(headers)
                .body(resultDataSetStorage)
                ;

        return response.toPact();
    }
}
