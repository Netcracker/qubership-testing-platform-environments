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

package org.qubership.atp.environments.config;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.qubership.atp.environments.utils.HttpUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class HttpClientConfig {

    @Value("${atp-environments.get.version.httpclient.socketTimeout}")
    private int socketTimeout;
    @Value("${atp-environments.get.version.httpclient.maxTotal}")
    private int maxTotal;
    @Value("${atp-environments.get.version.httpclient.defaultMaxPerRoute}")
    private int defaultMaxPerRoute;

    /**
     * Preconfigured HttpClient bean.
     *
     * @return CloseableHttpClient.
     */
    @Bean
    public CloseableHttpClient httpClient() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return HttpUtils.createTrustAllHttpClientBuilder()
                .setDefaultRequestConfig(requestConfig(socketTimeout))
                .setConnectionManager(connectionManager())
                .build();
    }

    private HttpClientConnectionManager connectionManager()
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        SSLContext sslContext = new SSLContextBuilder()
                .loadTrustMaterial(null, (chain, authType) -> true)
                .build();

        PoolingHttpClientConnectionManager connectionManager =
                new PoolingHttpClientConnectionManager(RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.INSTANCE)
                        .register("https", new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE))
                        .build());
        //Maximum number of connections
        connectionManager.setMaxTotal(maxTotal);
        //The default maximum number of connections per route
        connectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);
        return connectionManager;
    }

    /**
     * Configure request timeout to avoid requests hanging.
     *
     * @param socketTimeout in seconds
     * @return RequestConfig
     */
    private RequestConfig requestConfig(int socketTimeout) {
        log.debug("Configure RequestConfig [socketTimeout={}]", socketTimeout);
        RequestConfig config = RequestConfig.custom()
                .setSocketTimeout(socketTimeout * 1000)
                .build();
        return config;
    }
}
