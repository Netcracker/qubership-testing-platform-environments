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

import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.qubership.atp.environments.errorhandling.clients.EnvironmentOpenshiftDeploymentConfigsFetchException;
import org.qubership.atp.environments.errorhandling.clients.EnvironmentOpenshiftProjectFetchException;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.service.rest.server.response.ShortExternalService;
import org.qubership.atp.environments.utils.cloud.model.CloudService;
import org.qubership.atp.environments.version.checkers.OpenshiftVersionChecker.OpenShiftCredentialsNames;
import org.springframework.util.StringUtils;

import com.openshift.restclient.ClientBuilder;
import com.openshift.restclient.IClient;
import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.model.IDeploymentConfig;
import com.openshift.restclient.model.IProject;
import com.openshift.restclient.model.IResource;
import com.openshift.restclient.model.IService;
import com.openshift.restclient.model.route.IRoute;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("CPD-START")
public class OpenshiftClient extends ExternalCloudClient {

    private IClient client;

    @Override
    public void configure(ConnectionParameters connectionParameters) {
        String serverUrl = connectionParameters.get(OpenShiftCredentialsNames.HOST);
        String login = connectionParameters.get(OpenShiftCredentialsNames.LOGIN);
        String password = connectionParameters.get(OpenShiftCredentialsNames.PASSWORD);
        String token = connectionParameters.get(OpenShiftCredentialsNames.TOKEN);
        this.namespace = connectionParameters.get(OpenShiftCredentialsNames.NAMESPACE);
        if (!StringUtils.isEmpty(token)) {
            client = new ClientBuilder(serverUrl)
                    .usingToken(token)
                    .build();
        } else {
            client = new ClientBuilder(serverUrl)
                    .withUserName(login)
                    .withPassword(password)
                    .build();
        }
    }

    /**
     * Gets set of images, this entity contains data about Image.
     *
     * @return List.
     */
    public Set<String> getImageSet() {
        Set<String> images = new HashSet<>();
        List<IDeploymentConfig> deploymentConfigList = readDeploymentConfigs(this.namespace);
        deploymentConfigList.forEach(iDeploymentConfig -> images.addAll(iDeploymentConfig.getImages()));
        return images;
    }

    /**
     * Gets list of deployment configs, this entity contains data about Image.
     *
     * @param namespace - project name in openshift.
     * @return List.
     */
    public List<IDeploymentConfig> readDeploymentConfigs(String namespace) {
        String kind = ResourceKind.DEPLOYMENT_CONFIG;
        List<IDeploymentConfig> list;
        try {
            list = client.list(kind, namespace);
        } catch (Exception e) {
            log.error("Failed to fetch Openshift deployment configs");
            throw new EnvironmentOpenshiftDeploymentConfigsFetchException();
        }
        return list;
    }

    /**
     * Gets set of routes related to the project.
     *
     * @param openshiftProject - project name in openshift.
     * @return Set
     */
    public List<IRoute> getRoutes(String openshiftProject) {
        IProject projectOpenShift;
        try {
            projectOpenShift = client.get(ResourceKind.PROJECT, openshiftProject, openshiftProject);
        } catch (Exception e) {
            log.error("Failed to fetch Openshift project: {}", openshiftProject, e);
            throw new EnvironmentOpenshiftProjectFetchException(openshiftProject);
        }
        return projectOpenShift.getProject().getResources(ResourceKind.ROUTE);
    }

    /**
     * Gets set of routes related to the project.
     *
     * @param serviceName - route name in openShift.
     * @param routes      - routes in openShift project.
     * @return Service route
     */
    public String getRouteUrl(String serviceName, List<IRoute> routes) {
        if (StringUtils.isEmpty(serviceName)) {
            return "";
        }
        return routes.stream().map(route -> {
            if (route.getServiceName().equals(serviceName)) {
                String scheme = route.getTLSConfig() == null ? "http" : "https";
                return String.format("%s://%s", scheme, route.getHost());
            }
            return "";
        }).collect(Collectors.joining());
    }

    /**
     * Gets set of routes related to the project.
     *
     * @param openshiftProject - project name in openshift.
     * @return Set
     */
    public List<IService> getServices(String openshiftProject) {
        IProject projectOpenShift;
        try {
            projectOpenShift = client.get(ResourceKind.PROJECT, openshiftProject, openshiftProject);
        } catch (Exception e) {
            log.error("Failed to fetch Openshift project: {}", openshiftProject, e);
            throw new EnvironmentOpenshiftProjectFetchException(openshiftProject);
        }
        return projectOpenShift.getProject().getResources(ResourceKind.SERVICE);
    }

    @Override
    public List<ShortExternalService> getShortServices() {
        return getServices(this.namespace).stream()
                .map((service) ->
                        new ShortExternalService(service.getMetadata().get(OpenShiftApiConstants.ID),
                                service.getMetadata().get(OpenShiftApiConstants.NAME)))
                .collect(Collectors.toList());
    }

    private List<IService> getServicesList() {
        List<IService> serviceList = client.list(ResourceKind.SERVICE, this.namespace);
        return serviceList
                .stream()
                .filter(service -> nonNull(service.getMetadata())
                        && !StringUtils.isEmpty(service.getMetadata().get(OpenShiftApiConstants.ID)))
                .collect(Collectors.toList());
    }

    private Map<String, String> getRoutesMap() {
        Map<String, String> routesMap = new HashMap<>();
        getRoutes(this.namespace)
                .forEach(route -> routesMap.put(route.getServiceName(), route.getURL()));
        return routesMap;
    }

    @Override
    public List<CloudService> getServicesByExistingServices(List<System> existingServices) {
        List<IService> serviceList = getServicesList();
        Map<String, String> routeMap = getRoutesMap();
        Map<String, IService> servicesMapById = getServicesMapById(serviceList);
        Map<String, IService> servicesMapByName = getServicesMapByName(serviceList);
        List<CloudService> processedServiceList = new ArrayList<>();
        for (System existingService : existingServices) {
            IService externalService;
            if (nonNull(existingService.getExternalId())
                    && servicesMapById
                    .containsKey(existingService.getExternalId().toString())) {
                externalService = servicesMapById.get(existingService.getExternalId().toString());
            } else if (nonNull(existingService.getExternalName())
                    && servicesMapByName
                    .containsKey(existingService.getExternalName())) {
                externalService = servicesMapByName.get(existingService.getExternalName());
            } else {
                continue;
            }
            String serviceName = externalService.getName();
            if (serviceName != null) {
                processedServiceList.add(createCloudService(externalService,
                        routeMap.get(serviceName)));
            }
        }
        return processedServiceList;
    }

    private Map<String, IService> getServicesMapById(List<IService> serviceList) {
        return serviceList.stream()
                .collect(Collectors.toMap(service ->
                                service.getMetadata().get(OpenShiftApiConstants.ID),
                        service -> service));
    }

    private Map<String, IService> getServicesMapByName(List<IService> serviceList) {
        return serviceList.stream()
                .collect(Collectors.toMap(IResource::getName,
                        service -> service));
    }

    private CloudService createCloudService(IService externalService, String route) {
        CloudService service = new CloudService();
        service.setName(externalService.getName());
        service.setId(UUID.fromString(externalService
                .getMetadata()
                .get(OpenShiftApiConstants.ID)));
        service.setHost(route);
        return service;
    }

    @Override
    public List<CloudService> getServicesByServiceId(List<UUID> existingServiceIds) {
        Map<String, String> routeMap = getRoutesMap();
        List<IService> serviceList = getServicesList();
        Map<String, IService> servicesMap = getServicesMapById(serviceList);
        List<CloudService> processedServiceList = new ArrayList<>();
        existingServiceIds.forEach(serviceId -> {
            String serviceName = servicesMap.get(serviceId.toString()).getName();
            if (serviceName != null) {
                processedServiceList.add(createCloudService(servicesMap.get(serviceId.toString()),
                        routeMap.get(serviceName)));
            }
        });
        return processedServiceList;
    }

    protected static class OpenShiftApiConstants {

        protected static final String ID = "uid";
        protected static final String NAME = "name";
    }
}
