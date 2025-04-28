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

package org.qubership.atp.environments.service.ei;

import static org.qubership.atp.environments.repo.impl.AbstractRepository.SYSTEMS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mockito;
import org.qubership.atp.environments.ei.model.Environment;
import org.qubership.atp.environments.ei.model.System;
import org.qubership.atp.environments.ei.service.DuplicateNameChecker;
import org.qubership.atp.environments.mocks.MockTuple;
import org.qubership.atp.environments.repo.impl.SystemRepositoryImpl;

public class DuplicateNameCheckerTest {

    private final ThreadLocal<SystemRepositoryImpl> systemRepository = new ThreadLocal<>();
    private final ThreadLocal<DuplicateNameChecker> duplicateNameChecker = new ThreadLocal<>();

    @BeforeEach
    public void setUp() {
        SystemRepositoryImpl systemRepositoryMock = Mockito.mock(SystemRepositoryImpl.class);
        systemRepository.set(systemRepositoryMock);
        duplicateNameChecker.set(new DuplicateNameChecker(systemRepositoryMock));
    }

    @Test
    public void checkAndCorrectEnvironmentName_NameHasChanged(TestInfo testInfo) {
        String methodName = testInfo.getTestMethod().get().getName();
        Environment existingEnvironment = new Environment();
        existingEnvironment.setName(methodName);
        Function<Environment, Boolean> isNameUsedFunction =
                environment -> environment.getName().equals(methodName);
        duplicateNameChecker.get().checkAndCorrectName(existingEnvironment, isNameUsedFunction);
        Assertions.assertEquals(methodName + " Copy", existingEnvironment.getName());
    }

    @Test
    public void checkAndCorrectSystemName_NameHasChanged(TestInfo testInfo) {
        String methodName = testInfo.getTestMethod().get().getName();
        Environment existingEnvironment = new Environment();
        existingEnvironment.setId(UUID.randomUUID());
        existingEnvironment.setName(methodName);
        System system = new System();
        system.setId(UUID.randomUUID());
        system.setName(methodName);
        List<Object> objects = new ArrayList<>();
        objects.add(system.getName());
        objects.add(SYSTEMS.id);
        Mockito.when(systemRepository.get().checkSystemNameIsUniqueUnderEnvironment(any(), eq(methodName)))
                .thenReturn(Collections.singletonList(new MockTuple(objects.toArray(), UUID.randomUUID())));
        duplicateNameChecker.get().checkAndCorrectSystemName(system, existingEnvironment, null);
        Assertions.assertEquals(methodName + " Copy", system.getName());
    }
}
