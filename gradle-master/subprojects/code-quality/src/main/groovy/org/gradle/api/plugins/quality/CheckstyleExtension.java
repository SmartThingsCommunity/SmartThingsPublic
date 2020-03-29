/*
 * Copyright 2011 the original author or authors.
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
package org.gradle.api.plugins.quality;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ReplacedBy;
import org.gradle.api.resources.TextResource;
import org.gradle.internal.deprecation.DeprecationLogger;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configuration options for the Checkstyle plugin.
 *
 * @see CheckstylePlugin
 */
public class CheckstyleExtension extends CodeQualityExtension {

    private final Project project;

    private TextResource config;
    private Map<String, Object> configProperties = new LinkedHashMap<String, Object>();
    private int maxErrors;
    private int maxWarnings = Integer.MAX_VALUE;
    private boolean showViolations = true;
    private final DirectoryProperty configDirectory;

    public CheckstyleExtension(Project project) {
        this.project = project;
        this.configDirectory = project.getObjects().directoryProperty();
    }

    /**
     * The Checkstyle configuration file to use.
     */
    public File getConfigFile() {
        return getConfig().asFile();
    }

    /**
     * The Checkstyle configuration file to use.
     */
    public void setConfigFile(File configFile) {
        setConfig(project.getResources().getText().fromFile(configFile));
    }

    /**
     * The Checkstyle configuration to use. Replaces the {@code configFile} property.
     *
     * @since 2.2
     */
    public TextResource getConfig() {
        return config;
    }

    /**
     * The Checkstyle configuration to use. Replaces the {@code configFile} property.
     *
     * @since 2.2
     */
    public void setConfig(TextResource config) {
        this.config = config;
    }

    /**
     * The properties available for use in the configuration file. These are substituted into the configuration file.
     */
    public Map<String, Object> getConfigProperties() {
        return configProperties;
    }

    /**
     * The properties available for use in the configuration file. These are substituted into the configuration file.
     */
    public void setConfigProperties(Map<String, Object> configProperties) {
        this.configProperties = configProperties;
    }

    /**
     * Path to other Checkstyle configuration files. By default, this path is {@code $rootProject.projectDir/config/checkstyle}
     * <p>
     * This path will be exposed as the variable {@code config_loc} in Checkstyle's configuration files.
     * </p>
     *
     * @return path to other Checkstyle configuration files
     * @since 4.0
     */
    @Deprecated
    @ReplacedBy("configDirectory")
    public File getConfigDir() {
        DeprecationLogger.deprecateMethod(CheckstyleExtension.class, "getConfigDir()").replaceWith("CheckstyleExtension.getConfigDirectory()")
            .willBeRemovedInGradle7()
            .withDslReference(CheckstyleExtension.class, "configDir")
            .nagUser();
        return configDirectory.get().getAsFile();
    }

    /**
     * Path to other Checkstyle configuration files. By default, this path is {@code $rootProject.projectDir/config/checkstyle}
     * <p>
     * This path will be exposed as the variable {@code config_loc} in Checkstyle's configuration files.
     * </p>
     *
     * @since 4.0
     */
    @Deprecated
    public void setConfigDir(File configDir) {
        DeprecationLogger.deprecateMethod(CheckstyleExtension.class, "setConfigDir()").replaceWith("CheckstyleExtension.getConfigDirectory().set()")
            .willBeRemovedInGradle7()
            .withDslReference(CheckstyleExtension.class, "configDir")
            .nagUser();
        this.configDirectory.set(configDir);
    }

    /**
     * Path to other Checkstyle configuration files. By default, this path is {@code $rootProject.projectDir/config/checkstyle}
     * <p>
     * This path will be exposed as the variable {@code config_loc} in Checkstyle's configuration files.
     * </p>
     *
     * @return path to other Checkstyle configuration files
     * @since 4.7
     */
    public DirectoryProperty getConfigDirectory() {
        return configDirectory;
    }

    /**
     * The maximum number of errors that are tolerated before breaking the build
     * or setting the failure property. Defaults to <tt>0</tt>.
     * <p>
     * Example: maxErrors = 42
     *
     * @return the maximum number of errors allowed
     * @since 3.4
     */
    public int getMaxErrors() {
        return maxErrors;
    }

    /**
     * Set the maximum number of errors that are tolerated before breaking the build.
     *
     * @param maxErrors number of errors allowed
     * @since 3.4
     */
    public void setMaxErrors(int maxErrors) {
        this.maxErrors = maxErrors;
    }

    /**
     * The maximum number of warnings that are tolerated before breaking the build
     * or setting the failure property. Defaults to <tt>Integer.MAX_VALUE</tt>.
     * <p>
     * Example: maxWarnings = 1000
     *
     * @return the maximum number of warnings allowed
     * @since 3.4
     */
    public int getMaxWarnings() {
        return maxWarnings;
    }

    /**
     * Set the maximum number of warnings that are tolerated before breaking the build.
     *
     * @param maxWarnings number of warnings allowed
     * @since 3.4
     */
    public void setMaxWarnings(int maxWarnings) {
        this.maxWarnings = maxWarnings;
    }

    /**
     * Whether rule violations are to be displayed on the console. Defaults to <tt>true</tt>.
     *
     * Example: showViolations = false
     */
    public boolean isShowViolations() {
        return showViolations;
    }

    /**
     * Whether rule violations are to be displayed on the console. Defaults to <tt>true</tt>.
     *
     * Example: showViolations = false
     */
    public void setShowViolations(boolean showViolations) {
        this.showViolations = showViolations;
    }
}
