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

package org.qubership.atp.environments.repo.projections;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.qubership.atp.environments.errorhandling.request.EnvironmentsWithFilterRequestException;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.impl.EnvironmentImpl;
import org.qubership.atp.environments.repo.impl.AbstractRepository;
import org.qubership.atp.environments.repo.impl.EnvironmentRepositoryImpl;
import org.qubership.atp.environments.repo.mapper.EnvironmentMapper;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.MappingProjection;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressFBWarnings({"SE_TRANSIENT_FIELD_NOT_RESTORED", "SE_BAD_FIELD"})
public class GenericEnvironmentProjection extends MappingProjection<Environment> {

    private static final long serialVersionUID = 42L;
    protected final transient EnvironmentRepositoryImpl repo;
    private final List<String> fields;
    private final EnvironmentMapper environmentMapper;

    /**
     * GenericEnvironmentProjection constructor.
     */
    public GenericEnvironmentProjection(EnvironmentRepositoryImpl repo, List<String> fields,
                                        EnvironmentMapper mapper) {
        super(Environment.class, AbstractRepository.ENVIRONMENTS.all());
        this.repo = repo;
        this.fields = fields;
        this.environmentMapper = mapper;
    }

    @Override
    protected Environment map(Tuple tuple) {
        Environment environment = new EnvironmentImpl();
        if (CollectionUtils.isEmpty(fields)) {
            return new EnvironmentImpl();
        }
        for (String fieldName: fields) {
            try {
                if (fieldName.contains(EnvironmentMapper.SYSTEMS_FIELD_NAME)) {
                    UUID environmentId = tuple.get(AbstractRepository.ENVIRONMENTS.id);
                    if (environmentId != null) {
                        environment.setSystems(repo.getSystemRepo().get().getAllByParentId(environmentId));
                    }
                    continue;
                }
                Object value = tuple.get(environmentMapper.getField(fieldName,
                        AbstractRepository.ENVIRONMENTS));

                PropertyUtils.setProperty(environment, fieldName, value instanceof Timestamp
                        ? ((Timestamp) value).getTime() : value);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                String message = String.format("Incorrect field name: %s",
                        fieldName);
                log.error(message, e);
                throw new EnvironmentsWithFilterRequestException(message);
            }
        }
        return environment;
    }

}
