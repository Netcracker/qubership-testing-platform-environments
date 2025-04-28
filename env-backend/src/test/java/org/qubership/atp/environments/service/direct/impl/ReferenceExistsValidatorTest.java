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
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.javers.core.Javers;
import org.javers.repository.jql.JqlQuery;
import org.javers.repository.jql.QueryBuilder;
import org.javers.shadow.Shadow;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.Project;
import org.qubership.atp.environments.model.utils.Constants;
import org.qubership.atp.environments.versioning.model.entities.EnvironmentJ;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;

public class ReferenceExistsValidatorTest extends AbstractServiceTest {

    @Autowired
    private Javers javers;
    @Autowired
    private Validator validator;

    private static final String PROJECT_NAME = "Test_project_TICKET-15588";
    private static UUID PROJECT_ID;

    private static final String ENVIRONMENT_NAME = "EnvironmentTestReferenceExistsValidator";
    private static final UUID ENVIRONMENT_CATEGORY_ID = Constants.Environment.Category.ENVIRONMENT;


    @BeforeEach
    public void setUp() {
        Project projectTest = createProject(PROJECT_NAME);
        PROJECT_ID = projectTest.getId();
    }

    @Test
    public void valid_valueCorrect() {
        Environment environment = environmentService.create(
                PROJECT_ID, ENVIRONMENT_NAME, "", null, "", "", "", ENVIRONMENT_CATEGORY_ID, Collections.emptyList());

        checkShadowHasNoViolations(environment.getId(), 0);
    }

    @Test
    public void invalid_valueIncorrect() {
        Environment environment = environmentService.create(
                PROJECT_ID, ENVIRONMENT_NAME, "", null, "", "", "", ENVIRONMENT_CATEGORY_ID, Collections.emptyList());
        UUID environmentId = environment.getId();

        environmentService.delete(environmentId);
        projectService.delete(PROJECT_ID);

        checkShadowViolations(environmentId, 1, Sets.newHashSet("project"));
    }

    private void checkShadowHasNoViolations(UUID environmentId, int number) {
        checkShadowViolations(environmentId, number, Sets.newHashSet());
    }

    private void checkShadowViolations(UUID environmentId, int numberOfShadow, Set<String> expectedMessages) {
        JqlQuery query = QueryBuilder
                .byInstanceId(environmentId, EnvironmentJ.class)
                .build();
        List<Shadow<EnvironmentJ>> shadows = javers.findShadows(query);

        EnvironmentJ environmentJ = shadows.get(numberOfShadow).get();

        Set<ConstraintViolation<EnvironmentJ>> violations = validator.validate(environmentJ);

        Set<String> actualMessages = violations
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        Assertions.assertEquals(expectedMessages, actualMessages);
    }

}
