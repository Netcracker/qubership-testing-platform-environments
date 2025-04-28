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

package org.qubership.atp.environments.service.websocket;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.qubership.atp.auth.springbootstarter.feign.exception.FeignClientException;
import org.qubership.atp.environments.service.websocket.config.WebSocketSystemStatusTestConfig;
import org.qubership.atp.environments.service.websocket.utils.TestChannelInterceptor;
import org.qubership.atp.integration.configuration.configuration.LoggingHelpersConfiguration;
import org.qubership.atp.integration.configuration.configuration.MdcInterceptorsHelperConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.AbstractSubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {WebSocketSystemStatusUpdateController.class, LoggingHelpersConfiguration.class,
        MdcInterceptorsHelperConfiguration.class,
        WebSocketConfig.class,
        WebSocketSystemStatusTestConfig.class})
@ActiveProfiles("WebSocketTesting")
@Isolated
public class WebSocketSystemStatusUpdateControllerTest {

    @MockBean
    private WebSocketSystemStatusService socketService;

    @Autowired
    private AbstractSubscribableChannel clientInboundChannel;

    @Autowired
    private AbstractSubscribableChannel clientOutboundChannel;

    @Autowired
    private AbstractSubscribableChannel brokerChannel;

    private TestChannelInterceptor clientOutboundChannelInterceptor;
    private TestChannelInterceptor brokerChannelInterceptor;

    @BeforeEach
    public void setUp() throws Exception {
        this.brokerChannelInterceptor = new TestChannelInterceptor();
        this.clientOutboundChannelInterceptor = new TestChannelInterceptor();
        this.brokerChannel.addInterceptor(this.brokerChannelInterceptor);
        this.clientOutboundChannel.addInterceptor(this.clientOutboundChannelInterceptor);
    }

    @Test
    public void checkHealthSmoke_shouldBeAvailableController_whenConfigured() throws JsonProcessingException {
        // prepare test data
        SystemStatusCheckRequest request = SystemStatusCheckRequest.builder()
                .projectId(UUID.fromString("ea2be7c4-b9f2-4d63-a4b1-5d94075fcc9f"))
                .environmentId(UUID.fromString("9ec28f70-046b-4f02-b29f-2c999a7fa4d8"))
                .build();
        byte[] payload = new ObjectMapper().writeValueAsBytes(request);
        StompHeaderAccessor headers = StompHeaderAccessor.create(StompCommand.SEND);
        headers.setDestination("/ws/api/websocket/system");
        headers.setSessionId("0");
        headers.setSessionAttributes(new HashMap<>());
        Message<byte[]> message = MessageBuilder.createMessage(payload, headers.getMessageHeaders());
        // mock
        doAnswer(a -> "").when(socketService).processRequest(request);
        // send
        this.clientInboundChannel.send(message);
    }

    @Test
    public void checkHealth_shouldReturnError_whenWebsocketServiceThrewFeignClientException()
            throws JsonProcessingException, InterruptedException {
        // expect
        String expectedWsPath = "/websocket/system/response/error";
        // prepare test data
        SystemStatusCheckRequest request = SystemStatusCheckRequest.builder()
                .projectId(UUID.fromString("ea2be7c4-b9f2-4d63-a4b1-5d94075fcc9f"))
                .environmentId(UUID.fromString("9ec28f70-046b-4f02-b29f-2c999a7fa4d8"))
                .build();
        byte[] payload = new ObjectMapper().writeValueAsBytes(request);
        StompHeaderAccessor headers = StompHeaderAccessor.create(StompCommand.SEND);
        headers.setDestination("/ws/api/websocket/system");
        headers.setSessionId("0");
        headers.setSessionAttributes(new HashMap<>());
        Message<byte[]> message = MessageBuilder.createMessage(payload, headers.getMessageHeaders());
        // mock
        String mockError = "Healthcheck service not available";
        Map<String, Collection<String>> mockHeaders = new HashMap<String, Collection<String>>() {{
            put("Content-type", Collections.singletonList("application/json"));
        }};
        FeignClientException expectedException = new FeignClientException(500, mockError, Request.HttpMethod.POST,
                mockHeaders, Request.create(Request.HttpMethod.POST,"testurl", Collections.emptyMap(),
                null,null,null));
        doThrow(expectedException).when(socketService).processRequest(request);
        // send
        this.clientInboundChannel.send(message);
        Message<?> actualException = this.brokerChannelInterceptor.awaitMessage(10);
        Assertions.assertNotNull(actualException);
        Assertions.assertEquals(expectedWsPath, actualException.getHeaders().get("simpDestination"));
    }

    @Test
    public void checkHealth_shouldReturnError_whenWebsocketServiceThrewRuntimeException()
            throws JsonProcessingException, InterruptedException {
        // expect
        String expectedWsPath = "/websocket/system/response/error";
        // prepare test data
        SystemStatusCheckRequest request = SystemStatusCheckRequest.builder()
                .projectId(UUID.fromString("ea2be7c4-b9f2-4d63-a4b1-5d94075fcc9f"))
                .environmentId(UUID.fromString("9ec28f70-046b-4f02-b29f-2c999a7fa4d8"))
                .build();
        byte[] payload = new ObjectMapper().writeValueAsBytes(request);
        StompHeaderAccessor headers = StompHeaderAccessor.create(StompCommand.SEND);
        headers.setDestination("/ws/api/websocket/system");
        headers.setSessionId("0");
        headers.setSessionAttributes(new HashMap<>());
        Message<byte[]> message = MessageBuilder.createMessage(payload, headers.getMessageHeaders());
        // mock
        RuntimeException mockException = new RuntimeException("Healthcheck service not available");
        doThrow(mockException).when(socketService).processRequest(request);
        // send
        this.clientInboundChannel.send(message);
        Message<?> actualException = this.brokerChannelInterceptor.awaitMessage(10);
        Assertions.assertNotNull(actualException);
        Assertions.assertEquals(expectedWsPath, actualException.getHeaders().get("simpDestination"));
    }
}
