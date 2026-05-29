/*
 * # Copyright 2024-2026 NetCracker Technology Corporation
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
import java.util.Objects;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.qubership.atp.auth.springbootstarter.config.FeignConfiguration;
import org.qubership.atp.environments.clients.api.healthcheck.dto.SystemStatusDto;
import org.qubership.atp.environments.service.rest.client.HealthcheckFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslResponse;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import configuration.TestAppConfiguration;

@EnableFeignClients(clients = {HealthcheckFeignClient.class})
@ExtendWith(PactConsumerTestExt.class)
@SpringBootTest
@ActiveProfiles("disable-security")
@SpringJUnitConfig(classes = {TestAppConfiguration.class})
@Import({JacksonAutoConfiguration.class,
        HttpMessageConvertersAutoConfiguration.class,
        FeignConfiguration.class,
        FeignAutoConfiguration.class})
@TestPropertySource(
        properties = {"feign.atp.healthcheck.name=atp-healthcheck",
                "feign.atp.healthcheck.route=",
                "feign.atp.healthcheck.url=http://localhost:8888"
        })
@PactTestFor(providerName = "atp-healthcheck", port = "8888", pactVersion = PactSpecVersion.V3)
public class HealthcheckStatusFeignClientPactUnitTest {

    @Autowired
    HealthcheckFeignClient healthcheckFeignClient;

    @Test
    @PactTestFor(pactMethod = "createPact")
    public void allPass() {
        UUID projectId = UUID.fromString("c2737427-05e4-4c17-8032-455539deaa01");
        UUID environmentId = UUID.fromString("c2737427-05e4-4c17-8032-455539deaa02");
        UUID systemId = UUID.fromString("c2737427-05e4-4c17-8032-455539deaa03");

        ResponseEntity<SystemStatusDto> expectedSystemStatusDto = healthcheckFeignClient
                .checkSystem(projectId.toString(), environmentId.toString(), systemId.toString(), null, null, null);
        Assertions.assertEquals(200, expectedSystemStatusDto.getStatusCode().value());
        Assertions.assertTrue(Objects.requireNonNull(expectedSystemStatusDto.getHeaders().get("Content-Type"))
                .contains("application/json"));
    }

    @Pact(consumer = "atp-environments")
    public RequestResponsePact createPact(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        DslPart resultDataSetStorage = new PactDslJsonBody()
                .uuid("systemId")
                .stringType("name")
                .minArrayLike("processStatuses", 0, constructDslPart())
                .minArrayLike("configurationStatuses", 0, constructDslPart())
                .minArrayLike("connectionStatuses", 0, new PactDslJsonBody()
                        .stringType("connectionId")
                        .stringType("name")
                        .stringType("host")
                        .minArrayLike("processStatuses", 0, constructDslPart())
                        .minArrayLike("configurationStatuses", 0, constructDslPart()));

        PactDslResponse response = builder
                .given("all ok")
                .uponReceiving("GET /rest/status/system/{projectId}/{environmentId}/{systemId} OK")
                .path("/rest/status/system/c2737427-05e4-4c17-8032-455539deaa01/c2737427-05e4-4c17-8032-455539deaa02/c2737427-05e4-4c17-8032-455539deaa03")
                .method("GET")
                .willRespondWith()
                .status(200)
                .headers(headers)
                .body(resultDataSetStorage);

        return response.toPact();
    }

    private DslPart constructDslPart() {
        return new PactDslJsonBody()
                .stringType("process")
                .stringType("actual")
                .stringType("expected")
                .booleanType("isHtml")
                .booleanType("notAffectStatus")
                .booleanType("isMandatory")
                .array("attachments").closeArray();
    }
}
