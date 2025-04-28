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

package org.qubership.atp.environments.version.checkers;

import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.stream.Stream;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.model.impl.ConnectionImpl;

public class HttpVersionCheckerTest {

    private final ThreadLocal<HttpVersionChecker> versionChecker = new ThreadLocal<>();
    private final ThreadLocal<HttpEntity> httpEntity = new ThreadLocal<>();
    private static final String test_version = "Test Version";

    @BeforeEach
    public void setUp() throws Exception {
        ConnectionImpl connectionModel = new ConnectionImpl();
        connectionModel.setParameters(new ConnectionParameters());
        connectionModel.getParameters().putAll(
                Stream.of(new String[][]{
                        {"url", "http://host"},
                        {"login", "login"},
                        {"password", "password"}
                }).collect(HashMap::new, (m, v) -> m.put(v[0], v[1]), HashMap::putAll));

        CloseableHttpClient httpClient = Mockito.mock(CloseableHttpClient.class);
        HttpEntity httpEntityMock = Mockito.mock(HttpEntity.class);
        CloseableHttpResponse closeableHttpResponse = Mockito.mock(CloseableHttpResponse.class);
        when(closeableHttpResponse.getEntity()).thenReturn(httpEntityMock);
        when(httpClient.execute(notNull())).thenReturn(closeableHttpResponse);
        HttpVersionChecker versionCheckerThread = new HttpVersionChecker(httpClient);
        versionCheckerThread.setConnectionParameters(connectionModel);
        httpEntity.set(httpEntityMock);
        versionChecker.set(versionCheckerThread);
    }

    private InputStream getEntityInputStream(String result) {
        return new ByteArrayInputStream(result.getBytes());
    }

    @Test
    public void getVersion_PassedVersionReceived_VersionIsntEmpty() throws Exception {
        when(httpEntity.get().getContent()).thenReturn(getEntityInputStream(test_version));
        Assertions.assertEquals(test_version, versionChecker.get().getVersion());
    }

    @Test
    public void getVersion_PassedVersionReceived_EmptyVersion() throws Exception {
        when(httpEntity.get().getContent()).thenReturn(getEntityInputStream(""));
        Assertions.assertEquals("", versionChecker.get().getVersion());
    }

    @Test
    public void getVersion_PassedVersionReceived_HtmlVersion() throws Exception {
        when(httpEntity.get().getContent()).thenReturn(getEntityInputStream("<!DOCTYPE html"));
        Assertions.assertEquals("Unknown", versionChecker.get().getVersion());
    }

    @Test
    public void getVersion_PassedVersionReceived_txtVersion() throws Exception {
        versionChecker.get().setParametersVersionCheck("/version.txt");
        when(httpEntity.get().getContent()).thenReturn(getEntityInputStream("9.3.NC.ATP.CD94 build_number:32_9.3.NC.ATP.CD94_rev13157"));
        Assertions.assertEquals("32_9.3.NC.ATP.CD94_rev13157", versionChecker.get().getVersion());
    }

    @Test
    public void getVersion_PassedVersionReceived_portalInfo() throws Exception {
        versionChecker.get().setParametersVersionCheck("portal-info.jsp");
        when(httpEntity.get().getContent()).thenReturn(getEntityInputStream("Build number is32_9.3.NC.ATP.CD94_rev13157<123>"));
        Assertions.assertEquals("32_9.3.NC.ATP.CD94_rev13157", versionChecker.get().getVersion());
    }

}
