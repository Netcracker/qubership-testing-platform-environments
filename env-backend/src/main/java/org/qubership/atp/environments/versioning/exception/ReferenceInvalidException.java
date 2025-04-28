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

package org.qubership.atp.environments.versioning.exception;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ReferenceInvalidException extends RuntimeException {

    private Set<ConstraintViolation<Object>> violations;

    public ReferenceInvalidException(Set<ConstraintViolation<Object>> violations) {
        this.violations = violations;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder("Revision was not restored. Reference to the ");
        violations.forEach(violation -> {
            sb.append(violation.getMessage()).append(" ");
        });
        String verb = "is";
        if (violations.size() > 1) {
            verb = "are";
        }
        sb.append(verb).append(" invalid");
        return sb.toString();
    }
}
