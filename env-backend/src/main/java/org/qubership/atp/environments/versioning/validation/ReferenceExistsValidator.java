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

package org.qubership.atp.environments.versioning.validation;

import java.util.Collection;
import java.util.UUID;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.qubership.atp.environments.service.direct.IdentifiedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class ReferenceExistsValidator implements ConstraintValidator<ReferenceExists, Object> {

    private ApplicationContext applicationContext;
    private ReferenceExists annotation;

    @Autowired
    public ReferenceExistsValidator(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void initialize(ReferenceExists constraintAnnotation) {
        this.annotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        Class<? extends IdentifiedService> serviceClass = annotation.service();
        IdentifiedService service = applicationContext.getBean(serviceClass);
        if (value instanceof Collection) {
            return ((Collection<UUID>) value)
                    .stream()
                    .allMatch(service::existsById);
        } else if (value instanceof UUID) {
            return service.existsById((UUID) value);
        } else {
            return false;
        }
    }
}
