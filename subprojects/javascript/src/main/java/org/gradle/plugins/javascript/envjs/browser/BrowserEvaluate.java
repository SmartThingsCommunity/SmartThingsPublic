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

package org.gradle.plugins.javascript.envjs.browser;

import org.gradle.api.DefaultTask;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.plugins.javascript.envjs.http.HttpFileServer;
import org.gradle.plugins.javascript.envjs.http.simple.SimpleHttpFileServerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.Callable;

public class BrowserEvaluate extends DefaultTask {

    private Object content;
    private Object resource;
    private BrowserEvaluator evaluator;
    private Object result;

    public BrowserEvaluate() {
        dependsOn(new Callable<TaskDependency>() {
            @Override
            public TaskDependency call() throws Exception {
                return getProject().files(BrowserEvaluate.this.content).getBuildDependencies();
            }
        });
    }

    @InputDirectory
    @PathSensitive(PathSensitivity.NAME_ONLY)
    public File getContent() {
        return content == null ? null : getProject().files(content).getSingleFile();
    }

    /**
     * Sets content.
     *
     * @since 4.0
     */
    public void setContent(File content) {
        this.content = content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    @Input
    public String getResource() {
        return resource.toString();
    }

    /**
     * Sets resource.
     *
     * @since 4.0
     */
    public void setResource(String resource) {
        this.resource = resource;
    }

    public void setResource(Object resource) {
        this.resource = resource;
    }

    //@Input
    @Internal
    public BrowserEvaluator getEvaluator() {
        return evaluator;
    }

    public void setEvaluator(BrowserEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @OutputFile
    public File getResult() {
        return result == null ? null : getProject().file(result);
    }

    /**
     * Sets result.
     *
     * @since 4.0
     */
    public void setResult(File result) {
        this.result = result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    @TaskAction
    void doEvaluate() {
        HttpFileServer fileServer = new SimpleHttpFileServerFactory().start(getContent(), 0);

        try {
            Writer resultWriter = new FileWriter(getResult());
            getEvaluator().evaluate(fileServer.getResourceUrl(getResource()), resultWriter);
            resultWriter.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            fileServer.stop();
        }

        setDidWork(true);
    }
}
