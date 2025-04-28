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

package org.qubership.atp.environments.validating.strategies;

import java.util.List;
import java.util.UUID;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.utils.Constants;
import org.qubership.atp.environments.service.rest.server.response.ValidateTaToolResponse;
import org.qubership.atp.environments.validating.factories.ValidationStrategyFactory;
import org.springframework.stereotype.Component;

@Component(ValidationStrategyFactory.ITF_LITE)
public class ItlLiteTaToolStrategy implements ValidationStrategy {

    @Override
    public ValidateTaToolResponse validate(Environment taTool) {
        ValidateTaToolResponse response = new ValidateTaToolResponse();
        response.setId(taTool.getId());
        response.setName(taTool.getName());
        response.setValidated(false);
        System itfLiteSystem = findSystemByCategory(taTool.getSystems(), Constants.SystemCategories.ITF_LITE);
        if (itfLiteSystem == null) {
            response.setMessage("A Tool entity has to contain system with category "
                    + "'ITF Lite' and this system has to contain 'HTTP' connection"
                    + " and 'HTTP' connection has to contain 'URL' property");
            return response;
        }
        Connection httpConnection = findConnectionByTemplateId(itfLiteSystem.getConnections(),
                Constants.Environment.System.Connection.HTTP);
        if (httpConnection == null) {
            response.setMessage("'ITF Lite' System has to contain 'HTTP' connection"
                    + " and 'HTTP' connection has to contain 'URL' property");
            return response;
        }
        if (MapUtils.isEmpty(httpConnection.getParameters())
                || StringUtils.isBlank(httpConnection.getParameters().get("url"))) {
            response.setMessage("'HTTP' connection has to contain 'URL' property");
            return response;
        }
        response.setValidated(true);
        return response;
    }

    /**
     * Find first system by category.
     *
     * @param systems    list of systems
     * @param categoryId category ID
     * @return found System on NULL if not found
     */
    public System findSystemByCategory(List<System> systems, UUID categoryId) {
        if (!CollectionUtils.isEmpty(systems)) {
            return systems.stream().filter(system -> system.getSystemCategory() != null
                            && system.getSystemCategory().getId().equals(categoryId))
                    .findFirst().orElse(null);
        }
        return null;
    }

    /**
     * Find first connection by template ID.
     *
     * @param connections list of connections
     * @param templateId  template ID
     * @return found Connection on NULL if not found
     */
    public Connection findConnectionByTemplateId(List<Connection> connections, UUID templateId) {
        if (!CollectionUtils.isEmpty(connections)) {
            return connections.stream().filter(connection -> connection.getSourceTemplateId() != null
                            && connection.getSourceTemplateId().equals(templateId))
                    .findFirst().orElse(null);
        }
        return null;
    }
}
