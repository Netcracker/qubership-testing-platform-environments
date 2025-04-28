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

import static org.qubership.atp.environments.model.utils.Constants.Environment.System.Connection.TA_ENGINES_PROVIDER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubership.atp.environments.errorhandling.internal.EnvironmentJsonParseException;
import org.qubership.atp.environments.errorhandling.taengine.EnvironmentTaEngineValidationException;
import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.model.impl.ConnectionImpl;
import org.qubership.atp.environments.repo.impl.ConnectionRepositoryImpl;
import org.qubership.atp.environments.service.rest.client.CatalogFeignClient;
import org.qubership.atp.environments.utils.DateTimeUtil;

/**
 * ConnectionServiceImplTest - test for {@link ConnectionServiceImpl}
 */
public class ConnectionServiceImplTest extends AbstractServiceTest {

    private ConnectionRepositoryImpl connectionRepository;
    private ConnectionServiceImpl connectionServiceImpl;

    @BeforeEach
    public void setUp() throws Exception {
        CatalogFeignClient catalogClient = mock(CatalogFeignClient.class);
        connectionRepository = mock(ConnectionRepositoryImpl.class);
        connectionServiceImpl =
                new ConnectionServiceImpl(connectionRepository, new DateTimeUtil(), catalogClient, userInfoProvider);
    }

    @Test
    public void updateParameters_AcceptsRequestBodyJsonSuccessfully_ArgsVersionIsMissing() {
        ConnectionParameters parameters = new ConnectionParameters();
        parameters.putAll(
                Stream.of(new String[][]{{"Acquire_Create_Tool_Request_Body",
                        "{\"image\":\"artifactory-url/path-to-image\",\"args\":[\"-git=some-url/*.jar+actions\",\"-git=some-other-url/*+files\",\"-svn=one-more-url/+*.jar+resources/dependencies\",\"-jvm=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005\"],\"name\":\"advance-analytics-platform\"}"
                }})
                        .collect(HashMap::new, (m, v) -> m.put(v[0], v[1]), HashMap::putAll));
        doNothing().when(connectionRepository)
                .updateParameters(any(UUID.class), any(UUID.class), any(ConnectionParameters.class), anyLong(),
                        any(UUID.class), anyList());

        connectionServiceImpl.updateParameters(UUID.randomUUID(), parameters, anyList());
    }

    @Test
    public void update_ThrowsException_MandatoryParameterMissingVersion() {
        Connection newConnection = new ConnectionImpl();
        newConnection.setSourceTemplateId(TA_ENGINES_PROVIDER);
        newConnection.setParameters(new ConnectionParameters());
        newConnection.getParameters().putAll(
                Stream.of(new String[][]{
                        {"Acquire_Create_Tool_Request_Body",
                                "{\"image\":\"artifactory-url/path-to-image\",\"args\":[\"-git=some-url/*.jar+actions\",\"-git=some-other-url/*+files\",\"-svn=one-more-url/+*.jar+resources/dependencies\",\"-jvm=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005\",\"--argline=-log=debug -lifetimeout=9 -ntt.web.service.enabled=test -ntt.web.service.port=test webdriver.capabilities.name=advance-analytics-platform-05c1r chrome.option.1=--no-sandbox -ram.mode=test -environments.url=test -Dbootstrap.servers=test -Datp.ram.url=test webdriver.hub.url=http://ggr.selenoid.svc:5555/wd/hub\"],\"name\":\"advance-analytics-platform\",\"env\":[{\"name\":\"MAX_RAM\",\"value\":\"2048m\"},{\"name\":\"GRAYLOG_HOST\",\"value\":\"tcp:graylog-service-address\"},{\"name\":\"GRAYLOG_PORT\",\"value\":\"12201\"},{\"name\":\"Datp2.ram.enabled\",\"value\":\"12201\"},{\"name\":\"Datp.ram.url\",\"value\":\"12201\"},{\"name\":\"Dbootstrap.servers\",\"value\":\"12201\"},{\"name\":\"Dram.adapter.type\",\"value\":\"12201\"}]}"
                        }})
                        .collect(HashMap::new, (m, v) -> m.put(v[0], v[1]), HashMap::putAll));
        when(connectionRepository.update(any(UUID.class), any(UUID.class), anyString(), anyString(),
                any(ConnectionParameters.class), anyLong(), any(UUID.class), anyString(), any(UUID.class),
                any(List.class))).thenReturn(newConnection);

        EnvironmentTaEngineValidationException exception = assertThrows(EnvironmentTaEngineValidationException.class,
                        () -> connectionServiceImpl.update(newConnection));
        assertTrue(exception.getMessage().contains("{name=version, section=args}"));
    }

    @Test
    public void update_ThrowsException_MandatoryGroupMissingArgs() {
        Connection newConnection = new ConnectionImpl();
        newConnection.setSourceTemplateId(TA_ENGINES_PROVIDER);
        newConnection.setParameters(new ConnectionParameters());
        newConnection.getParameters().putAll(
                Stream.of(new String[][]{
                        {"Acquire_Create_Tool_Request_Body",
                                "{\"image\":\"artifactory-url/path-to-image\",\"name\":\"advance-analytics-platform\",\"args\":[\"-git=some-url/*.jar+actions\"]}"
                        }})
                        .collect(HashMap::new, (m, v) -> m.put(v[0], v[1]), HashMap::putAll));
        when(connectionRepository.update(any(UUID.class), any(UUID.class), anyString(), anyString(),
                any(ConnectionParameters.class), anyLong(), any(UUID.class), anyString(), any(UUID.class),
                any(List.class))).thenReturn(newConnection);

        EnvironmentTaEngineValidationException exception = assertThrows(EnvironmentTaEngineValidationException.class,
                () -> connectionServiceImpl.update(newConnection));
        assertTrue(exception.getMessage().contains("{name=version, section=args}"));
    }

    @Test
    public void update_ThrowsException_InvalidJson() {
        Connection newConnection = new ConnectionImpl();
        newConnection.setSourceTemplateId(TA_ENGINES_PROVIDER);
        newConnection.setParameters(new ConnectionParameters());
        newConnection.getParameters().putAll(
                Stream.of(new String[][]{
                        {"Acquire_Create_Tool_Request_Body",
                                "{\"image\":\"artifactory-url/path-to-image\",\"args\":[\"-git=some-url/*.jar+actions\",\"-git=some-other-url/*+files\",\"-svn=one-more-url/+*.jar+resources/dependencies\",\"-jvm=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005\"],\"name\":\"advance-analytics-platform\""
                        }})
                        .collect(HashMap::new, (m, v) -> m.put(v[0], v[1]), HashMap::putAll));
        when(connectionRepository.update(any(UUID.class), any(UUID.class), anyString(), anyString(),
                any(ConnectionParameters.class), anyLong(), any(UUID.class), anyString(), any(UUID.class),
                any(List.class))).thenReturn(newConnection);

        EnvironmentJsonParseException exception = assertThrows(EnvironmentJsonParseException.class,
                () -> connectionServiceImpl.update(newConnection));
        assertEquals("Failed to parse JSON data", exception.getMessage());
    }

    @Test
    public void create_SuccessfullyReturnsObjectFromRepository_ValidJson() {
        Connection newConnection = new ConnectionImpl();
        newConnection.setParameters(new ConnectionParameters());
        newConnection.getParameters().putAll(
                Stream.of(new String[][]{
                        {"Acquire_Create_Tool_Request_Body",
                                "{\"image\":\"artifactory-url/path-to-image\",\"args\":[\"-version=1.1\",\"-git=some-url/*.jar+actions\",\"-git=some-other-url/*+files\",\"-svn=one-more-url/+*.jar+resources/dependencies\",\"-jvm=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005\",\"--argline=-log=debug -atp.log=TEST -lifetimeout=9 -ntt.web.service.enabled=test -ntt.web.service.port=test webdriver.capabilities.name=advance-analytics-platform-05c1r chrome.option.1=--no-sandbox -ram.mode=test -environments.url=test -Dbootstrap.servers=test -Datp.ram.url=test webdriver.hub.url=http://ggr.selenoid.svc:5555/wd/hub\"],\"name\":\"advance-analytics-platform\",\"env\":[{\"name\":\"MAX_RAM\",\"value\":\"2048m\"},{\"name\":\"GRAYLOG_HOST\",\"value\":\"tcp:graylog-service-address\"},{\"name\":\"GRAYLOG_PORT\",\"value\":\"12201\"},{\"name\":\"Datp2.ram.enabled\",\"value\":\"test\"},{\"name\":\"Datp.ram.url\",\"value\":\"test\"},{\"name\":\"Dbootstrap.servers\",\"value\":\"test\"},{\"name\":\"Dram.adapter.type\",\"value\":\"test\"},{\"name\":\"Dkafka.topic.name\",\"value\":\"test\"}]}"
                        }})
                        .collect(HashMap::new, (m, v) -> m.put(v[0], v[1]), HashMap::putAll));
        when(connectionRepository.create(any(UUID.class), any(UUID.class), anyString(), anyString(),
                any(ConnectionParameters.class), anyLong(), any(UUID.class), anyString(), any(UUID.class),
                any(List.class), any(UUID.class))).thenReturn(newConnection);

        Connection createdConnection = connectionServiceImpl.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Test",
                "TestDescription",
                newConnection.getParameters(),
                "TestConnectionType",
                TA_ENGINES_PROVIDER,
                UUID.randomUUID(),
                Collections.singletonList("TEST"),
                UUID.randomUUID());

        assertEquals(newConnection, createdConnection);
    }

    @Test
    public void create_ThrowsException_DuplicatedParametersEnv() {
        Connection newConnection = new ConnectionImpl();
        newConnection.setParameters(new ConnectionParameters());
        newConnection.getParameters().putAll(
                Stream.of(new String[][]{
                        {"Acquire_Create_Tool_Request_Body",
                                "{\"image\":\"artifactory-url/path-to-image\",\"args\":[\"-version=1.1\",\"-git=some-url/*.jar+actions\",\"-git=some-other-url/*+files\",\"-svn=one-more-url/+*.jar+resources/dependencies\",\"-jvm=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005\",\"--argline=-log=debug -atp.log=TEST -lifetimeout=9 -ntt.web.service.enabled=test -ntt.web.service.port=test webdriver.capabilities.name=advance-analytics-platform-05c1r chrome.option.1=--no-sandbox -ram.mode=test -environments.url=test -Dbootstrap.servers=test -Datp.ram.url=test webdriver.hub.url=http://ggr.selenoid.svc:5555/wd/hub\"],\"env\":[],\"name\":\"advance-analytics-platform\",\"env\":[{\"name\":\"MAX_RAM\",\"value\":\"2048m\"},{\"name\":\"GRAYLOG_HOST\",\"value\":\"tcp:graylog-service-address\"},{\"name\":\"GRAYLOG_PORT\",\"value\":\"12201\"},{\"name\":\"Datp2.ram.enabled\",\"value\":\"test\"},{\"name\":\"Datp.ram.url\",\"value\":\"test\"},{\"name\":\"Dbootstrap.servers\",\"value\":\"test\"},{\"name\":\"Dram.adapter.type\",\"value\":\"test\"},{\"name\":\"Dkafka.topic.name\",\"value\":\"test\"}]}"
                        }})
                        .collect(HashMap::new, (m, v) -> m.put(v[0], v[1]), HashMap::putAll));
        when(connectionRepository.create(any(UUID.class), any(UUID.class), anyString(), anyString(),
                any(ConnectionParameters.class), anyLong(), any(UUID.class), anyString(), any(UUID.class),
                any(List.class), any(UUID.class))).thenReturn(newConnection);

        EnvironmentTaEngineValidationException exception =
                assertThrows(EnvironmentTaEngineValidationException.class,
                        () -> connectionServiceImpl.create(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Test",
                        "TestDescription",
                        newConnection.getParameters(),
                        "TestConnectionType",
                        TA_ENGINES_PROVIDER,
                        UUID.randomUUID(),
                        Collections.singletonList("TEST"),
                        UUID.randomUUID()));
        assertTrue(exception.getMessage().contains("{name=env, section=null}"));
    }

    @Test
    public void create_ThrowsException_DuplicatedParametersName() {
        Connection newConnection = new ConnectionImpl();
        newConnection.setParameters(new ConnectionParameters());
        newConnection.getParameters().putAll(
                Stream.of(new String[][]{
                        {"Acquire_Create_Tool_Request_Body",
                                "{\"image\":\"artifactory-url/path-to-image\",\"args\":[\"-version=1.1\",\"-git=some-url/*.jar+actions\",\"-git=some-other-url/*+files\",\"-svn=one-more-url/+*.jar+resources/dependencies\",\"-jvm=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005\",\"--argline=-log=debug -atp.log=TEST -lifetimeout=9 -ntt.web.service.enabled=test -ntt.web.service.port=test webdriver.capabilities.name=advance-analytics-platform-05c1r chrome.option.1=--no-sandbox -ram.mode=duplicateTest -environments.url=test -Dbootstrap.servers=test -Datp.ram.url=test webdriver.hub.url=http://ggr.selenoid.svc:5555/wd/hub\"],\"name\":\"advance-analytics-platform\",\"name\":\"advance-analytics-platform\",\"env\":[{\"name\":\"MAX_RAM\",\"value\":\"2048m\"},{\"name\":\"GRAYLOG_HOST\",\"value\":\"tcp:graylog-service-address\"},{\"name\":\"GRAYLOG_PORT\",\"value\":\"12201\"},{\"name\":\"Datp2.ram.enabled\",\"value\":\"test\"},{\"name\":\"Datp.ram.url\",\"value\":\"test\"},{\"name\":\"Dbootstrap.servers\",\"value\":\"test\"},{\"name\":\"Dram.adapter.type\",\"value\":\"test\"},{\"name\":\"Dkafka.topic.name\",\"value\":\"test\"}]}"
                        }})
                        .collect(HashMap::new, (m, v) -> m.put(v[0], v[1]), HashMap::putAll));
        when(connectionRepository.create(any(UUID.class), any(UUID.class), anyString(), anyString(),
                any(ConnectionParameters.class), anyLong(), any(UUID.class), anyString(), any(UUID.class),
                any(List.class), any(UUID.class))).thenReturn(newConnection);

        EnvironmentTaEngineValidationException exception =
                assertThrows(EnvironmentTaEngineValidationException.class,
                        () -> connectionServiceImpl.create(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                "Test",
                                "TestDescription",
                                newConnection.getParameters(),
                                "TestConnectionType",
                                TA_ENGINES_PROVIDER,
                                UUID.randomUUID(),
                                Collections.singletonList("TEST"),
                                UUID.randomUUID()));
        System.out.println(exception.getMessage());
        assertTrue(exception.getMessage().contains("{name=Name, section=null}"));
    }

    @Test
    public void create_ThrowsException_DuplicatedParametersDatp2RamEnabledEnv() {
        Connection newConnection = new ConnectionImpl();
        newConnection.setParameters(new ConnectionParameters());
        newConnection.getParameters().putAll(
                Stream.of(new String[][]{
                        {"Acquire_Create_Tool_Request_Body",
                                "{\"image\":\"artifactory-url/path-to-image\",\"args\":[\"-version=1.1\",\"-git=some-url/*.jar+actions\",\"-git=some-other-url/*+files\",\"-svn=one-more-url/+*.jar+resources/dependencies\",\"-jvm=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005\",\"--argline=-log=debug -atp.log=TEST -lifetimeout=9 -ntt.web.service.enabled=test -ntt.web.service.port=test webdriver.capabilities.name=advance-analytics-platform-05c1r chrome.option.1=--no-sandbox -ram.mode=duplicateTest -environments.url=test -Dbootstrap.servers=test -Datp.ram.url=test webdriver.hub.url=http://ggr.selenoid.svc:5555/wd/hub\"],\"name\":\"advance-analytics-platform\",\"env\":[{\"name\":\"MAX_RAM\",\"value\":\"2048m\"},{\"name\":\"GRAYLOG_HOST\",\"value\":\"tcp:graylog-service-address\"},{\"name\":\"GRAYLOG_PORT\",\"value\":\"12201\"},{\"name\":\"Datp2.ram.enabled\",\"value\":\"test\"},{\"name\":\"Datp2.ram.enabled\",\"value\":\"test\"},{\"name\":\"Datp.ram.url\",\"value\":\"test\"},{\"name\":\"Dbootstrap.servers\",\"value\":\"test\"},{\"name\":\"Dram.adapter.type\",\"value\":\"test\"},{\"name\":\"Dkafka.topic.name\",\"value\":\"test\"}]}"
                        }})
                        .collect(HashMap::new, (m, v) -> m.put(v[0], v[1]), HashMap::putAll));
        when(connectionRepository.create(any(UUID.class), any(UUID.class), anyString(), anyString(),
                any(ConnectionParameters.class), anyLong(), any(UUID.class), anyString(), any(UUID.class),
                any(List.class), any(UUID.class))).thenReturn(newConnection);

        EnvironmentTaEngineValidationException exception =
                assertThrows(EnvironmentTaEngineValidationException.class,
                        () -> connectionServiceImpl.create(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                "Test",
                                "TestDescription",
                                newConnection.getParameters(),
                                "TestConnectionType",
                                TA_ENGINES_PROVIDER,
                                UUID.randomUUID(),
                                Collections.singletonList("TEST"),
                                UUID.randomUUID()));
        assertTrue(exception.getMessage().contains("{name=datp2.ram.enabled, section=env}"));
    }

    @Test
    public void create_ThrowsException_DuplicatedParametersRamModArgline() {
        Connection newConnection = new ConnectionImpl();
        newConnection.setParameters(new ConnectionParameters());
        newConnection.getParameters().putAll(
                Stream.of(new String[][]{
                        {"Acquire_Create_Tool_Request_Body",
                                "{\"image\":\"artifactory-url/path-to-image\",\"args\":[\"-version=1.1\",\"-git=some-url/*.jar+actions\",\"-git=some-other-url/*+files\",\"-svn=one-more-url/+*.jar+resources/dependencies\",\"-jvm=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005\",\"--argline=-log=debug -atp.log=TEST -lifetimeout=9 -ntt.web.service.enabled=test -ntt.web.service.port=test webdriver.capabilities.name=advance-analytics-platform-05c1r chrome.option.1=--no-sandbox -ram.mode=test -ram.mode=duplicateTest -environments.url=test -Dbootstrap.servers=test -Datp.ram.url=test webdriver.hub.url=http://ggr.selenoid.svc:5555/wd/hub\"],\"name\":\"advance-analytics-platform\",\"env\":[{\"name\":\"MAX_RAM\",\"value\":\"2048m\"},{\"name\":\"GRAYLOG_HOST\",\"value\":\"tcp:graylog-service-address\"},{\"name\":\"GRAYLOG_PORT\",\"value\":\"12201\"},{\"name\":\"Datp2.ram.enabled\",\"value\":\"test\"},{\"name\":\"Datp.ram.url\",\"value\":\"test\"},{\"name\":\"Dbootstrap.servers\",\"value\":\"test\"},{\"name\":\"Dram.adapter.type\",\"value\":\"test\"},{\"name\":\"Dkafka.topic.name\",\"value\":\"test\"}]}"
                        }})
                        .collect(HashMap::new, (m, v) -> m.put(v[0], v[1]), HashMap::putAll));
        when(connectionRepository.create(any(UUID.class), any(UUID.class), anyString(), anyString(),
                any(ConnectionParameters.class), anyLong(), any(UUID.class), anyString(), any(UUID.class),
                any(List.class), any(UUID.class))).thenReturn(newConnection);

        EnvironmentTaEngineValidationException exception =
                assertThrows(EnvironmentTaEngineValidationException.class,
                        () -> connectionServiceImpl.create(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                "Test",
                                "TestDescription",
                                newConnection.getParameters(),
                                "TestConnectionType",
                                TA_ENGINES_PROVIDER,
                                UUID.randomUUID(),
                                Collections.singletonList("TEST"),
                                UUID.randomUUID()));
        assertTrue(exception.getMessage().contains("{name=ram.mode, section=argline}"));
    }

    @Test
    public void create_ThrowsException_DuplicatedParametersVersionArgs() {
        Connection newConnection = new ConnectionImpl();
        newConnection.setParameters(new ConnectionParameters());
        newConnection.getParameters().putAll(
                Stream.of(new String[][]{
                        {"Acquire_Create_Tool_Request_Body",
                                "{\"image\":\"artifactory-url/path-to-image\",\"args\":[\"-version=1.1\",\"-version=1.2\",\"-git=some-url/*.jar+actions\",\"-git=some-other-url/*+files\",\"-svn=one-more-url/+*.jar+resources/dependencies\",\"-jvm=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005\",\"--argline=-log=debug -atp.log=TEST -lifetimeout=9 -ntt.web.service.enabled=test -ntt.web.service.port=test webdriver.capabilities.name=advance-analytics-platform-05c1r chrome.option.1=--no-sandbox -ram.mode=duplicateTest -environments.url=test -Dbootstrap.servers=test -Datp.ram.url=test webdriver.hub.url=http://ggr.selenoid.svc:5555/wd/hub\"],\"name\":\"advance-analytics-platform\",\"env\":[{\"name\":\"MAX_RAM\",\"value\":\"2048m\"},{\"name\":\"GRAYLOG_HOST\",\"value\":\"tcp:graylog-service-address\"},{\"name\":\"GRAYLOG_PORT\",\"value\":\"12201\"},{\"name\":\"Datp2.ram.enabled\",\"value\":\"test\"},{\"name\":\"Datp.ram.url\",\"value\":\"test\"},{\"name\":\"Dbootstrap.servers\",\"value\":\"test\"},{\"name\":\"Dram.adapter.type\",\"value\":\"test\"},{\"name\":\"Dkafka.topic.name\",\"value\":\"test\"}]}"
                        }})
                        .collect(HashMap::new, (m, v) -> m.put(v[0], v[1]), HashMap::putAll));
        when(connectionRepository.create(any(UUID.class), any(UUID.class), anyString(), anyString(),
                any(ConnectionParameters.class), anyLong(), any(UUID.class), anyString(), any(UUID.class),
                any(List.class), any(UUID.class))).thenReturn(newConnection);

        EnvironmentTaEngineValidationException exception =
                assertThrows(EnvironmentTaEngineValidationException.class,
                        () -> connectionServiceImpl.create(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                "Test",
                                "TestDescription",
                                newConnection.getParameters(),
                                "TestConnectionType",
                                TA_ENGINES_PROVIDER,
                                UUID.randomUUID(),
                                Collections.singletonList("TEST"),
                                UUID.randomUUID()));
        assertTrue(exception.getMessage().contains("{name=version, section=args}"));
    }
}
