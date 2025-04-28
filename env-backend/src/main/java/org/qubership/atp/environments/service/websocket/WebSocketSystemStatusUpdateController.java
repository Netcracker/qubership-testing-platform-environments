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

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.qubership.atp.auth.springbootstarter.feign.exception.FeignClientException;
import org.qubership.atp.environments.enums.MdcField;
import org.qubership.atp.integration.configuration.mdc.MdcUtils;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@MessageMapping("/websocket")
@Slf4j
@RequiredArgsConstructor
public class WebSocketSystemStatusUpdateController {

    private final WebSocketSystemStatusService webSocketSystemStatusService;

    /**
     * Send request to Healthcheck, which checks provided environment.
     *
     * @param systemStatusRequest should have {@code @NonNull} values of all fields
     */
    @MessageMapping("/system")
    public void synchronizationActionUpdateProcess(@Payload SystemStatusCheckRequest systemStatusRequest) {
        if (systemStatusRequest != null) {
            MdcUtils.put(MdcField.ENVIRONMENT_ID.toString(), systemStatusRequest.getEnvironmentId());
            MdcUtils.put(MdcField.SYSTEM_ID.toString(), systemStatusRequest.getSystemId());
        log.info("Request to check status for {}", systemStatusRequest);
        try {
            webSocketSystemStatusService.processRequest(systemStatusRequest);
        } catch (FeignClientException e) {
            log.error("Feign client exception in WS controller /system, msg: {}", e.getErrorMessage());
            throw e;
        }
        }
    }

    /**
     * Handle exception for web socket requests.
     *
     * @param e       handled exception
     * @param headers ws headers
     */
    @MessageExceptionHandler
    @SendTo("/websocket/system/response/error")
    public MultiErrorResponse<String> handleException(Exception e, @Nullable @Headers Map<String, Object> headers) {
        log.error("Error during handling socket message. headers {}", headers, e);
        final String msg;
        if (e instanceof FeignClientException) {
            msg = ((FeignClientException) e).getErrorMessage();
        } else {
            msg = e.getMessage() != null && !e.getMessage().equals("null")
                    ? e.getMessage()
                    : "Please contact Administrator";
        }
        List<String> errors = Collections.singletonList("Cannot complete healthcheck. " + msg);
        HttpStatus responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        String requestPath = headers == null
                ? "Headers are null"
                : String.valueOf(headers.getOrDefault(SimpMessageHeaderAccessor.DESTINATION_HEADER,
                "Unknown request path"));
       return new MultiErrorResponse<>(responseStatus.value(), requestPath, errors);
    }

    @Data
    @NoArgsConstructor
    private static class ErrorResponse {

        protected int status;
        protected String path;
        protected Date timestamp = new Date();

        ErrorResponse(int status, String path) {
            this.status = status;
            this.path = path;
        }
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class MultiErrorResponse<T> extends ErrorResponse {

        private Collection<T> errors;

        public MultiErrorResponse(int status, String path, Collection<T> errors) {
            super(status, path);
            this.errors = errors;
        }
    }
}
