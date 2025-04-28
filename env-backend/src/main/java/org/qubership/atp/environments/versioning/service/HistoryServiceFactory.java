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

package org.qubership.atp.environments.versioning.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

@Component
public class HistoryServiceFactory {
    List<VersionHistoryService> versionHistoryServices;

    public HistoryServiceFactory(List<VersionHistoryService> versionHistoryServices) {
        this.versionHistoryServices = versionHistoryServices;
    }

    /**
     * Returns the concrete implementation of HistoryService depending of entity type.
     * @param itemType type of domain entity with supported history
     * @return HistoryService implementation
     */
    public Optional<VersionHistoryService> getHistoryService(String itemType) {
        return versionHistoryServices.stream()
                .filter(service -> itemType.equals(service.getItemType().toString()))
                .findFirst();
    }
}
