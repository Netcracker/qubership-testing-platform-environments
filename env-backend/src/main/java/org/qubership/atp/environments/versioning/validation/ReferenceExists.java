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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import org.qubership.atp.environments.service.direct.IdentifiedService;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ReferenceExistsValidator.class)
public @interface ReferenceExists {

    /**
     * Specifies the service bean which will be used to verify that object with given ID is exists.
     */
    Class<? extends IdentifiedService> service();

    /**
     * Points to a property key in ValidationMessages.properties,
     * which is used to resolve a message in case of violation.
     */
    String message();

    /**
     * Allows to define under which circumstances this validation is to be triggered.
     */
    Class<?>[] groups() default { };

    /**
     * Allows to define a payload to be passed with this validation.
     */
    Class<? extends Payload>[] payload() default { };

}
