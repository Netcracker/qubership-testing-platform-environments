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

package org.qubership.atp.environments.service.direct.impl;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import javax.annotation.Nonnull;

import org.qubership.atp.crypt.api.Encryptor;
import org.qubership.atp.crypt.exception.AtpEncryptException;
import org.qubership.atp.environments.errorhandling.internal.EnvironmentEncryptionException;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.service.direct.EncryptorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("encryptorService")
public class EncryptorServiceImpl implements EncryptorService {

    private boolean useEncryption;
    protected Encryptor encryptor;
    private final String isEncryptFlag = "{2ENC}";

    @Autowired
    public EncryptorServiceImpl(@Nonnull Encryptor encryptor,
                                @Value("${atp.crypto.enabled:false}") boolean useEncryption) {
        this.encryptor = encryptor;
        this.useEncryption = useEncryption;
    }

    @Override
    public ConnectionParameters encryptParameters(ConnectionParameters parameters) {
        ConnectionParameters encryptedParameters = new ConnectionParameters();
        if (parameters != null) {
            for (Map.Entry<String, String> param : parameters.entrySet()) {
                String encryptedParameter = encryptParameter(param.getKey(), param.getValue());
                encryptedParameters.put(param.getKey(), encryptedParameter);
            }
        }
        return encryptedParameters;
    }

    @Override
    public String encryptParameter(String name, String parameter) {
        if (!StringUtils.isEmpty(parameter) && parameter.contains(isEncryptFlag)) {
            parameter = parameter.replace(isEncryptFlag, "");
            String originalValue = decodeBase64(parameter);
            if (useEncryption) {
                try {
                    return encryptString(originalValue);
                } catch (AtpEncryptException e) {
                    log.error("Failed to encrypt parameter: {}", name);
                    throw new EnvironmentEncryptionException(name);
                }
            }
            return originalValue;
        }
        return parameter;
    }

    @Override
    public String encryptString(String sourceParameter) throws AtpEncryptException {
        return encryptor.encrypt(sourceParameter.trim());
    }

    @Override
    public String decodeBase64(String source) {
        return new String(Base64.getDecoder().decode(source), StandardCharsets.UTF_8);
    }
}
