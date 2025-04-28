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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.qubership.atp.environments.model.ParametersGettingVersion;
import org.qubership.atp.environments.model.ServerItf;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.impl.ConnectionImpl;
import org.qubership.atp.environments.model.impl.SystemCategoryImpl;
import org.qubership.atp.environments.model.impl.SystemImpl;
import org.qubership.atp.environments.service.rest.server.dto.generated.HistoryItemDtoGenerated;
import org.qubership.atp.environments.service.rest.server.dto.generated.HistoryItemResponseDtoGenerated;
import org.qubership.atp.environments.versioning.model.entities.SystemJ;
import org.qubership.atp.environments.versioning.service.CommitEntityService;
import org.qubership.atp.environments.versioning.service.JaversHistoryService;
import org.springframework.beans.factory.annotation.Autowired;

public class GetHistoryTest extends AbstractServiceTest {

    @Autowired
    private CommitEntityService<System> commitSystemService;
    @Autowired
    private JaversHistoryService javersHistoryService;

    @Test
    public void historyOfOneSimpleFieldTest() {
        SystemImpl system = new SystemImpl();
        UUID id = UUID.randomUUID();
        system.setId(id);
        system.setName("Name 1");
        commitSystemService.commit(system);

        system.setName("Name 2");
        commitSystemService.commit(system);

        HistoryItemResponseDtoGenerated history = javersHistoryService.getAllHistory(id, SystemJ.class, 0, 10);

        assertThat(getHistoryItemByVersion(history, 2).getChanged(),
                containsInAnyOrder("name"));
    }

    @Test
    public void historyChainOfManySimpleFieldsTest() {
        SystemImpl system = new SystemImpl();
        UUID id = UUID.randomUUID();
        system.setId(id);
        system.setName("Name 1");
        commitSystemService.commit(system);

        system.setName("Name 2");
        system.setDescription("Description 1");
        commitSystemService.commit(system);

        system.setDescription("Description 2");
        system.setModified(1L);
        commitSystemService.commit(system);

        system.setModified(2L);
        SystemCategoryImpl systemCategory = new SystemCategoryImpl();
        systemCategory.setId(UUID.randomUUID());
        system.setSystemCategory(systemCategory);
        commitSystemService.commit(system);

        systemCategory.setId(UUID.randomUUID());
        ParametersGettingVersion parametersGettingVersion = new ParametersGettingVersion();
        parametersGettingVersion.setParameters("Parameters 1");
        system.setParametersGettingVersion(parametersGettingVersion);
        commitSystemService.commit(system);

        parametersGettingVersion.setParameters("Parameters 2");
        ServerItf serverItf = new ServerItf();
        serverItf.setUrl("Some URL");
        system.setServerItf(serverItf);
        commitSystemService.commit(system);

        HistoryItemResponseDtoGenerated history = javersHistoryService.getAllHistory(id, SystemJ.class, 0, 10);

        assertThat(getHistoryItemByVersion(history, 1).getChanged(),
                containsInAnyOrder("id", "name"));

        assertThat(getHistoryItemByVersion(history, 2).getChanged(),
                containsInAnyOrder("name", "description"));

        assertThat(getHistoryItemByVersion(history, 3).getChanged(),
                containsInAnyOrder("description", "modified"));

        assertThat(getHistoryItemByVersion(history, 4).getChanged(),
                containsInAnyOrder("modified", "systemCategoryId"));

        assertThat(getHistoryItemByVersion(history, 5).getChanged(),
                containsInAnyOrder("systemCategoryId", "parametersGettingVersion"));

        assertThat(getHistoryItemByVersion(history, 6).getChanged(),
                containsInAnyOrder("parametersGettingVersion", "serverItf"));
    }

    @Test
    public void addedToConnectionListTest() {
        SystemImpl system = new SystemImpl();
        UUID id = UUID.randomUUID();
        system.setId(id);
        commitSystemService.commit(system);

        ConnectionImpl connection = new ConnectionImpl();
        connection.setId(UUID.randomUUID());
        system.setConnections(Collections.singletonList(connection));
        commitSystemService.commit(system);

        HistoryItemResponseDtoGenerated history = javersHistoryService.getAllHistory(id, SystemJ.class, 0, 10);

        assertThat(getHistoryItemByVersion(history, 2).getAdded(),
                containsInAnyOrder("connections"));

        assertThat(getHistoryItemByVersion(history, 2).getChanged(),
                nullValue());

        assertThat(getHistoryItemByVersion(history, 2).getDeleted(),
                nullValue());
    }

    @Test
    public void deletedFromConnectionListTest() {
        SystemImpl system = new SystemImpl();
        UUID id = UUID.randomUUID();
        system.setId(id);
        commitSystemService.commit(system);

        ConnectionImpl connection = new ConnectionImpl();
        connection.setId(UUID.randomUUID());
        system.setConnections(Collections.singletonList(connection));
        commitSystemService.commit(system);

        system.setConnections(Collections.emptyList());
        commitSystemService.commit(system);

        HistoryItemResponseDtoGenerated history = javersHistoryService.getAllHistory(id, SystemJ.class, 0, 10);

        assertThat(getHistoryItemByVersion(history, 3).getAdded(),
                nullValue());

        assertThat(getHistoryItemByVersion(history, 3).getChanged(),
                nullValue());

        assertThat(getHistoryItemByVersion(history, 3).getDeleted(),
                containsInAnyOrder("connections"));
    }

    @Test
    public void changedConnectionListTest() {
        SystemImpl system = new SystemImpl();
        UUID id = UUID.randomUUID();
        system.setId(id);
        commitSystemService.commit(system);

        ConnectionImpl connection = new ConnectionImpl();
        connection.setId(UUID.randomUUID());
        connection.setName("Name 1");
        system.setConnections(Collections.singletonList(connection));
        commitSystemService.commit(system);

        connection.setName("Name 2");
        commitSystemService.commit(system);

        HistoryItemResponseDtoGenerated history = javersHistoryService.getAllHistory(id, SystemJ.class, 0, 10);

        assertThat(getHistoryItemByVersion(history, 3).getAdded(),
                nullValue());

        assertThat(getHistoryItemByVersion(history, 3).getChanged(),
                containsInAnyOrder("connections"));

        assertThat(getHistoryItemByVersion(history, 3).getDeleted(),
                nullValue());
    }

    @Test
    public void allTypesOfChangesWithConnectionListTest() {
        SystemImpl system = new SystemImpl();
        UUID id = UUID.randomUUID();
        system.setId(id);
        commitSystemService.commit(system);

        ConnectionImpl connectionForDelete = new ConnectionImpl();
        connectionForDelete.setId(UUID.randomUUID());
        ConnectionImpl connectionForChange = new ConnectionImpl();
        connectionForChange.setId(UUID.randomUUID());
        connectionForChange.setName("Name 1");
        system.setConnections(Arrays.asList(connectionForChange, connectionForDelete));
        commitSystemService.commit(system);

        connectionForChange.setName("Name 2");
        ConnectionImpl connectionForAdd = new ConnectionImpl();
        connectionForAdd.setId(UUID.randomUUID());
        system.setConnections(Arrays.asList(connectionForChange, connectionForAdd));
        commitSystemService.commit(system);

        HistoryItemResponseDtoGenerated history = javersHistoryService.getAllHistory(id, SystemJ.class, 0, 10);

        assertThat(getHistoryItemByVersion(history, 3).getAdded(),
                containsInAnyOrder("connections"));

        assertThat(getHistoryItemByVersion(history, 3).getChanged(),
                containsInAnyOrder("connections"));

        assertThat(getHistoryItemByVersion(history, 3).getDeleted(),
                containsInAnyOrder("connections"));
    }

    @Test
    public void paginationLimitOffsetTest() {
        SystemImpl system = new SystemImpl();
        UUID id = UUID.randomUUID();
        system.setId(id);

        Random random = new Random();
        int overLimitCount = random.nextInt(10) + 11;
        for (int i = 0; i < overLimitCount; i++) {
            system.setName(String.valueOf(i));
            commitSystemService.commit(system);
        }

        int mainLimitedCount = random.nextInt(10) + 1;
        for (int i = 0; i < mainLimitedCount; i++) {
            system.setDescription(String.valueOf(i));
            commitSystemService.commit(system);
        }

        int skippedByOffsetCount = random.nextInt(10) + 1;
        for (int i = 0; i < skippedByOffsetCount; i++) {
            system.setModified((long) i);
            commitSystemService.commit(system);
        }

        HistoryItemResponseDtoGenerated history = javersHistoryService.getAllHistory(id, SystemJ.class,
                skippedByOffsetCount, mainLimitedCount);

        assertEquals(skippedByOffsetCount, history.getPageInfo().getOffset().intValue());
        assertEquals(mainLimitedCount, history.getPageInfo().getLimit().intValue());

        history.getHistoryItems().forEach(historyItem -> {
            assertThat(historyItem.getChanged(),
                    allOf(
                            not(containsInAnyOrder("name")),
                            containsInAnyOrder("description"),
                            not(containsInAnyOrder("modified"))
                    )
            );
        });
    }

    @Test
    public void paginationTotalItemsCountTest() {
        SystemImpl system = new SystemImpl();
        UUID id = UUID.randomUUID();
        system.setId(id);

        int commits = new Random().nextInt(30) + 1;
        for (int i = 0; i < commits; i++) {
            system.setName(String.valueOf(i));
            commitSystemService.commit(system);
        }

        HistoryItemResponseDtoGenerated history = javersHistoryService.getAllHistory(id, SystemJ.class, 0, 10);
        Integer actualCount = history.getPageInfo().getItemsTotalCount();

        assertEquals(commits, actualCount.intValue());
    }

    private HistoryItemDtoGenerated getHistoryItemByVersion(HistoryItemResponseDtoGenerated history, Integer version) {
        return history.getHistoryItems()
                .stream()
                .filter(historyItem -> historyItem.getVersion().equals(version))
                .findFirst()
                .get();
    }

}
