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

package org.gradle.deployment.internal;

class OutOfDateTrackingDeployment implements DeploymentInternal {
    private boolean changed;
    private Throwable failure;

    @Override
    public synchronized Status status() {
        Status result = new DefaultDeploymentStatus(changed, failure);
        changed = false;
        return result;
    }

    @Override
    public synchronized void outOfDate() {
        changed = true;
        failure = null;
    }

    @Override
    public synchronized void upToDate(Throwable failure) {
        this.failure = failure;
    }
}
