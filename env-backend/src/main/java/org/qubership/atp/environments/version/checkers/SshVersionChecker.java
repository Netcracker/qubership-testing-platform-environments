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

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.utils.jsch.SshConnectionManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SshVersionChecker implements VersionChecker {

    private String host;
    private int port = 22;
    private String login;
    private String password;
    private String key;
    private String passphrase;
    private String parameters;
    private String sessionTimeout;

    /**
     * Create SshConnectionProperties from SSH connection parameters.
     */
    public void setConnectionParameters(Connection parameters) {
        this.host = parameters.getParameters().get("ssh_host");
        int separatorIndex = host.indexOf(":");
        if (separatorIndex != -1) {
            try {
                port = Integer.parseInt(host.substring(separatorIndex + 1));
            } catch (Exception e) {
                log.error("Can't parse port from host string. Host: " + host);
            }
            host = host.substring(0, separatorIndex);
        }
        if (StringUtils.isNotBlank(parameters.getParameters().get("ssh_port"))) {
            port = Integer.parseInt(parameters.getParameters().get("ssh_port"));
        }
        this.login = parameters.getParameters().get("ssh_login");
        this.password = parameters.getParameters().get("ssh_password");
        this.key = parameters.getParameters().get("ssh_key");
        this.passphrase = parameters.getParameters().get("passphrase");
    }

    public void setSessionTimeout(String timeout) {
        this.sessionTimeout = timeout;
    }

    @Override
    public void setParametersVersionCheck(String parameters) {
        this.parameters = parameters;
    }

    @Override
    public String getVersion() {
        String version = null;
        try (SshConnectionManager manager = new SshConnectionManager(host, port, login, password, key, passphrase)) {
            if (StringUtils.isNotBlank(sessionTimeout)) {
                manager.setSessionTimeout(Integer.parseInt(sessionTimeout));
            }
            manager.open();
            Optional<String> commandResult = manager.runCommand(parameters);
            if (commandResult.isPresent()) {
                version = commandResult.get();
            }
        } catch (RuntimeException e) {
            log.error("Error on getting version by shell script. Cause: " + e.getMessage());
        }
        return !StringUtils.isBlank(version) ? version : "Unknown";
    }

    protected String processString(String inputString, boolean isTrimSpaces) {
        return isTrimSpaces ? trimMultipleSpaces(inputString) : inputString;
    }

    protected boolean calculateStatus(String actual, String expected) {
        return StringUtils.equals(actual, expected);
    }

    private String trimMultipleSpaces(String inputString) {
        Pattern trimPattern = Pattern.compile("\\s+");
        Matcher matcher = trimPattern.matcher(inputString);
        return matcher.replaceAll(" ");
    }
}
