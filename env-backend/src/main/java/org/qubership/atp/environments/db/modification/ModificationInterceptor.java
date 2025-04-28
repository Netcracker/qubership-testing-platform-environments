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

package org.qubership.atp.environments.db.modification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.aopalliance.intercept.Joinpoint;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.qubership.atp.environments.db.modification.afterbefore.CreateStrategy;
import org.qubership.atp.environments.db.modification.afterbefore.DeleteStrategy;
import org.qubership.atp.environments.db.modification.afterbefore.TrackedMethodStrategy;
import org.qubership.atp.environments.db.modification.afterbefore.UpdateStrategy;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.utils.tree.AllRefsIterator;
import org.qubership.atp.environments.service.direct.ConnectionService;
import org.qubership.atp.environments.service.direct.EnvironmentService;
import org.qubership.atp.environments.service.direct.SubscriptionService;
import org.qubership.atp.environments.service.direct.SystemService;
import org.qubership.atp.environments.service.direct.UpdateEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import com.google.common.collect.Iterators;

public class ModificationInterceptor implements MethodInterceptor {

    private static Logger LOG = LoggerFactory.getLogger(ModificationInterceptor.class);
    private final Map<Class, Handler> handlers = new ConcurrentHashMap<>();
    @Autowired
    @Lazy
    private SubscriptionService subscriptionService;
    @Autowired
    private UpdateEventService updateEventService;
    @Autowired
    @Lazy
    private SystemService systemService;
    @Autowired
    @Lazy
    private ConnectionService connectionService;
    @Autowired
    @Lazy
    private EnvironmentService environmentService;

    public ModificationInterceptor() {
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        return handlers.computeIfAbsent(methodInvocation.getThis().getClass(), this::createHandler)
                .apply(methodInvocation);
    }

    private Handler createHandler(Class clazz) {
        AllRefsIterator<Class> allClasses = new ClassHierarchy(clazz);
        Optional<Class> targetServiceOpt = Iterators.tryFind(allClasses,
                someClass -> someClass.getSimpleName().endsWith("Service")).toJavaUtil();
        if (!targetServiceOpt.isPresent()) {
            return Joinpoint::proceed;
        }
        Class targetService = targetServiceOpt.get();
        String targetServiceName = targetService.getSimpleName();
        String entityName = targetServiceName.substring(0, targetServiceName.length() - "Service".length());
        Optional<TrackedType> trackedTypeOpt = TrackedType.getByName(entityName);
        return trackedTypeOpt.<Handler>map(Notifier::new).orElse(Joinpoint::proceed);
    }

    private interface Handler {

        Object apply(MethodInvocation methodInvocation) throws Throwable;
    }

    public static class ProjectNotifier extends AbstractNotifier implements EntityTypeStrategy {

        /**
         * TODO Make javadoc documentation for this method.
         */
        public ProjectNotifier(UpdateEventService updateEventService, SubscriptionService subscriptionService,
                               TrackedMethod method) {
            super(updateEventService, subscriptionService, method, TrackedType.PROJECT);
        }

        @Override
        public Runnable prepareNotification(UUID projectId, ModificationInterceptor interceptor) throws Exception {
            List<UUID> listIdSubscriptionsForProject = subscriptionService.getListIdSubscriptionsForProject(projectId);
            return () -> {
                LOG.info("{} object with id {} {}", type, projectId, method.getName());
                notifyUpdateCurrentEntity(listIdSubscriptionsForProject, projectId);
            };
        }
    }

    public static class EnvironmentNotifier extends AbstractNotifier implements EntityTypeStrategy {

        /**
         * TODO Make javadoc documentation for this method.
         */
        public EnvironmentNotifier(UpdateEventService updateEventService, SubscriptionService subscriptionService,
                                   TrackedMethod method) {
            super(updateEventService, subscriptionService, method, TrackedType.ENVIRONMENT);
        }

        public void notifySystemCascadeDelete(UUID systemId) {
            changeStatusEntitiesUpdateEvents(subscriptionService.getListIdSubscriptionsForSystem(systemId), systemId,
                    TrackedMethod.DELETE, TrackedType.SYSTEM);
        }

        @Override
        public Runnable prepareNotification(UUID environmentId, ModificationInterceptor interceptor) throws Exception {
            UUID projectId = interceptor.environmentService.get(environmentId).getProjectId();
            List<UUID> listIdSubscriptionsForEnvironment =
                    subscriptionService.getListIdSubscriptionsForEnvironment(environmentId);
            List<System> systems = interceptor.environmentService.getSystems(environmentId);
            List<UUID> listIdCascadeSubscriptionsForProject =
                    subscriptionService.getListIdSubscriptionsForProject(projectId, true);
            return () -> {
                LOG.info("{} object with id {} {}", TrackedType.ENVIRONMENT, environmentId, method.getName());
                List<UUID> listIdSubscriptionsForProject =
                        subscriptionService.getListIdSubscriptionsForProject(projectId);
                notifyUpdateEntity(listIdSubscriptionsForProject, projectId, TrackedType.PROJECT);
                notifyUpdateCurrentEntity(listIdSubscriptionsForEnvironment, environmentId);
                // Related systems must be notified
                if (TrackedMethod.DELETE.equals(method)) {
                    for (System anySystem : systems) {
                        UUID idSystem = anySystem.getId();
                        notifySystemCascadeDelete(idSystem);
                    }
                }
                notifyUpdateEntity(listIdCascadeSubscriptionsForProject, environmentId, TrackedType.ENVIRONMENT);
            };
        }
    }

    public static class SystemNotifier extends AbstractNotifier implements EntityTypeStrategy {

        /**
         * TODO Make javadoc documentation for this method.
         */
        public SystemNotifier(UpdateEventService updateEventService, SubscriptionService subscriptionService,
                              TrackedMethod method) {
            super(updateEventService, subscriptionService, method, TrackedType.SYSTEM);
        }

        @Override
        public Runnable prepareNotification(UUID systemId, ModificationInterceptor interceptor) {
            List<Environment> listEnvironment = interceptor.systemService.get(systemId).getEnvironments();
            return () -> {
                LOG.info("{} object with id {} {}", TrackedType.SYSTEM, systemId, method.getName());
                List<UUID> listIdCascadeSubscriptionsForProject = new ArrayList<UUID>();
                List<UUID> listIdCascadeSubscriptionsForEnvironment = new ArrayList<UUID>();
                for (Environment environment : listEnvironment) {
                    UUID environmentId = environment.getId();
                    UUID projectId = environment.getProjectId();
                    listIdCascadeSubscriptionsForProject =
                            subscriptionService.getListIdSubscriptionsForProject(projectId, true);
                    listIdCascadeSubscriptionsForEnvironment =
                            subscriptionService.getListIdSubscriptionsForEnvironment(environmentId, true);
                    List<UUID> listIdSubscriptionsForSystem =
                            subscriptionService.getListIdSubscriptionsForSystem(systemId);
                    List<UUID> listIdSubscriptionsForProject =
                            subscriptionService.getListIdSubscriptionsForProject(projectId);
                    notifyUpdateEntity(listIdSubscriptionsForProject, projectId, TrackedType.PROJECT);
                    List<UUID> listIdSubscriptionsForEnvironment =
                            subscriptionService.getListIdSubscriptionsForEnvironment(environmentId);
                    notifyUpdateEntity(listIdSubscriptionsForEnvironment, environmentId, TrackedType.ENVIRONMENT);
                    notifyUpdateCurrentEntity(listIdSubscriptionsForSystem, systemId);
                    notifyUpdateCurrentEntity(listIdCascadeSubscriptionsForProject, systemId);
                    notifyUpdateCurrentEntity(listIdCascadeSubscriptionsForEnvironment, systemId);
                }
            };
        }
    }

    public static class ConnectionNotifier extends AbstractNotifier implements EntityTypeStrategy {

        /**
         * TODO Make javadoc documentation for this method.
         */
        public ConnectionNotifier(UpdateEventService updateEventService, SubscriptionService subscriptionService,
                                  TrackedMethod method) {
            super(updateEventService, subscriptionService, method, TrackedType.CONNECTION);
        }

        @Override
        public Runnable prepareNotification(UUID connectionId, ModificationInterceptor interceptor) {
            UUID systemId = interceptor.connectionService.get(connectionId).getSystemId();
            List<Environment> listEnvironment = interceptor.systemService.get(systemId).getEnvironments();
            return () -> {
                LOG.info("{} object with id {} {}", TrackedType.CONNECTION, connectionId,
                        method.getName());
                List<UUID> listIdCascadeSubscriptionsForEnvironment = new ArrayList<UUID>();
                List<UUID> listIdSubscriptionsForSystem = new ArrayList<UUID>();
                List<UUID> listIdCascadeSubscriptionsForProject = new ArrayList<UUID>();
                for (Environment environment : listEnvironment) {
                    UUID environmentId = environment.getId();
                    UUID projectId = environment.getProjectId();
                    listIdSubscriptionsForSystem =
                            subscriptionService.getListIdSubscriptionsForSystem(systemId);
                    listIdCascadeSubscriptionsForProject =
                            subscriptionService.getListIdSubscriptionsForProject(projectId, true);
                    listIdCascadeSubscriptionsForEnvironment =
                            subscriptionService.getListIdSubscriptionsForEnvironment(environmentId, true);
                    List<UUID> listIdSubscriptionsForProject =
                            subscriptionService.getListIdSubscriptionsForProject(projectId);
                    notifyUpdateEntity(
                            listIdSubscriptionsForProject, projectId, TrackedType.PROJECT);
                    List<UUID> listIdSubscriptionsForEnvironment =
                            subscriptionService.getListIdSubscriptionsForEnvironment(environmentId);
                    notifyUpdateEntity(listIdSubscriptionsForEnvironment, environmentId, TrackedType.ENVIRONMENT);
                    notifyUpdateEntity(listIdSubscriptionsForSystem, systemId, TrackedType.SYSTEM);
                    notifyUpdateEntity(listIdCascadeSubscriptionsForProject, systemId, TrackedType.SYSTEM);
                    notifyUpdateEntity(listIdCascadeSubscriptionsForEnvironment, systemId, TrackedType.SYSTEM);
                }
            };
        }
    }

    private static class ClassHierarchy extends AllRefsIterator<Class> {

        public ClassHierarchy(Class clazz) {
            super(Iterators.singletonIterator(clazz), false);
        }

        @Override
        protected Iterator<? extends Class> getChildren(@Nonnull Class parent) {
            return Stream.concat(Arrays.stream(parent.getInterfaces()), Stream.of(parent.getSuperclass()))
                    .filter(Objects::nonNull)
                    .iterator();
        }
    }

    private class Notifier implements Handler {

        private final TrackedType trackedType;

        private Notifier(TrackedType trackedType) {
            this.trackedType = trackedType;
        }

        @Override
        public Object apply(MethodInvocation methodInvocation) throws Throwable {
            Class<?> returnType = methodInvocation.getMethod().getReturnType();
            EntityTypeStrategy entityStrategy;
            TrackedMethodStrategy strategy;
            Optional<TrackedMethod> trackedMethodOpt = TrackedMethod.getByName(methodInvocation.getMethod().getName());
            if (!trackedMethodOpt.isPresent()) {
                return methodInvocation.proceed();
            }
            TrackedMethod trackedMethod = trackedMethodOpt.get();
            switch (trackedType) {
                case PROJECT:
                    entityStrategy = new ProjectNotifier(updateEventService, subscriptionService, trackedMethod);
                    break;
                case ENVIRONMENT:
                    entityStrategy = new EnvironmentNotifier(updateEventService, subscriptionService, trackedMethod);
                    break;
                case SYSTEM:
                    entityStrategy = new SystemNotifier(updateEventService, subscriptionService, trackedMethod);
                    break;
                case CONNECTION:
                    entityStrategy = new ConnectionNotifier(updateEventService, subscriptionService, trackedMethod);
                    break;
                default:
                    return methodInvocation.proceed();
            }
            switch (trackedMethod) {
                case CREATE:
                    if (!trackedType.entityType.isAssignableFrom(returnType)
                            && !UUID.class.isAssignableFrom(returnType)) {
                        return methodInvocation.proceed();
                    }
                    strategy = new CreateStrategy(ModificationInterceptor.this, entityStrategy);
                    break;
                case UPDATE:
                    strategy = new UpdateStrategy(ModificationInterceptor.this, entityStrategy);
                    break;
                case DELETE:
                    strategy = new DeleteStrategy(ModificationInterceptor.this, entityStrategy);
                    break;
                default:
                    return methodInvocation.proceed();
            }
            return strategy.proceed(methodInvocation);
        }
    }
}
