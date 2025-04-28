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

package org.qubership.atp.environments.utils.cloud;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.util.StringUtils.isEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.qubership.atp.environments.errorhandling.clients.EnvironmentKubeClientEntityFetchException;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.service.rest.server.response.ShortExternalService;
import org.qubership.atp.environments.utils.cloud.model.CloudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.NetworkingV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1HTTPIngressPath;
import io.kubernetes.client.openapi.models.V1Ingress;
import io.kubernetes.client.openapi.models.V1IngressList;
import io.kubernetes.client.openapi.models.V1IngressRule;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServiceList;
import io.kubernetes.client.util.Config;

public class KubeClient extends ExternalCloudClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(KubeClient.class);

    private CoreV1Api coreApi;
    private NetworkingV1Api extensionsV1beta1Api;
    private V1IngressList v1beta1IngressClassList;

    public KubeClient() {
    }

    private KubeClient(String serverUrl, String login, String password, String namespace) {
        configure(Config.fromUserPassword(serverUrl, login, password, false), serverUrl, namespace);
    }

    private KubeClient(String serverUrl, String accessToken, String namespace) {
        configure(Config.fromToken(serverUrl, accessToken, false), serverUrl, namespace);
    }

    private void configure(ApiClient apiClient, String serverUrl, String namespace) {
        this.serverUrl = serverUrl;
        this.namespace = namespace;
        Configuration.setDefaultApiClient(apiClient);
        this.coreApi = new CoreV1Api();
        this.extensionsV1beta1Api = new NetworkingV1Api();
    }

    @Override
    public void configure(ConnectionParameters connectionParameters) {
        String serverUrl = connectionParameters.get("url");
        String login = connectionParameters.get("login");
        String password = connectionParameters.get("password");
        String token = connectionParameters.get("token");
        this.namespace = connectionParameters.get("namespace");
        if (!isEmpty(token)) {
            configure(Config.fromToken(serverUrl, token, false), serverUrl, namespace);
        } else {
            configure(Config.fromUserPassword(serverUrl, login, password, false), serverUrl, namespace);
        }
    }

    /**
     * Gets list with images.
     *
     * @return List.
     */

    public Set<String> getImages() {
        try {
            V1PodList podList = coreApi.listNamespacedPod(namespace,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,false);
            Set<String> imageList = new TreeSet<>();
            podList.getItems().forEach(pod -> {
                if (pod.getSpec() != null && pod.getSpec().getContainers() != null) {
                    pod.getSpec().getContainers()
                            .forEach(container -> {
                                if (container.getImage() != null && !container.getImage().isEmpty()) {
                                    imageList.add(container.getImage());
                                }
                            });
                }
            });
            return imageList;
        } catch (ApiException e) {
            LOGGER.error("Failed to get pods from namespace {} , server {}", namespace,
                    this.serverUrl, e);
            throw new EnvironmentKubeClientEntityFetchException("images");
        }
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * Gets config map with token authorization.
     *
     * @param mapName - Name of the map.
     * @return V1ConfigMap
     */
    public V1ConfigMap getConfigMap(String mapName) {
        V1ConfigMap v1ConfigMap;
        try {
            v1ConfigMap = coreApi.readNamespacedConfigMap(mapName,
                    namespace, null);
        } catch (ApiException e) {
            LOGGER.error("Failed to get config map with name {}, namespace {} from server {}", mapName, namespace,
                    this.serverUrl, e);
            throw new EnvironmentKubeClientEntityFetchException("configmap");
        }
        return v1ConfigMap;
    }

    /**
     * Gets ingress-host for service.
     *
     * @param serviceName - name of service.
     * @return String
     */
    public String getIngressHost(String serviceName) {
        if (this.v1beta1IngressClassList == null || this.v1beta1IngressClassList.getItems().isEmpty()) {
            return "";
        }
        for (V1Ingress ingress : this.v1beta1IngressClassList.getItems()) {
            List<V1IngressRule> ingressRule = ingress.getSpec().getRules();
            List<V1HTTPIngressPath> ingressPaths = null;
            if (ingressRule != null
                    && !ingressRule.isEmpty()
                    && ingressRule.get(0).getHttp() != null) {
                ingressPaths = ingressRule.get(0).getHttp().getPaths();
            }
            if (ingressPaths != null
                    && !ingressPaths.isEmpty()
                    && ingressPaths.get(0).getBackend() != null
                    && ingressPaths.get(0).getBackend().getService() != null
                    && ingressPaths.get(0).getBackend().getService().getName().equals(serviceName)) {
                return ingressRule.get(0).getHost() != null ? ingressRule.get(0).getHost() : "";
            }
        }
        return "";
    }

    /**
     * Gets service list with token authorization.
     *
     * @return V1ServiceList
     */
    public List<V1Service> getServicesList() {
        try {
            V1ServiceList serviceList = coreApi.listNamespacedService(namespace,
                    null,
                    false,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null, null);
            return serviceList.getItems()
                    .stream()
                    .filter(service -> nonNull(service.getMetadata())
                    && nonNull(service.getMetadata().getUid()))
                    .collect(Collectors.toList());
        } catch (ApiException e) {
            LOGGER.error("Failed to get service list with namespace {} from server {}", namespace,
                    this.serverUrl, e);
            throw new EnvironmentKubeClientEntityFetchException("service list");
        }
    }

    /**
     * Sets ingress-list from kubernetes.
     */
    public void setIngressList() {
        try {
            this.v1beta1IngressClassList = extensionsV1beta1Api.listNamespacedIngress(namespace,
                    null,
                    false,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null, null);
        } catch (ApiException e) {
            LOGGER.error("Failed to get ingress list with namespace {} from server {}", namespace,
                    this.serverUrl, e);
            throw new EnvironmentKubeClientEntityFetchException("ingress list");
        }
    }

    /**
     * Gets service by service-UUID from service-list.
     *
     * @param id          - UUID of service.
     * @param serviceList - list with services.
     * @return V1Service
     */
    public V1Service findServiceById(List<V1Service> serviceList, String id) {
        for (V1Service service: serviceList) {
            if (service.getMetadata().getUid().equals(id)) {
                return service;
            }
        }
        return null;

    }

    /**
     * Gets service by service-UUID from service-list.
     *
     * @param name          - name of service.
     * @param serviceList - list with services.
     * @return V1Service
     */
    public V1Service findServiceByName(List<V1Service> serviceList, String name) {
        for (V1Service service: serviceList) {
            if (service.getMetadata().getName().equals(name)) {
                return service;
            }
        }
        return null;

    }

    @Override
    public List<ShortExternalService> getShortServices() {
        return getServicesList().stream()
                .map((service) ->
                        new ShortExternalService(service.getMetadata().getUid(),
                                service.getMetadata().getName()))
                .collect(Collectors.toList());
    }

    /**
     * Gets new KubeClient object.
     *
     * @param serverUrl   - Kubernetes host.
     * @param accessToken - authorization token.
     * @param login       - authorization login.
     * @param password    - authorization password.
     * @param namespace   - name of the project from Kubernetes.
     * @return KubeClient
     */
    public static KubeClient createKubeClient(String serverUrl,
                                              String accessToken,
                                              String login,
                                              String password,
                                              String namespace) {
        if (isEmpty(accessToken)) {
            return new KubeClient(serverUrl, login, password, namespace);
        } else {
            return new KubeClient(serverUrl, accessToken, namespace);
        }
    }

    /**
     * Gets services by service-UUIDs.
     *
     * @param existingServiceIds - UUID-list of services.
     * @return List
     */
    @Override
    public List<CloudService> getServicesByServiceId(List<UUID> existingServiceIds) {
        List<V1Service> serviceList = getServicesList();
        List<CloudService> processedServiceList = new ArrayList<>();
        setIngressList();
        existingServiceIds.forEach(serviceId -> {
            V1Service serviceFromKubernetes = findServiceById(serviceList, serviceId.toString());
            if (serviceFromKubernetes != null) {
                processedServiceList.add(createCloudService(serviceFromKubernetes));
            }
        });
        return processedServiceList;
    }

    private CloudService createCloudService(V1Service serviceFromKubernetes) {
        CloudService service = new CloudService();
        String serviceName = serviceFromKubernetes.getMetadata().getName();
        service.setId(UUID.fromString(serviceFromKubernetes.getMetadata().getUid()));
        if (!isEmpty(serviceName)) {
            service.setName(serviceName);
        } else {
            LOGGER.error("Service with name {} has invalid configuration", serviceName);
            throw new EnvironmentKubeClientEntityFetchException(String.format("'%s' service", serviceName));
        }
        service.setHost(getIngressHost(serviceName));
        return service;
    }

    @Override
    public List<CloudService> getServicesByExistingServices(List<System> existingServices) {
        List<V1Service> serviceList = getServicesList();
        List<CloudService> processedServiceList = new ArrayList<>();
        setIngressList();
        existingServices.forEach(service -> {
            V1Service serviceFromKubernetes = null;
            if (nonNull(service.getExternalId())) {
                serviceFromKubernetes = findServiceById(serviceList,
                        service.getExternalId().toString());
            }
            if (isNull(serviceFromKubernetes) && nonNull(service.getExternalName())) {
                serviceFromKubernetes = findServiceByName(serviceList,
                        service.getExternalName());
            }
            if (serviceFromKubernetes != null) {
                processedServiceList.add(createCloudService(serviceFromKubernetes));
            }
        });
        return processedServiceList;
    }
}
