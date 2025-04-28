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

package org.qubership.atp.environments.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpUtils {

    private static final Charset charset = StandardCharsets.UTF_8;

    /**
     * Use for extract String from response object.
     *
     * @param httpResponse response object
     */
    public static String getResponse(HttpResponse httpResponse, HttpRequest httpRequest) {
        String result = null;
        HttpEntity httpEntity;
        if (httpResponse != null) {
            httpEntity = httpResponse.getEntity();
            if (httpEntity != null) {
                try {
                    result = IOUtils.toString(httpEntity.getContent(), charset);
                } catch (IOException e) {
                    log.error("Can't read http entity. "
                                    + "Method[" + httpRequest.getRequestLine().getMethod() + "]. "
                                    + "URI[" + httpRequest.getRequestLine().getUri() + "]. "
                                    + "Status Code[" + httpResponse.getStatusLine().getStatusCode() + "]",
                            e);
                }
            }
        }
        return result;
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    public static String convertToHtml(HttpEntity httpEntity) throws Exception {
        StringBuilder result = new StringBuilder();
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(httpEntity.getContent(),
                StandardCharsets.UTF_8))) {
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
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
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, (chain, authType) -> true);
            SSLConnectionSocketFactory sslsf = new
                    SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);
            return HttpClients.custom().setSSLSocketFactory(sslsf);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error on creating HttpClientBuilder.", e);
            return HttpClientBuilder.create();
        }
    }
}
