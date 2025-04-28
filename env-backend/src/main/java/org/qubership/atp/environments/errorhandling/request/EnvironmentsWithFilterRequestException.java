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

package org.qubership.atp.environments.errorhandling.request;

import org.qubership.atp.environments.errorhandling.AtpEnvironmentException;
import org.qubership.atp.environments.service.rest.server.request.EnvironmentsWithFilterRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "ENV-5000")
public class EnvironmentsWithFilterRequestException extends AtpEnvironmentException {

    public static final String DEFAULT_MESSAGE = "Invalid request for getting environments %s";

    public EnvironmentsWithFilterRequestException(
            EnvironmentsWithFilterRequest request
    ) {
        super(String.format(DEFAULT_MESSAGE, request));
    }

    public EnvironmentsWithFilterRequestException(
            EnvironmentsWithFilterRequest request, String message
    ) {
        super(String.format(DEFAULT_MESSAGE, request) + "\n" + message);
    }

    public EnvironmentsWithFilterRequestException(String message
    ) {
        super(String.format(DEFAULT_MESSAGE, message));
    }
}
