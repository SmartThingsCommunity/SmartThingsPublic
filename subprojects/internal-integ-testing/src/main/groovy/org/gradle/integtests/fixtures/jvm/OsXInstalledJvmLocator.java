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

package org.gradle.integtests.fixtures.jvm;

import org.gradle.api.GradleException;
import org.gradle.process.internal.ExecException;
import org.gradle.process.internal.ExecHandleBuilder;
import org.gradle.process.internal.ExecHandleFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;

/**
 * Uses `java_home -V` to find JVM installations
 */
class OsXInstalledJvmLocator {
    private final ExecHandleFactory execHandleFactory;

    public OsXInstalledJvmLocator(ExecHandleFactory execHandleFactory) {
        this.execHandleFactory = execHandleFactory;
    }

    public Collection<JvmInstallation> findJvms() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ExecHandleBuilder execHandleBuilder = execHandleFactory.newExec();
            execHandleBuilder.workingDir(new File(".").getAbsoluteFile());
            execHandleBuilder.commandLine("/usr/libexec/java_home", "-V");
            // verbose output is written to stderr for some reason
            execHandleBuilder.setErrorOutput(outputStream);
            execHandleBuilder.setStandardOutput(new ByteArrayOutputStream());
            execHandleBuilder.build().start().waitForFinish().assertNormalExitValue();
            return new OsXJavaHomeParser().parse(new InputStreamReader(new ByteArrayInputStream(outputStream.toByteArray())));
        } catch (ExecException e) {
            if (new String(outputStream.toByteArray()).contains("No Java runtime present")) {
                return Collections.emptyList();
            }
            throw new GradleException("Could not locate installed JVMs.", e);
        } catch (Exception e) {
            throw new GradleException("Could not locate installed JVMs.", e);
        }
    }
}
