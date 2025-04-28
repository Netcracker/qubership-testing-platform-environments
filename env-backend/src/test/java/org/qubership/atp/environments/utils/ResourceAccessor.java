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

package org.qubership.atp.environments.utils;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ResourceAccessor {

    private static final String DEFAULT_RESOURCES_PATH = "src/test/resources";
    private final String resourcesPath;
    private final Class aClass;

    public ResourceAccessor(Class aClass) {
        this.resourcesPath = DEFAULT_RESOURCES_PATH;
        this.aClass = aClass;
    }

    public <T> List<T> readObjectsFromFilePath(Class<T> type, String path) {
        return FileUtils.readObjectsFromFilePath(type, getPath(path));
    }

    public <T> List<T> readObjectsFromFilePath(Class<T> type, String... paths) {
        return FileUtils.readObjectsFromFilePath(type, getPath(paths));
    }

    public <T> T readObjectFromFilePath(Class<T> type, String path) {
        return FileUtils.readObjectFromFilePath(type, getPath(path));
    }

    public String readStringFromFilePath(String path) {
        return new String(FileUtils.readBytesFromFile(getPath(path)), StandardCharsets.UTF_8);
    }

    public <T> T readObjectFromFilePath(Type type, String path) {
        return FileUtils.readObjectFromFilePath(type, getPath(path));
    }

    public <T> T readObjectFromFilePath(Class<T> type, String... paths) {
        return FileUtils.readObjectFromFilePath(type, getPath(paths));
    }

    public <T> T readObjectFromFilePath(Type type, String... paths) {
        return FileUtils.readObjectFromFilePath(type, getPath(paths));
    }

    public <T> T deserializeObjectFromBytesByFilePath(Class<T> type, String path) {
        return FileUtils.deserializeObjectFromBytesByFilePath(type, getPath(path));
    }

    public byte[] readBytesFromFilePath(String path) {
        return FileUtils.readBytesFromFile(getPath(path));
    }

    public byte[] readBytesFromFilePath(String... paths) {
        return FileUtils.readBytesFromFile(getPath(paths));
    }

    public String getFilePath(String... path) {
        return getFile(path).getPath();
    }

    public File getFile(String... path) {
        return getPath(path).toFile();
    }

    public Path getPath(String... path) {
        return Paths.get(getRootFilePath(), path);
    }

    public String getRootFilePath() {
        return getRootFile().getPath();
    }

    public File getRootFile() {
        return getRootPath().toFile();
    }

    public Path getRootPath() {
        String[] allSegments = aClass.getName()
                .split("[.]");
        return Paths.get(resourcesPath, allSegments);
    }
}
