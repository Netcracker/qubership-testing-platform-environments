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

package org.qubership.atp.environments.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.ssl.SSLContexts;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpUtils {

    private static final Charset charset = StandardCharsets.UTF_8;

    /**
     * Use for extract String from response object.
     *
     * @param httpResponse response object
     */
    public static String getResponse(ClassicHttpResponse httpResponse, ClassicHttpRequest httpRequest)
            throws URISyntaxException {
        String result = null;
        HttpEntity httpEntity;
        if (httpResponse != null) {
            httpEntity = httpResponse.getEntity();
            if (httpEntity != null) {
                try {
                    result = IOUtils.toString(httpEntity.getContent(), charset);
                } catch (IOException e) {
                    log.error("Can't read http entity. Method[{}]. URI[{}]. Status Code[{}]", httpRequest.getMethod(),
                            httpRequest.getUri(), httpResponse.getCode(), e);
                }
            }
        }
        return result;
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    public static String convertToHtml(HttpEntity httpEntity) {
        StringBuilder result = new StringBuilder();
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(httpEntity.getContent(),
                StandardCharsets.UTF_8))) {
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException ex) {
            log.error("Error on convert to html", ex);
        }
        return result.toString();
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    public static HttpClientBuilder createTrustAllHttpClientBuilder() {
        try {
            // The below #1-4 steps code is written by KAG using AI assistance, after upgrading to httpclient5
            // 1. Create an SSLContext that trusts all certificates
            SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, TrustAllStrategy.INSTANCE)
                    .build();

            // 2. Create an SSLConnectionSocketFactory using the SSLContext
            SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                    .setSslContext(sslContext)
                    .build();

            // 3. Create a ConnectionManager and set the SSL socket factory on it
            PoolingHttpClientConnectionManager connectionManager =
                    PoolingHttpClientConnectionManagerBuilder.create()
                            .setSSLSocketFactory(sslSocketFactory)
                            .build();

            // 4. Build the HttpClient and set the custom ConnectionManager
            return HttpClients.custom()
                    .setConnectionManager(connectionManager)
                    .setConnectionManagerShared(true); // Important for resource management
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error on creating HttpClient with trust-all SSL strategy.", e);
            return HttpClientBuilder.create();
        }
    }
}
