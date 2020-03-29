/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.launcher.daemon.server.stats;

import org.gradle.internal.time.Time;
import org.gradle.internal.time.Timer;

public class DaemonRunningStats {

    private final long startTime = System.currentTimeMillis();
    private final Timer daemonTimer = Time.startTimer();
    private final Timer currentBuildTimer = Time.startTimer();

    private int buildCount;
    private long allBuildsTime;

    public int getBuildCount() {
        return buildCount;
    }

    public String getPrettyUpTime() {
        return daemonTimer.getElapsed();
    }

    public long getStartTime() {
        return startTime;
    }

    public long getAllBuildsTime() {
        return allBuildsTime;
    }

    // TODO: these should be moved off to a separate type

    public void buildStarted() {
        ++buildCount;
        currentBuildTimer.reset();
    }

    public void buildFinished() {
        long buildTime = Math.max(currentBuildTimer.getElapsedMillis(), 1);
        allBuildsTime += buildTime;
    }
}
