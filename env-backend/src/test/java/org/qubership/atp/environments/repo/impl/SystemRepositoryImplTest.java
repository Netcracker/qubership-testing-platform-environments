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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.qubership.atp.environments.repo.impl.AbstractRepository.ENVIRONMENTS;
import static org.qubership.atp.environments.repo.impl.AbstractRepository.SYSTEMS;
import static org.qubership.atp.environments.repo.impl.AbstractRepository.SYSTEM_CATEGORIES;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import javax.inject.Provider;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubership.atp.environments.db.generated.QSystems;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.ParametersGettingVersion;
import org.qubership.atp.environments.model.ServerItf;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.SystemCategory;
import org.qubership.atp.environments.model.impl.ConnectionImpl;
import org.qubership.atp.environments.model.impl.EnvironmentImpl;
import org.qubership.atp.environments.model.impl.SystemCategoryImpl;
import org.qubership.atp.environments.model.impl.SystemImpl;
import org.qubership.atp.environments.model.utils.enums.Status;
import org.qubership.atp.environments.service.direct.KafkaService;
import org.qubership.atp.environments.versioning.service.CommitEntityService;

import com.google.common.collect.ImmutableList;
import com.mysema.commons.lang.CloseableIterator;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLUpdateClause;

/**
 * SystemRepositoryImplTest - test for {@link SystemRepositoryImpl}
 */
public class SystemRepositoryImplTest {

    private final ThreadLocal<SQLQueryFactory> queryFactory = new ThreadLocal<>();
    private final ThreadLocal<SQLQuery<Tuple>> sqlQuery = new ThreadLocal<>();
    private final ThreadLocal<SQLInsertClause> sqlInsertClause = new ThreadLocal<>();
    private final ThreadLocal<SQLUpdateClause> sqlUpdateClause = new ThreadLocal<>();
    private final ThreadLocal<CloseableIterator<Tuple>> iterate = new ThreadLocal<>();
    private final ThreadLocal<Tuple> row = new ThreadLocal<>();
    private final ThreadLocal<SystemRepositoryImpl> repository = new ThreadLocal<>();

    private System system;
    private SystemCategory systemCategory;
    private Environment environment;
    private Connection connection;
    private ParametersGettingVersion parametersGettingVersion;
    private ServerItf serverItf;
    private UUID testUUID;

    private static final Expression<?>[] COLUMNS;

    static {
        ImmutableList<Expression<?>> expressions =
                ImmutableList.<Expression<?>>builder()
                        .add(SYSTEMS.all())
                        .add(SYSTEM_CATEGORIES.all())
                        .add(ENVIRONMENTS.all())
                        .build();
        COLUMNS = expressions.toArray(new Expression[0]);
    }

    @BeforeEach
    public void setUp() throws Exception {
        Timestamp timestamp = new Timestamp(new Date().getTime());
        testUUID = UUID.randomUUID();
        environment = new EnvironmentImpl();
        environment.setCreated(timestamp.getTime());
        environment.setModified(timestamp.getTime());
        systemCategory = new SystemCategoryImpl();
        systemCategory.setCreated(timestamp.getTime());
        connection = new ConnectionImpl();
        parametersGettingVersion = new ParametersGettingVersion();
        serverItf = new ServerItf();
        system = new SystemImpl(testUUID, "system name", "system description", timestamp.getTime(), testUUID,
                timestamp.getTime(), testUUID, Collections.singletonList(environment), systemCategory,
                Collections.singletonList(connection), Status.PASS, timestamp.getTime(), "version", timestamp.getTime(),
                parametersGettingVersion, testUUID, serverItf, true, testUUID, testUUID, testUUID, null);

        SQLQueryFactory queryFactoryMock = mock(SQLQueryFactory.class);
        Provider environmentRepoMock = mock(Provider.class);
        when(environmentRepoMock.get()).thenReturn(mock(EnvironmentRepositoryImpl.class));

        queryFactory.set(queryFactoryMock);
        sqlQuery.set(mock(SQLQuery.class));
        sqlInsertClause.set(mock(SQLInsertClause.class));
        sqlUpdateClause.set(mock(SQLUpdateClause.class));
        iterate.set(mock(CloseableIterator.class));
        row.set(mock(Tuple.class));
        repository.set(new SystemRepositoryImpl(
                queryFactoryMock,
                environmentRepoMock,
                mock(Provider.class),
                mock(Provider.class),
                mock(CommitEntityService.class),
                mock(CommitEntityService.class),
                mock(KafkaService.class),
                null,
                mock(ContextRepository.class)));
    }

    @Test
    public void getSystemByIdV2_shouldGet_whenAllParametersProvided() {
        when(queryFactory.get().select(COLUMNS)).thenReturn(sqlQuery.get());
        when(sqlQuery.get().from(any(QSystems.class))).thenReturn(sqlQuery.get());
        when(sqlQuery.get().leftJoin(any())).thenReturn(sqlQuery.get());
        when(sqlQuery.get().on(any(Predicate.class))).thenReturn(sqlQuery.get());
        when(sqlQuery.get().where(any(Predicate.class))).thenReturn(sqlQuery.get());
        when(sqlQuery.get().iterate()).thenReturn(iterate.get());
        when(iterate.get().hasNext()).thenReturn(true).thenReturn(false);
        when(iterate.get().next()).thenReturn(row.get());
        when(row.get().get(SYSTEMS.id)).thenReturn(testUUID);
        when(row.get().get(SYSTEMS.created)).thenReturn(new Timestamp(system.getCreated()));
        when(row.get().get(SYSTEMS.modified)).thenReturn(new Timestamp(system.getModified()));
        when(row.get().get(SYSTEMS.dateOfLastCheck)).thenReturn(new Timestamp(system.getDateOfLastCheck()));
        when(row.get().get(SYSTEMS.dateOfCheckVersion)).thenReturn(new Timestamp(system.getDateOfCheckVersion()));
        when(row.get().get(SYSTEMS.description)).thenReturn(system.getDescription());
        when(row.get().get(SYSTEMS.name)).thenReturn(system.getName());
        when(row.get().get(SYSTEMS.status)).thenReturn(system.getStatus().toString());
        when(row.get().get(SYSTEMS.version)).thenReturn(system.getVersion());
        when(row.get().get(SYSTEMS.parametersGettingVersion)).thenReturn(null);
        when(row.get().get(SYSTEMS.serverItf)).thenReturn("{}");
        when(row.get().get(SYSTEMS.mergeByName)).thenReturn(false);
        when(row.get().get(ENVIRONMENTS.name)).thenReturn("environment name");
        when(row.get().get(ENVIRONMENTS.graylogName)).thenReturn("graylogName");
        when(row.get().get(ENVIRONMENTS.description)).thenReturn("environment description");
        when(row.get().get(ENVIRONMENTS.created)).thenReturn(new Timestamp(environment.getCreated()));
        when(row.get().get(ENVIRONMENTS.modified)).thenReturn(new Timestamp(environment.getModified()));
        when(row.get().get(SYSTEM_CATEGORIES.name)).thenReturn("System category name");
        when(row.get().get(SYSTEM_CATEGORIES.description)).thenReturn("System category description");
        when(row.get().get(SYSTEM_CATEGORIES.created)).thenReturn(new Timestamp(systemCategory.getCreated()));
        System systemResult = repository.get().getByIdV2(testUUID);

        Assertions.assertEquals(system, systemResult);
    }

    @Test
    public void createSystem_shouldCreate_whenAllParametersProvided() {
        when(queryFactory.get().insert(notNull())).thenReturn(sqlInsertClause.get());
        when(sqlInsertClause.get().set(any(), anyString())).thenReturn(sqlInsertClause.get());
        when(sqlInsertClause.get().set(any(), anyBoolean())).thenReturn(sqlInsertClause.get());
        when(sqlInsertClause.get().set(any(), anyLong())).thenReturn(sqlInsertClause.get());
        when(sqlInsertClause.get().set(any(), any(UUID.class))).thenReturn(sqlInsertClause.get());
        when(sqlInsertClause.get().set(any(), any(Timestamp.class))).thenReturn(sqlInsertClause.get());
        when(sqlInsertClause.get().set(any(), any(ServerItf.class))).thenReturn(sqlInsertClause.get());
        when(sqlInsertClause.get().set(any(), any(ParametersGettingVersion.class))).thenReturn(sqlInsertClause.get());
        when(sqlInsertClause.get().execute()).thenReturn(5L);
        when(queryFactory.get().update(notNull())).thenReturn(sqlUpdateClause.get());
        when(sqlUpdateClause.get().set(any(), any(UUID.class))).thenReturn(sqlUpdateClause.get());
        when(sqlUpdateClause.get().set(any(), any(Timestamp.class))).thenReturn(sqlUpdateClause.get());
        when(sqlUpdateClause.get().where(any(BooleanExpression.class))).thenReturn(sqlUpdateClause.get());
        when(sqlUpdateClause.get().execute()).thenReturn(4L);

        System systemResult =
                repository.get().create(testUUID, testUUID, system.getName(), system.getDescription(), system.getCreated(),
                        testUUID, testUUID, parametersGettingVersion, testUUID, serverItf, system.getMergeByName(),
                        testUUID, testUUID, testUUID, "");

        Assertions.assertEquals(system, systemResult);
    }
}
