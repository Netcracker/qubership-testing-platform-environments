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

package org.qubership.atp.environments.ei;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.qubership.atp.ei.node.ImportExecutor;
import org.qubership.atp.ei.node.dto.ExportImportData;
import org.qubership.atp.ei.node.dto.ValidationResult;
import org.qubership.atp.ei.node.dto.validation.UserMessage;
import org.qubership.atp.environments.ei.service.EnvironmentsImporter;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class EnvironmentsImportExecutor implements ImportExecutor {

    private final EnvironmentsImporter environmentsImporter;

    /**
     * Imports data to server.
     *
     * @param importData an instance of {@link ExportImportData}
     * @param workDir    directory where import files are located
     * @throws Exception exception during export
     */
    @Override
    public void importData(ExportImportData importData, Path workDir) throws Exception {
        log.info("Starts importData(importData: {}, workDir: {}).", importData, workDir);
        environmentsImporter.importEnvironments(workDir, importData);
        log.info("End of importData().");
    }

    @Override
    public ValidationResult preValidateData(ExportImportData exportImportData, Path workDir) {
        log.info("Starting environments pre validation(workDir: {})", workDir);
        List<String> messages = environmentsImporter.preValidateEnvironments(workDir,
                exportImportData.isInterProjectImport(),
                exportImportData.getReplacementMap());
        return new ValidationResult(messages.stream().map(UserMessage::new).collect(Collectors.toList()),
                new HashMap<>(exportImportData.getReplacementMap()));
    }

    @Override
    public ValidationResult validateData(ExportImportData importData, Path workDir) {
        Map<UUID, UUID> repMap = new HashMap<>(importData.getReplacementMap());
        List<String> messages = new ArrayList<>();
        if (importData.isImportFirstTime()) {
            log.info("validate will be skipped, because isImportFirstTime = true");
        } else if (importData.isCreateNewProject()) {
            handleCreateNewProjectValidation(workDir, repMap);
        } else if (importData.isInterProjectImport()) {
            handleInterProjectImportValidation(workDir, messages, repMap);
        } else {
            handleImportInTheSameProjectValidation(workDir, messages, repMap);
        }

        List<UserMessage> details = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(messages)) {
            details.addAll(messages.stream().map(UserMessage::new).collect(Collectors.toList()));
        }

        return new ValidationResult(details, repMap);
    }

    private void handleImportInTheSameProjectValidation(Path workDir, List<String> messages, Map<UUID, UUID> repMap) {
        messages.addAll(environmentsImporter.validateEnvironments(workDir, false, repMap));
    }

    private void handleInterProjectImportValidation(Path workDir, List<String> messages, Map<UUID, UUID> repMap) {
        environmentsImporter.fillRepMapWithSourceTargetValues(repMap, workDir);
        repMap.entrySet().forEach(entry -> {
            if (entry.getValue() == null) {
                entry.setValue(UUID.randomUUID());
            }
        });
        messages.addAll(environmentsImporter.validateEnvironments(workDir, true, repMap));
    }

    private void handleCreateNewProjectValidation(Path workDir, Map<UUID, UUID> repMap) {
        List<UUID> allIds = new ArrayList<>(environmentsImporter.getObjectIds(workDir));
        allIds.forEach(id -> repMap.put(id, UUID.randomUUID()));
    }

}
