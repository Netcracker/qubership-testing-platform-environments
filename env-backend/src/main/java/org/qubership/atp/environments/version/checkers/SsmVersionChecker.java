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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.qubership.atp.environments.model.Connection;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Qualifier("ssmVersionChecker")
@RequiredArgsConstructor
@SuppressWarnings("CPD-START")
public class SsmVersionChecker implements VersionChecker {

    @Setter
    private String systemName;
    @Setter
    private String ssmSolutionAlias;
    @Setter
    private String ssmInstanceAlias;
    private String url;
    private String login;
    private String password;

    private final CloseableHttpClient httpClient;

    private static final String basePath = "/ssm-backend/api/v1/solution/%s/instance/%s/microservice";
    private static final String emptyVersion = "info_not_available";

    @Override
    public void setConnectionParameters(Connection parameters) {
        this.url = parameters.getParameters().get("url");
        this.login = parameters.getParameters().get("login");
        this.password = parameters.getParameters().get("password");
    }

    @Override
    public void setParametersVersionCheck(String parameters) {
    }

    @Override
    public String getVersion() {
        String version = "";
        try {
            log.info("Get version for {} microservice from SSM by solution: {} and instance: {}", systemName,
                    ssmSolutionAlias, ssmInstanceAlias);
            HttpGet httpRequest = new HttpGet(Validate.notNull(url)
                    + String.format(basePath, ssmSolutionAlias, ssmInstanceAlias));
            setAuthHeaders(httpRequest);
            httpRequest.addHeader("x-client-application-name", "NCECARE");
            HttpResponse response = httpClient.execute(httpRequest);
            HttpEntity entity = response.getEntity();
            String content = EntityUtils.toString(entity);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode microservicesArrayNode = mapper.readTree(content);
            if (microservicesArrayNode.isArray()) {
                for (JsonNode microserviceNode : microservicesArrayNode) {
                    if (microserviceNode.get("name").asText().equals(systemName)) {
                        String ver = microserviceNode.get("runTimeVersion").asText();
                        if (!emptyVersion.equals(ver)) {
                            log.info("System found microservice {} with version {}", systemName, ver);
                            version = ver;
                        } else {
                            log.info("System found {} microservice but version not provided", systemName);
                        }
                        break;
                    }
                }
            } else {
                log.warn("Not valid response from SSM server");
            }
        } catch (IOException | PathNotFoundException e) {
            log.error(e.getMessage());
        }
        return version.isEmpty() ? "Unknown" : version;
    }

    private void setAuthHeaders(HttpGet httpRequest) {
        String auth = this.login + ":" + this.password;
        byte[] encodedAuth = Base64.encodeBase64(
                auth.getBytes(StandardCharsets.UTF_8));
        String authHeader = "Basic " + new String(encodedAuth, StandardCharsets.UTF_8);
        httpRequest.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
    }
}
