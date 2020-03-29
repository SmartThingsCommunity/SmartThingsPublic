/*
 * Copyright 2014 the original author or authors.
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

package org.gradle.language.scala.internal.toolchain

import org.gradle.api.internal.ClassPathRegistry
import org.gradle.initialization.ClassLoaderRegistry
import org.gradle.language.base.internal.compile.CompileSpec
import org.gradle.process.internal.JavaForkOptionsFactory
import org.gradle.workers.internal.ActionExecutionSpecFactory
import org.gradle.workers.internal.WorkerDaemonFactory
import spock.lang.Specification

class DefaultScalaToolProviderTest extends Specification {
    JavaForkOptionsFactory forkOptionsFactory = Mock()
    ClassPathRegistry classPathRegistry = Mock()
    ClassLoaderRegistry classLoaderRegistry = Mock()
    WorkerDaemonFactory workerDaemonFactory = Mock()
    ActionExecutionSpecFactory actionExecutionSpecFactory = Mock()
    Set<File> scalacClasspath = Mock()
    Set<File> zincClasspath = Mock()
    File rootProjectDir = Mock()

    def "newCompiler provides decent error for unsupported CompileSpec"() {
        setup:
        DefaultScalaToolProvider scalaToolProvider = new DefaultScalaToolProvider(rootProjectDir, workerDaemonFactory, forkOptionsFactory, scalacClasspath, zincClasspath, classPathRegistry, classLoaderRegistry, actionExecutionSpecFactory)

        when:
        scalaToolProvider.newCompiler(UnknownCompileSpec.class)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "Cannot create Compiler for unsupported CompileSpec type 'UnknownCompileSpec'"
    }
}

class UnknownCompileSpec implements CompileSpec {}

