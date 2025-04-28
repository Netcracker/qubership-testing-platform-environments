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

package org.qubership.atp.environments.repo.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.qubership.atp.environments.repo.impl.AbstractRepository.ENVIRONMENTS;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.qubership.atp.environments.service.rest.server.request.FilterRequest;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.sql.SQLQuery;

public class EnvironmentMapperTest {

    private final EnvironmentMapper environmentFilter = new EnvironmentMapper();


    @Test
    public void test_mapFilterWithSystemsField_succesful() {
        List<FilterRequest> filterRequests = Collections.singletonList(new FilterRequest("systems"
                + ".categoryId", Collections.singletonList(UUID.randomUUID().toString())));

        BooleanExpression result = environmentFilter.mapFilter(new SQLQuery(), null, filterRequests);
        assertNotNull(result);
    }

    @Test
    public void test_filterQueryForUUID_succesful() {
        List<String> filterValue = Collections.singletonList(UUID.randomUUID().toString());
        BooleanExpression result = environmentFilter.filterQuery("id", ENVIRONMENTS.id, filterValue);

        assertNotNull(result);
        assertTrue(result.toString().contains(filterValue.get(0)));
    }

    @Test
    public void test_filterQueryForTimestamp_succesful() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        List<String> filterValue = Collections.singletonList(timestamp.toString());
        BooleanExpression result = environmentFilter.filterQuery("created", ENVIRONMENTS.created,
                filterValue);

        assertNotNull(result);
        assertTrue(result.toString().contains(filterValue.get(0)));
    }

    @Test
    public void test_filterQueryForStrin_succesfulg() {
        List<String> filterValue = Collections.singletonList("description");
        BooleanExpression result = environmentFilter.filterQuery("description", ENVIRONMENTS.description,
                filterValue);

        assertNotNull(result);
        assertTrue(result.toString().contains(filterValue.get(0)));
    }

    @Test
    public void test_getField_succesful() {
        Expression<?> result = environmentFilter.getField("id", ENVIRONMENTS);
        assertNotNull(result);
        assertEquals(ENVIRONMENTS.id, result);
    }


}
