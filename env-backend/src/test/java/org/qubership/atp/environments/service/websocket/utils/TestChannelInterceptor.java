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

package org.qubership.atp.environments.service.websocket.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

/**
 * A ChannelInterceptor that caches messages.
 */
public class TestChannelInterceptor implements ChannelInterceptor {

    private final BlockingQueue<Message<?>> messages = new ArrayBlockingQueue<>(100);

    private final List<String> destinationPatterns = new ArrayList<>();

    private final PathMatcher matcher = new AntPathMatcher();

    public void setIncludedDestinations(String... patterns) {
        this.destinationPatterns.addAll(Arrays.asList(patterns));
    }

    /**
     * @return the next received message or {@code null} if the specified time elapses
     */
    public Message<?> awaitMessage(long timeoutInSeconds) throws InterruptedException {
        return this.messages.poll(timeoutInSeconds, TimeUnit.SECONDS);
    }

    @Override
    public Message<?> preSend(@Nonnull Message<?> message, @Nonnull MessageChannel channel) {
        if (this.destinationPatterns.isEmpty()) {
            this.messages.add(message);
        } else {
            StompHeaderAccessor headers = StompHeaderAccessor.wrap(message);
            if (headers.getDestination() != null) {
                for (String pattern : this.destinationPatterns) {
                    if (this.matcher.match(pattern, headers.getDestination())) {
                        this.messages.add(message);
                        break;
                    }
                }
            }
        }
        return message;
    }
}