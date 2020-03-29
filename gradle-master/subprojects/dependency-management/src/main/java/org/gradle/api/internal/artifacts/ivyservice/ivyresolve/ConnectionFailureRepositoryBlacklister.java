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

package org.gradle.api.internal.artifacts.ivyservice.ivyresolve;

import com.google.common.collect.Sets;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.util.Set;

import static org.gradle.internal.resolve.ResolveExceptionAnalyzer.isCriticalFailure;

public class ConnectionFailureRepositoryBlacklister implements RepositoryBlacklister {
    private static final Logger LOGGER = Logging.getLogger(ConnectionFailureRepositoryBlacklister.class);

    private final Set<String> blacklistedRepositories = Sets.newConcurrentHashSet();

    @Override
    public boolean isBlacklisted(String repositoryId) {
        return blacklistedRepositories.contains(repositoryId);
    }

    @Override
    public boolean blacklistRepository(String repositoryId, Throwable throwable) {
        boolean blacklisted = isBlacklisted(repositoryId);

        if (blacklisted) {
            return true;
        }

        if (isCriticalFailure(throwable)) {
            LOGGER.debug("Repository {} has been blacklisted for this build due to connectivity issues", repositoryId);
            blacklistedRepositories.add(repositoryId);
            return true;
        }

        return false;
    }

    @Override
    public Set<String> getBlacklistedRepositories() {
        return blacklistedRepositories;
    }
}
