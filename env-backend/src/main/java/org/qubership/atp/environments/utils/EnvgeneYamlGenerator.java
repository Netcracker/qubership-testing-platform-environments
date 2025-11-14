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

package org.qubership.atp.environments.utils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.collections4.CollectionUtils;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.service.direct.DecryptorService;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for generating envgene YAML format from systems data.
 */
@Slf4j
public class EnvgeneYamlGenerator {

    private static final String ATP_ENVGENE_CONFIGURATION = "ATP_ENVGENE_CONFIGURATION";
    private static final String SYSTEMS_KEY = "systems";
    private static final String CONNECTIONS_KEY = "connections";
    private static final String PASSWORD_KEY = "password";
    private static final String TOKEN_KEY = "token";
    private static final String ENC_PREFIX = "{ENC}";

    private final Yaml yaml;
    private final DecryptorService decryptorService;

    public EnvgeneYamlGenerator(DecryptorService decryptorService) {
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        this.yaml = new Yaml(options);
        this.decryptorService = decryptorService;
    }

    /**
     * Generates deployment parameters YAML string from systems collection.
     * Contains all connection parameters except encrypted credentials and password/token fields.
     *
     * @param systems collection of systems to process
     * @return YAML string with deployment parameters
     */
    @Nonnull
    public String generateDeploymentParametersYaml(@Nonnull Collection<System> systems) {
        log.debug("Generating deployment parameters YAML for {} systems", systems.size());
        
        Map<String, Object> root = buildYamlStructure(systems, false);
        return yaml.dump(root);
    }

    /**
     * Generates credentials YAML string from systems collection.
     * Contains only encrypted parameters (starting with {ENC}) and password/token fields.
     * Encrypted values (with {ENC} prefix) are decrypted before being written to the YAML.
     *
     * @param systems collection of systems to process
     * @return YAML string with credentials (decrypted values)
     */
    @Nonnull
    public String generateCredentialsYaml(@Nonnull Collection<System> systems) {
        log.debug("Generating credentials YAML for {} systems", systems.size());
        
        Map<String, Object> root = buildYamlStructure(systems, true);
        return yaml.dump(root);
    }

    /**
     * Builds the YAML structure from systems collection.
     *
     * @param systems collection of systems
     * @param credentialsOnly if true, only include credentials; if false, only include deployment parameters
     * @return map representing the YAML structure
     */
    @Nonnull
    private Map<String, Object> buildYamlStructure(@Nonnull Collection<System> systems, boolean credentialsOnly) {
        Map<String, Object> root = new LinkedHashMap<>();
        Map<String, Object> config = new LinkedHashMap<>();
        root.put(ATP_ENVGENE_CONFIGURATION, config);
        
        List<Map<String, Object>> systemsList = systems.stream()
                .filter(system -> CollectionUtils.isNotEmpty(system.getConnections()))
                .map(system -> buildSystemMap(system, credentialsOnly))
                .filter(systemMap -> !systemMap.isEmpty())
                .collect(Collectors.toList());
        
        config.put(SYSTEMS_KEY, systemsList);
        return root;
    }

    /**
     * Builds a map representing a system with its connections.
     * The result is a map with a single key (system name) and value (system data with connections).
     *
     * @param system the system to process
     * @param credentialsOnly if true, only include credentials; if false, only include deployment parameters
     * @return map representing the system with system name as key
     */
    @Nonnull
    private Map<String, Object> buildSystemMap(@Nonnull System system, boolean credentialsOnly) {
        Map<String, Object> systemMap = new LinkedHashMap<>();
        List<Map<String, Object>> connectionsList = system.getConnections().stream()
                .map(connection -> buildConnectionMap(connection, credentialsOnly))
                .filter(connectionMap -> !connectionMap.isEmpty())
                .collect(Collectors.toList());
        
        if (!connectionsList.isEmpty()) {
            // Create a map with system name as key and system data (with connections) as value
            Map<String, Object> systemData = new LinkedHashMap<>();
            systemData.put(CONNECTIONS_KEY, connectionsList);
            // Put system name as key with system data as value
            systemMap.put(system.getName(), systemData);
        }
        
        return systemMap;
    }

    /**
     * Builds a map representing a connection with its parameters.
     *
     * @param connection the connection to process
     * @param credentialsOnly if true, only include credentials; if false, only include deployment parameters
     * @return map representing the connection
     */
    @Nonnull
    private Map<String, Object> buildConnectionMap(@Nonnull Connection connection, boolean credentialsOnly) {
        Map<String, Object> connectionMap = new LinkedHashMap<>();
        ConnectionParameters parameters = connection.getParameters();
        
        if (parameters == null || parameters.isEmpty()) {
            return connectionMap;
        }
        
        Map<String, String> filteredParameters = filterParameters(parameters, credentialsOnly);
        
        if (!filteredParameters.isEmpty()) {
            Map<String, Object> connectionEntry = new LinkedHashMap<>();
            connectionEntry.putAll(filteredParameters);
            connectionMap.put(connection.getName(), connectionEntry);
        }
        
        return connectionMap;
    }

    /**
     * Filters parameters based on whether we want credentials or deployment parameters.
     * When credentialsOnly is true and a value starts with {ENC}, it is decrypted before being added to the filtered map.
     *
     * @param parameters the connection parameters to filter
     * @param credentialsOnly if true, return only credentials; if false, return only deployment parameters
     * @return filtered map of parameters (with decrypted values if credentialsOnly is true)
     */
    @Nonnull
    private Map<String, String> filterParameters(@Nonnull ConnectionParameters parameters, boolean credentialsOnly) {
        Map<String, String> filtered = new LinkedHashMap<>();
        
        parameters.forEach((key, value) -> {
            boolean isCredential = isCredentialParameter(key, value);
            
            if (credentialsOnly && isCredential) {
                filtered.put(key, decryptorService.decryptParameter(value));
            } else if (!credentialsOnly && !isCredential) {
                filtered.put(key, value);
            }
        });
        
        return filtered;
    }

    /**
     * Determines if a parameter is a credential parameter.
     * A parameter is considered a credential if:
     * - The key is "password" or "token"
     * - OR the value starts with "{ENC}"
     *
     * @param key the parameter key
     * @param value the parameter value
     * @return true if the parameter is a credential
     */
    private boolean isCredentialParameter(@Nonnull String key, String value) {
        if (key.equals(PASSWORD_KEY) || key.equals(TOKEN_KEY)) {
            return true;
        }
        
        if (value != null && value.startsWith(ENC_PREFIX)) {
            return true;
        }
        
        return false;
    }
}

