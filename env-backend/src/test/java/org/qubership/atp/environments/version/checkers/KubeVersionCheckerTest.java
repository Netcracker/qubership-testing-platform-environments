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

import static org.mockito.ArgumentMatchers.any;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.model.impl.ConnectionImpl;
import org.qubership.atp.environments.model.utils.enums.TypeGettingVersion;
import org.qubership.atp.environments.utils.cloud.KubeClient;

import io.kubernetes.client.openapi.models.V1ConfigMap;

public class KubeVersionCheckerTest {

    @Test
    public void getVersion_validKubeConnection_gotFormattedJsonStructure() {
        V1ConfigMap v1ConfigMap = createV1ConfigMapAndSetData();
        KubeVersionChecker kubeVersionChecker =
                createKubeVersionCheckerAndSetConnection(TypeGettingVersion.BY_KUBERNETES_CONFIGMAP);
        try (MockedStatic<KubeClient> mock = Mockito.mockStatic(KubeClient.class)) {
            KubeClient kubeClient = Mockito.mock(KubeClient.class);
            mock.when(() -> KubeClient.createKubeClient(any(), any(), any(), any(), any(String.class)))
                    .thenReturn(kubeClient);
            mock.when(() -> kubeClient.getConfigMap(any(String.class))).thenReturn(v1ConfigMap);
            String result = kubeVersionChecker.getVersion();
            String configMapVersionExpectedResult = "{\n"
                    + "  \"cloudbss-bsscs\": \"release-2020\",\n"
                    + "  \"cloud-core\": \"release-6-19\",\n"
                    + "  \"cloud-core-ext\": \"release-7-3-0\"\n"
                    + "}";
            Assertions.assertEquals(configMapVersionExpectedResult, result);
        }
    }

    @Test
    public void getVersion_validKubeConnection_gotListImages() {
        Set<String> images = getImages();
        KubeVersionChecker kubeVersionChecker =
                createKubeVersionCheckerAndSetConnection(TypeGettingVersion.BY_KUBERNETES_IMAGES);
        try (MockedStatic<KubeClient> mock = Mockito.mockStatic(KubeClient.class)) {
            KubeClient kubeClient = Mockito.mock(KubeClient.class);
            mock.when(() -> KubeClient.createKubeClient(any(), any(), any(), any(), any(String.class))).thenReturn(kubeClient);
            mock.when(kubeClient::getImages).thenReturn(images);
            String result = kubeVersionChecker.getVersion();
            String imagesVersionExpectedResult = "#\n*\n.\n1\n2\n3\n:\n@\nA\nB\nC\n^\na\nb\nc";
            Assertions.assertEquals(imagesVersionExpectedResult, result);
        }
    }

    private V1ConfigMap createV1ConfigMapAndSetData() {
        Map<String, String> map = new HashMap<>();
        map.put("cloud-core-ext", "release-7-3-0");
        map.put("cloud-core", "release-6-19");
        map.put("cloudbss-bsscs", "release-2020");
        V1ConfigMap v1ConfigMap = new V1ConfigMap();
        v1ConfigMap.setData(map);
        return v1ConfigMap;
    }

    private Set<String> getImages() {
        Set<String> images = new TreeSet<>();
        images.add("c");
        images.add("b");
        images.add("a");
        images.add("a");
        images.add("A");
        images.add("C");
        images.add("B");
        images.add("3");
        images.add("2");
        images.add("1");
        images.add("2");
        images.add(".");
        images.add("#");
        images.add(":");
        images.add("@");
        images.add("*");
        images.add("^");
        return images;
    }

    private KubeVersionChecker createKubeVersionCheckerAndSetConnection(TypeGettingVersion typeGettingVersion) {
        ConnectionParameters connectionParameters = new ConnectionParameters();
        connectionParameters.put("mapName", "version");
        connectionParameters.put("namespace", "dev-ci");
        Connection connection = new ConnectionImpl();
        connection.setParameters(connectionParameters);
        KubeVersionChecker kubeVersionChecker = new KubeVersionChecker();
        kubeVersionChecker.setConnectionParameters(connection);
        kubeVersionChecker.setGettingType(typeGettingVersion);
        return kubeVersionChecker;
    }
}
