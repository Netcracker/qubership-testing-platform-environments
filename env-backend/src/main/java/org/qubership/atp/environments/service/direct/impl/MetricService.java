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

package org.qubership.atp.environments.service.direct.impl;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricService {

    private final MeterRegistry meterRegistry;

    private static final String IP_ADDR = "ip_address";
    private static final String URI = "uri";
    private static final String REQUESTS_TOTAL = "requests_by_ip";

    /**
     * Registers new IP address, increments counter.
     *
     * @param ipAddr IP address of client or service.
     * @param uri    requested endpoint.
     */
    public void requestToService(String ipAddr, String uri) {
        incrementMetric(REQUESTS_TOTAL, ipAddr, uri);
    }

    private void incrementMetric(String name, String ipAddr, String uri) {
        meterRegistry.counter(name,
                IP_ADDR, ipAddr,
                URI, uri).increment();
    }

    public Timer checkVersionTimer(String... tags) {
        return meterRegistry.timer("atp.environments.check.version.duration", tags);
    }
}
