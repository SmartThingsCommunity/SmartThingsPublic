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

package org.gradle.performance.util

import org.eclipse.jgit.storage.file.FileRepositoryBuilder

class Git {
    private static Git git
    final String commitId
    final String branchName

    public static Git current() {
        if (git == null) {
            git = new Git()
        }
        return git
    }

    private Git() {
        def repository = new FileRepositoryBuilder().findGitDir().build()
        try {
            branchName = System.getenv("BUILD_BRANCH") ?: repository.branch
            commitId = repository.resolve(repository.fullBranch).name
        } finally {
            repository.close()
        }
    }
}
