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

package org.gradle.language.fixtures

import groovy.transform.CompileStatic
import org.gradle.api.internal.tasks.compile.incremental.processing.IncrementalAnnotationProcessorType
import org.gradle.api.internal.tasks.compile.processing.AnnotationProcessorDetector
import org.gradle.test.fixtures.file.TestFile

/**
 * Base class for all annotation processor test fixtures. Each processor listens to a single annotation.
 * It provides the basic scaffolding, like fields for the filer, element utils and messager as well as
 * finding the annotated elements. Subclasses only need to provide the processing logic given those elements.
 *
 * The declared type of the processor can be overwritten to test various error cases, e.g. a processor that
 * declares itself as incremental, but doesn't honor that contract.
 */
@CompileStatic
abstract class AnnotationProcessorFixture {
    protected final String annotationName
    IncrementalAnnotationProcessorType declaredType

    AnnotationProcessorFixture(String annotationName) {
        this.annotationName = annotationName
    }

    final void writeApiTo(TestFile projectDir) {
        // Annotation handled by processor
        projectDir.file("src/main/java/${annotationName}.java").text = """
            public @interface $annotationName {
            }
"""
    }

    AnnotationProcessorFixture withDeclaredType(IncrementalAnnotationProcessorType type) {
        declaredType = type
        this
    }

    def writeSupportLibraryTo(TestFile projectDir) {
        //no support library by default
    }

    final void writeAnnotationProcessorTo(TestFile projectDir) {
        // The annotation processor
        projectDir.file("src/main/java/${annotationName}Processor.java").text = """
            import java.io.*;
            import java.util.*;
            import javax.annotation.processing.*;
            import javax.lang.model.*;
            import javax.lang.model.element.*;
            import javax.lang.model.util.*;
            import javax.tools.*;

            import static javax.tools.StandardLocation.*;

            @SupportedOptions({ "message" })
            public class ${annotationName}Processor extends AbstractProcessor {
                private Map<String, String> options;
                private Elements elementUtils;
                private Filer filer;
                private Messager messager;
    
                @Override
                public Set<String> getSupportedAnnotationTypes() {
                    return Collections.singleton(${annotationName}.class.getName());
                }
                
                ${supportedOptionsBlock}
            
                @Override
                public SourceVersion getSupportedSourceVersion() {
                    return SourceVersion.latestSupported();
                }
    
                @Override
                public synchronized void init(ProcessingEnvironment processingEnv) {
                    elementUtils = processingEnv.getElementUtils();
                    filer = processingEnv.getFiler();
                    messager = processingEnv.getMessager();
                    options = processingEnv.getOptions();
                }
    
                @Override
                public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
                    for (TypeElement annotation : annotations) {
                        if (annotation.getQualifiedName().toString().equals(${annotationName}.class.getName())) {
                            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
                            ${generatorCode}
                        }
                    }
                    return true;
                }
            }
"""
        projectDir.file("src/main/resources/$AnnotationProcessorDetector.PROCESSOR_DECLARATION").text = "${annotationName}Processor"
        if (declaredType) {
            projectDir.file("src/main/resources/$AnnotationProcessorDetector.INCREMENTAL_PROCESSOR_DECLARATION").text = "${annotationName}Processor,$declaredType"
        }
    }

    protected abstract String getGeneratorCode();

    protected String getSupportedOptionsBlock() {
        ""
    }
}
