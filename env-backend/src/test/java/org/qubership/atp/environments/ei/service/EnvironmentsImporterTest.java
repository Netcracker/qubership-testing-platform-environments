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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubership.atp.crypt.api.Decryptor;
import org.qubership.atp.ei.node.services.ObjectLoaderFromDiskService;
import org.qubership.atp.environments.ei.model.Environment;
import org.qubership.atp.environments.model.impl.EnvironmentImpl;
import org.qubership.atp.environments.repo.impl.SystemRepositoryImpl;
import org.qubership.atp.environments.service.direct.ConnectionService;
import org.qubership.atp.environments.service.direct.EnvironmentService;
import org.qubership.atp.environments.service.direct.ProjectService;
import org.qubership.atp.environments.service.direct.SystemCategoriesService;
import org.qubership.atp.environments.service.direct.SystemService;

public class EnvironmentsImporterTest {

    private final ThreadLocal<EnvironmentService> environmentService = new ThreadLocal<>();
    private final ThreadLocal<EnvironmentsImporter> importer = new ThreadLocal<>();

    @BeforeEach
    public void setUp() throws Exception {
        EnvironmentService environmentServiceMock = mock(EnvironmentService.class);
        environmentService.set(environmentServiceMock);
        importer.set(new EnvironmentsImporter(
                mock(ObjectLoaderFromDiskService.class),
                environmentServiceMock,
                mock(SystemService.class),
                mock(SystemCategoriesService.class),
                mock(ConnectionService.class),
                mock(ProjectService.class),
                new DuplicateNameChecker(mock(SystemRepositoryImpl.class)),
                mock(Decryptor.class)));
    }

    @Test
    public void checkAndCorrectName_noDuplicateInBase() {
        Environment object = new Environment();
        object.setId(UUID.randomUUID());
        object.setName("Object Name");

        org.qubership.atp.environments.model.Environment fromBase = null;
        when(environmentService.get().getByNameAndProjectId(eq(object.getName()), any())).thenReturn(fromBase);

        importer.get().checkAndCorrectName(object);

        Assertions.assertEquals(object.getName(), "Object Name");
    }

    @Test
    public void checkAndCorrectName_theSameObject() {
        Environment object = new Environment();
        object.setId(UUID.randomUUID());
        object.setName("Object Name");

        org.qubership.atp.environments.model.Environment fromBase = new EnvironmentImpl();
        fromBase.setName(object.getName());
        fromBase.setId(object.getId());
        when(environmentService.get().getByNameAndProjectId(eq(object.getName()), any())).thenReturn(fromBase);

        importer.get().checkAndCorrectName(object);

        Assertions.assertEquals(object.getName(), "Object Name");
    }

    @Test
    public void checkAndCorrectName_theSameButWithOtherNameObject() {
        Environment object = new Environment();
        object.setId(UUID.randomUUID());
        object.setName("Object Name");

        org.qubership.atp.environments.model.Environment fromBase = new EnvironmentImpl();
        fromBase.setName("Object Name 2");
        fromBase.setId(object.getId());
        when(environmentService.get().getByNameAndProjectId(eq(object.getName()), any())).thenReturn(fromBase);

        importer.get().checkAndCorrectName(object);

        Assertions.assertEquals(object.getName(), "Object Name");
    }

    @Test
    public void checkAndCorrectName_twoDuplicateInBase() {
        Environment object = new Environment();
        object.setId(UUID.randomUUID());
        object.setName("Object Name");

        org.qubership.atp.environments.model.Environment fromBase = new EnvironmentImpl();
        fromBase.setName("Object Name");
        fromBase.setId(UUID.randomUUID());
        when(environmentService.get().getByNameAndProjectId(eq(fromBase.getName()), any())).thenReturn(fromBase);

        org.qubership.atp.environments.model.Environment fromBase2 = new EnvironmentImpl();
        fromBase2.setName("Object Name Copy");
        fromBase2.setId(UUID.randomUUID());
        when(environmentService.get().getByNameAndProjectId(eq(fromBase2.getName()), any())).thenReturn(fromBase2);

        org.qubership.atp.environments.model.Environment fromBase3 = new EnvironmentImpl();
        fromBase3.setName("Object Name Copy _1");
        fromBase3.setId(UUID.randomUUID());
        when(environmentService.get().getByNameAndProjectId(eq(fromBase3.getName()), any())).thenReturn(fromBase3);

        importer.get().checkAndCorrectName(object);

        Assertions.assertEquals(object.getName(), "Object Name Copy _2");
    }
}
