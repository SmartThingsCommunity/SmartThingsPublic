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

package org.gradle.initialization;

/**
 * A listener that is notified when a session is started and completed. No more than one session may be active at any time.
 *
 * One or more builds may be run during a session. For example, when running in continuous mode, multiple builds are run during a single session.
 *
 * This listener type is available to session services up to global services.
 */
public interface SessionLifecycleListener {
    /**
     * Called at the start of the session, immediately after initializing the session services.
     */
    void afterStart();

    /**
     * Called at the completion of the session, immediately prior to tearing down the session services.
     */
    void beforeComplete();
}
