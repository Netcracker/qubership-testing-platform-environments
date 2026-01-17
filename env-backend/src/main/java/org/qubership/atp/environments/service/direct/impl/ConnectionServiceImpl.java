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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.qubership.atp.auth.springbootstarter.entities.UserInfo;
import org.qubership.atp.auth.springbootstarter.ssl.Provider;
import org.qubership.atp.environments.config.HazelcastConfig;
import org.qubership.atp.environments.enums.ExecutorTemplateEnum;
import org.qubership.atp.environments.enums.TaEngineParamSectionEnum;
import org.qubership.atp.environments.errorhandling.internal.EnvironmentJsonParseException;
import org.qubership.atp.environments.errorhandling.taengine.EnvironmentTaEngineValidationException;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.model.utils.Constants;
import org.qubership.atp.environments.repo.impl.ConnectionRepositoryImpl;
import org.qubership.atp.environments.service.direct.ConnectionService;
import org.qubership.atp.environments.service.rest.client.CatalogFeignClient;
import org.qubership.atp.environments.service.rest.server.dto.TaEngineAbstractParam;
import org.qubership.atp.environments.utils.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

@Slf4j
@Service("connectionService")
@SuppressWarnings("CPD-START")
public class ConnectionServiceImpl implements ConnectionService {

    private final ConnectionRepositoryImpl connectionRepository;
    private final CatalogFeignClient catalogClient;
    private final DateTimeUtil dateTimeUtil;
    private final Provider<UserInfo> userInfoProvider;

    @Value("${catalogue.integration.enabled}")
    private boolean catalogueIntegration;

    /**
     * Autowired constructor.
     */
    @Autowired
    public ConnectionServiceImpl(ConnectionRepositoryImpl connectionRepository, DateTimeUtil dateTimeUtil,
                                 CatalogFeignClient catalogClient, Provider<UserInfo> userInfoProvider) {
        this.connectionRepository = connectionRepository;
        this.catalogClient = catalogClient;
        this.dateTimeUtil = dateTimeUtil;
        this.userInfoProvider = userInfoProvider;
    }

    @Nullable
    @Override
    public Connection get(@Nonnull UUID id) {
        return connectionRepository.getById(id);
    }

    @Nullable
    @Override
    public Connection getByParentAndName(@Nonnull UUID systemId, @Nonnull String name) {
        return connectionRepository.getByParentIdAndName(systemId, name);
    }

    @Override
    public boolean existsById(@Nonnull UUID id) {
        return connectionRepository.existsById(id);
    }

    @Nonnull
    @Override
    public List<Connection> getAll() {
        return connectionRepository.getAll();
    }

    @Nonnull
    @Override
    public List<Connection> getAll(@Nonnull List<UUID> environmentIds,
                                   @Nonnull UUID systemCategoryId) {
        return connectionRepository.getAll(environmentIds, systemCategoryId);
    }

    /**
     * Getting a list of project connections.
     *
     * @param projectId Project identifier
     * @return list of names
     */
    @Override
    public List<Connection> getConnectionsByProjectId(@Nonnull UUID projectId) {
        return connectionRepository.getConnectionsByProjectId(projectId);
    }

    @Nonnull
    @Override
    @Transactional
    public Connection create(UUID systemId,
                             String name,
                             String description,
                             ConnectionParameters parameters,
                             String connectionType,
                             UUID sourceTemplateId,
                             UUID projectId,
                             List<String> services) {
        return create(systemId,
                null,
                name,
                description,
                parameters,
                connectionType,
                sourceTemplateId,
                projectId,
                services, null);
    }

    @Nonnull
    @Override
    @Transactional
    public Connection create(UUID systemId,
                             UUID connectionId,
                             String name,
                             String description,
                             ConnectionParameters parameters,
                             String connectionType,
                             UUID sourceTemplateId,
                             UUID projectId,
                             List<String> services, UUID sourceId) {
        Connection connection;
        UUID userId = userInfoProvider.get().getId();
        if (parameters != null) {
            parameters.replaceAll((k, v) -> (v == null) ? v : v.trim());
            validateTaEngineProviderParameters(sourceTemplateId, parameters);
        }
        name = name.trim();
        if (connectionId == null) {
            connection = connectionRepository.create(systemId, name, description, parameters,
                    dateTimeUtil.timestampAsUtc(), userId, connectionType, sourceTemplateId, services, sourceId);
        } else {
            connection = connectionRepository.create(systemId, connectionId, name, description, parameters,
                    dateTimeUtil.timestampAsUtc(), userId, connectionType, sourceTemplateId, services, sourceId);
        }
        if (catalogueIntegration && projectId != null) {
            catalogClient.updateActions(projectId);
        }
        return connection;
    }

    @Nonnull
    @Override
    public Connection create(UUID systemId, String name, String description, ConnectionParameters parameters,
                             String connectionType, UUID sourceTemplateId, List<String> services) {
        return create(systemId, name, description, parameters, connectionType, sourceTemplateId, null, services);
    }

    @Nonnull
    @Override
    public Connection replicate(@Nonnull UUID systemId, UUID connectionId, @Nonnull String name, String description,
                                ConnectionParameters parameters, String connectionType, UUID sourceTemplateId,
                                List<String> services, UUID sourceId) {
        return create(systemId, connectionId, name, description, parameters, connectionType, sourceTemplateId,
                null, services, sourceId);
    }

    @Override
    public Connection update(Connection connection) {
        return update(connection.getId(),
                connection.getSystemId(),
                connection.getName(),
                connection.getDescription(),
                connection.getParameters(),
                connection.getConnectionType(),
                connection.getSourceTemplateId(),
                null,
                connection.getServices(),
                connection.getSourceId());
    }

    @Override
    @Transactional
    public Connection update(UUID id,
                             UUID systemId,
                             String name,
                             String description,
                             ConnectionParameters parameters,
                             String connectionType,
                             UUID sourceTemplateId,
                             UUID projectId,
                             List<String> services) {
        UUID userId = userInfoProvider.get().getId();
        if (parameters != null) {
            parameters.replaceAll((k, v) -> (v == null) ? v : v.trim());
            validateTaEngineProviderParameters(sourceTemplateId, parameters);
        }
        Connection connection = connectionRepository.update(id, systemId, name.trim(), description, parameters,
                dateTimeUtil.timestampAsUtc(), userId,
                connectionType, sourceTemplateId, services);
        if (catalogueIntegration && projectId != null) {
            catalogClient.updateActions(projectId);
        }
        return connection;
    }

    @Override
    @Transactional
    public Connection update(UUID id,
                             UUID systemId,
                             String name,
                             String description,
                             ConnectionParameters parameters,
                             String connectionType,
                             UUID sourceTemplateId,
                             UUID projectId,
                             List<String> services, UUID sourceId) {
        UUID userId = userInfoProvider.get().getId();
        if (parameters != null) {
            parameters.replaceAll((k, v) -> (v == null) ? v : v.trim());
            validateTaEngineProviderParameters(sourceTemplateId, parameters);
        }
        Connection connection = connectionRepository.update(id, systemId, name.trim(), description, parameters,
                dateTimeUtil.timestampAsUtc(), userId,
                connectionType, sourceTemplateId, services, sourceId);
        if (catalogueIntegration && projectId != null) {
            catalogClient.updateActions(projectId);
        }
        return connection;
    }

    @Override
    public Connection update(UUID id, UUID systemId, String name, String description, ConnectionParameters parameters,
                             String connectionType, UUID sourceTemplateId, List<String> services) {
        return update(id, systemId, name, description, parameters, connectionType, sourceTemplateId, null, services);
    }

    @Override
    @Cacheable(value = HazelcastConfig.CONNECTION_TEMPLATES_CACHE,
            key = "T(org.springframework.cache.interceptor.SimpleKey).EMPTY")
    public List<Connection> getConnectionTemplates() {
        return connectionRepository.getConnectionTemplates();
    }

    @Override
    public Connection getConnectionTemplateByName(String name) {
        return connectionRepository.getConnectionTemplateByName(name);
    }

    @Override
    public List<Connection> getConnectionByHost(String host) {
        return connectionRepository.getConnectionsByHost(host);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        UUID userId = userInfoProvider.get().getId();
        UUID systemId = connectionRepository.getSystemId(id);
        connectionRepository.delete(systemId, id, dateTimeUtil.timestampAsUtc(), userId);
    }

    @Override
    public void updateParameters(UUID id, ConnectionParameters parameters, List<String> services) {
        if (parameters != null) {
            parameters.replaceAll((k, v) -> (v == null) ? v : v.trim());
        }
        UUID systemId = connectionRepository.getSystemId(id);
        connectionRepository.updateParameters(systemId, id, parameters, dateTimeUtil.timestampAsUtc(),
                userInfoProvider.get().getId(), services);
    }

    @Override
    public void validateTaEngineProviderParameters(UUID sourceTemplateId, ConnectionParameters parameters) {
        if (parameters.validationIsEnabled()
                && sourceTemplateId != null
                && sourceTemplateId.equals(Constants.Environment.System.Connection.TA_ENGINES_PROVIDER)) {
            Preconditions.checkNotNull(parameters.get("Acquire_Create_Tool_Request_Body"),
                    "Acquire Create Tool Request Body can not be empty");
            List<Map<String, String>> invalidFields = new ArrayList<>();
            String acquireCreateToolRequestBody = parameters.get("Acquire_Create_Tool_Request_Body");

            checkDuplicates(acquireCreateToolRequestBody, invalidFields);

            Map<String, ?> firstLevelParameters =
                    parseJsonAsMap(acquireCreateToolRequestBody, CaseInsensitiveMap.class);
            validateParameter((String) firstLevelParameters.get("image"), "image", null, invalidFields);
            validateParameter((String) firstLevelParameters.get("name"), "name", null, invalidFields);
            List<String> argsList = (List<String>) firstLevelParameters.get("args");
            if (argsList != null && !argsList.isEmpty()) {
                log.info("Args not empty");
                validateParameter(getMatchingParameter(argsList, "version"), "version", "args", invalidFields);
                checkUrlRules("Nexus", 2, 4, argsList, "args", invalidFields);
                checkUrlRules("SVN", 2, 4, argsList, "args", invalidFields);
                checkUrlRules("Git", 3, 5, argsList, "args", invalidFields);
                checkUrlRules("CP", 1, 1, argsList, "args", invalidFields);
            }
            if (!invalidFields.isEmpty()) {
                log.error("Failed to validate connection because of incorrect fields: {}", invalidFields);
                throw new EnvironmentTaEngineValidationException(invalidFields);
            }
        }
    }

    @Override
    public List<Connection> getByIds(List<UUID> ids) {
        List<Connection> connections = connectionRepository.getByIds(ids);
        return connections != null ? connections : Collections.emptyList();
    }

    public UUID getProjectId(UUID connectionId) {
        return connectionRepository.getProjectId(connectionId);
    }

    @SuppressWarnings("unchecked")
    private Map<String, ?> parseJsonAsMap(String jsonBody, Class<? extends Map> mapClass) {
        try {
            return new ObjectMapper().readValue(jsonBody, mapClass);
        } catch (IOException e) {
            log.error("Failed to parse JSON data", e);
            throw new EnvironmentJsonParseException("Failed to parse JSON data");
        }
    }

    private void checkDuplicates(String acquireCreateToolRequestBody, List<Map<String, String>> invalidFields) {
        List<String> excludeList = Arrays.asList("svn", "nexus", "git", "cp");
        Map<String, ?> parameterMap = parseJsonAsMap(acquireCreateToolRequestBody, HashMap.class);
        JSONObject jsonObject = JSONObject.fromObject(acquireCreateToolRequestBody);
        recursiveCheck(parameterMap, null, excludeList, invalidFields);
        for (Map.Entry<String, ?> entry : parameterMap.entrySet()) {
            String key = entry.getKey();
            Object valueMap = entry.getValue();
            Object valueJson = jsonObject.get(key);
            if (valueMap.equals(valueJson)) {
                continue;
            } else if (valueMap instanceof List && valueJson instanceof List) {
                List<?> listFromMap = (List<?>) valueMap;
                List<?> listFromJson = (List<?>) valueJson;
                if (listFromMap.equals(listFromJson)) {
                    continue;
                }
            }
            addInvalidField(invalidFields, null, key);
        }
    }

    private void validateParameter(String parameter, String parameterName, String parameterSection,
                                   List<Map<String, String>> invalidFields) {
        if (parameter == null || parameter.isEmpty()) {
            addInvalidField(invalidFields, parameterSection, parameterName);
        }
    }

    private void checkUrlRules(String ruleName, int minPlus, int maxPlus, List<String> argsList,
                               String parameterSection, List<Map<String, String>> invalidFields) {
        String regexStringContainsRule = "^[-]{0,2}((?i)" + ruleName + "=).?$";
        String regexDeleteRuleName = "^[-]{0,2}((?i)" + ruleName + "=)";
        String regexUrlRule =
                "^(http[s]?://)[\\w\\-._~:/?#[\\\\]@!$&'()*,;=.\\\\]+([+][\\w\\-._~:/?#[\\\\]@!$&'()*,;=.\\\\]+)"
                        + "{" + minPlus + "," + maxPlus + "}$";
        argsList
                .stream()
                .filter(x -> x.matches(regexStringContainsRule))
                .map(x -> {
                    validateParameter(getMatchingParameter(argsList, ruleName), ruleName, parameterSection,
                            invalidFields);
                    return x.replaceFirst(regexDeleteRuleName, "");
                })
                .allMatch(x -> {
                    if (x.matches(regexUrlRule)) {
                        return true;
                    }
                    addInvalidField(invalidFields, parameterSection, ruleName);
                    return false;
                });
    }

    @Nonnull
    private String getMatchingParameter(List<String> parametersList, String parameter) {
        String regexFind = "^[-]{0,2}((?i)" + parameter + "=).+$";
        String regexReplace = "^[-]{0,2}((?i)" + parameter + "=)";
        return parametersList.stream()
                .filter((x) -> x.matches(regexFind))
                .collect(Collectors.joining())
                .replaceFirst(regexReplace, "");
    }

    private void addInvalidField(List<Map<String, String>> invalidFields, String section, String field) {
        field = findBusinessName(field, section);
        invalidFields.add(
                Stream.of(new String[][]{{"name", field}, {"section", section}})
                        .collect(HashMap::new, (m, v) -> m.put(v[0], v[1]), HashMap::putAll));
    }

    private String findBusinessName(String field, String section) {
        for (ExecutorTemplateEnum templateEnum : ExecutorTemplateEnum.values()) {
            List<TaEngineAbstractParam> attributesFromTemplate = new ArrayList<>();
            attributesFromTemplate.addAll(templateEnum.getTemplate().getAdditionalParams());
            attributesFromTemplate.addAll(templateEnum.getTemplate().getDefaultParams());
            attributesFromTemplate.addAll(templateEnum.getTemplate().getTaEngineParams());
            attributesFromTemplate.addAll(templateEnum.getTemplate().getTaEngineProviderParams());
            for (TaEngineAbstractParam defaultParam : attributesFromTemplate) {
                if (defaultParam.getName().toUpperCase(Locale.ROOT).equals(field.toUpperCase(Locale.ROOT))) {
                    TaEngineParamSectionEnum templateSection = defaultParam.getSection();
                    if (section == null && templateSection == null) {
                        return defaultParam.getBusinessName();
                    } else if (section != null
                            && templateSection != null
                            && section.toUpperCase(Locale.ROOT)
                            .equals(templateSection.getSection().toUpperCase(Locale.ROOT))) {
                        return defaultParam.getBusinessName();
                    }
                }
            }
        }
        return field;
    }

    private <T> void recursiveCheck(T parameter,
                                    String parameterSection,
                                    List<String> excludeList,
                                    List<Map<String, String>> invalidFields) {
        List<String> stringList = new ArrayList<>();
        String regexCanBeParsedAsArray = "^[-]{0,2}(([^=](?=.))+=).+$";
        String regexCanExtractParameterName = "^[-]{0,2}([^=]+=)((?=[^\\s])[^,])+$";
        String regexExtraSymbols = "(?=[^.:])\\W";
        String regexParameterName = "^[-]{0,2}([^=]+=)";
        String regexParsingSeparator = "(\\s+-*)|(\\s?,\\s?)";
        String regexSectionName = "^[-]{0,2}(([^=](?=.))+=)";

        if (parameter instanceof List) {
            ((List<?>) parameter).forEach(listElement -> {
                if (listElement instanceof String) {
                    String stringParameter = (String) listElement;
                    if (stringParameter.matches(regexCanExtractParameterName)) {
                        Matcher matcher = Pattern.compile(regexParameterName)
                                .matcher(stringParameter);
                        String parameterName;
                        if (matcher.find()) {
                            parameterName = matcher.group(1)
                                    .toLowerCase(Locale.ROOT)
                                    .replaceAll(regexExtraSymbols, "");
                            validateParameterInSection(parameterSection, excludeList, invalidFields, stringList,
                                    parameterName);
                        }
                    }
                } else if (listElement instanceof Map) {
                    Map<String, ?> parameterMap = (Map<String, ?>) listElement;
                    decomposeMapForValidation(parameterSection, excludeList, invalidFields, stringList, parameterMap);
                }
                recursiveCheck(listElement, parameterSection, excludeList, invalidFields);
            });
        } else if (parameter instanceof Map) {
            Map<String, ?> parameterMap = (Map<String, ?>) parameter;
            decomposeMapForValidation(parameterSection, excludeList, invalidFields, stringList, parameterMap);
        } else if (parameter instanceof String) {
            String stringParameter = (String) parameter;
            if (stringParameter.matches(regexCanBeParsedAsArray)) {
                Matcher matcher = Pattern.compile(regexSectionName)
                        .matcher(stringParameter);
                String sectionName = null;
                if (matcher.find()) {
                    sectionName = matcher.group(1).replaceAll(regexExtraSymbols, "");
                }
                String[] parsedStrings = stringParameter.replaceFirst(regexSectionName, "")
                        .split(regexParsingSeparator);
                recursiveCheck(Arrays.asList(parsedStrings), sectionName, excludeList, invalidFields);
            }
        } else {
            log.debug("object of '{}' type not processed: {}", parameter.getClass().getName(), parameter);
        }
    }

    private void validateParameterInSection(String parameterSection,
                                            List<String> excludeList,
                                            List<Map<String, String>> invalidFields,
                                            List<String> stringList,
                                            String parameterName) {
        if (stringList.contains(parameterName) && !excludeList.contains(parameterName)) {
            addInvalidField(invalidFields, parameterSection, parameterName);
        } else {
            stringList.add(parameterName);
        }
    }

    private void decomposeMapForValidation(String parameterSection,
                                           List<String> excludeList,
                                           List<Map<String, String>> invalidFields,
                                           List<String> stringList,
                                           Map<String, ?> parameterMap) {
        parameterMap.forEach((key, value) -> {
            if (key.equals("name") && parameterMap.containsKey("value")) {
                String parameterName = String.valueOf(value)
                        .toLowerCase(Locale.ROOT);
                validateParameterInSection(parameterSection, excludeList, invalidFields, stringList, parameterName);
            } else if (key.equals("value")) {
                recursiveCheck(value, parameterSection, excludeList, invalidFields);
            } else {
                String parameterName = key.toLowerCase(Locale.ROOT);
                validateParameterInSection(parameterSection, excludeList, invalidFields, stringList, parameterName);
                recursiveCheck(value, parameterName, excludeList, invalidFields);
            }
        });
    }
}
