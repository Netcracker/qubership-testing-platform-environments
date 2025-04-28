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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.qubership.atp.environments.errorhandling.clients.EnvironmentOpenshiftImageListFetchException;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.utils.cloud.ExternalCloudClient;
import org.qubership.atp.environments.utils.cloud.OpenshiftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenshiftVersionChecker implements VersionChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenshiftVersionChecker.class);
    private final HashMap<UUID, ConnectionParameters> connectionParametersMap = new HashMap<>();

    @Override
    public String getVersion() {
        Set<String> set = new HashSet<>();
        connectionParametersMap.forEach((connectionId, parameters) -> {
            OpenshiftClient openshiftClient = (OpenshiftClient) ExternalCloudClient.createClient(parameters,
                    OpenshiftClient.class);
            try {
                set.addAll(new HashSet<>(openshiftClient.getImageSet()));
            } catch (Exception e) {
                LOGGER.error("Failed to get image list for project {} from openshift client",
                        OpenShiftCredentialsNames.NAMESPACE, e);
                String projectName = parameters.get(OpenShiftCredentialsNames.NAMESPACE);
                throw new EnvironmentOpenshiftImageListFetchException(projectName);
            }
        });
        List<String> sortedList = new ArrayList<>(set);
        Collections.sort(sortedList);
        return sortedList.stream().collect(Collectors.joining("\n", "", ""));
    }


    @Override
    public void setConnectionParameters(Connection parameters) {
        this.connectionParametersMap.put(parameters.getId(), parameters.getParameters());
    }

    @Override
    public void setParametersVersionCheck(String parameters) {
    }

    public interface OpenShiftCredentialsNames {

        String HOST = "url";
        String LOGIN = "login";
        String PASSWORD = "password";
        String NAMESPACE = "project";
        String TOKEN = "token";
    }
}
