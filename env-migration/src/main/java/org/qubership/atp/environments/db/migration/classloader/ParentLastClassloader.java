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

package org.qubership.atp.environments.db.migration.classloader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class ParentLastClassloader extends URLClassLoader {

    private static final String JAR = ".jar";
    private static final Logger LOGGER = LoggerFactory.getLogger(ParentLastClassloader.class);
    private static final String Q_CLASSES_JAR_EXPRESSION = "env-q-classes-generation.+\\.jar";
    private final String sourcePath;
    private URLClassLoader urlClassLoader;
    private String jdbcType;

    /**
     * ClassLoader based on {@link URLClassLoader} for loading classes from self.
     * In case class not found in this CL, it will try to load it from parent CL.
     *
     * @param sourcePath - location of resources. update.xml, migration scripts/jars etc.
     *                   For example: ./scripts/
     * @param jdbcType   - type of database. This parameter need for loading generated q-classes.
     *                   If you want use postgres database, and your jar with classes has name
     *                   env-q-classes-generation-pg.jar, then you must define jdbcType=pg
     *                   When CL trying to load jar, it split the name on three parts:
     *                   1. general name = env-q-classes-generation
     *                   2. suffix = pg
     *                   3. extension = .jar
     *                   The suffix must match to jdbcType for loading this jar.
     *                   So, you can check how it works in method {@link #getUrlClassLoader(List)}.
     */
    public ParentLastClassloader(String sourcePath, String jdbcType) {
        super(new URL[]{}, getSystemClassLoader());
        this.jdbcType = jdbcType;
        if (Strings.isNullOrEmpty(jdbcType)) {
            throw new IllegalStateException("Parameter 'jdbc_type' can't be null or empty."
                    + "Value of jdbc_type depends of suffix in jar name, which contains generated q-classes."
                    + "env-q-classes-generation-pg.jar - there is jdbc_type is 'pg'");
        }
        this.sourcePath = sourcePath;
        try (Stream<Path> walk = Files.walk(Paths.get(sourcePath))) {
            walk
                    .filter(path -> !isJarFile(path))
                    .forEach(path -> {
                        try {
                            this.addURL(path.toUri().toURL());
                        } catch (MalformedURLException e) {
                            LOGGER.error("Unable convert file to url", e);
                        }
                    });
        } catch (IOException e) {
            LOGGER.error("Unable to add resource files to classpath", e);
        }
    }

    private boolean isJarFile(Path path) {
        return path.toString().toLowerCase(Locale.ENGLISH).endsWith(JAR);
    }

    @Override
    @SuppressFBWarnings("DE_MIGHT_IGNORE")
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> resources = null;
        try {
            resources = findResources(name);
        } catch (Exception e) {
            /*Ignore it*/
        }
        if (resources != null && !resources.hasMoreElements()) {
            return loadFromSuper(name);
        }
        return resources;
    }

    private Enumeration<URL> loadFromSuper(String name) throws IOException {
        LOGGER.warn("Resource not found in self class loader. Resource: " + name);
        return super.getResources(name);
    }

    /**
     * Load class from current classloader.
     * In case the liquibase started executing new one changeset,
     * then classloader will load compatible jars with q-classes and shaded jar
     * with required dependencies.
     * So, if package start with v000.TICKET671.CustomChangeSet
     * Then classloader will load jars from {@code sourcePath}/v000/TICKET671
     * Which of q-classes jar will be loaded? It depends of {@code jdbc_type}.
     *
     * @param className target class name.
     * @return the {@link Class}
     * @throws ClassNotFoundException in case Class not found in current and parent classloader.
     */
    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        if (className.matches("^v\\d+.+")) { //Is liquibase started loading new change set?
            refreshClassLoader(className); //reload compatible q-classes.jar and dependencies
        }
        Class<?> loadedClass = urlClassLoader.loadClass(className);
        if (loadedClass == null) {
            super.loadClass(className);
        }
        return loadedClass;
    }

    protected void refreshClassLoader(String className) {
        String jarPath = className
                .replaceAll("\\.", "/").replaceFirst("\\w+$", "");
        try {
            Path dir = Paths.get(sourcePath, jarPath);
            List<Path> jarList = Files.list(dir).filter(this::isJarFile).collect(Collectors.toList());
            if (urlClassLoader != null) {
                urlClassLoader.close();
            }
            urlClassLoader = getUrlClassLoader(jarList);
        } catch (IOException e) {
            throw new IllegalStateException("Jar file is not found by path:" + jarPath, e);
        }
    }

    private FindFirstClassLoader getUrlClassLoader(List<Path> jarList) {
        final List<URL> urls = new LinkedList<>();
        jarList.forEach(path -> {
            try {
                String name = path.toFile().getName().toLowerCase(Locale.ENGLISH);
                if (name.matches(Q_CLASSES_JAR_EXPRESSION)) {
                    loadGeneratedQClasses(urls, name, path);
                } else {
                    urls.add(getUrl(path));
                }
            } catch (IOException e) {
                LOGGER.error("Can't convert path to URL. File path:" + path, e);
            }
        });
        return new FindFirstClassLoader(urls.toArray(new URL[urls.size()]), getClass().getClassLoader());
    }

    private void loadGeneratedQClasses(List<URL> urls, String name, Path path) throws IOException {
        if (name.endsWith(jdbcType + JAR)) {
            urls.add(getUrl(path)); //load jar with q-classes
        }
    }

    private URL getUrl(Path jarFile) throws IOException {
        //The syntax of a JAR URL is: jar:${path}!/
        return new URL("jar:file:" + jarFile.toString() + "!/");
    }

    private static class FindFirstClassLoader extends URLClassLoader {

        private FindFirstClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            try {
                return findClass(name);
            } catch (Exception e) {
                return super.loadClass(name);
            }
        }
    }
}
