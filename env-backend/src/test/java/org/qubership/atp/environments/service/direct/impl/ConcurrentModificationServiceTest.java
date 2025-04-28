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

import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.Project;
import org.qubership.atp.environments.model.utils.Constants;
import org.qubership.atp.environments.service.direct.ConcurrentModificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

public class ConcurrentModificationServiceTest extends AbstractServiceTest {

    @Autowired
    private ConcurrentModificationService concurrentModificationService;

    private static final String PROJECT_NAME = "Test_project_TICKET-14948";
    private static UUID PROJECT_ID;
    private static final String ENVIRONMENT_NAME = "EnvironmentTestConcurrentModification";
    private static final String ENVIRONMENT_NEW_NAME = ENVIRONMENT_NAME.concat("Updated");
    private static final String ENVIRONMENT_DESCRIPTION = "Some description";
    private static final UUID ENVIRONMENT_CATEGORY_ID = Constants.Environment.Category.ENVIRONMENT;

    @BeforeEach
    public void setUp() {
        Project projectTest = createProject(PROJECT_NAME);
        PROJECT_ID = projectTest.getId();
    }

    @Test
    public void getConcurrentModificationStatus_200_NotModified() {
        Environment environment = environmentService.create(
                PROJECT_ID, ENVIRONMENT_NAME, "", ENVIRONMENT_DESCRIPTION, "", "", "",
                ENVIRONMENT_CATEGORY_ID, Collections.emptyList());
        UUID environmentId = environment.getId();
        HttpStatus status = concurrentModificationService.getConcurrentModificationHttpStatus(
                environmentId, null, environmentService);
        Assertions.assertEquals(HttpStatus.OK, status);
    }

    @Test
    public void getConcurrentModificationStatus_200_ModifiedDateMatched() {
        Environment environment = environmentService.create(
                PROJECT_ID, ENVIRONMENT_NAME, "", ENVIRONMENT_DESCRIPTION, "", "", "",
                ENVIRONMENT_CATEGORY_ID, Collections.emptyList());
        UUID environmentId = environment.getId();
        environmentService.update(
                environmentId, ENVIRONMENT_NEW_NAME, "", ENVIRONMENT_DESCRIPTION, "", "", "", PROJECT_ID,
                ENVIRONMENT_CATEGORY_ID, Collections.emptyList());
        Long modified = environmentService.get(environmentId).getModified();
        HttpStatus status = concurrentModificationService.getConcurrentModificationHttpStatus(
                environmentId, modified, environmentService);
        Assertions.assertEquals(HttpStatus.OK, status);
    }

    @Test
    public void getConcurrentModificationStatus_226_ModifiedDateNotMatched() {
        Environment environment = environmentService.create(
                PROJECT_ID, ENVIRONMENT_NAME, "", ENVIRONMENT_DESCRIPTION, "", "", "",
                ENVIRONMENT_CATEGORY_ID, Collections.emptyList());
        UUID environmentId = environment.getId();
        Long modifiedEarlier = environment.getModified();
        environmentService.update(environmentId, ENVIRONMENT_NEW_NAME, "", ENVIRONMENT_DESCRIPTION, "", "",
                "", PROJECT_ID,
                ENVIRONMENT_CATEGORY_ID, Collections.emptyList());
        HttpStatus status = concurrentModificationService.getConcurrentModificationHttpStatus(
                environmentId, modifiedEarlier, environmentService);
        Assertions.assertEquals(HttpStatus.IM_USED, status);
    }
}
