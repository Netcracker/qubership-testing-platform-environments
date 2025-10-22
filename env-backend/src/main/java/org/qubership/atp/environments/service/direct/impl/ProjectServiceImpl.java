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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.collections4.CollectionUtils;
import org.qubership.atp.auth.springbootstarter.entities.Operation;
import org.qubership.atp.auth.springbootstarter.entities.UserInfo;
import org.qubership.atp.auth.springbootstarter.exceptions.AtpException;
import org.qubership.atp.auth.springbootstarter.exceptions.AtpIllegalNullableArgumentException;
import org.qubership.atp.auth.springbootstarter.security.permissions.PolicyEnforcement;
import org.qubership.atp.auth.springbootstarter.ssl.Provider;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.Identified;
import org.qubership.atp.environments.model.Project;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.impl.Context;
import org.qubership.atp.environments.model.utils.Constants;
import org.qubership.atp.environments.model.utils.HazelcastMapName;
import org.qubership.atp.environments.repo.impl.ContextRepository;
import org.qubership.atp.environments.repo.impl.EnvironmentRepositoryImpl;
import org.qubership.atp.environments.repo.impl.ProjectRepositoryImpl;
import org.qubership.atp.environments.service.direct.ConnectionService;
import org.qubership.atp.environments.service.direct.EnvironmentService;
import org.qubership.atp.environments.service.direct.ProjectAccessService;
import org.qubership.atp.environments.service.direct.ProjectService;
import org.qubership.atp.environments.service.direct.SystemService;
import org.qubership.atp.environments.service.rest.server.dto.EnvironmentDto;
import org.qubership.atp.environments.service.rest.server.request.ProjectSearchRequest;
import org.qubership.atp.environments.utils.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.google.common.base.Preconditions;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;

@SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
@Service("projectService")
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepositoryImpl projectRepository;
    private final EnvironmentRepositoryImpl environmentRepository;
    private final EnvironmentService environmentService;
    private final SystemService systemService;
    private final ConnectionService connectionService;
    private final DateTimeUtil dateTimeUtil;
    private final ContextRepository contextRepo;
    private final Provider<UserInfo> userInfoProvider;
    private final PolicyEnforcement policyEnforcement;
    private final ProjectAccessService projectAccessService;

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Autowired
    public ProjectServiceImpl(ProjectRepositoryImpl projectRepository,
                              EnvironmentRepositoryImpl environmentRepository,
                              EnvironmentService environmentService,
                              SystemService systemService,
                              ConnectionService connectionService,
                              DateTimeUtil dateTimeUtil, ContextRepository contextRepo,
                              Provider<UserInfo> userInfoProvider, PolicyEnforcement policyEnforcement,
                              ProjectAccessService projectAccessService) {
        this.projectRepository = projectRepository;
        this.environmentRepository = environmentRepository;
        this.environmentService = environmentService;
        this.systemService = systemService;
        this.connectionService = connectionService;
        this.dateTimeUtil = dateTimeUtil;
        this.contextRepo = contextRepo;
        this.userInfoProvider = userInfoProvider;
        this.policyEnforcement = policyEnforcement;
        this.projectAccessService = projectAccessService;
    }

    @Nullable
    @Override
    public Project get(@Nonnull UUID id) {
        return projectRepository.getById(id);
    }

    @Nullable
    @Override
    public Project getShort(@Nonnull UUID id) {
        return projectRepository.getShortById(id);
    }

    @Nullable
    @Override
    public Project getShortByName(String name) {
        return projectRepository.getShortByName(name);
    }

    @Override
    public List<Project> getProjectsByRequest(ProjectSearchRequest request) {
        return projectRepository.getProjectsByRequest(request, projectAccessService.getProjectIdsWithAccess());
    }

    @Override
    public boolean existsById(@Nonnull UUID id) {
        return projectRepository.existsById(id);
    }

    @Nonnull
    @Override
    public List<Project> getAll() {
        return checkProjectAccess(projectRepository.getAll());
    }

    @Nonnull
    @Override
    public List<Project> getAllShort() {
        return checkProjectAccess(projectRepository.getAllShort());
    }

    @Nonnull
    @Override
    @Transactional
    public Project create(Project project) {
        projectRepository.getContext().setFullDbFetching(true);
        return projectRepository.create(project.getName(), project.getShortName(), project.getDescription(),
                dateTimeUtil.timestampAsUtc());
    }

    @Nonnull
    @Override
    @Transactional
    public Environment create(UUID projectId, EnvironmentDto environment, UUID categoryId) {
        contextRepo.setContext(new Context(true));
        Environment result = environmentService.create(projectId, environment.getName(), environment.getGraylogName(),
                environment.getDescription(), environment.getSsmSolutionAlias(),
                environment.getSsmInstanceAlias(), environment.getConsulEgressConfigPath(),
                categoryId, environment.getTags());
        List<System> system = environment.getSystems().stream()
                .map(systemDto -> systemService.create(result.getId(), systemDto))
                .collect(Collectors.toList());
        result.setSystems(system);
        return result;
    }

    @Nonnull
    @Override
    public Project replicate(@Nonnull UUID projectId, @Nonnull String name, String shortName, String description,
                             Long created) throws AtpException {
        projectRepository.getContext().setFullDbFetching(true);
        if (StringUtils.isEmpty(name)) {
            log.error("Found illegal nullable project name parameter");
            throw new AtpIllegalNullableArgumentException("project name", "method argument");
        }
        return projectRepository.create(projectId, name, null, description, created);
    }

    @Nonnull
    @Override
    @Transactional
    public Project createAsIs(Project project) {
        projectRepository.getContext().setFullDbFetching(true);
        return projectRepository.create(
                project.getId(),
                project.getName(),
                project.getShortName(),
                project.getDescription(),
                dateTimeUtil.timestampAsUtc());
    }

    @Nonnull
    @Override
    @Transactional
    public Project copy(UUID id, String name, String shortName, String description) {
        projectRepository.getContext().setFullDbFetching(true);
        UUID userId = userInfoProvider.get().getId();
        Project sourceProject = projectRepository.getById(id);
        Preconditions.checkNotNull(sourceProject, "Project not found by id:" + id);
        Project newProject = projectRepository.create(name, shortName, description, dateTimeUtil.timestampAsUtc());
        sourceProject.getEnvironments().forEach(environment -> {
            Environment newEnv = environmentRepository.create(environment.getName(),
                    environment.getGraylogName(),
                    environment.getDescription(),
                    environment.getSsmSolutionAlias(),
                    environment.getSsmInstanceAlias(),
                    environment.getConsulEgressConfigPath(),
                    dateTimeUtil.timestampAsUtc(),
                    userId,
                    newProject.getId(),
                    environment.getCategoryId(),
                    environment.getTags());
            environment.getSystems().forEach(system -> environmentService.createSystem(newEnv.getId(), system));
        });
        return newProject;
    }

    @Override
    @Transactional
    public void update(UUID id, String name, String shortName, String description) {
        projectRepository.getContext().setFullDbFetching(true);
        projectRepository.update(id, name, shortName, description, dateTimeUtil.timestampAsUtc());
    }

    @Nonnull
    @Override
    @CacheEvict(value = HazelcastMapName.PROJECTS_CACHE, key = "#id")
    public void update(UUID id, String name, String shortName) {
        projectRepository.getContext().setFullDbFetching(true);
        projectRepository.update(id, name, shortName, dateTimeUtil.timestampAsUtc());
    }

    @Override
    @Transactional
    @CacheEvict(value = HazelcastMapName.PROJECTS_CACHE, key = "#projectId")
    public void delete(UUID projectId) {
        projectRepository.delete(projectId);
    }

    @Override
    public List<Environment> getEnvironments(UUID projectId) {
        return environmentRepository.getAllByParentId(Objects.requireNonNull(this.get(projectId)).getId(),
                Constants.Environment.Category.ENVIRONMENT);
    }

    @Override
    public List<Environment> getShortEnvironments(UUID projectId) {
        return environmentRepository.getAllShortByParentId(Objects.requireNonNull(this.get(projectId)).getId(),
                Constants.Environment.Category.ENVIRONMENT);
    }

    @Override
    public List<Environment> getTemporaryEnvironments(UUID projectId) {
        return environmentRepository.getAllByParentId(Objects.requireNonNull(this.get(projectId)).getId(),
                Constants.Environment.Category.TEMPORARY_ENVIRONMENT);
    }

    @Override
    public List<Environment> getAllEnvironments(UUID projectId) {
        return environmentRepository.getAllByParentId(Objects.requireNonNull(this.get(projectId)).getId(),
                Constants.Environment.Category.ENVIRONMENT, Constants.Environment.Category.TEMPORARY_ENVIRONMENT);
    }

    @Override
    public List<Environment> getTools(UUID projectId) {
        return environmentRepository.getAllByParentId(Objects.requireNonNull(this.get(projectId)).getId(),
                Constants.Environment.Category.TOOL);
    }

    @Override
    public List<Environment> getShortTools(UUID projectId) {
        return environmentRepository.getAllShortByParentId(Objects.requireNonNull(this.get(projectId)).getId(),
                Constants.Environment.Category.TOOL);
    }

    @Override
    public List<System> getSystemsByProjectId(UUID projectId) {
        return systemService.getSystemsByProjectId(projectId);
    }

    @Override
    public List<System> getSystemsByProjectIdAndCategoryName(UUID projectId, String categoryName) {
        return systemService.getSystemsByProjectIdAndCategoryName(projectId, categoryName);
    }

    @Override
    public List<Connection> getConnections(UUID projectId) {
        return connectionService.getConnectionsByProjectId(projectId);
    }

    @Override
    public List<String> getSystemNames(UUID projectId) {
        return environmentRepository.getSystemNamesByProjectId(projectId);
    }

    @Override
    public List<String> getConnectionNames(UUID projectId) {
        return environmentRepository.getConnectionNamesByProjectId(projectId);
    }

    @Override
    public List<Project> getProjectsByHost(String host) {
        contextRepo.setContext(new Context(true));
        List<Connection> connections = connectionService.getConnectionByHost(host);
        List<UUID> sysIds = connections.stream().map(Connection::getSystemId).collect(Collectors.toList());
        List<System> systems = new ArrayList<>(systemService.getByIds(sysIds));
        List<Environment> environments = new ArrayList<>();
        systems.forEach(system -> environments.addAll(system.getEnvironments()));
        Set<UUID> environmentsIds = environments.stream().map(Identified::getId).collect(Collectors.toSet());
        Set<UUID> prjIds =
                environments.stream().map(Environment::getProjectId).collect(Collectors.toSet());
        List<Project> projects = projectRepository.getByIds(prjIds);
        projects.forEach(project -> project.setEnvironments(
                project.getEnvironments().stream()
                        .filter(environment -> environmentsIds.contains(environment.getId()))
                        .collect(Collectors.toList())));
        return projects;
    }


    @Override
    public Project getProjectWithSpecifiedEnvironments(UUID projectId, List<UUID> environmentIds) {
        Project project = projectRepository.getLazyById(projectId);
        if (project != null) {
            project.setEnvironments(environmentService.getByIds(environmentIds));
        }
        return project;
    }

    private List<Project> checkProjectAccess(List<Project> projects) {
        return CollectionUtils.isEmpty(projects)
                ? Collections.emptyList()
                : projects.stream()
                .filter(project -> policyEnforcement.checkAccess(project.getId(), Operation.READ))
                .collect(Collectors.toList());
    }


}
