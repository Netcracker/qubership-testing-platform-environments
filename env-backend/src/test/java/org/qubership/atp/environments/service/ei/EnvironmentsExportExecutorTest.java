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

package org.qubership.atp.environments.service.ei;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.qubership.atp.crypt.api.Decryptor;
import org.qubership.atp.ei.node.dto.ExportFormat;
import org.qubership.atp.ei.node.dto.ExportImportData;
import org.qubership.atp.ei.node.dto.ExportScope;
import org.qubership.atp.ei.node.exceptions.ExportException;
import org.qubership.atp.ei.node.services.FileService;
import org.qubership.atp.ei.node.services.ObjectSaverToDiskService;
import org.qubership.atp.environments.ei.EnvironmentsExportExecutor;
import org.qubership.atp.environments.ei.ServiceScopeEntities;
import org.qubership.atp.environments.enums.UserManagementEntities;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.Project;
import org.qubership.atp.environments.model.impl.ConnectionImpl;
import org.qubership.atp.environments.model.impl.EnvironmentImpl;
import org.qubership.atp.environments.model.impl.ProjectImpl;
import org.qubership.atp.environments.model.impl.SystemImpl;
import org.qubership.atp.environments.service.direct.EnvironmentService;
import org.qubership.atp.environments.service.direct.SystemCategoriesService;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnvironmentsExportExecutorTest {

    private final ThreadLocal<EnvironmentService> environmentService = new ThreadLocal<>();
    private final ThreadLocal<SystemCategoriesService> systemCategoriesService = new ThreadLocal<>();
    private final ThreadLocal<ObjectSaverToDiskService> objectSaverToDiskService = new ThreadLocal<>();
    private final ThreadLocal<Decryptor> decryptor = new ThreadLocal<>();
    private final ThreadLocal<EnvironmentsExportExecutor> executor = new ThreadLocal<>();

    private static final UUID uuid1 = UUID.randomUUID();
    private static final UUID uuid2 = UUID.randomUUID();
    private static final UUID uuid3 = UUID.randomUUID();
    private static final UUID uuid4 = UUID.randomUUID();
    private static final UUID uuid5 = UUID.fromString("ec6866dd-a4c6-4f94-8ba3-4a62adacf6b4");
    private static final UUID firstConnectionId = UUID.randomUUID();
    private static final UUID secondConnectionId = UUID.randomUUID();

    private static final UUID HTTP_CONNECTION_ID = UUID.fromString("2a0eab16-0fe7-4a12-8155-78c0c151abdf");

    private static final String erFile = "{\n" +
            "  \"id\" : \"" + uuid2 + "\",\n" +
            "  \"name\" : \"environment\",\n" +
            "  \"graylogName\" : \"\",\n" +
            "  \"description\" : \"\",\n" +
            "  \"ssmSolutionAlias\" : \"\",\n" +
            "  \"ssmInstanceAlias\" : \"\",\n" +
            "  \"consulEgressConfigPath\" : \"\",\n" +
            "  \"projectId\" : \"" + uuid1 + "\",\n" +
            "  \"categoryId\" : \"" + uuid4 + "\",\n" +
            "  \"systems\" : [ {\n" +
            "    \"id\" : \"" + uuid3 + "\",\n" +
            "    \"name\" : \"system\",\n" +
            "    \"description\" : null,\n" +
            "    \"status\" : null,\n" +
            "    \"parentSystemId\" : null,\n" +
            "    \"connections\" : null,\n" +
            "    \"systemCategoryId\" : null,\n" +
            "    \"serverItf\" : null,\n" +
            "    \"linkToSystemId\" : null,\n" +
            "    \"externalId\" : null,\n" +
            "    \"sourceId\" : null,\n" +
            "    \"externalName\" : null,\n" +
            "    \"parametersGettingVersion\" : null\n" +
            "  } ],\n" +
            "  \"sourceId\" : null,\n" +
            "  \"tags\" : [ ]\n" +
            "}\n";

    private static final String erPostmanFileLF = "{\n" +
            "  \"id\" : \"" + uuid2 + "\",\n" +
            "  \"name\" : \"environment\",\n" +
            "  \"values\" : [ {\n" +
            "    \"key\" : \"ENV.system.firstConnection.db_password\",\n" +
            "    \"value\" : \"\",\n" +
            "    \"enabled\" : true\n" +
            "  }, {\n" +
            "    \"key\" : \"ENV.system.secondConnection.url\",\n" +
            "    \"value\" : \"http://system.com\",\n" +
            "    \"enabled\" : true\n" +
            "  }, {\n" +
            "    \"key\" : \"ENV.system.secondConnection.encrypted_password\",\n" +
            "    \"value\" : \"\",\n" +
            "    \"enabled\" : true\n" +
            "  } ],\n" +
            "  \"_postman_variable_scope\" : \"environment\"\n" +
            "}\n";

    private static final String erPostmanFileUnix = "{" + System.lineSeparator() +
            "  \"id\" : \"" + uuid2 + "\"," + System.lineSeparator() +
            "  \"name\" : \"environment\"," + System.lineSeparator() +
            "  \"values\" : [ {" + System.lineSeparator() +
            "    \"key\" : \"ENV.system.firstConnection.db_password\"," + System.lineSeparator() +
            "    \"value\" : \"\"," + System.lineSeparator() +
            "    \"enabled\" : true" + System.lineSeparator() +
            "  }, {" + System.lineSeparator() +
            "    \"key\" : \"ENV.system.secondConnection.url\"," + System.lineSeparator() +
            "    \"value\" : \"http://system.com\"," + System.lineSeparator() +
            "    \"enabled\" : true" + System.lineSeparator() +
            "  }, {" + System.lineSeparator() +
            "    \"key\" : \"ENV.system.secondConnection.encrypted_password\"," + System.lineSeparator() +
            "    \"value\" : \"\"," + System.lineSeparator() +
            "    \"enabled\" : true" + System.lineSeparator() +
            "  } ]," + System.lineSeparator() +
            "  \"_postman_variable_scope\" : \"environment\"" + System.lineSeparator() +
            "}" + System.lineSeparator();

    private final static String encryptedValue = "{ENC}{h+T5Ewv9OqcWU6vONNazIw==}{baE2evr6uWQ35rz/M++q0g==}";
    private final ThreadLocal<ConnectionImpl> firstConnection = new ThreadLocal<>();
    private final ThreadLocal<ConnectionImpl> secondConnection = new ThreadLocal<>();
    private final ThreadLocal<SystemImpl> system = new ThreadLocal<>();
    private final ThreadLocal<Environment> environment = new ThreadLocal<>();
    private final ThreadLocal<Project> project = new ThreadLocal<>();
    private final ThreadLocal<ExportScope> exportScope = new ThreadLocal<>();

    @BeforeEach
    public void setUp() {
        EnvironmentService environmentServiceMock = mock(EnvironmentService.class);
        SystemCategoriesService systemCategoriesServiceMock = mock(SystemCategoriesService.class);
        ObjectSaverToDiskService objectSaverToDiskServiceMock = new ObjectSaverToDiskService(new FileService(), true);
        Decryptor decryptorMock = mock(Decryptor.class);

        ProjectImpl projectThread = new ProjectImpl(uuid1, "projectTest2", null,
                null, null, null, null);

        SystemImpl systemThread = SystemImpl.builder()
                .uuid(uuid3)
                .name("system")
                .build();
        EnvironmentImpl environmentThread = new EnvironmentImpl(uuid2, "environment", "",
                "", "", "", "", null, null, null, null, projectThread.getId(), Arrays.asList(systemThread), uuid4, null, Collections.emptyList());
        ConnectionImpl firstConnectionThread = new ConnectionImpl(firstConnectionId, "firstConnection", "",
                null, null, null, null, null,
                systemThread.getId(), null, null, null, null);
        ConnectionImpl secondConnectionThread = new ConnectionImpl(secondConnectionId, "secondConnection", "",
                null, null, null, null, null,
                systemThread.getId(), null, HTTP_CONNECTION_ID, null, null);
        secondConnectionThread.setParameters(new ConnectionParameters());
        secondConnectionThread.getParameters().put("url", "http://system.com");

        secondConnectionThread.getParameters().put("encrypted_password", encryptedValue);

        ExportScope exportScopeThread = new ExportScope();
        exportScopeThread.getEntities().put(ServiceScopeEntities.ENTITY_ENVIRONMENTS.getValue(),
                Sets.newHashSet(environmentThread.getId().toString()));

        when(decryptorMock.isEncrypted(eq(encryptedValue))).thenReturn(true);
        when(environmentServiceMock.getByIds(any()))
                .thenReturn(Collections.singletonList(environmentThread));

        environmentService.set(environmentServiceMock);
        systemCategoriesService.set(systemCategoriesServiceMock);
        objectSaverToDiskService.set(objectSaverToDiskServiceMock);
        decryptor.set(decryptorMock);
        executor.set(new EnvironmentsExportExecutor(environmentServiceMock, systemCategoriesServiceMock,
                objectSaverToDiskServiceMock, decryptorMock));

        project.set(projectThread);
        system.set(systemThread);
        environment.set(environmentThread);
        firstConnection.set(firstConnectionThread);
        secondConnection.set(secondConnectionThread);
        exportScope.set(exportScopeThread);
    }

    @Test
    public void testExportEnvironment_entitySavedToJsonFile(@TempDir Path workDir) throws ExportException, IOException {
        ExportImportData exportData = new ExportImportData(project.get().getId(), exportScope.get(), ExportFormat.ATP);
        system.get().setConnections(null);

        executor.get().exportToFolder(exportData, workDir);
        Path filePath = workDir
                .resolve("Environment")
                .resolve(environment.get().getId() + ".json");
        StringBuilder builderFile = new StringBuilder();
        try (Stream<String> lines = Files.lines(filePath, Charset.defaultCharset())) {
            lines.forEach(str -> builderFile.append(str).append("\n"));
        }

        Assertions.assertEquals(erFile, builderFile.toString());
    }

    @Test
    public void testExportEnvironment_EncryptedValueIsSkipped(@TempDir Path workDir) throws ExportException, IOException {
        ExportImportData exportData = new ExportImportData(project.get().getId(), exportScope.get(), ExportFormat.ATP);
        system.get().setConnections(Arrays.asList(firstConnection.get(), secondConnection.get()));

        executor.get().exportToFolder(exportData, workDir);
        Path filePath = workDir
                .resolve("Environment")
                .resolve(environment.get().getId() + ".json");
        StringBuilder builderFile = new StringBuilder();
        try (Stream<String> lines = Files.lines(filePath, Charset.defaultCharset())) {
            lines.forEach(str -> builderFile.append(str).append("\n"));
        }

        Assertions.assertFalse(builderFile.toString().contains(encryptedValue));
    }

    @Test
    public void testExportEnvironmentWithPostmanType_correctlyJson(@TempDir Path workDir) throws ExportException, IOException {
        ExportImportData exportData = new ExportImportData(uuid5, exportScope.get(), ExportFormat.POSTMAN);
        ConnectionParameters connectionParameters = new ConnectionParameters();
        connectionParameters.put("db_password", "{ENC}{FTWS0F9tPFtVXWz+jIm92A==}{PYidIg7FjtAgSLDALI/VMg==}");

        firstConnection.get().setParameters(connectionParameters);
        system.get().setConnections(Arrays.asList(firstConnection.get(), secondConnection.get()));

        when(decryptor.get().isEncrypted(eq("{ENC}{FTWS0F9tPFtVXWz+jIm92A==}{PYidIg7FjtAgSLDALI/VMg==}"))).thenReturn(true);

        executor.get().exportToFolder(exportData, workDir);
        Path filePath = workDir.resolve(UserManagementEntities.ENVIRONMENT.name().toLowerCase())
                .resolve(environment.get().getName() + ".environment" + ".json");
        StringBuilder builderFile = new StringBuilder();
        try (Stream<String> lines = Files.lines(filePath, Charset.defaultCharset())) {
            lines.forEach(str -> builderFile.append(str).append("\n"));
        }

        String os = System.getProperty("os.name");

        if (os.toLowerCase().contains("windows")) {
            log.info("actual json: {}" , builderFile);
            log.info("expected json: {}" , erPostmanFileLF);
            Assertions.assertEquals(erPostmanFileLF, builderFile.toString(), "Not correctly ER");
        } else {
            log.info("actual json: {}" , builderFile);
            log.info("expected json: {}" , erPostmanFileLF);
            Assertions.assertEquals(erPostmanFileUnix, builderFile.toString(), "Not correctly result [Unix]");
        }

    }

    @Test
    public void testExportEnvironment_convertToNtt(@TempDir Path workDir) throws ExportException, IOException,
            ParserConfigurationException, XPathExpressionException, SAXException {
        ExportImportData exportData = new ExportImportData(project.get().getId(), exportScope.get(), ExportFormat.NTT);
        system.get().setConnections(Arrays.asList(firstConnection.get(), secondConnection.get()));

        executor.get().exportToFolder(exportData, workDir);

        Path configFile = workDir.resolve("config").resolve("new_settings.xml");
        Assertions.assertTrue(Files.exists(configFile));

        String configBody = StringUtils.join(Files.readAllLines(configFile), "\n");

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document xmlDocument = builder.parse(new InputSource(new StringReader(configBody)));
        XPath xPath = XPathFactory.newInstance().newXPath();

        String systemAlias =
                (String) xPath.compile("./settings-configuration/serversSettings/server/@alias").evaluate(xmlDocument
                        , XPathConstants.STRING);
        String systemInstance =
                (String) xPath.compile("./settings-configuration/serversSettings/server/@instance")
                        .evaluate(xmlDocument, XPathConstants.STRING);
        String systemUrl =
                (String) xPath.compile("./settings-configuration/serversSettings/server/@url").evaluate(xmlDocument,
                        XPathConstants.STRING);
        String listEnv =
                (String) xPath.compile("./settings-configuration/environmentLists/list/@name").evaluate(xmlDocument,
                        XPathConstants.STRING);
        String environmentName =
                (String) xPath.compile("./settings-configuration/environmentItems/item/@environment")
                        .evaluate(xmlDocument, XPathConstants.STRING);
        String environmentServer =
                (String) xPath.compile("./settings-configuration/environmentItems/item/@server").evaluate(xmlDocument
                        , XPathConstants.STRING);

        Assertions.assertEquals(systemAlias, "system");
        Assertions.assertEquals(listEnv, "environment");
        Assertions.assertEquals(environmentName, "environment");
        Assertions.assertEquals(environmentServer, "system");
        Assertions.assertEquals(systemInstance, "http://system.com");
        Assertions.assertEquals(systemInstance, systemUrl);
    }
}
