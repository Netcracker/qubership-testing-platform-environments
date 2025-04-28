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

package org.qubership.atp.environments.version.checkers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.ParametersGettingVersion;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.utils.Constants;
import org.qubership.atp.environments.model.utils.enums.TypeGettingVersion;
import org.qubership.atp.environments.repo.impl.SystemRepositoryImpl;
import org.qubership.atp.environments.service.direct.DecryptorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class VersionCheckerFactory {

    private final DecryptorService decryptorService;
    private final CloseableHttpClient httpClient;
    private final SystemRepositoryImpl systemRepository;

    @Value("${atp-environments.ssh.session.timeout.seconds}")
    private String sshSessionTimeout;

    private static final String SSM_CATEGORY_NAME = "SSM";

    /**
     * This method creates VersionChecker by TypeGettingVersion.
     */
    public VersionChecker createChecker(System system) {
        if (system != null && system.getParametersGettingVersion() != null) {
            ParametersGettingVersion parametersGettingVersion = system.getParametersGettingVersion();
            TypeGettingVersion type = parametersGettingVersion.getType();
            VersionChecker checker;
            switch (type) {
                case BY_SQL_QUERY:
                    checker = new DbVersionChecker();
                    break;
                case BY_SHELL_SCRIPT:
                    checker = new SshVersionChecker();
                    ((SshVersionChecker)checker).setSessionTimeout(sshSessionTimeout);
                    break;
                case BY_KUBERNETES_IMAGES:
                case BY_KUBERNETES_CONFIGMAP:
                    checker = new KubeVersionChecker();
                    ((KubeVersionChecker) checker).setGettingType(type);
                    break;
                case BY_OPENSHIFT_CONFIGURATION:
                    checker = new OpenshiftVersionChecker();
                    List<Connection> openShiftConnections = getAllConnectionsByType(system,
                            type.getListValueId());
                    Preconditions.checkState(!CollectionUtils.isEmpty(openShiftConnections),
                            "No Openshift connection found");
                    for (Connection connection : openShiftConnections) {
                        checker.setConnectionParameters(decryptorService.decryptConnection(connection));
                    }
                    return checker;
                case BY_HTTP_ENDPOINT:
                    checker = new HttpVersionChecker(httpClient);
                    break;
                case BY_HTTP_ENDPOINT_BASIC_AUTH:
                    checker = new HttpVersionChecker(httpClient);
                    ((HttpVersionChecker) checker).setAuthorization();
                    ((HttpVersionChecker) checker).setJsonHeaders(parametersGettingVersion.getHeaders());
                    break;
                case BY_SSM:
                    log.debug("check version by SSM");
                    log.debug("searching for a system with SSM category and HTTP connection");
                    for (Environment env : system.getEnvironments()) {
                        String solutionAlias = env.getSsmSolutionAlias();
                        String instanceAlias = env.getSsmInstanceAlias();
                        List<System> systems = systemRepository.getAllByParentId(env.getId(), SSM_CATEGORY_NAME);
                        if (StringUtils.isEmpty(solutionAlias) || StringUtils.isEmpty(instanceAlias)
                                || CollectionUtils.isEmpty(systems)) {
                            continue;
                        }
                        for (System sys : systems) {
                            Optional<Connection> conn = sys.getConnections().stream()
                                    .filter(connection -> connection.getSourceTemplateId() != null
                                            && connection.getSourceTemplateId()
                                            .equals(Constants.Environment.System.Connection.HTTP))
                                    .findFirst();
                            if (conn.isPresent()) {
                                log.info("System with SSM category found. SystemId: [{}]", sys.getId());
                                checker = new SsmVersionChecker(httpClient);
                                ((SsmVersionChecker) checker).setSystemName(system.getName());
                                ((SsmVersionChecker) checker).setSsmSolutionAlias(solutionAlias);
                                ((SsmVersionChecker) checker).setSsmInstanceAlias(instanceAlias);
                                checker.setConnectionParameters(decryptorService.decryptConnection(conn.get()));
                                return checker;
                            }
                        }
                    }
                    log.warn("No system with SSM category and HTTP connection found");
                    return null;
                default:
                    return null;
            }
            Connection connection = getConnectionByType(system,
                    type.getListValueId());
            Preconditions.checkState(connection != null && !MapUtils.isEmpty(connection.getParameters()),
                    type.getConnectionNameByGettingType() + " connection parameters can't be empty");
            checker.setConnectionParameters(decryptorService.decryptConnection(connection));
            checker.setParametersVersionCheck(parametersGettingVersion.getParameters());
            return checker;
        }
        return null;
    }

    private Connection getConnectionByType(System system, UUID connectionType) {
        return system.getConnections().stream()
                .filter(connection -> connection.getSourceTemplateId() != null
                        && connection.getSourceTemplateId().equals(connectionType))
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException(String.format("Connection with type [ID:%s] not found", connectionType)));
    }

    private List<Connection> getAllConnectionsByType(System system, UUID connectionType) {
        return system.getConnections().stream()
                .filter(connection -> connection.getSourceTemplateId() != null
                        && connection.getSourceTemplateId().equals(connectionType))
                .collect(Collectors.toList());
    }
}
