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

package org.qubership.atp.environments.utils.jsch;

import java.io.Closeable;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.qubership.atp.environments.errorhandling.ssh.EnvironmentSshCommandRunException;
import org.qubership.atp.environments.utils.Reader;

import com.google.common.base.Preconditions;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SshConnectionManager implements Closeable, AutoCloseable {

    private String host;
    private int port;
    private String login;
    private String password;
    private String key;
    private String passphrase;
    private Session session;
    private int sessionTimeout;

    /**
     * SSH connection constructor.
     */
    public SshConnectionManager(String host, int port, String login, String password, String key, String passphrase) {
        this.host = host;
        this.port = port;
        this.login = login;
        this.password = password;
        this.key = key;
        this.passphrase = passphrase;
        this.sessionTimeout = 7000;
    }

    /**
     * Open SSH connection.
     */
    public void open() {
        try {
            JSch jsch = new JSch();
            if (StringUtils.isNotBlank(key)) {
                byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
                byte[] passphraseBytes = null;
                if (passphrase != null) {
                    passphraseBytes = passphrase.getBytes(StandardCharsets.UTF_8);
                }
                jsch.addIdentity("id_rsa", keyBytes, null, passphraseBytes);
            }
            session = jsch.getSession(login, host, port);
            session.setConfig(getProperties(session));
            session.setPassword(password);
            session.setTimeout(sessionTimeout);
            session.connect();
        } catch (JSchException e) {
            String messageTemplate = "Can't open ssh connection. Host: [%s], Port: [%s], Login: [%s].";
            String message = String.format(messageTemplate, login, host, port);
            log.error(message, e);
        }
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    private Properties getProperties(Session session) {
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        config.put("PreferredAuthentications", "publickey,keyboard-interactive,password");
        config.put("cipher.c2s", session.getConfig("cipher.c2s") + ",ssh-rsa,signature.dss");
        config.put("cipher.s2c", session.getConfig("cipher.s2c") + ",ssh-rsa,signature.dss");
        config.put("server_host_key", session.getConfig("server_host_key") + ",ssh-rsa,signature.dss");
        config.put("PubkeyAcceptedAlgorithms", session.getConfig("PubkeyAcceptedAlgorithms")
                + ",ssh-rsa,signature.dss");
        return config;
    }


    /**
     * See {@link #runCommand(String, Collector)}.
     */
    public Optional<String> runCommand(String command) {
        try {
            return runCommand(command, Collectors.joining());
        } catch (TimeoutException e) {
            return Optional.of(e.getMessage());
        }
    }

    /**
     * Preferred way to execute commands.
     * ! Uses 'bash -c ...' without '--login' option.
     * ! May not work as expected since environment variables may differ from interactive mode.
     *
     * @return command output. May not be present.
     * @throws RuntimeException if command executed with error.
     */
    public <R> Optional<R> runCommand(String command, Collector<? super String, ?, R> collector)
            throws TimeoutException {
        Preconditions.checkState(session.isConnected(),
                "Not connected to an open session.  Call open() first!");
        ChannelExec channel = null;
        try {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setPty(false);
            channel.setCommand(command);
            channel.setInputStream(null);
            //PrintStream out = new PrintStream(channel.getOutputStream());
            InputStream in = channel.getInputStream();
            InputStream errorIn = channel.getErrStream();
            channel.connect();
            // you can also send input to your running process like so:
            // String someInputToProcess = "something";
            // out.println(someInputToProcess);
            // out.flush();
            ChannelExec finalChannel = channel;
            return Reader.readInput(finalChannel::isClosed, in, errorIn, collector);
        } catch (TimeoutException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to run SSH command: {}", command, e);
            throw new EnvironmentSshCommandRunException(command);
        } finally {
            if (channel != null) {
                channel.disconnect();//should be no exception
            }
        }
    }

    @Override
    public void close() {
        try {
            session.disconnect();
        } catch (Exception e) {
            log.warn("Could not disconnect session.", e);
        }
    }
}
