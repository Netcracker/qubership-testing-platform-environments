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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.model.impl.ConnectionImpl;
import org.qubership.atp.environments.utils.Reader;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SshVersionCheckerTest {

    private static final String sshHost = "ssh_host_example";
    private static final String sshPassword = "password";
    private static final String sshKey = "aaa";
    private static final String passphrase = "aaa";
    private static final String sshPort = "5432";
    private static final String sshLogin = "login";
    private static final String test_version = "Test Version";

    private final ThreadLocal<SshVersionChecker> versionChecker = new ThreadLocal<>();
    private final ThreadLocal<Session> session = new ThreadLocal<>();

    @BeforeEach
    public void setUp() throws Exception {
        Connection connectionModel = new ConnectionImpl();
        connectionModel.setParameters(new ConnectionParameters());
        connectionModel.getParameters().putAll(
                Stream.of(new String[][]{
                        {"ssh_host", sshHost},
                        {"ssh_key", sshKey},
                        {"passphrase", passphrase},
                        {"ssh_port", sshPort},
                        {"ssh_login", sshLogin},
                        {"ssh_password", sshPassword}
                })
                        .collect(HashMap::new, (m, v) -> m.put(v[0], v[1]), HashMap::putAll));
        SshVersionChecker versionCheckerThread = new SshVersionChecker();
        versionCheckerThread.setConnectionParameters(connectionModel);


        ChannelExec channelExec = Mockito.mock(ChannelExec.class);
        Session sessionMock = Mockito.mock(Session.class);
        when(sessionMock.isConnected()).thenReturn(true);
        when(sessionMock.openChannel("exec")).thenReturn(channelExec);

        versionChecker.set(versionCheckerThread);
        session.set(sessionMock);
    }

    @Test
    public void getVersion_PassedVersionReceived_VersionIsntEmpty() {
        try (MockedConstruction<JSch> ignored = Mockito.mockConstruction(JSch.class,
                (mock, context) -> when(mock.getSession(any(), any(), anyInt())).thenReturn(session.get()))) {
            try (MockedStatic<Reader> reader = Mockito.mockStatic(Reader.class)) {
                reader.when(() -> Reader.readInput(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                        .thenReturn(Optional.of(test_version));
                Assertions.assertEquals(test_version, versionChecker.get().getVersion());
            }
        }
    }

    @Test
    public void getVersion_PassedVersionReceived_EmptyVersion() {
        try (MockedConstruction<JSch> ignored = Mockito.mockConstruction(JSch.class,
                (mock, context) -> when(mock.getSession(any(), any(), anyInt())).thenReturn(session.get()))) {
            try (MockedStatic<Reader> reader = Mockito.mockStatic(Reader.class)) {
                reader.when(() -> Reader.readInput(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                        .thenReturn(Optional.of(""));
                Assertions.assertEquals("Unknown", versionChecker.get().getVersion());
            }
        }
    }
}
