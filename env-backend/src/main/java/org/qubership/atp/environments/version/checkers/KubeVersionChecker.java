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

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.qubership.atp.environments.errorhandling.clients.EnvironmentKuberClientConfigMapFetchException;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.utils.enums.TypeGettingVersion;
import org.qubership.atp.environments.utils.cloud.KubeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public class KubeVersionChecker implements VersionChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(KubeVersionChecker.class);

    private String server;
    private String token;
    private String mapName;
    private String namespace;
    private String login;
    private String password;
    private TypeGettingVersion gettingType;

    @Override
    public String getVersion() {
        KubeClient kubeClient = KubeClient.createKubeClient(this.server, this.token, this.login,
                this.password, this.namespace);
        String version;
        try {
            if (gettingType == TypeGettingVersion.BY_KUBERNETES_CONFIGMAP) {
                Map<String, String> configMap = Objects.requireNonNull(kubeClient.getConfigMap(this.mapName).getData());
                Gson gson = new Gson();
                JsonElement json = gson.toJsonTree(configMap);
                version = new GsonBuilder().setPrettyPrinting().create().toJson(json);
            } else {
                Set<String> images = kubeClient.getImages();
                version = String.join("\n", images);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to get config map with name {}, namespace {} from kuber client", mapName, namespace,
                    e);
            throw new EnvironmentKuberClientConfigMapFetchException(mapName, namespace);
        }
        return version;
    }

    @Override
    public void setConnectionParameters(Connection parameters) {
        this.server = parameters.getParameters().get("url");
        this.token = parameters.getParameters().get("token");
        this.mapName = "version";
        this.namespace = parameters.getParameters().get("namespace");
        this.login = parameters.getParameters().get("login");
        this.password = parameters.getParameters().get("password");
    }

    public void setGettingType(TypeGettingVersion type) {
        this.gettingType = type;
    }

    @Override
    public void setParametersVersionCheck(String parameters) {
    }
}
