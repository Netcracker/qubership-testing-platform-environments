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

package org.qubership.atp.environments.model.utils.enums;

import java.io.Serializable;
import java.util.Arrays;
import java.util.UUID;

import org.qubership.atp.environments.model.utils.Constants;
import org.qubership.atp.environments.model.utils.HasListValueId;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TypeGettingVersion implements HasListValueId, Serializable {
    BY_HTTP_ENDPOINT(Constants.Environment.System.Connection.HTTP),
    BY_HTTP_ENDPOINT_BASIC_AUTH(Constants.Environment.System.Connection.HTTP),
    BY_SHELL_SCRIPT(Constants.Environment.System.Connection.SSH),
    BY_SQL_QUERY(Constants.Environment.System.Connection.DB),
    BY_KUBERNETES_CONFIGMAP(Constants.Environment.System.Connection.KUBERNETES_PROJECT),
    BY_KUBERNETES_IMAGES(Constants.Environment.System.Connection.KUBERNETES_PROJECT),
    BY_OPENSHIFT_CONFIGURATION(Constants.Environment.System.Connection.OPENSHIFT_SERVER),
    BY_SSM(Constants.Environment.System.Connection.HTTP);

    private UUID listValueId;

    /**
     * Get by list value id.
     */
    public static TypeGettingVersion getByListValueId(UUID listValueId) {
        TypeGettingVersion result = Arrays.stream(values())
                .filter(currentType -> currentType.getListValueId().equals(listValueId))
                .findFirst()
                .orElse(null);
        return result;
    }

    /**
     * Get connection name by TypeGettingVersion.
     * @return connection name
     */
    public String getConnectionNameByGettingType() {
        switch (this) {
            case BY_HTTP_ENDPOINT:
            case BY_HTTP_ENDPOINT_BASIC_AUTH:
                return "HTTP";
            case BY_KUBERNETES_CONFIGMAP:
            case BY_KUBERNETES_IMAGES:
                return "HTTP-KubernetesProject";
            case BY_OPENSHIFT_CONFIGURATION:
                return "HTTP-OpenShiftProject";
            case BY_SHELL_SCRIPT:
                return "SSH";
            case BY_SQL_QUERY:
                return "DB";
            default:
                return "";
        }
    }
}
