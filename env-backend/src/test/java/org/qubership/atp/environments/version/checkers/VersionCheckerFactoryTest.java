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

import static org.mockito.Mockito.mock;

import java.util.Collections;

import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.model.ParametersGettingVersion;
import org.qubership.atp.environments.model.System;
import org.qubership.atp.environments.model.impl.SystemImpl;
import org.qubership.atp.environments.model.utils.Constants;
import org.qubership.atp.environments.model.utils.enums.TypeGettingVersion;
import org.qubership.atp.environments.repo.impl.SystemRepositoryImpl;
import org.qubership.atp.environments.service.direct.DecryptorService;
import org.qubership.atp.environments.utils.TestEntityUtils;

public class VersionCheckerFactoryTest {
    protected CloseableHttpClient httpClient = Mockito.mock(CloseableHttpClient.class);
    private DecryptorService decryptorService = Mockito.mock(DecryptorService.class);
    private VersionCheckerFactory versionCheckerFactory;
    private System system;
    private ParametersGettingVersion parametersGettingVersion;
    private ConnectionParameters connectionParameters;

    @Before
    public void setUp() {
        versionCheckerFactory = new VersionCheckerFactory(decryptorService, httpClient, mock(SystemRepositoryImpl.class));
        system = new SystemImpl();
        parametersGettingVersion = new ParametersGettingVersion();
        connectionParameters = new ConnectionParameters();
        connectionParameters.put("ssh_host", "URL");
    }

    @Test
    public void factoryTest_createHttpChecker_done() {
        Connection connection = TestEntityUtils.createConnection("HTTP", connectionParameters,
                Constants.Environment.System.Connection.HTTP);
        Mockito.when(decryptorService.decryptConnection(connection)).thenReturn(connection);
        system.setConnections(Collections.singletonList(connection));
        parametersGettingVersion.setType(TypeGettingVersion.BY_HTTP_ENDPOINT);
        system.setParametersGettingVersion(parametersGettingVersion);
        VersionChecker versionChecker = versionCheckerFactory.createChecker(system);
        Assert.assertTrue(versionChecker instanceof HttpVersionChecker);
        parametersGettingVersion.setType(TypeGettingVersion.BY_HTTP_ENDPOINT_BASIC_AUTH);
        versionChecker = versionCheckerFactory.createChecker(system);
        Assert.assertTrue(versionChecker instanceof HttpVersionChecker);
    }

    @Test
    public void factoryTest_createSshChecker_done() {
        Connection connection = TestEntityUtils.createConnection("SSH", connectionParameters,
                Constants.Environment.System.Connection.SSH);
        Mockito.when(decryptorService.decryptConnection(connection)).thenReturn(connection);
        system.setConnections(Collections.singletonList(connection));
        parametersGettingVersion.setType(TypeGettingVersion.BY_SHELL_SCRIPT);
        system.setParametersGettingVersion(parametersGettingVersion);
        VersionChecker versionChecker = versionCheckerFactory.createChecker(system);
        Assert.assertTrue(versionChecker instanceof SshVersionChecker);
    }

    @Test
    public void factoryTest_createDBChecker_done() {
        connectionParameters.put("db_type", "oracle");
        Connection connection = TestEntityUtils.createConnection("DB", connectionParameters,
                Constants.Environment.System.Connection.DB);
        Mockito.when(decryptorService.decryptConnection(connection)).thenReturn(connection);
        system.setConnections(Collections.singletonList(connection));
        parametersGettingVersion.setType(TypeGettingVersion.BY_SQL_QUERY);
        system.setParametersGettingVersion(parametersGettingVersion);
        VersionChecker versionChecker = versionCheckerFactory.createChecker(system);
        Assert.assertTrue(versionChecker instanceof DbVersionChecker);
    }

    @Test
    public void factoryTest_createKubernetesChecker_done() {
        Connection connection = TestEntityUtils.createConnection("Kubernetes", connectionParameters,
                Constants.Environment.System.Connection.KUBERNETES_PROJECT);
        Mockito.when(decryptorService.decryptConnection(connection)).thenReturn(connection);
        system.setConnections(Collections.singletonList(connection));
        parametersGettingVersion.setType(TypeGettingVersion.BY_KUBERNETES_CONFIGMAP);
        system.setParametersGettingVersion(parametersGettingVersion);
        VersionChecker versionChecker = versionCheckerFactory.createChecker(system);
        Assert.assertTrue(versionChecker instanceof KubeVersionChecker);
        parametersGettingVersion.setType(TypeGettingVersion.BY_KUBERNETES_IMAGES);
        versionChecker = versionCheckerFactory.createChecker(system);
        Assert.assertTrue(versionChecker instanceof KubeVersionChecker);

    }

    @Test
    public void factoryTest_createOpenShiftChecker_done() {
        Connection connection = TestEntityUtils.createConnection("Openshift", connectionParameters,
                Constants.Environment.System.Connection.OPENSHIFT_SERVER);
        Mockito.when(decryptorService.decryptConnection(connection)).thenReturn(connection);
        system.setConnections(Collections.singletonList(connection));
        parametersGettingVersion.setType(TypeGettingVersion.BY_OPENSHIFT_CONFIGURATION);
        system.setParametersGettingVersion(parametersGettingVersion);
        VersionChecker versionChecker = versionCheckerFactory.createChecker(system);
        Assert.assertTrue(versionChecker instanceof OpenshiftVersionChecker);

    }


}
