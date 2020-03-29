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

package org.gradle.plugins.javascript.jshint;

import com.google.gson.GsonBuilder;
import org.gradle.api.GradleException;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.plugins.javascript.jshint.internal.JsHintResult;
import org.gradle.plugins.javascript.jshint.internal.JsHintSpec;
import org.gradle.plugins.javascript.jshint.internal.JsHintWorker;
import org.gradle.plugins.javascript.rhino.worker.internal.DefaultRhinoWorkerHandleFactory;
import org.gradle.plugins.javascript.rhino.worker.internal.RhinoWorkerHandleFactory;
import org.gradle.process.internal.worker.RequestHandler;
import org.gradle.process.internal.worker.WorkerProcessFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsHint extends SourceTask {

    private Object rhinoClasspath;
    private Object jsHint;
    private String encoding = "UTF-8";
    private Object jsonReport;
    private LogLevel logLevel = getProject().getGradle().getStartParameter().getLogLevel();
    private File projectDir = getProject().getProjectDir();

    @Inject
    protected WorkerProcessFactory getWorkerProcessBuilderFactory() {
        throw new UnsupportedOperationException();
    }

    @Inject
    protected ObjectFactory getObjectFactory() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PathSensitive(PathSensitivity.RELATIVE)
    public FileTree getSource() {
        return super.getSource();
    }

    @Classpath
    public FileCollection getRhinoClasspath() {
        return getObjectFactory().fileCollection().from(rhinoClasspath);
    }

    /**
     * Sets Rhino classpath.
     *
     * @since 4.0
     */
    public void setRhinoClasspath(FileCollection rhinoClasspath) {
        this.rhinoClasspath = rhinoClasspath;
    }

    public void setRhinoClasspath(Object rhinoClasspath) {
        this.rhinoClasspath = rhinoClasspath;
    }

    @PathSensitive(PathSensitivity.RELATIVE)
    @InputFiles
    public FileCollection getJsHint() {
        return getObjectFactory().fileCollection().from(jsHint);
    }

    /**
     * Sets jshint file.
     *
     * @since 4.0
     */
    public void setJsHint(FileCollection jsHint) {
        this.jsHint = jsHint;
    }

    public void setJsHint(Object jsHint) {
        this.jsHint = jsHint;
    }

    @Input
    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @OutputFile
    public File getJsonReport() {
        return jsonReport == null ? null : getProject().file(jsonReport);
    }

    /**
     * Sets JSON report file.
     *
     * @since 4.0
     */
    public void setJsonReport(File jsonReport) {
        this.jsonReport = jsonReport;
    }

    public void setJsonReport(Object jsonReport) {
        this.jsonReport = jsonReport;
    }

    @TaskAction
    public void doJsHint() {
        RhinoWorkerHandleFactory handleFactory = new DefaultRhinoWorkerHandleFactory(getWorkerProcessBuilderFactory());

        RequestHandler<JsHintSpec, JsHintResult> worker = handleFactory.create(getRhinoClasspath(), JsHintWorker.class, logLevel, projectDir);

        JsHintSpec spec = new JsHintSpec();
        spec.setSource(getSource().getFiles()); // flatten because we need to serialize
        spec.setEncoding(getEncoding());
        spec.setJsHint(getJsHint().getSingleFile());

        JsHintResult result = worker.run(spec);
        setDidWork(true);

        // TODO - this is all terribly lame. We need some proper reporting here (which means implementing Reporting).

        Logger logger = getLogger();
        boolean anyErrors = false;

        Map<String, Map<?, ?>> reportData = new LinkedHashMap<String, Map<?, ?>>(result.getResults().size());
        for (Map.Entry<File, Map<String, Object>> fileEntry: result.getResults().entrySet()) {
            File file = fileEntry.getKey();
            Map<String, Object> data = fileEntry.getValue();

            reportData.put(file.getAbsolutePath(), data);

            if (data.containsKey("errors")) {
                anyErrors = true;

                URI projectDirUri = projectDir.toURI();
                @SuppressWarnings("unchecked") Map<String, Object> errors = (Map<String, Object>) data.get("errors");
                if (!errors.isEmpty()) {
                    URI relativePath = projectDirUri.relativize(file.toURI());
                    logger.warn("JsHint errors for file: {}", relativePath.getPath());
                    for (Map.Entry<String, Object> errorEntry : errors.entrySet()) {
                        @SuppressWarnings("unchecked") Map<String, Object> error = (Map<String, Object>) errorEntry.getValue();
                        int line = Float.valueOf(error.get("line").toString()).intValue();
                        int character = Float.valueOf(error.get("character").toString()).intValue();
                        String reason = error.get("reason").toString();

                        logger.warn("  {}:{} > {}", new Object[] {line, character, reason});
                    }
                }
            }
        }

        File jsonReportFile = getJsonReport();
        if (jsonReportFile != null) {
            try {
                FileWriter reportWriter = new FileWriter(jsonReportFile);
                new GsonBuilder().setPrettyPrinting().create().toJson(reportData, reportWriter);
                reportWriter.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        if (anyErrors) {
            throw new GradleException("JsHint detected errors");
        }
    }
}
