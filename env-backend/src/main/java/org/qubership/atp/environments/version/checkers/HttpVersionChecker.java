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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.qubership.atp.environments.model.Connection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.jayway.jsonpath.PathNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("CPD-START")
public class HttpVersionChecker implements VersionChecker {

    private static final Pattern BUILD_VERSION_PATTERN = Pattern.compile("build_number:\\s*(.*?)\\s*\\n?$",
            Pattern.MULTILINE);
    private static final Pattern BUILD_VERSION_PORTAL = Pattern.compile("Build number is\\s*(.*?)\\s*\\n?<[^>]*>$",
            Pattern.MULTILINE);
    private String url;
    private String login;
    private String password;
    private Boolean auth = false;
    private String parameters = "";
    private String jsonHeaders = "";
    private final CloseableHttpClient httpClient;

    @Override
    public void setConnectionParameters(Connection parameters) {
        this.url = parameters.getParameters().get("url");
        this.login = parameters.getParameters().get("login");
        this.password = parameters.getParameters().get("password");
    }

    @Override
    public void setParametersVersionCheck(String parameters) {
        this.parameters = parameters;
    }

    public void setJsonHeaders(String jsonHeaders) {
        this.jsonHeaders = jsonHeaders;
    }

    public void setAuthorization() {
        this.auth = true;
    }

    @Override
    public String getVersion() {
        List<String> version = new ArrayList<>();
        try {
            HttpGet httpRequest = new HttpGet(Validate.notNull(url).concat(parameters));
            httpRequest.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            if (this.auth) {
                if (!StringUtils.isEmpty(jsonHeaders)) {
                    HashMap<String, String> headers = parseJsonHeaders();
                    if (!headers.containsKey(HttpHeaders.AUTHORIZATION.toLowerCase(Locale.ENGLISH))) {
                        setAuthHeaders(httpRequest);
                    }
                    if (!headers.containsKey("x-client-application-name")) {
                        httpRequest.addHeader("x-client-application-name", "NCECARE");
                    }
                    for (Map.Entry<String, String> header : headers.entrySet()) {
                        httpRequest.addHeader(header.getKey(), header.getValue());
                    }
                } else {
                    setAuthHeaders(httpRequest);
                    httpRequest.addHeader("x-client-application-name", "NCECARE");
                }
            }
            HttpResponse response = httpClient.execute(httpRequest);
            HttpEntity entity = response.getEntity();
            String content = EntityUtils.toString(entity);
            if (!content.contains("<!DOCTYPE html")) {
                if ("/version_history.txt".equals(parameters)
                        || "/version.txt".equals(parameters)) {
                    version.addAll(parseVersions(content, BUILD_VERSION_PATTERN));
                } else if (parameters.matches(".*portal-info.jsp")) {
                    version.addAll(parseVersions(content, BUILD_VERSION_PORTAL));
                } else {
                    version.add(content);
                }
            }
            return !version.isEmpty() ? version.get(0) : "Unknown";
        } catch (IOException | PathNotFoundException | ParseException e) {
            log.error(e.getMessage());
        }
        return !version.isEmpty() ? version.toString() : "Unknown";
    }

    private List<String> parseVersions(String versionBody, Pattern pattern) {
        List<String> versions = new ArrayList<>();
        if (versionBody.toLowerCase(Locale.ENGLISH).contains("html")) {
            versions.add(versionBody);
        }
        Matcher matcher = pattern.matcher(versionBody);
        while (matcher.find()) {
            versions.add(matcher.group(1));
        }
        Collections.reverse(versions);
        return versions;
    }

    private void setAuthHeaders(HttpGet httpRequest) {
        String auth = this.login + ":" + this.password;
        byte[] encodedAuth = Base64.encodeBase64(
                auth.getBytes(StandardCharsets.UTF_8));
        String authHeader = "Basic " + new String(encodedAuth, StandardCharsets.UTF_8);
        httpRequest.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
    }

    private HashMap<String, String> parseJsonHeaders() throws ParseException {
        HashMap<String, String> headers = new HashMap<>();
        if (!StringUtils.isEmpty(jsonHeaders)) {
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(jsonHeaders);
            JSONArray headersArray = (JSONArray) jsonObject.get("headers");
            for (Object header : headersArray) {
                JSONObject jsonHeader = (JSONObject) header;
                String name = ((String) jsonHeader.get("name")).toLowerCase(Locale.ENGLISH);
                String body = (String) jsonHeader.get("body");
                headers.put(name, body);
            }
        }
        return headers;
    }
}
