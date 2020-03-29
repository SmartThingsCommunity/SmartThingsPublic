/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.internal.scan.config;

import org.gradle.StartParameter;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.internal.Factory;
import org.gradle.internal.InternalBuildAdapter;
import org.gradle.internal.event.ListenerManager;
import org.gradle.util.VersionNumber;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.gradle.internal.scan.config.BuildScanPluginCompatibility.OLD_SCAN_PLUGIN_VERSION_MESSAGE;

/**
 * This is the meeting point between Gradle and the build scan plugin during initialization. This is effectively build scoped.
 */
public class BuildScanConfigManager implements BuildScanConfigInit, BuildScanConfigProvider, BuildScanPluginApplied {

    private static final Logger LOGGER = Logging.getLogger(BuildScanConfigManager.class);

    private static final VersionNumber FIRST_VERSION_AWARE_OF_UNSUPPORTED = VersionNumber.parse("1.11");

    public static final String NO_PLUGIN_MSG = "An internal error occurred that prevented a build scan from being created.\n" +
        "Please report this via https://github.com/gradle/gradle/issues";

    private static final String SYSPROP_KEY = "scan";
    private static final List<String> ENABLED_SYS_PROP_VALUES = Arrays.asList(null, "", "yes", "true");

    private final StartParameter startParameter;
    private final ListenerManager listenerManager;
    private final BuildScanPluginCompatibility compatibility;
    private final Factory<BuildScanConfig.Attributes> configAttributes;

    private Requestedness requestedness = Requestedness.DEFAULTED;
    private boolean collected;

    BuildScanConfigManager(
        StartParameter startParameter,
        ListenerManager listenerManager,
        BuildScanPluginCompatibility compatibility,
        Factory<BuildScanConfig.Attributes> configAttributes
    ) {
        this.startParameter = startParameter;
        this.listenerManager = listenerManager;
        this.compatibility = compatibility;
        this.configAttributes = configAttributes;
    }

    @Override
    public void init() {
        boolean checkForPlugin = false;
        if (startParameter.isBuildScan()) {
            checkForPlugin = true;
            requestedness = Requestedness.ENABLED;
        } else if (startParameter.isNoBuildScan()) {
            requestedness = Requestedness.DISABLED;
        } else {
            // Before there was --scan, there was -Dscan or -Dscan=true or -Dscan=yes
            Map<String, String> sysProps = startParameter.getSystemPropertiesArgs();
            if (sysProps.containsKey(SYSPROP_KEY)) {
                String sysProp = sysProps.get(SYSPROP_KEY);
                checkForPlugin = ENABLED_SYS_PROP_VALUES.contains(sysProp);
            }
        }

        if (checkForPlugin) {
            warnIfBuildScanPluginNotApplied();
        }
    }

    private void warnIfBuildScanPluginNotApplied() {
        // Note: this listener manager is scoped to the root Gradle object.
        listenerManager.addListener(new InternalBuildAdapter() {
            @Override
            public void projectsEvaluated(Gradle gradle) {
                if (gradle.getParent() == null && !collected) {
                    LOGGER.warn(NO_PLUGIN_MSG);
                }
            }
        });
    }

    @Override
    public BuildScanConfig collect(BuildScanPluginMetadata pluginMetadata) {
        if (collected) {
            throw new IllegalStateException("Configuration has already been collected.");
        }

        VersionNumber pluginVersion = VersionNumber.parse(pluginMetadata.getVersion()).getBaseVersion();
        if (pluginVersion.compareTo(BuildScanPluginCompatibility.FIRST_GRADLE_ENTERPRISE_PLUGIN_VERSION) < 0) {
            throw new UnsupportedBuildScanPluginVersionException(OLD_SCAN_PLUGIN_VERSION_MESSAGE);
        }

        collected = true;
        BuildScanConfig.Attributes configAttributes = this.configAttributes.create();
        String unsupportedReason = compatibility.unsupportedReason();

        if (unsupportedReason != null) {
            if (isPluginAwareOfUnsupported(pluginVersion)) {
                return requestedness.toConfig(unsupportedReason, configAttributes);
            } else {
                throw new UnsupportedBuildScanPluginVersionException(unsupportedReason);
            }
        }

        return requestedness.toConfig(null, configAttributes);
    }

    private boolean isPluginAwareOfUnsupported(VersionNumber pluginVersion) {
        return pluginVersion.compareTo(FIRST_VERSION_AWARE_OF_UNSUPPORTED) >= 0;
    }

    @Override
    public boolean isBuildScanPluginApplied() {
        return collected;
    }

    private enum Requestedness {

        DEFAULTED(false, false),
        ENABLED(true, false),
        DISABLED(false, true);

        private final boolean enabled;
        private final boolean disabled;

        Requestedness(boolean enabled, boolean disabled) {
            this.enabled = enabled;
            this.disabled = disabled;
        }

        BuildScanConfig toConfig(final String unsupported, final BuildScanConfig.Attributes attributes) {
            return new BuildScanConfig() {
                @Override
                public boolean isEnabled() {
                    return enabled;
                }

                @Override
                public boolean isDisabled() {
                    return disabled;
                }

                @Override
                public String getUnsupportedMessage() {
                    return unsupported;
                }

                @Override
                public Attributes getAttributes() {
                    return attributes;
                }
            };
        }
    }

}
