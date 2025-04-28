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

import static org.qubership.atp.environments.repo.impl.AbstractRepository.ENVIRONMENTS;
import static org.qubership.atp.environments.repo.impl.AbstractRepository.ENVIRONMENT_SYSTEMS;
import static org.qubership.atp.environments.repo.impl.AbstractRepository.SYSTEMS;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.qubership.atp.environments.errorhandling.request.EnvironmentsWithFilterRequestException;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.service.rest.server.request.FilterRequest;
import org.springframework.stereotype.Component;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLQuery;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EnvironmentMapper implements QueryMapper<Environment> {

    private final String delimiter = ".";
    public static final String SYSTEMS_FIELD_NAME = "systems";

    @Override
    public BooleanExpression mapFilter(SQLQuery<Environment> sqlQuery,
                                       BooleanExpression booleanExpression,
                                       List<FilterRequest> filterRequests) {
        for (FilterRequest filterRequest : filterRequests) {
            if (filterRequest == null
                    || filterRequest.getName() == null
                    || CollectionUtils.isEmpty(filterRequest.getValue())) {
                continue;
            }
            String fieldName = filterRequest.getName();
            Expression<?> fieldValue;
            RelationalPathBase<?> table = ENVIRONMENTS;
            if (fieldName.contains(delimiter)) {
                String tableName = fieldName.substring(0, fieldName.indexOf(delimiter));
                if (tableName.equals(SYSTEMS_FIELD_NAME)) {
                    table = SYSTEMS;
                    fieldName = fieldName.substring(fieldName.indexOf(delimiter) + 1);
                    sqlQuery =
                            sqlQuery.leftJoin(ENVIRONMENT_SYSTEMS)
                                    .on(ENVIRONMENTS.id.eq(ENVIRONMENT_SYSTEMS.environmentId))
                                    .leftJoin(SYSTEMS).on(ENVIRONMENT_SYSTEMS.systemId.eq(SYSTEMS.id));
                }
            }
            fieldValue = getField(fieldName, table);
            if (booleanExpression == null) {
                booleanExpression = filterQuery(fieldName, fieldValue, filterRequest.getValue());
                continue;
            }
            booleanExpression = booleanExpression.and(filterQuery(fieldName, fieldValue,
                    filterRequest.getValue()));
        }
        return booleanExpression;
    }

    /**
     * Method for generating boolean expression for custom filter.
     */
    public BooleanExpression filterQuery(String fieldName, Expression<?> fieldValue,
                                         List<String> filterValue) {
        if (fieldValue instanceof SimplePath && ((SimplePath) fieldValue).getType() == UUID.class) {
            return ((SimplePath<UUID>) fieldValue).in(filterValue.stream()
                    .map(UUID::fromString).collect(Collectors.toList()));
        }
        if (fieldValue instanceof DateTimePath) {
            return ((DateTimePath<Timestamp>) fieldValue).in(filterValue.stream()
                    .map(Timestamp::valueOf).collect(Collectors.toList()));
        }
        if (fieldValue instanceof StringExpression) {
            return ((StringExpression) fieldValue).in(filterValue);
        }
        throw new EnvironmentsWithFilterRequestException(String.format("Incorrect field name: %s",
                fieldName));
    }

    /**
     * Method for getting field from Qtable.
     */
    public Expression<?> getField(String fieldName, RelationalPathBase<?> table) {
        try {
            Field field = table.getClass().getField(fieldName);
            return (Expression<?>) field.get(table);
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            String message = String.format("Incorrect field name: %s",
                    fieldName);
            log.error(message, exception);
            throw new EnvironmentsWithFilterRequestException(message);
        }
    }
}
