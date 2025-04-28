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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.model.impl.ConnectionImpl;

public class SsmVersionCheckerTest {

    private final ThreadLocal<HttpEntity> httpEntity = new ThreadLocal<>();
    private final ThreadLocal<CloseableHttpClient> httpClient = new ThreadLocal<>();
    private final ThreadLocal<FileInputStream> inputStream = new ThreadLocal<>();
    private final ThreadLocal<SsmVersionChecker> versionChecker = new ThreadLocal<>();

    private static Connection connectionModel;
    private static final String solutionAlias = "solution";
    private static final String instanceAlias = "instance";

    @BeforeAll
    public static void init() {
        connectionModel = new ConnectionImpl();
        connectionModel.setParameters(new ConnectionParameters(){{
            put("url", "http://localhost");
            put("login", "login");
            put("password", "password");
        }});
    }

    @BeforeEach
    public void setUp() throws Exception {
        CloseableHttpClient httpClientMock = Mockito.mock(CloseableHttpClient.class);
        HttpEntity httpEntityMock = Mockito.mock(HttpEntity.class);
        CloseableHttpResponse closeableHttpResponse = Mockito.mock(CloseableHttpResponse.class);
        when(closeableHttpResponse.getEntity()).thenReturn(httpEntityMock);
        when(httpClientMock.execute(notNull())).thenReturn(closeableHttpResponse);
        SsmVersionChecker versionCheckerThread = new SsmVersionChecker(httpClientMock);
        versionCheckerThread.setConnectionParameters(connectionModel);
        versionCheckerThread.setSsmSolutionAlias(solutionAlias);
        versionCheckerThread.setSsmInstanceAlias(instanceAlias);

        httpEntity.set(httpEntityMock);
        httpClient.set(httpClientMock);
        inputStream.set(new FileInputStream(Paths.get("src/test/resources/ssmVersionCheckerResponse.json").toFile()));
        versionChecker.set(versionCheckerThread);
    }

    @AfterEach
    public void close() throws IOException {
        inputStream.get().close();
    }

    @Test
    public void getVersion() throws IOException {
        versionChecker.get().setSystemName("access-control");
        when(httpEntity.get().getContent()).thenReturn(inputStream.get());
        Assertions.assertEquals("0.0.1", versionChecker.get().getVersion());
        ArgumentCaptor<HttpUriRequest> argument = ArgumentCaptor.forClass(HttpUriRequest.class);
        verify(httpClient.get()).execute(argument.capture());
        assertEquals("/ssm-backend/api/v1/solution/" + solutionAlias + "/instance/"
                        + instanceAlias + "/microservice",
                argument.getValue().getURI().getPath());
    }

    @Test
    public void getVersion_versionNotProvided() throws IOException {
        versionChecker.get().setSystemName("Account Management Core");
        when(httpEntity.get().getContent()).thenReturn(inputStream.get());
        Assertions.assertEquals("Unknown", versionChecker.get().getVersion());
    }

    @Test
    public void getVersion_microserviceNotFound() throws IOException {
        versionChecker.get().setSystemName("Unknown_service");
        when(httpEntity.get().getContent()).thenReturn(inputStream.get());
        Assertions.assertEquals("Unknown", versionChecker.get().getVersion());
    }

}
