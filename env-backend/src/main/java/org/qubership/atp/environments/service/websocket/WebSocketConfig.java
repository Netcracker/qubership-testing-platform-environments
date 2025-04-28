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

import java.util.Arrays;

import org.qubership.atp.integration.configuration.interceptors.MdcChannelInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final String WS_API = "/ws/api";

    private final MdcChannelInterceptor mdcChannelInterceptor;

    @Value("${ws.inbound.channel.thread.pool.core-pool-size}")
    private Integer wsInboundCorePoolSize;
    @Value("${ws.inbound.channel.thread.pool.max-pool-size}")
    private Integer wsInboundMaxPoolSize;
    @Value("${ws.inbound.channel.thread.pool.queue-capacity}")
    private Integer wsInboundQueueCapacity;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        Arrays.stream(WebSocketEventType.values())
                .forEach(eventType -> config.enableSimpleBroker(eventType.getDestinationPrefix()));
        config.setApplicationDestinationPrefixes(WS_API);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(WS_API).setAllowedOrigins("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(wsInboundCorePoolSize);
        threadPoolTaskExecutor.setMaxPoolSize(wsInboundMaxPoolSize);
        threadPoolTaskExecutor.setQueueCapacity(wsInboundQueueCapacity);
        registration.interceptors(mdcChannelInterceptor);
        registration.taskExecutor(threadPoolTaskExecutor);
    }
}
