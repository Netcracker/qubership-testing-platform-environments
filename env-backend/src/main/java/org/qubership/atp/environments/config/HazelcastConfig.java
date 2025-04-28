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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.qubership.atp.environments.cache.EnvironmentHazelcastCache;
import org.qubership.atp.environments.model.utils.HazelcastMapName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.NoOpCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientConnectionStrategyConfig;
import com.hazelcast.com.google.common.cache.CacheBuilder;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableCaching
@Profile({"!test"})
@Slf4j
public class HazelcastConfig {

    @Value("${hazelcast.enable-caching}")
    private boolean cachingIsEnable;

    @Value("${hazelcast.system-versions.cache-period}")
    private int cacheVersionsPeriod;

    @Value("${hazelcast.connections-by-system-id.cache-period}")
    private int cacheConnectionsBySystemIdPeriod;

    @Value("${hazelcast.environments_by_system_id.cache-period}")
    private int cacheEnvironmentsBySystemIdsPeriod;

    @Value("${hazelcast.systems_by_environment_id.cache-period}")
    private int cacheSystemsByEnvironmentIdPeriod;

    public static final String CONNECTION_TEMPLATES_CACHE = "CONNECTION_TEMPLATES_CACHE";

    @Value("${hazelcast.cluster-name}")
    private String clusterName;

    @Value("${hazelcast.address}")
    private String hazelcastAddress;

    /**
     * Hazelcast config.
     *
     * @return configuration bean
     */
    @Bean
    @ConditionalOnProperty(name = "hazelcast.enable-caching", havingValue = "true")
    public ClientConfig hazelCastConfig() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setClusterName(clusterName);
        clientConfig.setInstanceName("atp-environment-" + UUID.randomUUID());
        clientConfig.getNetworkConfig().addAddress(hazelcastAddress);
        clientConfig.getConnectionStrategyConfig().setReconnectMode(ClientConnectionStrategyConfig.ReconnectMode.ASYNC);
        return clientConfig;
    }

    /**
     * Hazelcast client.
     *
     * @return client bean
     */

    @Bean(name = "hazelcastClient")
    @ConditionalOnProperty(name = "hazelcast.enable-caching", havingValue = "true")
    public HazelcastInstance hazelcastClient(ClientConfig hazelCastConfig) {
        if (cachingIsEnable) {
            return HazelcastClient.getOrCreateHazelcastClient(hazelCastConfig);
        } else {
            return null;
        }
    }

    /**
     * System version cache map.
     *
     * @return system versions cache map bean
     */

    @Bean(name = "systemCachedMap")
    public Cache systemCachedMap(CacheManager cacheManager) {
        return cacheManager != null && cacheManager.getCache(HazelcastMapName.SYSTEM_VERSION) != null
                ? cacheManager.getCache(HazelcastMapName.SYSTEM_VERSION)
                : new NoOpCache(HazelcastMapName.SYSTEM_VERSION);
    }

    /**
     * Clear hazelcast maps.
     *
     * @param hazelcastInstance HazelcastInstance
     */
    public static void clearHazelcastMap(HazelcastInstance hazelcastInstance) {
        if (hazelcastInstance != null) {
            List<String> existingMapNames = hazelcastInstance
                    .getDistributedObjects()
                    .stream()
                    .map(DistributedObject::getName)
                    .collect(Collectors.toList());
            Arrays.stream(HazelcastMapName.class.getFields())
                    .forEach(mapNameField -> {
                        try {
                            String mapName = (String) mapNameField.get(null);
                            if (existingMapNames.stream().anyMatch(name -> name.equals(mapName))) {
                                hazelcastInstance
                                        .getMap(mapName)
                                        .evictAll();
                            }
                        } catch (IllegalAccessException e) {
                            log.error("Error while occurred evict map", e);
                        }
                    });
        }
    }

    /**
     * Cache Manager.
     *
     * @return cache manager
     */

    @Bean
    public CacheManager cacheManager(@Autowired(required = false) @Qualifier("hazelcastClient")
                                     HazelcastInstance hazelcastClient) {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        List<Cache> caches = new ArrayList<>();
        caches.add(new CaffeineCache(CONNECTION_TEMPLATES_CACHE,
                Caffeine.newBuilder().recordStats().expireAfterWrite(6, TimeUnit.HOURS).build(),
                true));
        if (cachingIsEnable) {
            clearHazelcastMap(hazelcastClient);
            caches.add(new EnvironmentHazelcastCache(hazelcastClient.getMap(HazelcastMapName.SYSTEM_VERSION),
                    cacheVersionsPeriod, TimeUnit.SECONDS));
            caches.add(new EnvironmentHazelcastCache(hazelcastClient.getMap(HazelcastMapName.CONNECTIONS_BY_SYSTEM_ID),
                    cacheConnectionsBySystemIdPeriod, TimeUnit.SECONDS));
            caches.add(new EnvironmentHazelcastCache(hazelcastClient.getMap(HazelcastMapName.ENVIRONMENTS_BY_SYSTEM_ID),
                    cacheEnvironmentsBySystemIdsPeriod, TimeUnit.SECONDS));
            caches.add(new EnvironmentHazelcastCache(hazelcastClient.getMap(HazelcastMapName.SYSTEMS_BY_ENVIRONMENT_ID),
                    cacheSystemsByEnvironmentIdPeriod, TimeUnit.SECONDS));
            caches.add(new ConcurrentMapCache(HazelcastMapName.ATP_AUTH_PROJECT_CACHE,
                    CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.MINUTES).maximumSize(100).build().asMap(),
                    true));
        } else {
            caches.add(new CaffeineCache(HazelcastMapName.SYSTEM_VERSION,
                    Caffeine.newBuilder().recordStats().expireAfterWrite(cacheVersionsPeriod, TimeUnit.SECONDS).build(),
                    true));
            caches.add(new CaffeineCache(HazelcastMapName.CONNECTIONS_BY_SYSTEM_ID,
                    Caffeine.newBuilder().recordStats().expireAfterWrite(cacheConnectionsBySystemIdPeriod,
                            TimeUnit.SECONDS).build(),
                    true));
            caches.add(new CaffeineCache(HazelcastMapName.ENVIRONMENTS_BY_SYSTEM_ID,
                    Caffeine.newBuilder().recordStats().expireAfterWrite(cacheEnvironmentsBySystemIdsPeriod,
                            TimeUnit.SECONDS).build(),
                    true));
            caches.add(new CaffeineCache(HazelcastMapName.SYSTEMS_BY_ENVIRONMENT_ID,
                    Caffeine.newBuilder().recordStats().expireAfterWrite(cacheSystemsByEnvironmentIdPeriod,
                            TimeUnit.SECONDS).build(),
                    true));
            caches.add(new CaffeineCache(HazelcastMapName.ATP_AUTH_PROJECT_CACHE,
                    Caffeine.newBuilder().recordStats().expireAfterWrite(2, TimeUnit.MINUTES).maximumSize(100).build(),
                    true));
        }
        cacheManager.setCaches(caches);
        return cacheManager;
    }
}
