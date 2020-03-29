/*
 * Copyright 2012 the original author or authors.
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

package org.gradle.api.internal.tasks.compile;

import org.gradle.api.tasks.compile.GroovyCompileOptions;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;

public class DefaultGroovyJavaJointCompileSpec extends DefaultJavaCompileSpec implements GroovyJavaJointCompileSpec {
    private GroovyCompileOptions compileOptions;
    private List<File> groovyClasspath;
    private File compilationMappingFile;

    @Override
    public GroovyCompileOptions getGroovyCompileOptions() {
        return compileOptions;
    }

    public void setGroovyCompileOptions(GroovyCompileOptions compileOptions) {
        this.compileOptions = compileOptions;
    }

    @Override
    public List<File> getGroovyClasspath() {
        return groovyClasspath;
    }

    @Override
    public void setGroovyClasspath(List<File> groovyClasspath) {
        this.groovyClasspath = groovyClasspath;
    }

    @Override
    @Nullable
    public File getCompilationMappingFile() {
        return compilationMappingFile;
    }

    @Override
    public void setCompilationMappingFile(@Nullable File compilationMappingFile) {
        this.compilationMappingFile = compilationMappingFile;
    }
}
