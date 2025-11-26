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

package org.qubership.atp.environments.service.direct.impl;

import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.qubership.atp.auth.springbootstarter.entities.UserInfo;
import org.qubership.atp.auth.springbootstarter.ssl.Provider;
import org.qubership.atp.environments.enums.MdcField;
import org.qubership.atp.environments.errorhandling.clients.EnvironmentCloudClientCreationException;
import org.qubership.atp.environments.errorhandling.clients.EnvironmentIllegalCloudConnectionTemplateClassException;
import org.qubership.atp.environments.errorhandling.clients.EnvironmentOpenshiftProjectFetchException;
import org.qubership.atp.environments.helper.JsonPathHandler;
import org.qubership.atp.environments.helper.RegexpHandler;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.Identified;
import org.qubership.atp.environments.model.ParametersGettingVersion;
import org.qubership.atp.environments.model.ServerItf;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.SystemCategory;
import org.qubership.atp.environments.model.utils.Constants;
import org.qubership.atp.environments.model.utils.HazelcastMapName;
import org.qubership.atp.environments.model.utils.enums.Status;
import org.qubership.atp.environments.repo.impl.ConnectionRepositoryImpl;
import org.qubership.atp.environments.repo.impl.EnvironmentRepositoryImpl;
import org.qubership.atp.environments.repo.impl.SystemRepositoryImpl;
import org.qubership.atp.environments.service.direct.ConnectionService;
import org.qubership.atp.environments.service.direct.DecryptorService;
import org.qubership.atp.environments.service.direct.EncryptorService;
import org.qubership.atp.environments.service.direct.KafkaService;
import org.qubership.atp.environments.service.direct.SystemCategoriesService;
import org.qubership.atp.environments.service.direct.SystemService;
import org.qubership.atp.environments.service.rest.server.dto.ConnectionDto;
import org.qubership.atp.environments.service.rest.server.dto.CreateSystemDto;
import org.qubership.atp.environments.service.rest.server.dto.EventType;
import org.qubership.atp.environments.service.rest.server.dto.SharingRequestDto;
import org.qubership.atp.environments.service.rest.server.dto.StatusDto;
import org.qubership.atp.environments.service.rest.server.dto.SystemDto;
import org.qubership.atp.environments.service.rest.server.response.ShortExternalService;
import org.qubership.atp.environments.utils.DateTimeUtil;
import org.qubership.atp.environments.utils.EnvgeneYamlGenerator;
import org.qubership.atp.environments.utils.cloud.ExternalCloudClient;
import org.qubership.atp.environments.utils.cloud.KubeClient;
import org.qubership.atp.environments.utils.cloud.OpenshiftClient;
import org.qubership.atp.environments.utils.cloud.model.CloudService;
import org.qubership.atp.environments.version.checkers.VersionChecker;
import org.qubership.atp.environments.version.checkers.VersionCheckerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.openshift.restclient.model.route.IRoute;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("systemService")
@SuppressWarnings("CPD-START")
public class SystemServiceImpl implements SystemService {

    @Value("${atp-environments.regexp.timeout}")
    private int regexpTimeout;
    private final SystemRepositoryImpl systemRepository;
    private final ConnectionRepositoryImpl connectionRepository;
    private final ConnectionService connectionService;
    private final SystemCategoriesService systemCategoriesService;
    private final EnvironmentRepositoryImpl environmentRepository;
    private final DateTimeUtil dateTimeUtil;
    private final EncryptorService encryptorService;
    private final DecryptorService decryptorService;
    private final Provider<UserInfo> userInfoProvider;
    private final RegexpHandler regexpHandler;
    private final JsonPathHandler jsonPathHandler;
    private final KafkaService kafkaService;
    private final VersionCheckerFactory versionCheckerFactory;
    private final MetricService metricService;

    @Lazy
    @Autowired
    private SystemService ref;

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Autowired
    public SystemServiceImpl(SystemRepositoryImpl systemRepository, ConnectionRepositoryImpl connectionRepository,
                             ConnectionService connectionService,
                             SystemCategoriesService systemCategoriesService, DateTimeUtil dateTimeUtil,
                             EnvironmentRepositoryImpl environmentRepository,
                             EncryptorService encryptorService, DecryptorService decryptorService,
                             Provider<UserInfo> userInfoProvider,
                             KafkaService kafkaService,
                             RegexpHandler regexpHandler,
                             JsonPathHandler jsonPathHandler,
                             VersionCheckerFactory versionCheckerFactory,
                             MetricService metricService) {
        this.systemRepository = systemRepository;
        this.connectionRepository = connectionRepository;
        this.connectionService = connectionService;
        this.systemCategoriesService = systemCategoriesService;
        this.environmentRepository = environmentRepository;
        this.dateTimeUtil = dateTimeUtil;
        this.encryptorService = encryptorService;
        this.decryptorService = decryptorService;
        this.userInfoProvider = userInfoProvider;
        this.regexpHandler = regexpHandler;
        this.jsonPathHandler = jsonPathHandler;
        this.kafkaService = kafkaService;
        this.versionCheckerFactory = versionCheckerFactory;
        this.metricService = metricService;
    }

    @Nullable
    @Override
    public System get(@Nonnull UUID id) {
        return systemRepository.getById(id);
    }

    @Override
    public boolean existsById(@Nonnull UUID id) {
        return systemRepository.existsById(id);
    }

    @Override
    public System getV2(@Nonnull UUID id) {
        return systemRepository.getByIdV2(id);
    }

    @Override
    public List<System> getAll() {
        return systemRepository.getAll();
    }

    @Override
    @Transactional
    public System replicate(@Nonnull UUID environmentId, @Nonnull UUID systemId, @Nonnull String name,
                            String description, UUID systemCategoryId,
                            ParametersGettingVersion parametersGettingVersion, UUID parentSystemId,
                            ServerItf serverItf, UUID linkToSystemId, UUID externalId, UUID sourceId,
                            String externalName) {
        systemRepository.getContext().setFullDbFetching(true);
        String trimmedSystemName = name.trim();
        UUID userId = userInfoProvider.get().getId();
        return systemRepository.create(environmentId,
                systemId,
                trimmedSystemName,
                description,
                dateTimeUtil.timestampAsUtc(),
                userId,
                systemCategoryId,
                parametersGettingVersion,
                parentSystemId,
                serverItf,
                false,
                linkToSystemId,
                externalId, sourceId,
                externalName);
    }

    @Override
    @Transactional
    public System create(UUID environmentId, String name, String description, UUID systemCategoryId,
                         ParametersGettingVersion parametersGettingVersion, UUID parentSystemId,
                         ServerItf serverItf, Boolean mergeByName, UUID linkToSystemId, UUID externalId,
                         String externalName) {
        systemRepository.getContext().setFullDbFetching(true);
        String trimmedSystemName = name.trim();
        Preconditions.checkArgument(
                systemRepository.checkSystemNameIsUniqueUnderEnvironment(environmentId, trimmedSystemName)
                        .isEmpty(), "System with name \"%s\" already exists under environment", trimmedSystemName);
        UUID userId = userInfoProvider.get().getId();
        return systemRepository.create(environmentId, trimmedSystemName, description, dateTimeUtil.timestampAsUtc(),
                userId, systemCategoryId, parametersGettingVersion, parentSystemId, serverItf, mergeByName,
                linkToSystemId, externalId, externalName);
    }

    @Override
    @Transactional
    public Connection create(UUID systemId, ConnectionDto connection) {
        systemRepository.getContext().setFullDbFetching(true);
        UUID sourceTemplateId = connection.getSourceTemplateId();
        if (sourceTemplateId != null) {
            Connection sourceId = connectionRepository.getById(sourceTemplateId);
            Preconditions.checkNotNull(sourceId, "Wrong sourceTemplateId: %s", sourceTemplateId);
        }
        return connectionService.replicate(systemId, connection.getId(), connection.getName(),
                connection.getDescription(), connection.getParameters(), connection.getConnectionType(),
                connection.getSourceTemplateId(), connection.getServices(), null);
    }

    @Nonnull
    @Override
    @Transactional
    public System create(UUID environmentId, CreateSystemDto system) {
        systemRepository.getContext().setFullDbFetching(true);
        UUID systemCategoryId = system.getSystemCategoryId();
        if (systemCategoryId != null) {
            SystemCategory systemCategory = systemCategoriesService.get(systemCategoryId);
            Preconditions.checkNotNull(systemCategory, "Wrong categoryId: %s", systemCategoryId);
        }
        System result = create(environmentId, system.getName(), system.getDescription(),
                systemCategoryId, system.getParametersGettingVersion(), system.getParentSystemId(),
                system.getServerItf(), system.getMergeByName(), system.getLinkToSystemId(), system.getExternalId(),
                system.getExternalName());
        List<Connection> connection = system.getConnections().stream()
                .map(connectionDto -> create(result.getId(), connectionDto))
                .collect(Collectors.toList());
        result.setConnections(connection);
        return result;
    }

    @Override
    @Transactional
    public System copy(UUID id, UUID environmentId, String name, String description, UUID systemCategoryId,
                       ParametersGettingVersion parametersGettingVersion, UUID parentSystemId, ServerItf serverItf,
                       Boolean mergeByName, UUID linkToSystemId, UUID externalId, String externalName) {
        systemRepository.getContext().setFullDbFetching(true);
        System toCopy = systemRepository.getById(id);
        System newSys = create(environmentId,
                name,
                description,
                systemCategoryId,
                parametersGettingVersion,
                parentSystemId,
                serverItf,
                mergeByName,
                linkToSystemId,
                externalId,
                externalName);
        if (toCopy.getConnections() != null) {
            newSys.setConnections(new ArrayList<>());
            toCopy.getConnections()
                    .forEach(connection -> newSys.getConnections()
                            .add(connectionService.create(newSys.getId(),
                                    connection.getName(),
                                    connection.getDescription(),
                                    connection.getParameters(),
                                    connection.getConnectionType(),
                                    connection.getSourceTemplateId(),
                                    connection.getServices())));
        }
        return newSys;
    }

    @Override
    @Transactional
    @CacheEvict(value = HazelcastMapName.ENVIRONMENTS_BY_SYSTEM_ID, key = "#systemId", condition = " #systemId != null "
            + "&& (!#sharingRequestDto.getShareList().isEmpty() || !#sharingRequestDto.getUnShareList().isEmpty())")
    public System shareProcessing(UUID systemId, SharingRequestDto sharingRequestDto) {
        System sharedSystem = systemRepository.getById(systemId);
        sharedSystem = share(sharedSystem, sharingRequestDto.getShareList());
        sharedSystem = unShare(sharedSystem.getId(), sharingRequestDto.getUnShareList());
        return sharedSystem;
    }

    @Override
    @Transactional
    public System share(System sharedSystem, List<UUID> environmentIds) {
        systemRepository.getContext().setFullDbFetching(true);
        UUID userId = userInfoProvider.get().getId();
        System returnedSystem = sharedSystem;
        for (UUID environmentId : environmentIds) {
            Environment environment = environmentRepository.getById(environmentId);
            Preconditions.checkArgument(systemRepository.checkSystemNameIsUniqueUnderEnvironment(environmentId,
                            sharedSystem.getName()).isEmpty(),
                    "System with name \"%s\" already exists under environment \"%s\"",
                    sharedSystem.getName(),
                    environment.getName());
            returnedSystem = systemRepository
                    .share(sharedSystem.getId(), environment, dateTimeUtil.timestampAsUtc(), userId);
        }
        return returnedSystem == null ? get(sharedSystem.getId()) : returnedSystem;
    }

    @Override
    @Transactional
    public System unShare(UUID systemId, List<UUID> environmentIds) {
        systemRepository.getContext().setFullDbFetching(true);
        UUID userId = userInfoProvider.get().getId();
        System returnedSystem = null;
        for (UUID environmentId : environmentIds) {
            returnedSystem = systemRepository.unShare(systemId, environmentId, dateTimeUtil.timestampAsUtc(), userId);
        }
        return returnedSystem == null ? get(systemId) : returnedSystem;
    }

    @Override
    public List<Connection> updateOpenshiftRoute(UUID environmentId) {
        systemRepository.getContext().setFullDbFetching(true);
        List<Connection> openShiftServerConnection = connectionRepository.getAll(environmentId,
                Constants.Environment.System.Connection.OPENSHIFT_SERVER,
                Constants.SystemCategories.OPENSHIFT_SERVER);
        return updateUrlRoutes(environmentId, Constants.Environment.System.Connection.OPENSHIFT_SYSTEM,
                openShiftServerConnection);
    }

    @Override
    public List<Connection> updateOpenshiftRoute(UUID systemId, UUID environmentId) {
        systemRepository.getContext().setFullDbFetching(true);
        List<Connection> openShiftServerConnection = connectionRepository.getAll(environmentId,
                Constants.Environment.System.Connection.OPENSHIFT_SERVER,
                Constants.SystemCategories.OPENSHIFT_SERVER,
                systemId);
        return updateUrlRoutes(environmentId,
                Constants.Environment.System.Connection.OPENSHIFT_SYSTEM,
                openShiftServerConnection);
    }

    @Override
    @Cacheable(value = HazelcastMapName.SYSTEM_VERSION, key = "#id", condition = "#id!=null")
    public Object getCachedVersionById(UUID id) {
        systemRepository.getContext().setFullDbFetching(true);
        return updateVersionBySystemId(id, false);
    }

    @Override
    @Cacheable(value = HazelcastMapName.SYSTEM_VERSION, key = "#system.id",
            condition = "#system!=null && #system.id!=null")
    public System getCachedVersionBySystem(System system) {
        systemRepository.getContext().setFullDbFetching(true);
        MDC.put(MdcField.SYSTEM_ID.toString(), system.getId().toString());
        return updateVersionBySystem(system, false);
    }

    /**
     * Updating route parameter url openshift project.
     */
    @Transactional
    public List<Connection> updateUrlRoutes(UUID environmentId, UUID openShiftSystemConnection,
                                            List<Connection> openShiftServerConnection) {
        systemRepository.getContext().setFullDbFetching(true);
        List<Connection> updatedConnections = new ArrayList<>();
        List<Connection> connections = connectionRepository.getAll(environmentId,
                openShiftSystemConnection);
        for (Connection openShiftProject : openShiftServerConnection) {
            openShiftProject.setParameters(decryptorService.decryptParameters(openShiftProject.getParameters()));
            String etalonProject = openShiftProject.getParameters().get("etalon_project");
            String project = openShiftProject.getParameters().get("project");
            List<String> projectIds = Stream.of(etalonProject, project)
                    .filter(str -> !Strings.isNullOrEmpty(str))
                    .distinct()
                    .collect(Collectors.toList());
            OpenshiftClient osClient = (OpenshiftClient) ExternalCloudClient
                    .createClient(openShiftProject.getParameters(),
                            OpenshiftClient.class);
            for (String projectId : projectIds) {
                List<IRoute> routes;
                try {
                    routes = osClient.getRoutes(projectId);
                } catch (Exception e) {
                    log.error("Failed to fetch Openshift project: {}", projectId, e);
                    throw new EnvironmentOpenshiftProjectFetchException(projectId);
                }
                for (Connection connection : connections) {
                    if (connection.getParameters().get("root_synchronize_project")
                            .equals(openShiftProject.getId().toString())) {
                        String routeNameParam = connection.getParameters().get("route_name");
                        String routeUrl = osClient.getRouteUrl(routeNameParam, routes);
                        if (!routeUrl.isEmpty()) {
                            updatedConnections.add(connection);
                            connection.getParameters().put("url", routeUrl);
                            UUID systemId = connectionRepository.getSystemId(connection.getId());
                            connectionRepository.updateParameters(systemId, connection.getId(),
                                    connection.getParameters(),
                                    dateTimeUtil.timestampAsUtc(), userInfoProvider.get().getId(),
                                    null);
                        } else {
                            log.info("route_name from Connection[ID:{}] not found", connection.getId());
                        }
                    }
                }
            }
        }
        return updatedConnections;
    }

    @Override
    @Transactional
    public System update(System system) {
        systemRepository.getContext().setFullDbFetching(true);
        return update(system.getId(), system.getName(), system.getDescription(),
                system.getSystemCategoryId(), system.getParametersGettingVersion(), system.getParentSystemId(),
                system.getServerItf(), system.getMergeByName(), system.getLinkToSystemId(), system.getExternalId(),
                system.getExternalName());
    }

    @Override
    @Transactional
    public System update(UUID id, String name, String description, UUID systemCategoryId,
                         ParametersGettingVersion parametersGettingVersion, UUID parentSystemId, ServerItf serverItf,
                         Boolean mergeByName, UUID linkToSystemId, UUID externalId, String externalName) {
        systemRepository.getContext().setFullDbFetching(true);
        UUID userId = userInfoProvider.get().getId();
        System system = get(id);
        name = name.trim();
        if (!name.equals(system.getName())) {
            system.setName(name);
            system.getEnvironments()
                    .stream()
                    .map(Identified::getId)
                    .forEach(environmentId -> Preconditions.checkArgument(
                            systemRepository.checkSystemNameIsUniqueUnderEnvironment(environmentId, system.getName())
                                    .isEmpty(),
                            "System with name \"%s\" already exists under environment \"%s\"",
                            system.getName(),
                            environmentRepository.getById(environmentId).getName()));
        }
        return systemRepository.update(id, name, description, dateTimeUtil.timestampAsUtc(), userId, systemCategoryId,
                Status.NOTHING, null, system.getVersion(), system.getDateOfCheckVersion(), parametersGettingVersion,
                parentSystemId, serverItf, mergeByName, linkToSystemId, externalId, externalName, true, true);
    }

    @Override
    @Transactional
    public System update(SystemDto system) {
        systemRepository.getContext().setFullDbFetching(true);
        List<UUID> environmentIdsToUnshare = null;
        List<UUID> environmentIdsToShare = null;
        List<UUID> environmentsToCheck = new ArrayList<>();
        System existedSystem = get(system.getId());
        List<UUID> existedEnvironmentUuids = existedSystem.getEnvironmentIds().stream().map(Identified::getId)
                .collect(Collectors.toList());
        if (system.getEnvironmentIds() != null) {
            List<UUID> newEnvironmentUuids = system.getEnvironmentIds();
            environmentIdsToUnshare = getDifference(existedEnvironmentUuids, newEnvironmentUuids);
            environmentIdsToShare = getDifference(newEnvironmentUuids, existedEnvironmentUuids);
        }
        if (isSystemNameChanged(system)) {
            environmentsToCheck.addAll(existedEnvironmentUuids);
        }
        if (environmentIdsToShare != null && !environmentIdsToShare.isEmpty()) {
            environmentsToCheck.addAll(environmentIdsToShare);
        }
        if (environmentIdsToUnshare != null && !environmentIdsToUnshare.isEmpty()) {
            environmentsToCheck.removeAll(environmentIdsToUnshare);
        }
        system.setName(system.getName().trim());
        for (UUID envId : environmentsToCheck) {
            Preconditions.checkArgument(
                    systemRepository.checkSystemNameIsUniqueUnderEnvironment(envId, system.getName())
                            .isEmpty(),
                    "System with name \"%s\" already exists under environment \"%s\"",
                    system.getName(),
                    Objects.requireNonNull(environmentRepository.getById(envId)).getName());
        }
        System result = update(system.getId(), system.getName(), system.getDescription(),
                system.getSystemCategoryId(), system.getParametersGettingVersion(), system.getParentSystemId(),
                system.getServerItf(), system.getMergeByName(), system.getLinkToSystemId(), system.getExternalId(),
                system.getExternalName());
        UUID userId = userInfoProvider.get().getId();
        if (system.getConnections() != null) {
            List<Connection> connection = system.getConnections().stream().map(c -> {
                        ConnectionParameters parameters = c.getParameters();
                        if (parameters != null) {
                            parameters.replaceAll((k, v) -> (v == null) ? v : v.trim());
                        }
                        return new ConnectionDto(c.getId(), c.getName(), c.getSystemId(), parameters,
                                c.getSourceTemplateId(), c.getConnectionType(), c.getCreated(), c.getModified(),
                                c.getServices());
                    }).map(conn ->
                            connectionRepository.update(conn.getId(), conn.getSystemId(), conn.getName(),
                                    conn.getDescription(), conn.getParameters(), dateTimeUtil.timestampAsUtc(), userId,
                                    conn.getConnectionType(), conn.getSourceTemplateId(), conn.getServices()))
                    .collect(Collectors.toList());
            result.setConnections(connection);
        }
        if (system.getEnvironmentIds() != null) {
            List<Environment> environmentsToShare = environmentRepository.getByIds(environmentIdsToShare);
            if (!CollectionUtils.isEmpty(environmentsToShare)) {
                environmentsToShare.forEach(environment -> systemRepository
                        .share(system.getId(), environment, dateTimeUtil.timestampAsUtc(), userId));
            }
            environmentIdsToUnshare.forEach(id -> systemRepository
                    .unShare(system.getId(), id, dateTimeUtil.timestampAsUtc(), userId));
            List<Environment> environmentIds = system.getEnvironmentIds().stream()
                    .map(environmentRepository::getById)
                    .collect(Collectors.toList());
            result.setEnvironmentIds(environmentIds);
        }
        return result;
    }

    public boolean isSystemNameChanged(SystemDto system) {
        return !system.getName().equals(systemRepository.getById(system.getId()).getName());
    }

    /**
     * Returns list of UUIDs which exist in first list, but absent in second.
     *
     * @param first  First list.
     * @param second Second list.
     */
    public List<UUID> getDifference(List<UUID> first, List<UUID> second) {
        List<UUID> result = new ArrayList<>();
        if (first != null && second != null) {
            result = first.stream().filter(id ->
                    !second.contains(id)).collect(Collectors.toList());
        }
        return result;
    }

    @Override
    @Transactional
    public void delete(UUID id, UUID environmentId) {
        UUID userId = userInfoProvider.get().getId();
        systemRepository.delete(id, environmentId, dateTimeUtil.timestampAsUtc(), userId, true);
    }

    @Override
    @Transactional
    public void deleteLinkedServices(UUID parentSystemId, UUID environmentId) {
        List<System> services = getLinkedSystemByParentId(parentSystemId);
        List<UUID> serviceIds = services.stream()
                .map(System::getId)
                .collect(Collectors.toList());
        deleteSystemsByIds(serviceIds, environmentId);
    }

    @Override
    @Transactional
    public void deleteSystemsByIds(List<UUID> systemIds, UUID environmentId) {
        UUID userId = userInfoProvider.get().getId();
        systemIds.forEach(id -> systemRepository
                .delete(id, environmentId, dateTimeUtil.timestampAsUtc(), userId,
                        false));
        kafkaService.sendEnvironmentKafkaNotification(environmentId, EventType.UPDATE,
                environmentRepository.getProjectId(environmentId));
    }

    @Override
    public List<Connection> getConnections(UUID systemId) {
        return connectionRepository.getAllByParentId(systemId);
    }

    @Override
    public Connection getConnectionBySystemIdAndConnectionType(UUID systemId, UUID connectionType) {
        List<Connection> connections = connectionRepository.getAllByParentIdAndConnectionType(systemId, connectionType);
        return !connections.isEmpty() ? connections.get(0) : null;
    }

    /**
     * Save System status and date of last check system status in DB.
     *
     * @param id     System id.
     * @param status System status.
     * @return {@link System}
     */
    public System saveStatusAndDateOfLastCheck(UUID id, Status status) {
        systemRepository.getContext().setFullDbFetching(true);
        UUID userId = userInfoProvider.get().getId();
        System system = get(id);
        return saveStatusAndDateOfLastCheck(system, status, userId);
    }

    /**
     * Save System status and date of last check system status in DB.
     *
     * @param system System.
     * @param status System status.
     * @return {@link System}
     */
    @Transactional
    public System saveStatusAndDateOfLastCheck(System system, Status status, UUID userId) {
        systemRepository.getContext().setFullDbFetching(true);
        MDC.put(MdcField.SYSTEM_ID.toString(), system.getId().toString());
        return systemRepository.update(system.getId(), system.getName(), system.getDescription(),
                dateTimeUtil.timestampAsUtc(), userId, system.getSystemCategoryId(),
                status, dateTimeUtil.timestampAsUtc(),
                system.getVersion(), system.getDateOfCheckVersion(),
                system.getParametersGettingVersion(), system.getParentSystemId(),
                system.getServerItf(), system.getMergeByName(), system.getLinkToSystemId(), system.getExternalId(),
                system.getExternalName(), true, false);
    }

    /**
     * Save List of System statuses and date of last check system status in DB.
     *
     * @param statuses System statuses.
     * @return list of {@link System}
     */

    @Override
    public List<System> saveStatusesAndDateOfLastCheck(List<StatusDto> statuses, UUID projectId) {
        try {
            systemRepository.getContext().setFullDbFetching(true);
            UUID userId = userInfoProvider.get().getId();
            List<UUID> systemIds = statuses.stream()
                    .map(StatusDto::getSystemId)
                    .collect(Collectors.toList());
            List<System> systems = systemRepository.getSystemsByIdsAndProjectId(systemIds, projectId);
            Map<UUID, StatusDto> statusesMap = Maps
                    .uniqueIndex(statuses, StatusDto::getSystemId);
            Map<System, Status> newStatusesMap = new HashMap<>();
            for (System system : systems) {
                newStatusesMap.put(system, Status.valueOf(statusesMap.get(system.getId()).getStatus()));
            }
            return newStatusesMap
                    .entrySet()
                    .stream()
                    .map(entry -> saveStatusAndDateOfLastCheck(entry.getKey(), entry.getValue(), userId))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error occurred while statuses updating in project : {}", projectId, e);
            return Collections.emptyList();
        }
    }

    /**
     * Save system version and date of last check system version in DB.
     *
     * @param system  System where version should updated.
     * @param version System version.
     * @return {@link System}
     */
    @Transactional
    @Override
    @CachePut(value = HazelcastMapName.SYSTEM_VERSION, key = "#system.id",
            condition = "#system.id!=null")
    public System saveVersionAndDateOfLastCheck(System system, String version) {
        systemRepository.getContext().setFullDbFetching(true);
        UUID userId = userInfoProvider.get().getId();
        return systemRepository.update(system.getId(), system.getName(), system.getDescription(),
                dateTimeUtil.timestampAsUtc(), userId, system.getSystemCategoryId(), system.getStatus(),
                system.getDateOfLastCheck(), version, dateTimeUtil.timestampAsUtc(),
                system.getParametersGettingVersion(), system.getParentSystemId(), system.getServerItf(),
                system.getMergeByName(), system.getLinkToSystemId(), system.getExternalId(),
                system.getExternalName(), false, false);
    }

    /**
     * Getting version by system.
     *
     * @param system System where version should updated.
     * @return String version
     */
    public String getSystemVersionByTypeCheck(System system) {
        systemRepository.getContext().setFullDbFetching(true);
        String version = null;
        Stopwatch timer = Stopwatch.createStarted();
        try {
            ParametersGettingVersion paramsGetVersion = system.getParametersGettingVersion();
            if (paramsGetVersion != null) {
                log.info("Start check version for system [ID:{}, Name:{}, Project ID:{}] by check version type: {}",
                        system.getId(), system.getName(), system.getEnvironments().get(0).getProjectId(),
                        paramsGetVersion.getType());
                VersionChecker versionChecker = versionCheckerFactory.createChecker(system);
                version = getPostProcessedVersion(paramsGetVersion, versionChecker.getVersion(), system);
                log.info("End of check version for system [ID:{}, Name:{}, Project ID:{}] by check version type: {}",
                        system.getId(), system.getName(), system.getEnvironments().get(0).getProjectId(),
                        paramsGetVersion.getType());
            }
        } catch (RuntimeException e) {
            log.error("Error occurred while check version for system [ID:{}, Name:{}, Project ID:{}]",
                    system.getId(), system.getName(), system.getEnvironments().get(0).getProjectId());
            throw e;
        } finally {
            String projectId =
                    system == null || CollectionUtils.isEmpty(system.getEnvironments())
                            ? "unknown"
                            : system.getEnvironments().get(0).getProjectId().toString();
            metricService.checkVersionTimer(
                            "project", projectId)
                    .record(timer.stop().elapsed());
        }
        return !StringUtils.isBlank(version) ? version : "Unknown";
    }

    private String getPostProcessedVersion(ParametersGettingVersion paramsGetVersion, String version, System system) {
        if (!StringUtils.isEmpty(paramsGetVersion.getParsingValue())) {
            switch (paramsGetVersion.getParsingType()) {
                case REGEXP:
                    String threadName = Thread.currentThread().getName();
                    try {
                        changeRegexpThreadName(system, "regexpThread");
                        log.info("Start version processing by regexp. System ID:{}, Regexp Pattern:{} ",
                                system.getId(),
                                paramsGetVersion.getParsingValue());
                        return regexpHandler
                                .getByRegExp(version, paramsGetVersion.getParsingValue(), regexpTimeout);
                    } catch (RuntimeException e) {
                        log.error("Error while check version regexp processing, System ID:{}, Cause:{}",
                                system.getId(),
                                e.getMessage());
                        throw e;
                    } finally {
                        Thread.currentThread().setName(threadName);
                    }
                case JSONPATH:
                    return jsonPathHandler.getByJsonpath(version, paramsGetVersion.getParsingValue());
                default:
                    break;
            }
        }
        return version;
    }

    private void changeRegexpThreadName(System system, String threadPrefix) {
        if (system != null) {
            Environment environment = null;
            if (!CollectionUtils.isEmpty(system.getEnvironments())) {
                environment = system.getEnvironments().get(0);
            }
            String newThreadName =
                    threadPrefix + UUID.randomUUID()
                            + "-systemName:"
                            + system.getName();
            if (environment != null) {
                newThreadName += "-environmentName:"
                        + environment.getName()
                        + "-projectId:"
                        + environment.getProjectId();
            }
            Thread.currentThread().setName(newThreadName.replace(" ", "_"));
        }
    }

    /**
     * Update version by system id.
     *
     * @param id          System id.
     * @param updateCache flag for update system on version-cache
     * @return System
     */
    public System updateVersionBySystemId(UUID id, boolean updateCache) {
        systemRepository.getContext().setFullDbFetching(true);
        MDC.put(MdcField.SYSTEM_ID.toString(), id.toString());
        System system = get(id);
        return updateVersionBySystem(system, updateCache);
    }

    /**
     * Update version by system.
     *
     * @param system      System .
     * @param updateCache flag for update system on version-cache
     * @return System
     */
    public System updateVersionBySystem(System system, boolean updateCache) {
        systemRepository.getContext().setFullDbFetching(true);
        Preconditions.checkNotNull(system, "System not found.");
        String version = getSystemVersionByTypeCheck(system);
        Preconditions.checkNotNull(version, "Version not found.");
        if (updateCache) {
            return ref.saveVersionAndDateOfLastCheck(system, version);
        } else {
            return saveVersionAndDateOfLastCheck(system, version);
        }
    }

    /**
     * Get version with HTML-marking.
     *
     * @param system System for transformating.
     * @return String
     */
    @Override
    public System transformSystemVersionToHtml(System system) {
        String version = system.getVersion();
        StringBuilder tableTagWithVersion = new StringBuilder("<table>");
        for (String versionString : version.split("\n")) {
            tableTagWithVersion.append("<tr><td>").append(versionString).append("</td></tr>");
        }
        tableTagWithVersion.append("</table>");
        system.setVersion(tableTagWithVersion.toString());
        return system;
    }

    /**
     * Update version by environment id.
     *
     * @param id Environment id.
     * @return List Systems
     */
    @CacheEvict(value = HazelcastMapName.SYSTEMS_BY_ENVIRONMENT_ID, key = "#id", condition = "#id!=null")
    public List<System> updateVersionByEnvironmentId(UUID id) {
        systemRepository.getContext().setFullDbFetching(true);
        List<System> listSystems = systemRepository.getAllByParentId(id).stream()
                .filter(e -> e.getParametersGettingVersion() != null)
                .collect(Collectors.toList());
        List<System> listSystemsWithUpdatedVersions = new ArrayList<>();
        for (System system : listSystems) {
            MDC.put(MdcField.SYSTEM_ID.toString(), system.getId().toString());
            String version = "";
            System updatedSystem = null;
            try {
                version = getSystemVersionByTypeCheck(system);
                if (version != null) {
                    updatedSystem = ref.saveVersionAndDateOfLastCheck(system,
                            version);
                } else {
                    throw new NullPointerException("Version is null");
                }
            } catch (Exception e) {
                log.error("An error occurred while getting version of system"
                                + " (systemName: {} , systemId: {} )",
                        system.getName(),
                        system.getId().toString(), e);
                updatedSystem = ref.saveVersionAndDateOfLastCheck(system,
                        "Unknown");
                updatedSystem.setCheckVersionError(e.getMessage());
                updatedSystem.setVersion("ERROR");
            } finally {
                listSystemsWithUpdatedVersions.add(updatedSystem);
            }
        }
        return listSystemsWithUpdatedVersions;
    }

    @Override
    public List<System> getByIds(List<UUID> systems) {
        return systemRepository.getByListIds(systems);
    }

    @Override
    public List<System> createListFromCloudServer(List<UUID> serviceIds,
                                                  UUID cloudServerSystemId,
                                                  UUID environmentId,
                                                  Class<? extends ExternalCloudClient> clientClass) {
        systemRepository.getContext().setFullDbFetching(true);
        ExternalCloudClient cloudClient = getCloudClient(cloudServerSystemId, clientClass);
        List<System> createdSystems = new ArrayList<>();
        List<CloudService> serviceList = cloudClient
                .getServicesByServiceId(serviceIds);
        Connection httpConnectionTemplate = this.connectionRepository
                .getById(Constants.Environment.System.Connection.HTTP);
        UUID serviceTemplateId = getConstantByClientClass(clientClass, true);
        serviceList.forEach(service -> {
            System newService = create(environmentId,
                    service.getName(),
                    null,
                    serviceTemplateId,
                    new ParametersGettingVersion(),
                    null,
                    null,
                    false,
                    cloudServerSystemId,
                    service.getId(),
                    service.getName());
            if (service.getHost() != null && service.getHost().length() > 0) {
                ConnectionParameters parameters = httpConnectionTemplate.getParameters();
                String url = serviceTemplateId.equals(Constants.SystemCategories.KUBERNETES_SERVICE)
                        ? "https://" + service.getHost()
                        : service.getHost();
                parameters.put("url", url);
                Connection httpConnection = connectionService.create(
                        newService.getId(),
                        httpConnectionTemplate.getName(),
                        httpConnectionTemplate.getDescription(),
                        parameters,
                        httpConnectionTemplate.getConnectionType(),
                        httpConnectionTemplate.getId(),
                        systemRepository.getProjectId(newService.getId()),
                        httpConnectionTemplate.getServices()
                );
                newService.getConnections().add(httpConnection);
            }
            createdSystems.add(newService);
        });
        return createdSystems;
    }

    @Override
    public List<System> updateServicesFromCloudServer(UUID cloudServerSystemId,
                                                      Class<? extends ExternalCloudClient> clientClass) {
        systemRepository.getContext().setFullDbFetching(true);
        List<System> existingServices = systemRepository.getByLinkToSystemId(cloudServerSystemId);
        ExternalCloudClient client = getCloudClient(cloudServerSystemId, clientClass);
        List<CloudService> cloudServices = client
                .getServicesByExistingServices(existingServices);
        List<Connection> updatedConnections = new ArrayList<>();
        List<System> updatedSystems = new ArrayList<>();
        existingServices.forEach(existingService -> {
            List<CloudService> foundServices = cloudServices.stream()
                    .filter(cloudService -> nonNull(existingService.getExternalId())
                            && existingService.getExternalId().equals(cloudService.getId()))
                    .collect(Collectors.toList());
            if (foundServices.isEmpty()) {
                foundServices = cloudServices.stream()
                        .filter(cloudService -> nonNull(existingService.getExternalName())
                                && existingService.getExternalName().equals(cloudService.getName()))
                        .collect(Collectors.toList());
            }
            if (!foundServices.isEmpty()) {
                CloudService serviceFromCloudServer = foundServices.get(0);
                existingService.setName(serviceFromCloudServer.getName());
                existingService.setExternalName(serviceFromCloudServer.getName());
                existingService.setExternalId(serviceFromCloudServer.getId());
                String httpUrl = existingService.getSystemCategory()
                        .getId()
                        .equals(Constants.SystemCategories.KUBERNETES_SERVICE)
                        ? "https://" + serviceFromCloudServer.getHost()
                        : serviceFromCloudServer.getHost();
                if (httpUrl != null && httpUrl.length() > 0) {
                    List<Connection> httpConnections = new ArrayList<>();
                    existingService.getConnections().forEach(connection -> {
                        if (connection.getSourceTemplateId().equals(Constants.Environment.System.Connection.HTTP)) {
                            httpConnections.add(connection);
                        }
                    });
                    if (httpConnections.size() == 0) {
                        Connection httpConnectionTemplate = this.connectionRepository
                                .getById(Constants.Environment.System.Connection.HTTP);
                        ConnectionParameters parameters = httpConnectionTemplate.getParameters();
                        parameters.put("url", httpUrl);
                        updatedConnections.add(connectionService.create(
                                existingService.getId(),
                                httpConnectionTemplate.getName(),
                                httpConnectionTemplate.getDescription(),
                                parameters,
                                httpConnectionTemplate.getConnectionType(),
                                httpConnectionTemplate.getId(),
                                systemRepository.getProjectId(existingService.getId()),
                                httpConnectionTemplate.getServices()
                        ));
                    } else {
                        httpConnections.get(0).getParameters().put("url", httpUrl);
                        updatedConnections.add(connectionService.update(httpConnections.get(0)));
                    }
                }
                existingService.setConnections(updatedConnections);
                update(existingService);
                updatedSystems.add(existingService);
            }
        });
        return updatedSystems;
    }

    @Override
    public List<ShortExternalService> getShortExternalServices(UUID kubernetesServerSystemId,
                                                               Class<? extends ExternalCloudClient> clientClass) {
        return getCloudClient(kubernetesServerSystemId, clientClass).getShortServices();
    }

    @SuppressFBWarnings("REC_CATCH_EXCEPTION")
    private ExternalCloudClient getCloudClient(UUID kubernetesServerSystemId,
                                               Class<? extends ExternalCloudClient> clientClass) {
        systemRepository.getContext().setFullDbFetching(true);
        ExternalCloudClient client;
        try {
            System cloudServerSystem = systemRepository.getById(kubernetesServerSystemId);
            Preconditions.checkNotNull(cloudServerSystem, "Server for {} not found.", clientClass);
            Connection cloudConnection = getConnectionBySystemIdAndConnectionType(cloudServerSystem.getId(),
                    getConstantByClientClass(clientClass, false));
            Connection decryptedCloudConnection = decryptorService.decryptConnection(cloudConnection);
            Preconditions.checkNotNull(decryptedCloudConnection, "Project for {} not found.", clientClass);
            client = ExternalCloudClient.createClient(decryptedCloudConnection.getParameters(), clientClass);
        } catch (Exception e) {
            String clientClassName = clientClass.getName();
            log.error("Failed to create cloud client for class: {}", clientClassName);
            throw new EnvironmentCloudClientCreationException(clientClassName);
        }
        return client;
    }

    private UUID getConstantByClientClass(Class<? extends ExternalCloudClient> clientClass, boolean isSystemCategory) {
        if (clientClass.equals(OpenshiftClient.class)) {
            if (isSystemCategory) {
                return Constants.SystemCategories.OPENSHIFT_SERVICE;
            } else {
                return Constants.Environment.System.Connection.OPENSHIFT_SERVER;
            }
        }
        if (clientClass.equals(KubeClient.class)) {
            if (isSystemCategory) {
                return Constants.SystemCategories.KUBERNETES_SERVICE;
            } else {
                return Constants.Environment.System.Connection.KUBERNETES_PROJECT;
            }
        }
        String className = clientClass.getName();
        log.error("Failed to find cloud connection template by specified class: {}", className);
        throw new EnvironmentIllegalCloudConnectionTemplateClassException(className);
    }

    @Transactional
    @Override
    public System updateParametersGettingVersion(UUID id, ParametersGettingVersion parametersGettingVersion) {
        systemRepository.getContext().setFullDbFetching(true);
        UUID userId = userInfoProvider.get().getId();
        System system = systemRepository.getById(id);
        return systemRepository.update(id, system.getName(), system.getDescription(), dateTimeUtil.timestampAsUtc(),
                userId, system.getSystemCategoryId(), system.getStatus(), system.getDateOfLastCheck(),
                system.getVersion(), system.getDateOfCheckVersion(), parametersGettingVersion,
                system.getParentSystemId(), system.getServerItf(), system.getMergeByName(), system.getLinkToSystemId(),
                system.getExternalId(), system.getExternalName(), true, true);
    }

    @Override
    public List<System> getLinkedSystemByParentId(UUID systemId) {
        return systemRepository.getByLinkToSystemId(systemId);
    }

    @Override
    public System getBySourceId(UUID sourceId) {
        return systemRepository.getBySourceId(sourceId);
    }

    @Override
    public List<System> getSystemsByProjectId(UUID projectId) {
        return systemRepository.getSystemsByProjectId(projectId);
    }

    @Override
    public List<System> getSystemsByProjectIdAndCategoryName(UUID projectId, String categoryName) {
        UUID categoryId = systemCategoriesService.getByName(categoryName).getId();
        List<System> systemList = getSystemsByProjectId(projectId);
        return systemList.stream().filter(system -> categoryId.equals(system.getSystemCategoryId()))
                .collect(Collectors.toList());
    }

    @Override
    public System getShortSystem(UUID id) {
        return systemRepository.getShortById(id);
    }

    @Nullable
    @Override
    public System getSystemByNameAndEnvironmentId(String name, UUID environmentId) {
        return systemRepository.getSystemByNameAndEnvironmentId(name, environmentId);
    }

    @Override
    @Nonnull
    public String[] generateSystemsYaml(@Nonnull Collection<System> systems) {
        EnvgeneYamlGenerator yamlGenerator = new EnvgeneYamlGenerator();
        String deploymentParamsYaml = yamlGenerator.generateDeploymentParametersYaml(systems);
        String credentialsYaml = yamlGenerator.generateCredentialsYaml(systems);
        return new String[]{deploymentParamsYaml, credentialsYaml};
    }
}
