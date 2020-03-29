/*
 * Copyright 2013 the original author or authors.
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

package org.gradle.integtests.fixtures.executer

import org.gradle.api.Action
import org.gradle.cache.CacheBuilder
import org.gradle.cache.FileLockManager
import org.gradle.cache.PersistentCache
import org.gradle.cache.internal.CacheFactory
import org.gradle.cache.internal.DefaultCacheFactory
import org.gradle.cache.internal.DefaultFileLockManager
import org.gradle.cache.internal.DefaultProcessMetaDataProvider
import org.gradle.cache.internal.locklistener.NoOpFileLockContentionHandler
import org.gradle.internal.concurrent.DefaultExecutorFactory
import org.gradle.internal.progress.NoOpProgressLoggerFactory
import org.gradle.test.fixtures.file.TestFile
import org.gradle.testfixtures.internal.NativeServicesTestFixture
import org.gradle.util.GradleVersion

import static org.gradle.cache.internal.filelock.LockOptionsBuilder.mode

abstract class DownloadableGradleDistribution extends DefaultGradleDistribution {

    private static final CACHE_FACTORY = createCacheFactory()

    private static CacheFactory createCacheFactory() {
        return new DefaultCacheFactory(
                new DefaultFileLockManager(
                        new DefaultProcessMetaDataProvider(
                                NativeServicesTestFixture.getInstance().get(org.gradle.internal.nativeintegration.ProcessEnvironment)),
                        20 * 60 * 1000 // allow up to 20 minutes to download a distribution
                        , new NoOpFileLockContentionHandler()), new DefaultExecutorFactory(), new NoOpProgressLoggerFactory())
    }

    protected TestFile versionDir
    private PersistentCache cache

    DownloadableGradleDistribution(String version, TestFile versionDir) {
        super(GradleVersion.version(version), versionDir.file("gradle-$version"), versionDir.file("gradle-$version-bin.zip"))
        this.versionDir = versionDir
    }

    TestFile getBinDistribution() {
        download()
        super.getBinDistribution()
    }

    TestFile getGradleHomeDir() {
        download()
        super.getGradleHomeDir()
    }

    private void download() {
        if (cache == null) {
            def downloadAction = { cache ->
                URL url = getDownloadURL();
                System.out.println("downloading $url")
                super.binDistribution.copyFrom(url)
                super.binDistribution.usingNativeTools().unzipTo(versionDir)
            }
            //noinspection GrDeprecatedAPIUsage
            cache = CACHE_FACTORY.open(versionDir, version.version, [:], CacheBuilder.LockTarget.DefaultTarget, mode(FileLockManager.LockMode.Shared).useCrossVersionImplementation(), downloadAction as Action, null)
        }

        super.binDistribution.assertIsFile()
        super.gradleHomeDir.assertIsDir()
    }

    abstract protected URL getDownloadURL();
}
