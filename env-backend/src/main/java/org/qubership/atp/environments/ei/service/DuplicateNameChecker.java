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

package org.qubership.atp.environments.ei.service;

import static org.qubership.atp.environments.repo.impl.AbstractRepository.SYSTEMS;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.qubership.atp.environments.ei.model.Environment;
import org.qubership.atp.environments.ei.model.System;
import org.qubership.atp.environments.model.Identified;
import org.qubership.atp.environments.repo.impl.SystemRepositoryImpl;
import org.springframework.stereotype.Service;

import com.querydsl.core.Tuple;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class DuplicateNameChecker {

    private final SystemRepositoryImpl systemRepository;

    /**
     * Check and correct name.
     *
     * @param object             the object
     * @param isNameUsedFunction the is name used function
     */
    public void checkAndCorrectName(Environment object,
                                    Function<Environment, Boolean> isNameUsedFunction) {
        int i = 0;
        String newName;
        String initName = object.getName();
        while (isNameUsedFunction.apply(object)) {
            if (i == 0) {
                initName = object.getName() + " Copy";
                newName = initName;
            } else {
                newName = initName + " _" + i;
            }
            object.setName(newName);
            ++i;
        }
    }

    /**
     * Check and correct name.
     *
     * @param system             the system
     * @param environment             the environment
     */
    public void checkAndCorrectSystemName(System system,
                                          Environment environment,
                                          org.qubership.atp.environments.model.System originalSystem) {
        int i = 0;
        String newName;
        String initName = system.getName();
        while (isSystemNameUsed(environment, system, originalSystem)) {
            if (i == 0) {
                initName = system.getName() + " Copy";
                newName = initName;
            } else {
                newName = initName + " _" + i;
            }
            system.setName(newName);
            ++i;
        }
    }

    /**
     * Checks if system name is used under environment.
     *
     * @param system             the system
     * @param environment        the environment
     */
    public boolean isSystemNameUsed(Environment environment,
                                    System system,
                                    org.qubership.atp.environments.model.System originalSystem) {
        List<Tuple> fromBase = originalSystem != null && CollectionUtils.isNotEmpty(originalSystem.getEnvironments())
                ? systemRepository.checkSystemNameIsUniqueUnderEnvironments(originalSystem.getEnvironments()
                .stream().map(Identified::getId).collect(Collectors.toList()), system.getName())
                : systemRepository.checkSystemNameIsUniqueUnderEnvironment(environment.getId(), system.getName());
        return !CollectionUtils.isEmpty(fromBase)
                && fromBase.stream().noneMatch(base -> Objects.equals(base.get(SYSTEMS.id), system.getId()));
    }
}
