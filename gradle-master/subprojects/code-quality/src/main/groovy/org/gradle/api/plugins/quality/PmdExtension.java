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

import org.gradle.api.Incubating;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.resources.TextResource;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration options for the PMD plugin.
 *
 * @see PmdPlugin
 */
public class PmdExtension extends CodeQualityExtension {

    private final Project project;

    private List<String> ruleSets;
    private TargetJdk targetJdk;
    private int rulePriority = 5;
    private TextResource ruleSetConfig;
    private ConfigurableFileCollection ruleSetFiles;
    private boolean consoleOutput;
    private Property<Integer> maxFailures;
    private Property<Boolean> incrementalAnalysis;

    public PmdExtension(Project project) {
        this.project = project;
        // TODO: Enable this by default when toolVersion >= 6.0.0 if it's stable enough.
        this.incrementalAnalysis = project.getObjects().property(Boolean.class).convention(false);
        this.maxFailures = project.getObjects().property(Integer.class).convention(0);
    }

    /**
     * The built-in rule sets to be used. See the <a href="https://pmd.github.io/pmd-6.21.0/pmd_rules_java.html">official list</a> of built-in rule sets.
     *
     * <pre>
     *     ruleSets = ["category/java/errorprone.xml", "category/java/bestpractices.xml"]
     * </pre>
     */
    public List<String> getRuleSets() {
        return ruleSets;
    }

    /**
     * The built-in rule sets to be used. See the <a href="https://pmd.github.io/pmd-6.21.0/pmd_rules_java.html">official list</a> of built-in rule sets.
     *
     * <pre>
     *     ruleSets = ["category/java/errorprone.xml", "category/java/bestpractices.xml"]
     * </pre>
     */
    public void setRuleSets(List<String> ruleSets) {
        this.ruleSets = ruleSets;
    }

    /**
     * Convenience method for adding rule sets.
     *
     * <pre>
     *     ruleSets "category/java/errorprone.xml", "category/java/bestpractices.xml"
     * </pre>
     *
     * @param ruleSets the rule sets to be added
     */
    public void ruleSets(String... ruleSets) {
        this.ruleSets.addAll(Arrays.asList(ruleSets));
    }

    /**
     * The target jdk to use with pmd, 1.3, 1.4, 1.5, 1.6, 1.7 or jsp
     */
    public TargetJdk getTargetJdk() {
        return targetJdk;
    }

    /**
     * Sets the target jdk used with pmd.
     *
     * @param targetJdk The target jdk
     * @since 4.0
     */
    public void setTargetJdk(TargetJdk targetJdk) {
        this.targetJdk = targetJdk;
    }

    /**
     * The maximum number of failures to allow before stopping the build.
     *
     * If <pre>ignoreFailures</pre> is set, this is ignored and no limit is enforced.
     *
     * @since 6.4
     */
    @Incubating
    public Property<Integer> getMaxFailures() {
        return maxFailures;
    }

    /**
     * Sets the target jdk used with pmd.
     *
     * @param value The value for the target jdk as defined by {@link TargetJdk#toVersion(Object)}
     */
    public void setTargetJdk(Object value) {
        targetJdk = TargetJdk.toVersion(value);
    }

    /**
     * The rule priority threshold; violations for rules with a lower priority will not be reported. Default value is 5, which means that all violations will be reported.
     *
     * This is equivalent to PMD's Ant task minimumPriority property.
     *
     * See the official documentation for the <a href="https://pmd.github.io/pmd-6.21.0/pmd_userdocs_configuring_rules.html">list of priorities</a>.
     *
     * <pre>
     *     rulePriority = 3
     * </pre>
     */
    public int getRulePriority() {
        return rulePriority;
    }

    /**
     * Sets the rule priority threshold.
     */
    public void setRulePriority(int intValue) {
        Pmd.validate(intValue);
        rulePriority = intValue;
    }

    /**
     * The custom rule set to be used (if any). Replaces {@code ruleSetFiles}, except that it does not currently support multiple rule sets.
     *
     * See the <a href="https://pmd.github.io/pmd-6.21.0/pmd_userdocs_making_rulesets.html">official documentation</a> for how to author a rule set.
     *
     * <pre>
     *     ruleSetConfig = resources.text.fromFile("config/pmd/myRuleSet.xml")
     * </pre>
     *
     * @since 2.2
     */
    @Nullable
    public TextResource getRuleSetConfig() {
        return ruleSetConfig;
    }

    /**
     * The custom rule set to be used (if any). Replaces {@code ruleSetFiles}, except that it does not currently support multiple rule sets.
     *
     * See the <a href="https://pmd.github.io/pmd-6.21.0/pmd_userdocs_making_rulesets.html">official documentation</a> for how to author a rule set.
     *
     * <pre>
     *     ruleSetConfig = resources.text.fromFile("config/pmd/myRuleSet.xml")
     * </pre>
     *
     * @since 2.2
     */
    public void setRuleSetConfig(@Nullable TextResource ruleSetConfig) {
        this.ruleSetConfig = ruleSetConfig;
    }

    /**
     * The custom rule set files to be used. See the <a href="https://pmd.github.io/pmd-6.21.0/pmd_userdocs_making_rulesets.html">official documentation</a> for how to author a rule set file.
     * If you want to only use custom rule sets, you must clear {@code ruleSets}.
     *
     * <pre>
     *     ruleSetFiles = files("config/pmd/myRuleSet.xml")
     * </pre>
     */
    public FileCollection getRuleSetFiles() {
        return ruleSetFiles;
    }

    /**
     * The custom rule set files to be used. See the <a href="https://pmd.github.io/pmd-6.21.0/pmd_userdocs_making_rulesets.html">official documentation</a> for how to author a rule set file.
     * This adds to the default rule sets defined by {@link #getRuleSets()}.
     *
     * <pre>
     *     ruleSetFiles = files("config/pmd/myRuleSets.xml")
     * </pre>
     */
    public void setRuleSetFiles(FileCollection ruleSetFiles) {
        this.ruleSetFiles = project.getObjects().fileCollection().from(ruleSetFiles);
    }

    /**
     * Convenience method for adding rule set files.
     *
     * <pre>
     *     ruleSetFiles "config/pmd/myRuleSet.xml"
     * </pre>
     *
     * @param ruleSetFiles the rule set files to be added
     */
    public void ruleSetFiles(Object... ruleSetFiles) {
        this.ruleSetFiles.from(ruleSetFiles);
    }

    /**
     * Whether or not to write PMD results to {@code System.out}.
     */
    public boolean isConsoleOutput() {
        return consoleOutput;
    }

    /**
     * Whether or not to write PMD results to {@code System.out}.
     */
    public void setConsoleOutput(boolean consoleOutput) {
        this.consoleOutput = consoleOutput;
    }

    /**
     * Controls whether to use incremental analysis or not.
     *
     * This is only supported for PMD 6.0.0 or better. See <a href="https://pmd.github.io/pmd-6.21.0/pmd_userdocs_incremental_analysis.html"></a> for more details.
     *
     * @since 5.6
     */
    @Incubating
    public Property<Boolean> getIncrementalAnalysis() {
        return incrementalAnalysis;
    }
}
