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

package org.qubership.atp.environments.versioning.service.impl;

import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.javers.core.Changes;
import org.javers.core.ChangesByCommit;
import org.javers.core.Javers;
import org.javers.core.commit.CommitId;
import org.javers.core.diff.changetype.PropertyChange;
import org.javers.core.diff.changetype.container.ContainerChange;
import org.javers.repository.jql.JqlQuery;
import org.javers.repository.jql.QueryBuilder;
import org.qubership.atp.environments.service.rest.server.dto.generated.HistoryItemDtoGenerated;
import org.qubership.atp.environments.service.rest.server.dto.generated.HistoryItemResponseDtoGenerated;
import org.qubership.atp.environments.service.rest.server.dto.generated.PageInfoDtoGenerated;
import org.qubership.atp.environments.versioning.model.entities.AbstractJaversEntity;
import org.qubership.atp.environments.versioning.model.values.ConnectionJ;
import org.qubership.atp.environments.versioning.service.JaversHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JaversHistoryServiceImpl implements JaversHistoryService {

    private final Javers javers;

    @Autowired
    public JaversHistoryServiceImpl(Javers javers) {
        this.javers = javers;
    }

    @Override
    public HistoryItemResponseDtoGenerated getAllHistory(UUID id,
                                                         Class<? extends AbstractJaversEntity> type,
                                                         Integer offset, Integer limit) {
        log.debug("Retrieving entity history. Input parameters: id = {}, type = {}, offset = {}, limit = {}",
                id, type, offset, limit);
        JqlQuery query = getQuery(id, type, offset, limit);
        Changes changes = javers.findChanges(query);
        List<ChangesByCommit> changesByCommits = changes.groupByCommit();
        List<HistoryItemDtoGenerated> historyItemDtoList = changesByCommits
                .stream()
                .map(changesByCommit -> createHistoryItem(id, type, changesByCommit))
                .collect(Collectors.toList());
        HistoryItemResponseDtoGenerated response = new HistoryItemResponseDtoGenerated();
        response.setHistoryItems(historyItemDtoList);
        response.setPageInfo(getPageInfo(id, type, offset, limit));
        log.debug("Entity history retrieval completed. Input parameters: id = {}, type = {}, offset = {}, limit = {}. "
                + "Result = {}", id, type, offset, limit, response);
        return response;
    }

    private HistoryItemDtoGenerated createHistoryItem(UUID id,
                                                      Class<? extends AbstractJaversEntity> type,
                                                      ChangesByCommit changesByCommit) {
        log.trace("Creating historyItem. Input parameters: id = {}, type = {}, changesByCommit = {}",
                id, type, changesByCommit);
        HistoryItemDtoGenerated historyItemDto = new HistoryItemDtoGenerated();
        Integer version = getVersionByCommit(id, type, changesByCommit.getCommit().getId());
        historyItemDto.setVersion(version);
        historyItemDto.setModifiedWhen(changesByCommit.getCommit().getCommitDate().atOffset(ZoneOffset.UTC));
        historyItemDto.setModifiedBy(changesByCommit.getCommit().getAuthor());
        changesByCommit.get()
                .stream()
                .filter(change -> change instanceof PropertyChange)
                .map(change -> (PropertyChange) change)
                .forEach((PropertyChange change) -> putChangeToHistoryItem(change, historyItemDto));
        log.trace("Created historyItem. Input parameters: id = {}, type = {}, changesByCommit = {}. Result = {}",
                id, type, changesByCommit, historyItemDto);
        return historyItemDto;
    }

    private void putChangeToHistoryItem(PropertyChange change, HistoryItemDtoGenerated historyItemDto) {
        log.trace("Processing propertyChange = {}", change);
        String propertyName = change.getPropertyName();
        if (change instanceof ContainerChange) {
            ContainerChange containerChange = (ContainerChange) change;
            List<?> addedValues = convertToSignificantValues(containerChange.getAddedValues());
            List<?> removedValues = convertToSignificantValues(containerChange.getRemovedValues());
            Collection<?> intersection = CollectionUtils.intersection(addedValues, removedValues);
            addedValues.removeAll(intersection);
            removedValues.removeAll(intersection);
            if (CollectionUtils.isNotEmpty(intersection)) {
                historyItemDto.addChangedItem(propertyName);
            }
            if (CollectionUtils.isNotEmpty(addedValues)) {
                historyItemDto.addAddedItem(propertyName);
            }
            if (CollectionUtils.isNotEmpty(removedValues)) {
                historyItemDto.addDeletedItem(propertyName);
            }
        } else {
            historyItemDto.addChangedItem(propertyName);
        }
    }

    private Integer getVersionByCommit(UUID id, Class<? extends AbstractJaversEntity> type, CommitId commitId) {
        JqlQuery query = getQuery(id, type, commitId);
        return (int) javers.findSnapshots(query).get(0).getVersion();
    }

    private List<?> convertToSignificantValues(List<?> values) {
        if (!values.isEmpty()) {
            Object obj = values.get(0);
            if (obj instanceof ConnectionJ) {
                return values
                        .stream()
                        .map(connectionJ -> ((ConnectionJ) connectionJ).getId())
                        .collect(Collectors.toList());
            }
        }
        return values;
    }

    private PageInfoDtoGenerated getPageInfo(UUID id, Class<? extends AbstractJaversEntity> type,
                                             Integer offset, Integer limit) {
        PageInfoDtoGenerated pageInfo = new PageInfoDtoGenerated();
        pageInfo.setOffset(offset);
        pageInfo.setLimit(limit);
        Changes changes = javers.findChanges(getQuery(id, type));
        int countOfCommits = changes.groupByCommit().size();
        pageInfo.setItemsTotalCount(countOfCommits);
        return pageInfo;
    }

    private JqlQuery getQuery(UUID id, Class<? extends AbstractJaversEntity> type, Integer offset, Integer limit) {
        return QueryBuilder.byInstanceId(id, type)
                .withNewObjectChanges()
                .skip(offset)
                .limit(limit)
                .build();
    }

    private JqlQuery getQuery(UUID id, Class<? extends AbstractJaversEntity> type, CommitId commitId) {
        return QueryBuilder.byInstanceId(id, type)
                .withCommitId(commitId)
                .build();
    }

    private JqlQuery getQuery(UUID id, Class<? extends AbstractJaversEntity> type) {
        return QueryBuilder.byInstanceId(id, type)
                .withNewObjectChanges()
                .build();
    }
}
