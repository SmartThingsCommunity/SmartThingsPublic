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

package org.gradle.api.internal.tasks.properties

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.AbstractTask
import org.gradle.api.internal.ConventionTask
import org.gradle.api.internal.DynamicObjectAware
import org.gradle.api.internal.HasConvention
import org.gradle.api.internal.IConventionAware
import org.gradle.api.internal.tasks.properties.annotations.PropertyAnnotationHandler
import org.gradle.api.internal.tasks.properties.annotations.TypeAnnotationHandler
import org.gradle.api.model.ReplacedBy
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.CompileClasspath
import org.gradle.api.tasks.Console
import org.gradle.api.tasks.Destroys
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.LocalState
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.OutputFiles
import org.gradle.cache.internal.TestCrossBuildInMemoryCacheFactory
import org.gradle.internal.reflect.DefaultTypeValidationContext
import org.gradle.internal.reflect.PropertyMetadata
import org.gradle.internal.reflect.TypeValidationContext
import org.gradle.internal.reflect.annotations.impl.DefaultTypeAnnotationMetadataStore
import org.gradle.internal.scripts.ScriptOrigin
import org.gradle.internal.service.ServiceRegistryBuilder
import org.gradle.internal.service.scopes.ExecutionGlobalServices
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import javax.inject.Inject
import java.lang.annotation.Annotation

import static ModifierAnnotationCategory.NORMALIZATION
import static org.gradle.internal.reflect.TypeValidationContext.Severity.WARNING

class DefaultTypeMetadataStoreTest extends Specification {

    static final PROCESSED_PROPERTY_TYPE_ANNOTATIONS = [
        Input, InputFile, InputFiles, InputDirectory, Nested, OutputFile, OutputDirectory, OutputFiles, OutputDirectories, Destroys, LocalState
    ]

    static final UNPROCESSED_PROPERTY_TYPE_ANNOTATIONS = [
        Console, Internal, ReplacedBy
    ]

    @Shared GroovyClassLoader groovyClassLoader
    def services = ServiceRegistryBuilder.builder().provider(new ExecutionGlobalServices()).build()
    def cacheFactory = new TestCrossBuildInMemoryCacheFactory()
    def typeAnnotationMetadataStore = new DefaultTypeAnnotationMetadataStore(
        [CustomCacheable],
        ModifierAnnotationCategory.asMap((PROCESSED_PROPERTY_TYPE_ANNOTATIONS + [SearchPath]) as Set<Class<? extends Annotation>>),
        ["java", "groovy"],
        [DefaultTask],
        [Object, GroovyObject],
        [ConfigurableFileCollection, Property],
        UNPROCESSED_PROPERTY_TYPE_ANNOTATIONS,
        { false },
        cacheFactory
    )
    def metadataStore = new DefaultTypeMetadataStore([], services.getAll(PropertyAnnotationHandler), [Classpath, CompileClasspath], typeAnnotationMetadataStore, cacheFactory)

    def setupSpec() {
        groovyClassLoader = new GroovyClassLoader(getClass().classLoader)
    }

    static class TaskWithCustomAnnotation extends DefaultTask {
        @SearchPath FileCollection searchPath
    }

    @CustomCacheable
    static class TypeWithCustomAnnotation {
    }

    def "can use custom annotation handler"() {
        def annotationHandler = Stub(PropertyAnnotationHandler)
        _ * annotationHandler.propertyRelevant >> true
        _ * annotationHandler.annotationType >> SearchPath

        def metadataStore = new DefaultTypeMetadataStore([], [annotationHandler], [], typeAnnotationMetadataStore, cacheFactory)

        when:
        def typeMetadata = metadataStore.getTypeMetadata(TaskWithCustomAnnotation)
        def propertiesMetadata = typeMetadata.propertiesMetadata

        then:
        propertiesMetadata.size() == 1
        def propertyMetadata = propertiesMetadata.first()
        propertyMetadata.propertyName == 'searchPath'
        propertyMetadata.propertyType == SearchPath
        typeMetadata.getAnnotationHandlerFor(propertyMetadata) == annotationHandler
        collectProblems(typeMetadata).empty
    }

    def "custom annotation handler can inspect for static property problems"() {
        def annotationHandler = Stub(PropertyAnnotationHandler)
        _ * annotationHandler.propertyRelevant >> true
        _ * annotationHandler.annotationType >> SearchPath
        _ * annotationHandler.validatePropertyMetadata(_, _) >> { PropertyMetadata metadata, TypeValidationContext context ->
            context.visitPropertyProblem(WARNING, metadata.propertyName, "is broken")
        }

        def metadataStore = new DefaultTypeMetadataStore([], [annotationHandler], [], typeAnnotationMetadataStore, cacheFactory)

        when:
        def typeMetadata = metadataStore.getTypeMetadata(TaskWithCustomAnnotation)
        def propertiesMetadata = typeMetadata.propertiesMetadata

        then:
        propertiesMetadata.size() == 1
        def propertyMetadata = propertiesMetadata.first()
        propertyMetadata.propertyName == 'searchPath'
        collectProblems(typeMetadata) == ["Property 'searchPath' is broken."]
    }

    def "custom annotation that is not relevant can have validation problems"() {
        def annotationHandler = Stub(PropertyAnnotationHandler)
        _ * annotationHandler.propertyRelevant >> false
        _ * annotationHandler.annotationType >> SearchPath
        _ * annotationHandler.validatePropertyMetadata(_, _) >> { PropertyMetadata metadata, TypeValidationContext context ->
            context.visitPropertyProblem(WARNING, metadata.propertyName, "is broken")
        }

        def metadataStore = new DefaultTypeMetadataStore([], [annotationHandler], [], typeAnnotationMetadataStore, cacheFactory)

        when:
        def typeMetadata = metadataStore.getTypeMetadata(TaskWithCustomAnnotation)
        def propertiesMetadata = typeMetadata.propertiesMetadata

        then:
        propertiesMetadata.empty
        collectProblems(typeMetadata) == ["Property 'searchPath' is broken."]
    }

    def "custom type annotation handler can inspect for static type problems"() {
        def typeAnnotationHandler = Stub(TypeAnnotationHandler)
        _ * typeAnnotationHandler.annotationType >> CustomCacheable
        _ * typeAnnotationHandler.validateTypeMetadata(_, _) >> { Class type, TypeValidationContext context ->
            context.visitTypeProblem(WARNING, type, "type is broken")
        }

        def metadataStore = new DefaultTypeMetadataStore([typeAnnotationHandler], [], [], typeAnnotationMetadataStore, cacheFactory)

        when:
        def taskMetadata = metadataStore.getTypeMetadata(DefaultTask)

        then:
        collectProblems(taskMetadata).empty

        when:
        def typeMetadata = metadataStore.getTypeMetadata(TypeWithCustomAnnotation)

        then:
        collectProblems(typeMetadata) == ["Type 'DefaultTypeMetadataStoreTest.TypeWithCustomAnnotation': type is broken." as String]
    }

    @Unroll
    def "can override @#parentAnnotation.simpleName property type with @#childAnnotation.simpleName"() {
        def parentTask = groovyClassLoader.parseClass """
            class ParentTask extends org.gradle.api.DefaultTask {
                @$parentAnnotation.name Object getValue() { null }
            }
        """

        def childTask = groovyClassLoader.parseClass """
            class ChildTask extends ParentTask {
                @Override @$childAnnotation.name Object getValue() { null }
            }
        """

        def parentMetadata = metadataStore.getTypeMetadata(parentTask)
        def parentProperty = parentMetadata.propertiesMetadata.first()

        def childMetadata = metadataStore.getTypeMetadata(childTask)
        def childProperty = childMetadata.propertiesMetadata.first()

        expect:
        isOfType(parentProperty, parentAnnotation)
        isOfType(childProperty, childAnnotation)
        collectProblems(parentMetadata).empty
        collectProblems(childMetadata).empty

        where:
        [parentAnnotation, childAnnotation] << [PROCESSED_PROPERTY_TYPE_ANNOTATIONS, PROCESSED_PROPERTY_TYPE_ANNOTATIONS].combinations()*.flatten()
    }

    @Unroll
    def "can override @#processedAnnotation.simpleName property type with @#unprocessedAnnotation.simpleName"() {
        def parentTask = groovyClassLoader.parseClass """
            class ParentTask extends org.gradle.api.DefaultTask {
                @$processedAnnotation.name Object getValue() { null }
            }
        """

        def childTask = groovyClassLoader.parseClass """
            class ChildTask extends ParentTask {
                @Override @$unprocessedAnnotation.name Object getValue() { null }
            }
        """

        def parentMetadata = metadataStore.getTypeMetadata(parentTask)
        def parentProperty = parentMetadata.propertiesMetadata.first()

        def childMetadata = metadataStore.getTypeMetadata(childTask)

        expect:
        isOfType(parentProperty, processedAnnotation)
        childMetadata.propertiesMetadata.empty
        collectProblems(parentMetadata).empty
        collectProblems(childMetadata).empty

        where:
        [processedAnnotation, unprocessedAnnotation] << [PROCESSED_PROPERTY_TYPE_ANNOTATIONS, UNPROCESSED_PROPERTY_TYPE_ANNOTATIONS].combinations()*.flatten()
    }

    @Unroll
    def "can override @#unprocessedAnnotation.simpleName property type with @#processedAnnotation.simpleName"() {
        def parentTask = groovyClassLoader.parseClass """
            class ParentTask extends org.gradle.api.DefaultTask {
                @$unprocessedAnnotation.name Object getValue() { null }
            }
        """

        def childTask = groovyClassLoader.parseClass """
            class ChildTask extends ParentTask {
                @Override @$processedAnnotation.name Object getValue() { null }
            }
        """

        def parentMetadata = metadataStore.getTypeMetadata(parentTask)

        def childMetadata = metadataStore.getTypeMetadata(childTask)
        def childProperty = childMetadata.propertiesMetadata.first()

        expect:
        parentMetadata.propertiesMetadata.empty
        isOfType(childProperty, processedAnnotation)
        collectProblems(parentMetadata).empty
        collectProblems(childMetadata).empty

        where:
        [processedAnnotation, unprocessedAnnotation] << [PROCESSED_PROPERTY_TYPE_ANNOTATIONS, UNPROCESSED_PROPERTY_TYPE_ANNOTATIONS].combinations()*.flatten()
    }

    class ClasspathPropertyTask extends DefaultTask {
        @Classpath FileCollection classpathOnly
        @Classpath @InputFiles FileCollection classpathInputFiles
        @InputFiles @Classpath FileCollection inputFilesClasspath
    }

    class CompileClasspathPropertyTask extends DefaultTask {
        @CompileClasspath FileCollection classpathOnly
        @CompileClasspath @InputFiles FileCollection classpathInputFiles
        @InputFiles @CompileClasspath FileCollection inputFilesClasspath
    }

    // Third-party plugins that need to support Gradle versions both pre- and post-3.2
    // need to declare their @Classpath properties as @InputFiles as well
    @Issue("https://github.com/gradle/gradle/issues/913")
    @Unroll
    def "@#annotation.simpleName is recognized as normalization no matter how it's defined"() {
        when:
        def typeMetadata = metadataStore.getTypeMetadata(sampleType)

        then:
        def properties = typeMetadata.propertiesMetadata
        properties*.propertyName as List == ["classpathInputFiles", "classpathOnly", "inputFilesClasspath"]
        properties*.propertyType as List == [InputFiles, InputFiles, InputFiles]
        properties*.getAnnotationForCategory(NORMALIZATION)*.annotationType() as List == [annotation, annotation, annotation]
        collectProblems(typeMetadata).empty

        where:
        annotation       | sampleType
        Classpath        | ClasspathPropertyTask
        CompileClasspath | CompileClasspathPropertyTask
    }

    @Unroll
    def "all properties on #workClass are ignored"() {
        when:
        def typeMetadata = metadataStore.getTypeMetadata(workClass).propertiesMetadata.findAll { it.propertyType == null }
        then:
        typeMetadata*.propertyName.empty

        where:
        workClass << [ConventionTask.class, DefaultTask.class, AbstractTask.class, Task.class, Object.class, GroovyObject.class, IConventionAware.class, ExtensionAware.class, HasConvention.class, ScriptOrigin.class, DynamicObjectAware.class]
    }

    @SuppressWarnings("GrDeprecatedAPIUsage")
    static class SimpleTask extends DefaultTask {
        @Input String inputString
        @InputFile File inputFile
        @InputDirectory File inputDirectory
        @InputFiles File inputFiles
        @OutputFile File outputFile
        @OutputFiles Set<File> outputFiles
        @OutputDirectory File outputDirectory
        @OutputDirectories Set<File> outputDirectories
        @Destroys Set<File> destroys
        @LocalState File someCache
        @Inject Object injectedService
        @Internal Object internal
        @ReplacedBy("inputString") String oldProperty
        @Console boolean console
    }

    def "can get annotated properties of simple task"() {
        when:
        def properties = metadataStore.getTypeMetadata(SimpleTask).propertiesMetadata

        then:
        properties.propertyName.sort() == ["destroys", "inputDirectory", "inputFile", "inputFiles", "inputString", "outputDirectories", "outputDirectory", "outputFile", "outputFiles", "someCache"]
    }

    static class TypeWithUnannotatedProperties extends DefaultTask {
        String bad1
        File bad2
        @Input String useful
    }

    def "warns about and ignores properties that are not annotated"() {
        when:
        def metadata = metadataStore.getTypeMetadata(TypeWithUnannotatedProperties)

        then:
        metadata.propertiesMetadata.propertyName == ["useful"]
        collectProblems(metadata) == [
            "Property 'bad1' is not annotated with an input or output annotation.",
            "Property 'bad2' is not annotated with an input or output annotation."
        ]
    }

    static class TypeWithNonRelevantProperties extends DefaultTask {
        @ReplacedBy("notUseful2") String notUseful1
        @Console String notUseful2
        @Input String useful
    }

    def "ignores properties that are not relevant"() {
        when:
        def metadata = metadataStore.getTypeMetadata(TypeWithNonRelevantProperties)

        then:
        metadata.propertiesMetadata.propertyName == ["useful"]
        collectProblems(metadata) == []
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    private static class IsGetterTask extends DefaultTask {
        @Input
        private boolean feature1
        private boolean feature2

        boolean isFeature1() {
            return feature1
        }
        void setFeature1(boolean enabled) {
            this.feature1 = enabled
        }
        boolean isFeature2() {
            return feature2
        }
        void setFeature2(boolean enabled) {
            this.feature2 = enabled
        }
    }

    private static List<String> collectProblems(TypeMetadata metadata) {
        def validationContext = DefaultTypeValidationContext.withoutRootType(false)
        metadata.visitValidationFailures(null, validationContext)
        return validationContext.problems.keySet().toList()
    }

    private static boolean isOfType(PropertyMetadata metadata, Class<? extends Annotation> type) {
        metadata.propertyType == type
    }
}
