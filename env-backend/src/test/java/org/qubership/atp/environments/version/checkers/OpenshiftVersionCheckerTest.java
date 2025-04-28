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

import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.model.impl.ConnectionImpl;
import org.qubership.atp.environments.utils.cloud.ExternalCloudClient;
import org.qubership.atp.environments.utils.cloud.OpenshiftClient;

public class OpenshiftVersionCheckerTest {

    protected String expectedResult = "type_1/image_20210324-110636\n" +
            "type_2/image_20200602-062319";

    @Test
    public void getVersion() {
        String result;
        OpenshiftClient openshiftClient = Mockito.mock(OpenshiftClient.class);
        OpenshiftVersionChecker openshiftVersionChecker = createOpenshiftVersionCheckerAndSetConnection();
        try (MockedStatic<ExternalCloudClient> mock = Mockito.mockStatic(ExternalCloudClient.class)) {
            mock.when(() -> ExternalCloudClient.createClient(Mockito.any(), Mockito.eq(OpenshiftClient.class)))
                    .thenReturn(openshiftClient);
            when(openshiftClient.getImageSet()).thenReturn(getListImages());
            result = openshiftVersionChecker.getVersion();
            Assertions.assertEquals(expectedResult, result);
        }
    }

    private Set<String> getListImages() {
        Set<String> s = new HashSet<>();
        s.add("type_1/image_20210324-110636");
        s.add("type_2/image_20200602-062319");
        return s;
    }

    private OpenshiftVersionChecker createOpenshiftVersionCheckerAndSetConnection() {
        ConnectionParameters connectionParameters = new ConnectionParameters();
        connectionParameters.put("project", "dev-ci");
        connectionParameters.put("url", "server");
        connectionParameters.put("login", "user");
        connectionParameters.put("password", "password");
        Connection connection = new ConnectionImpl();
        connection.setId(UUID.randomUUID());
        connection.setParameters(connectionParameters);
        OpenshiftVersionChecker versionChecker = new OpenshiftVersionChecker();
        versionChecker.setConnectionParameters(connection);
        return versionChecker;
    }
}
