/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.internal.locking;

import com.google.common.collect.ImmutableList;
import org.gradle.api.internal.DocumentationRegistry;
import org.gradle.api.internal.DomainObjectContext;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Property;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;

public class LockFileReaderWriter {

    private static final Logger LOGGER = Logging.getLogger(LockFileReaderWriter.class);
    private static final DocumentationRegistry DOC_REG = new DocumentationRegistry();

    static final String UNIQUE_LOCKFILE_NAME = "gradle.lockfile";
    static final String FILE_SUFFIX = ".lockfile";
    static final String DEPENDENCY_LOCKING_FOLDER = "gradle/dependency-locks";
    static final Charset CHARSET = StandardCharsets.UTF_8;
    static final List<String> LOCKFILE_HEADER_LIST = ImmutableList.of("# This is a Gradle generated file for dependency locking.", "# Manual edits can break the build and are not advised.", "# This file is expected to be part of source control.");
    static final String EMPTY_CONFIGURATIONS_ENTRY = "empty=";
    static final String BUILD_SCRIPT_PREFIX = "buildscript-";

    private final Path lockFilesRoot;
    private final DomainObjectContext context;
    private final Property<File> lockFile;

    public LockFileReaderWriter(FileResolver fileResolver, DomainObjectContext context, Property<File> lockFile) {
        this.context = context;
        this.lockFile = lockFile;
        Path resolve = null;
        if (fileResolver.canResolveRelativePath()) {
            resolve = fileResolver.resolve(DEPENDENCY_LOCKING_FOLDER).toPath();
            lockFile.convention(fileResolver.resolve(decorate(UNIQUE_LOCKFILE_NAME)));
        }
        this.lockFilesRoot = resolve;
        LOGGER.debug("Lockfiles root: {}", lockFilesRoot);
    }

    public void writeLockFile(String configurationName, List<String> resolvedModules) {
        checkValidRoot(configurationName);

        makeLockfilesRoot();
        List<String> content = new ArrayList<>(50);
        content.addAll(LOCKFILE_HEADER_LIST);
        content.addAll(resolvedModules);
        try {
            Files.write(lockFilesRoot.resolve(decorate(configurationName) + FILE_SUFFIX), content, CHARSET);
        } catch (IOException e) {
            throw new RuntimeException("Unable to write lock file", e);
        }
    }

    private void makeLockfilesRoot() {
        if (!Files.exists(lockFilesRoot)) {
            try {
                Files.createDirectories(lockFilesRoot);
            } catch (IOException e) {
                throw new RuntimeException("Issue creating dependency-lock directory", e);
            }
        }
    }

    public List<String> readLockFile(String configurationName) {
        checkValidRoot(configurationName);

        Path lockFile = lockFilesRoot.resolve(decorate(configurationName) + FILE_SUFFIX);
        if (Files.exists(lockFile)) {
            List<String> result;
            try {
                result = Files.readAllLines(lockFile, CHARSET);
            } catch (IOException e) {
                throw new RuntimeException("Unable to load lock file", e);
            }
            List<String> lines = result;
            filterNonModuleLines(lines);
            return lines;
        } else {
            return null;
        }
    }

    private String decorate(String configurationName) {
        if (context.isScript()) {
            return BUILD_SCRIPT_PREFIX + configurationName;
        } else {
            return configurationName;
        }
    }

    private void checkValidRoot() {
        if (lockFilesRoot == null) {
            throw new IllegalStateException("Dependency locking cannot be used for project '" + context.getProjectPath() + "'." +
                " See limitations in the documentation (" + DOC_REG.getDocumentationFor("dependency_locking", "locking_limitations") +").");
        }
    }

    private void checkValidRoot(String configurationName) {
        if (lockFilesRoot == null) {
            throw new IllegalStateException("Dependency locking cannot be used for configuration '" + context.identityPath(configurationName) + "'." +
                " See limitations in the documentation (" + DOC_REG.getDocumentationFor("dependency_locking", "locking_limitations") +").");
        }
    }

    private void filterNonModuleLines(List<String> lines) {
        Iterator<String> iterator = lines.iterator();
        while (iterator.hasNext()) {
            String value = iterator.next().trim();
            if (value.startsWith("#") || value.isEmpty()) {
                iterator.remove();
            }
        }
    }

    public Map<String, List<String>> readUniqueLockFile() {
        checkValidRoot();
        Predicate<String> empty = String::isEmpty;
        Predicate<String> comment = s -> s.startsWith("#");
        Path uniqueLockFile = getUniqueLockfilePath();
        List<String> emptyConfigurations = new ArrayList<>();
        Map<String, List<String>> uniqueLockState = new HashMap<>(10);
        if (Files.exists(uniqueLockFile)) {
            try {
                Files.lines(uniqueLockFile, CHARSET).filter(empty.or(comment).negate())
                    .filter(line -> {
                        if (line.startsWith(EMPTY_CONFIGURATIONS_ENTRY)) {
                            // This is a perf hack for handling the last line in the file in a special way
                            collectEmptyConfigurations(line, emptyConfigurations);
                            return false;
                        } else {
                            return true;
                        }
                    }).forEach(line -> parseLine(line, uniqueLockState));
            } catch (IOException e) {
                throw new RuntimeException("Unable to load unique lockfile", e);
            }
            for (String emptyConfiguration : emptyConfigurations) {
                uniqueLockState.computeIfAbsent(emptyConfiguration, k -> new ArrayList<>());
            }
            return uniqueLockState;
        } else {
            return new HashMap<>();
        }
    }

    private void collectEmptyConfigurations(String line, List<String> emptyConfigurations) {
        if (line.length() > EMPTY_CONFIGURATIONS_ENTRY.length()) {
            String[] configurations = line.substring(EMPTY_CONFIGURATIONS_ENTRY.length()).split(",");
            Collections.addAll(emptyConfigurations, configurations);
        }
    }

    private Path getUniqueLockfilePath() {
        return lockFile.map(File::toPath).get();
    }

    private void parseLine(String line, Map<String, List<String>> result) {
        String[] split = line.split("=");
        String[] configurations = split[1].split(",");
        for (String configuration : configurations) {
            result.compute(configuration, (k, v) -> {
                List<String> mapping;
                if (v == null) {
                    mapping = new ArrayList<>();
                } else {
                    mapping = v;
                }
                mapping.add(split[0]);
                return mapping;
            });
        }
    }

    public boolean canWrite() {
        return lockFilesRoot != null;
    }

    public void writeUniqueLockfile(Map<String, List<String>> lockState) {
        checkValidRoot();
        makeLockfilesRoot();
        Path lockfilePath = getUniqueLockfilePath();

        // Revert mapping
        Map<String, List<String>> dependencyToConfigurations = new TreeMap<>();
        List<String> emptyConfigurations = new ArrayList<>();
        mapLockStateFromDependencyToConfiguration(lockState, dependencyToConfigurations, emptyConfigurations);

        writeUniqueLockfile(lockfilePath, dependencyToConfigurations, emptyConfigurations);
    }

    private void writeUniqueLockfile(Path lockfilePath, Map<String, List<String>> dependencyToConfigurations, List<String> emptyConfigurations) {
        try {
            Files.createDirectories(lockfilePath.getParent());
            List<String> content = new ArrayList<>(50);
            content.addAll(LOCKFILE_HEADER_LIST);
            for (Map.Entry<String, List<String>> entry : dependencyToConfigurations.entrySet()) {
                String builder = entry.getKey() + "=" + String.join(",", entry.getValue());
                content.add(builder);
            }
            content.add("empty=" + String.join(",", emptyConfigurations));
            Files.write(lockfilePath, content, CHARSET);
        } catch (IOException e) {
            throw new RuntimeException("Unable to write unique lockfile", e);
        }
    }

    private void mapLockStateFromDependencyToConfiguration(Map<String, List<String>> lockState, Map<String, List<String>> dependencyToConfigurations, List<String> emptyConfigurations) {
        for (Map.Entry<String, List<String>> entry : lockState.entrySet()) {
            List<String> dependencies = entry.getValue();
            if (dependencies.isEmpty()) {
                emptyConfigurations.add(entry.getKey());
            } else {
                for (String dependency : dependencies) {
                    dependencyToConfigurations.compute(dependency, (k, v) -> {
                        List<String> confs = v;
                        if (v == null) {
                            confs = new ArrayList<>();
                        }
                        confs.add(entry.getKey());
                        return confs;
                    });
                }
            }
        }
    }
}
