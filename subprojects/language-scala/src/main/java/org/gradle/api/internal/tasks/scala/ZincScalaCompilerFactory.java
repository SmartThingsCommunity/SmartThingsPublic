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

package org.gradle.api.internal.tasks.scala;

import com.google.common.collect.Iterables;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.cache.CacheRepository;
import org.gradle.cache.FileLockManager;
import org.gradle.cache.PersistentCache;
import org.gradle.internal.jvm.Jvm;
import org.gradle.internal.time.Time;
import org.gradle.internal.time.Timer;
import sbt.internal.inc.AnalyzingCompiler$;
import sbt.internal.inc.RawCompiler;
import sbt.internal.inc.ScalaInstance;
import scala.Option;
import scala.collection.JavaConverters;
import xsbti.ArtifactInfo;
import xsbti.compile.ClasspathOptionsUtil;
import xsbti.compile.ScalaCompiler;
import xsbti.compile.ZincCompilerUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.gradle.cache.internal.filelock.LockOptionsBuilder.mode;

public class ZincScalaCompilerFactory {
    private static final Logger LOGGER = Logging.getLogger(ZincScalaCompilerFactory.class);

    static ZincScalaCompiler getCompiler(CacheRepository cacheRepository, final Iterable<File> scalaClasspath) {
        ScalaInstance scalaInstance = getScalaInstance(scalaClasspath);
        String zincVersion = ZincCompilerUtil.class.getPackage().getImplementationVersion();
        String scalaVersion = scalaInstance.actualVersion();
        String javaVersion = Jvm.current().getJavaVersion().getMajorVersion();
        String zincCacheKey = String.format("zinc-%s_%s_%s", zincVersion, scalaVersion, javaVersion);
        String zincCacheName = String.format("%s compiler cache", zincCacheKey);
        final PersistentCache zincCache = cacheRepository.cache(zincCacheKey)
                .withDisplayName(zincCacheName)
                .withLockOptions(mode(FileLockManager.LockMode.OnDemand))
                .open();

        File compilerBridgeSourceJar = findFile("compiler-bridge", scalaClasspath);
        File bridgeJar = getBridgeJar(zincCache, scalaInstance, compilerBridgeSourceJar, sbt.util.Logger.xlog2Log(new SbtLoggerAdapter()));
        ScalaCompiler scalaCompiler = ZincCompilerUtil.scalaCompiler(scalaInstance, bridgeJar, ClasspathOptionsUtil.auto());

        return new ZincScalaCompiler(scalaInstance, scalaCompiler, new AnalysisStoreProvider());
    }

    private static ClassLoader getClassLoader(final Iterable<File> classpath) {
        try {
            List<URL> urls = new ArrayList<URL>();
            for (File file : classpath) {
                urls.add(file.toURI().toURL());
            }
            return new URLClassLoader(urls.toArray(new URL[0]));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ScalaInstance getScalaInstance(final Iterable<File> scalaClasspath) {
        ClassLoader scalaClassLoader = getClassLoader(scalaClasspath);
        String scalaVersion = getScalaVersion(scalaClassLoader);

        File libraryJar = findFile(ArtifactInfo.ScalaLibraryID, scalaClasspath);
        File compilerJar = findFile(ArtifactInfo.ScalaCompilerID, scalaClasspath);

        return new ScalaInstance(
                scalaVersion,
                scalaClassLoader,
                getClassLoader(Arrays.asList(libraryJar)),
                libraryJar,
                compilerJar,
                Iterables.toArray(scalaClasspath, File.class),
                Option.empty()
        );
    }

    private static File getBridgeJar(PersistentCache zincCache, ScalaInstance scalaInstance, File compilerBridgeSourceJar, sbt.util.Logger logger) {
        return zincCache.useCache(() -> {
            final File bridgeJar = new File(zincCache.getBaseDir(), "compiler-bridge.jar");
            if (bridgeJar.exists()) {
                // compiler interface exists, use it
                return bridgeJar;
            } else {
                // generate from sources jar
                final Timer timer = Time.startTimer();
                RawCompiler rawCompiler = new RawCompiler(scalaInstance, ClasspathOptionsUtil.manual(), logger);
                scala.collection.Iterable<File> sourceJars = JavaConverters.collectionAsScalaIterable(Collections.singletonList(compilerBridgeSourceJar));
                scala.collection.Iterable<File> xsbtiJars = JavaConverters.collectionAsScalaIterable(Arrays.asList(scalaInstance.allJars()));
                AnalyzingCompiler$.MODULE$.compileSources(sourceJars, bridgeJar, xsbtiJars, "compiler-bridge", rawCompiler, logger);

                final String interfaceCompletedMessage = String.format("Scala Compiler interface compilation took %s", timer.getElapsed());
                if (timer.getElapsedMillis() > 30000) {
                    LOGGER.warn(interfaceCompletedMessage);
                } else {
                    LOGGER.debug(interfaceCompletedMessage);
                }

                return bridgeJar;
            }
        });
    }

    private static File findFile(String prefix, Iterable<File> files) {
        for (File f: files) {
            if (f.getName().startsWith(prefix)) {
                return f;
            }
        }
        throw new IllegalStateException(String.format("Cannot find any files starting with %s in %s", prefix, files));
    }

    private static String getScalaVersion(ClassLoader scalaClassLoader) {
        try {
            Properties props = new Properties();
            props.load(scalaClassLoader.getResourceAsStream("library.properties"));
            return props.getProperty("version.number");
        } catch (IOException e) {
            throw new IllegalStateException("Unable to determine scala version");
        }
    }


}
