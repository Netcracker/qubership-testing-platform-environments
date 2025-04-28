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

package org.qubership.atp.environments.repo.impl;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Provider;

import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.Project;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.impl.Context;
import org.qubership.atp.environments.model.utils.Constants;
import org.qubership.atp.environments.repo.projections.FullProjectProjection;
import org.qubership.atp.environments.repo.projections.LazyProjectProjection;
import org.qubership.atp.environments.repo.projections.ShortProjectProjection;
import org.qubership.atp.environments.service.direct.KafkaService;
import org.qubership.atp.environments.service.rest.server.dto.EventType;
import org.qubership.atp.environments.service.rest.server.request.ProjectSearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.google.common.base.Preconditions;
import com.querydsl.core.types.MappingProjection;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLQueryFactory;

@Repository
@SuppressWarnings("CPD-START")
public class ProjectRepositoryImpl extends AbstractRepository implements ProjectionRepository<Project> {

    private final SQLQueryFactory queryFactory;
    private final Provider<EnvironmentRepositoryImpl> environmentRepo;
    private final Provider<SystemRepositoryImpl> systemRepo;


    private final FullProjectProjection projection = new FullProjectProjection(this);
    private final ShortProjectProjection shortProjectProjection = new ShortProjectProjection(this);
    private final LazyProjectProjection lazyProjection = new LazyProjectProjection(this);

    private final ContextRepository contextRepository;
    private final KafkaService kafkaService;

    /**
     * Constructor of ProjectRepository.
     */
    @Autowired
    public ProjectRepositoryImpl(SQLQueryFactory queryFactory, Provider<EnvironmentRepositoryImpl> environmentRepo,
                                 Provider<SystemRepositoryImpl> systemRepo,
                                 ContextRepository repository, KafkaService kafkaService) {
        this.queryFactory = queryFactory;
        this.environmentRepo = environmentRepo;
        this.systemRepo = systemRepo;
        this.contextRepository = repository;
        this.kafkaService = kafkaService;
    }

    public Provider<EnvironmentRepositoryImpl> getEnvironmentRepo() {
        return environmentRepo;
    }

    /**
     * Getting project by id.
     */
    @Nullable
    public Project getById(@Nonnull UUID id) {
        Project project = queryFactory.select(resolveProjection()).from(PROJECTS)
                .where(PROJECTS.id.eq(id)).fetchOne();
        Preconditions.checkNotNull(project, "Project %s not found", id);
        return project;
    }

    /**
     * Getting short project by id.
     */
    @Nullable
    public Project getShortById(@Nonnull UUID id) {
        Project project = queryFactory.select(shortProjectProjection).from(PROJECTS)
                .where(PROJECTS.id.eq(id)).fetchOne();
        Preconditions.checkNotNull(project, "Project %s not found", id);
        return project;
    }

    /**
     * Getting short project by name.
     */
    @Nullable
    public Project getShortByName(@Nonnull String name) {
        Project project = queryFactory.select(shortProjectProjection).from(PROJECTS)
                .where(PROJECTS.name.eq(name)).fetchOne();
        Preconditions.checkNotNull(project, "Project %s not found", name);
        return project;
    }

    @Transactional
    public boolean existsById(@Nonnull UUID id) {
        return queryFactory.select(shortProjectProjection).from(PROJECTS).where(PROJECTS.id.eq(id))
                .fetchCount() > 0;
    }

    @Nonnull
    public List<Project> getAll() {
        return queryFactory.select(resolveProjection()).from(PROJECTS)
                .where(PROJECTS.id.ne(Constants.Project.DEFAULT)).orderBy(PROJECTS.name.asc()).fetch();
    }

    @Nonnull
    public List<Project> getAllShort() {
        return queryFactory.select(shortProjectProjection).from(PROJECTS)
                .where(PROJECTS.id.ne(Constants.Project.DEFAULT)).orderBy(PROJECTS.name.asc()).fetch();
    }

    /**
     * Creates project on Env side by id, name, shortName, description.
     *
     * @param id          - project id
     * @param name        - project name
     * @param shortName   - alias
     * @param description - project description
     * @param created     - created time
     * @return - project entity
     */
    @Nonnull
    public Project create(@Nonnull UUID id,
                          @Nonnull String name,
                          @Nullable String shortName,
                          String description,
                          Long created) {
        Timestamp createdTimestamp = new Timestamp(created);
        queryFactory.insert(PROJECTS)
                .set(PROJECTS.name, name)
                .set(PROJECTS.shortName, name)
                .set(PROJECTS.description, description)
                .set(PROJECTS.created, createdTimestamp)
                .set(PROJECTS.id, id)
                .execute();
        return projection.create(id, name, shortName, description, createdTimestamp, null);
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    public Project create(@Nonnull String name,
                          @Nullable String shortName,
                          String description,
                          Long created) {
        Timestamp createdTimestamp = new Timestamp(created);
        UUID id = queryFactory.insert(PROJECTS)
                .set(PROJECTS.name, name)
                .set(PROJECTS.shortName, name)
                .set(PROJECTS.description, description)
                .set(PROJECTS.created, createdTimestamp)
                .executeWithKey(PROJECTS.id);
        return projection.create(id, name, shortName, description, createdTimestamp, null);
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    @Nonnull
    public Project update(@Nonnull UUID projectId,
                          @Nonnull String name,
                          @Nullable String shortName,
                          String description,
                          Long modified) {
        Timestamp modifiedTimestamp = new Timestamp(modified);
        long update = queryFactory.update(PROJECTS)
                .set(PROJECTS.name, name)
                .set(PROJECTS.shortName, name)
                .set(PROJECTS.description, description)
                .set(PROJECTS.modified, modifiedTimestamp)
                .where(PROJECTS.id.eq(projectId)).execute();
        Preconditions.checkArgument(update > 0, "Nothing updated");
        Timestamp created = queryFactory.select(PROJECTS.created)
                .from(PROJECTS)
                .where(PROJECTS.id.eq(projectId)).fetchOne();
        return projection.create(projectId, name, shortName, description, created, modifiedTimestamp);
    }

    /**
     * Updates project on Env side.
     *
     * @param projectId - project id
     * @param name      - project name
     * @param shortName - alias
     * @param modified  - time
     * @return - updated entity
     */
    @Nonnull
    public Project update(@Nonnull UUID projectId,
                          @Nonnull String name,
                          @Nullable String shortName,
                          Long modified) {
        Timestamp modifiedTimestamp = new Timestamp(modified);
        long update = queryFactory.update(PROJECTS)
                .set(PROJECTS.name, name)
                .set(PROJECTS.shortName, name)
                .set(PROJECTS.modified, modifiedTimestamp)
                .where(PROJECTS.id.eq(projectId)).execute();
        Preconditions.checkArgument(update > 0, "Nothing updated");
        Timestamp created = queryFactory.select(PROJECTS.created)
                .from(PROJECTS)
                .where(PROJECTS.id.eq(projectId)).fetchOne();
        return projection.create(projectId, name, shortName, created, modifiedTimestamp);
    }

    public void delete(UUID projectId) {
        deleteReferenceToTable(projectId, PROJECTS, PROJECTS.id);
        sendProjectDeleteNotification(projectId);
    }

    private void deleteReferenceToTable(UUID projectId, RelationalPathBase path, SimplePath<UUID> simplePath) {
        queryFactory.delete(path).where(simplePath.eq(projectId)).execute();
    }

    private void sendProjectDeleteNotification(UUID projectId) {
        Project project;
        try {
            project = getById(projectId);
        } catch (NullPointerException e) {
            return;
        }
        if (!CollectionUtils.isEmpty(project.getEnvironments())) {
            project.getEnvironments().forEach(environment -> {
                kafkaService.sendEnvironmentKafkaNotification(environment.getId(), EventType.DELETE, projectId);
                if (!CollectionUtils.isEmpty(environment.getSystems())) {
                    environment.getSystems()
                            .forEach(system -> kafkaService
                                    .sendSystemKafkaNotification(system.getId(), EventType.DELETE, projectId));
                }
            });
        }
    }

    public List<Project> getByIds(Set<UUID> prjIds) {
        return queryFactory.select(projection).from(PROJECTS).where(PROJECTS.id.in(prjIds)).fetch();
    }

    /**
     * Get lazy project by ID.
     *
     * @param id project id
     * @return {@link Project} instance
     */
    public Project getLazyById(UUID id) {
        Project project = queryFactory.select(shortProjectProjection).from(PROJECTS)
                .where(PROJECTS.id.eq(id)).fetchOne();
        Preconditions.checkNotNull(project, "Project %s not found", id);
        return project;
    }

    @Override
    public Context getContext() {
        return contextRepository.getContext();
    }

    @Override
    public MappingProjection<Project> getFullProjection() {
        return projection;
    }

    @Override
    public MappingProjection<Project> getLazyProjection() {
        return lazyProjection;
    }

    @Override
    public MappingProjection<Project> getShortProjection() {
        return shortProjectProjection;
    }

    /**
     * Get projects by request and list pf accessed project ids.
     *
     * @param request for getting projects.
     * @param projectIdWithAccess list of the accessed project ids.
     *
     * @return list of the projects
     */
    public List<Project> getProjectsByRequest(ProjectSearchRequest request, List<UUID> projectIdWithAccess) {
        List<UUID> projectIds = CollectionUtils.isEmpty(request.getProjectIds())
                ? projectIdWithAccess
                : request.getProjectIds()
                    .stream()
                    .filter(projectIdWithAccess::contains)
                    .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(projectIds)) {
            return Collections.emptyList();
        }
        BooleanExpression projectPredicate = PROJECTS.id.in(projectIds);
        List<String> projectNames = request.getProjectNames();
        if (!CollectionUtils.isEmpty(projectNames)) {
            projectPredicate = projectPredicate.and(PROJECTS.name.in(projectNames));
        }
        List<Project> projects = queryFactory.select(getShortProjection()).from(PROJECTS)
                .where(projectPredicate).fetch();
        for (Project project: projects) {
            fillEnvironmentForProject(request, project);
        }
        return projects;
    }

    private void fillEnvironmentForProject(ProjectSearchRequest request, Project project) {
        BooleanExpression environmentPredicate = ENVIRONMENTS.projectId.in(project.getId());
        List<UUID> environmentIds = request.getEnvironmentIds();
        if (!CollectionUtils.isEmpty(environmentIds)) {
            environmentPredicate = environmentPredicate.and(ENVIRONMENTS.id.in(environmentIds));
        }
        List<String> environmentNames = request.getEnvironmentNames();
        if (!CollectionUtils.isEmpty(environmentNames)) {
            environmentPredicate = environmentPredicate.and(ENVIRONMENTS.name.in(environmentNames));
        }
        List<Environment> environments = queryFactory
                .select(environmentRepo.get().getShortProjection()).where(environmentPredicate)
                .from(ENVIRONMENTS).fetch();
        for (Environment environment: environments) {
            fillSystemsForEnvironment(request, environment);
        }
        project.setEnvironments(environments);
    }

    private void fillSystemsForEnvironment(ProjectSearchRequest request, Environment environment) {
        BooleanExpression systemPredicate = ENVIRONMENT_SYSTEMS.environmentId.eq(environment.getId());
        List<UUID> systemIds = request.getSystemIds();
        if (!CollectionUtils.isEmpty(systemIds)) {
            systemPredicate = systemPredicate.and(SYSTEMS.id.in(systemIds));
        }
        List<String> systemNames = request.getSystemNames();
        if (!CollectionUtils.isEmpty(systemNames)) {
            systemPredicate = systemPredicate.and(SYSTEMS.name.in(systemNames));
        }
        List<System> systems = queryFactory
                .select(systemRepo.get().getFullProjection()).from(SYSTEMS)
                .leftJoin(ENVIRONMENT_SYSTEMS).on(ENVIRONMENT_SYSTEMS.systemId.eq(SYSTEMS.id))
                .where(systemPredicate).fetch();
        environment.setSystems(systems);
    }
}


