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

package org.gradle.api.internal.artifacts.transform

import com.google.common.collect.ImmutableList
import org.gradle.api.artifacts.component.BuildIdentifier
import org.gradle.api.artifacts.transform.TransformAction
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.internal.artifacts.DefaultBuildIdentifier
import org.gradle.api.internal.artifacts.DefaultProjectComponentIdentifier
import org.gradle.api.internal.artifacts.dsl.dependencies.ProjectFinder
import org.gradle.api.internal.attributes.ImmutableAttributes
import org.gradle.api.internal.file.TestFiles
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.internal.tasks.TaskDependencyResolveContext
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.FileNormalizer
import org.gradle.internal.Try
import org.gradle.internal.component.local.model.ComponentFileArtifactIdentifier
import org.gradle.internal.execution.TestExecutionHistoryStore
import org.gradle.internal.fingerprint.AbsolutePathInputNormalizer
import org.gradle.internal.fingerprint.FileCollectionFingerprinter
import org.gradle.internal.fingerprint.FileCollectionFingerprinterRegistry
import org.gradle.internal.fingerprint.impl.AbsolutePathFileCollectionFingerprinter
import org.gradle.internal.fingerprint.impl.DefaultFileCollectionFingerprinterRegistry
import org.gradle.internal.fingerprint.impl.DefaultFileCollectionSnapshotter
import org.gradle.internal.fingerprint.impl.OutputFileCollectionFingerprinter
import org.gradle.internal.hash.ClassLoaderHierarchyHasher
import org.gradle.internal.hash.HashCode
import org.gradle.internal.operations.BuildOperationExecutor
import org.gradle.internal.operations.CallableBuildOperation
import org.gradle.internal.service.ServiceRegistry
import org.gradle.internal.snapshot.impl.DefaultValueSnapshotter
import org.gradle.test.fixtures.AbstractProjectBuilderSpec
import org.gradle.util.Path
import org.gradle.work.InputChanges
import spock.lang.Unroll

import java.util.function.BiFunction

class DefaultTransformerInvocationFactoryTest extends AbstractProjectBuilderSpec {

    def immutableTransformsStoreDirectory = temporaryFolder.file("output")
    def mutableTransformsStoreDirectory = temporaryFolder.file("child/build/transforms")

    def classloaderHasher = Stub(ClassLoaderHierarchyHasher) {
        getClassLoaderHash(_ as ClassLoader) >> HashCode.fromInt(1234)
    }
    def valueSnapshotter = new DefaultValueSnapshotter(classloaderHasher, null)

    def executionHistoryStore = new TestExecutionHistoryStore()
    def virtualFileSystem = TestFiles.virtualFileSystem()
    def workExecutorTestFixture = new WorkExecutorTestFixture(virtualFileSystem, classloaderHasher, valueSnapshotter)
    def fileCollectionSnapshotter = new DefaultFileCollectionSnapshotter(virtualFileSystem, TestFiles.genericFileTreeSnapshotter(), TestFiles.fileSystem())

    def transformationWorkspaceProvider = new TestTransformationWorkspaceProvider(immutableTransformsStoreDirectory, executionHistoryStore)

    def fileCollectionFactory = TestFiles.fileCollectionFactory()
    def artifactTransformListener = Mock(ArtifactTransformListener)
    def dependencyFingerprinter = new AbsolutePathFileCollectionFingerprinter(fileCollectionSnapshotter)
    def outputFilesFingerprinter = new OutputFileCollectionFingerprinter(fileCollectionSnapshotter)
    def fingerprinterRegistry = new DefaultFileCollectionFingerprinterRegistry([dependencyFingerprinter, outputFilesFingerprinter])

    def projectServiceRegistry = Stub(ServiceRegistry) {
        get(CachingTransformationWorkspaceProvider) >> new TestTransformationWorkspaceProvider(mutableTransformsStoreDirectory, executionHistoryStore)
    }

    def childProject = Stub(ProjectInternal) {
        getServices() >> projectServiceRegistry
    }

    def projectFinder = Stub(ProjectFinder) {
        findProject(_ as BuildIdentifier, _ as String) >> childProject
    }

    def dependencies = Stub(ArtifactTransformDependencies) {
        getFiles() >> []
        fingerprint(_ as FileCollectionFingerprinter) >> { FileCollectionFingerprinter fingerprinter -> fingerprinter.empty() }
    }

    def buildOperationExecutor = Stub(BuildOperationExecutor) {
        call(_) >> { CallableBuildOperation op ->
            op.call(null)
        }
    }

    def invoker = new DefaultTransformerInvocationFactory(
        workExecutorTestFixture.workExecutor,
        virtualFileSystem,
        artifactTransformListener,
        transformationWorkspaceProvider,
        fileCollectionFactory,
        fileCollectionSnapshotter,
        projectFinder,
        buildOperationExecutor
    )

    private static class TestTransformer implements Transformer {
        private final HashCode secondaryInputsHash
        private final BiFunction<File, File, List<File>> transformationAction

        static TestTransformer create(HashCode secondaryInputsHash = HashCode.fromInt(1234), BiFunction<File, File, List<File>> transformationAction) {
            return new TestTransformer(secondaryInputsHash, transformationAction)
        }

        TestTransformer(HashCode secondaryInputsHash, BiFunction<File, File, List<File>> transformationAction) {
            this.transformationAction = transformationAction
            this.secondaryInputsHash = secondaryInputsHash
        }

        @Override
        Class<?> getImplementationClass() {
            return TransformAction.class
        }

        @Override
        ImmutableAttributes getFromAttributes() {
            return ImmutableAttributes.EMPTY
        }

        @Override
        boolean requiresDependencies() {
            return false
        }

        @Override
        boolean requiresInputChanges() {
            return false
        }

        @Override
        boolean isCacheable() {
            return false
        }

        @Override
        ImmutableList<File> transform(Provider<FileSystemLocation> inputArtifactProvider, File outputDir, ArtifactTransformDependencies dependencies, InputChanges inputChanges) {
            return ImmutableList.copyOf(transformationAction.apply(inputArtifactProvider.get().asFile, outputDir))
        }

        @Override
        HashCode getSecondaryInputHash() {
            return secondaryInputsHash
        }

        @Override
        Class<? extends FileNormalizer> getInputArtifactNormalizer() {
            return AbsolutePathInputNormalizer
        }

        @Override
        Class<? extends FileNormalizer> getInputArtifactDependenciesNormalizer() {
            return AbsolutePathInputNormalizer
        }

        @Override
        boolean isIsolated() {
            return true
        }

        @Override
        void isolateParameters(FileCollectionFingerprinterRegistry fingerprinterRegistry) {
        }

        @Override
        String getDisplayName() {
            return "Test transformer"
        }

        @Override
        void visitDependencies(TaskDependencyResolveContext context) {
        }
    }

    @Unroll
    def "executes transformations in workspace (#transformationType)"(TransformationType transformationType) {
        def inputArtifact = temporaryFolder.file("input")
        inputArtifact.text = "my input"
        def transformer = TestTransformer.create { input, outputDir ->
            def outputFile = new File(outputDir, input.name)
            outputFile.text = input.text + "transformed"
            return [outputFile]
        }

        when:
        def result = invoke(transformer, inputArtifact, dependencies, dependency(transformationType, inputArtifact), fingerprinterRegistry)

        then:
        result.get().size() == 1
        def transformedFile = result.get()[0]
        transformedFile.parentFile.parentFile == workspaceDirectory(transformationType)

        where:
        transformationType << TransformationType.values()
    }

    def "up-to-date on second run"() {
        def inputArtifact = temporaryFolder.file("input")
        inputArtifact.text = "my input"
        int transformerInvocations = 0
        def transformer = TestTransformer.create { input, outputDir ->
            transformerInvocations++
            def outputFile = new File(outputDir, input.name)
            outputFile.text = input.text + "transformed"
            return [outputFile]
        }

        when:
        invoke(transformer, inputArtifact, dependencies, TransformationSubject.initial(inputArtifact), fingerprinterRegistry)

        then:
        transformerInvocations == 1
        1 * artifactTransformListener.beforeTransformerInvocation(_, _)
        1 * artifactTransformListener.afterTransformerInvocation(_, _)

        when:
        invoke(transformer, inputArtifact, dependencies, TransformationSubject.initial(inputArtifact), fingerprinterRegistry)
        then:
        transformerInvocations == 1
        1 * artifactTransformListener.beforeTransformerInvocation(_, _)
        1 * artifactTransformListener.afterTransformerInvocation(_, _)
    }

    def "re-runs transform when previous execution failed"() {
        def inputArtifact = temporaryFolder.file("input")
        inputArtifact.text = "my input"
        def failure = new RuntimeException("broken")
        int transformerInvocations = 0
        def transformer = TestTransformer.create { input, outputDir ->
            transformerInvocations++
            def outputFile = new File(outputDir, input.name)
            assert !outputFile.exists()
            outputFile.text = input.text + "transformed"
            if (transformerInvocations == 1) {
                throw failure
            }
            return [outputFile]
        }

        when:
        def result = invoke(transformer, inputArtifact, dependencies, TransformationSubject.initial(inputArtifact), fingerprinterRegistry)

        then:
        transformerInvocations == 1
        1 * artifactTransformListener.beforeTransformerInvocation(_, _)
        1 * artifactTransformListener.afterTransformerInvocation(_, _)
        def wrappedFailure = result.failure.get()
        wrappedFailure.cause == failure

        when:
        invoke(transformer, inputArtifact, dependencies, TransformationSubject.initial(inputArtifact), fingerprinterRegistry)
        then:
        transformerInvocations == 2
        1 * artifactTransformListener.beforeTransformerInvocation(_, _)
        1 * artifactTransformListener.afterTransformerInvocation(_, _)
    }

    def "re-runs transform when output has been modified"() {
        def inputArtifact = temporaryFolder.file("input")
        inputArtifact.text = "my input"
        File outputFile = null
        int transformerInvocations = 0
        def transformer = TestTransformer.create { input, outputDir ->
            transformerInvocations++
            outputFile = new File(outputDir, input.name)
            assert !outputFile.exists()
            outputFile.text = input.text + " transformed"
            return [outputFile]
        }

        when:
        invoke(transformer, inputArtifact, dependencies, TransformationSubject.initial(inputArtifact), fingerprinterRegistry)
        then:
        transformerInvocations == 1
        outputFile?.isFile()

        when:
        outputFile.text = "changed"
        virtualFileSystem.invalidateAll()

        invoke(transformer, inputArtifact, dependencies, TransformationSubject.initial(inputArtifact), fingerprinterRegistry)
        then:
        transformerInvocations == 2
    }

    @Unroll
    def "different workspace for different secondary inputs (#transformationType)"(TransformationType transformationType) {
        def inputArtifact = temporaryFolder.file("input")
        inputArtifact.text = "my input"
        def workspaces = new HashSet<File>()
        def transformationAction = { File input, File workspace ->
            workspaces.add(workspace)
            def outputFile = new File(workspace, input.name)
            outputFile.text = input.text + " transformed"
            return ImmutableList.of(outputFile)
        }
        def transformer1 = TestTransformer.create(HashCode.fromInt(1234), transformationAction)
        def transformer2 = TestTransformer.create(HashCode.fromInt(4321), transformationAction)

        def subject = dependency(transformationType, inputArtifact)
        when:
        invoke(transformer1, inputArtifact, dependencies, subject, fingerprinterRegistry)
        invoke(transformer2, inputArtifact, dependencies, subject, fingerprinterRegistry)

        then:
        workspaces.size() == 2

        where:
        transformationType << TransformationType.values()
    }

    @Unroll
    def "different workspace for different input artifact paths (#transformationType)"(TransformationType transformationType) {
        def inputArtifact1 = temporaryFolder.file("input1")
        inputArtifact1.text = "my input"
        def inputArtifact2 = temporaryFolder.file("input2")
        inputArtifact1.text = "my input"
        def workspaces = new HashSet<File>()
        def transformationAction = { File input, File workspace ->
            workspaces.add(workspace)
            def outputFile = new File(workspace, input.name)
            outputFile.text = input.text + " transformed"
            return ImmutableList.of(outputFile)
        }
        def transformer = TestTransformer.create(HashCode.fromInt(1234), transformationAction)
        when:
        invoke(transformer, inputArtifact1, dependencies, dependency(transformationType, inputArtifact1), fingerprinterRegistry)
        then:
        workspaces.size() == 1

        when:
        virtualFileSystem.invalidateAll()
        inputArtifact1.text = "changed"
        invoke(transformer, inputArtifact2, dependencies, dependency(transformationType, inputArtifact2), fingerprinterRegistry)

        then:
        workspaces.size() == 2

        where:
        transformationType << TransformationType.values()
    }

    def "different workspace for different immutable input artifacts"() {
        def inputArtifact = temporaryFolder.file("input")
        inputArtifact.text = "my input"
        def workspaces = new HashSet<File>()
        def transformationAction = { File input, File workspace ->
            workspaces.add(workspace)
            def outputFile = new File(workspace, input.name)
            outputFile.text = input.text + " transformed"
            return ImmutableList.of(outputFile)
        }
        def transformer = TestTransformer.create(HashCode.fromInt(1234), transformationAction)
        def subject = immutableDependency(inputArtifact)

        when:
        invoke(transformer, inputArtifact, dependencies, subject, fingerprinterRegistry)
        then:
        workspaces.size() == 1

        when:
        virtualFileSystem.invalidateAll()
        inputArtifact.text = "changed"
        invoke(transformer, inputArtifact, dependencies, subject, fingerprinterRegistry)

        then:
        workspaces.size() == 2
    }

    def "same workspace for different mutable input artifacts"() {
        def inputArtifact = temporaryFolder.file("input")
        inputArtifact.text = "my input"
        def workspaces = new HashSet<File>()
        def transformationAction = { File input, File workspace ->
            workspaces.add(workspace)
            def outputFile = new File(workspace, input.name)
            outputFile.text = input.text + " transformed"
            return ImmutableList.of(outputFile)
        }
        def transformer = TestTransformer.create(HashCode.fromInt(1234), transformationAction)
        def subject = mutableDependency(inputArtifact)

        when:
        invoke(transformer, inputArtifact, dependencies, subject, fingerprinterRegistry)
        then:
        workspaces.size() == 1

        when:
        virtualFileSystem.invalidateAll()
        inputArtifact.text = "changed"
        invoke(transformer, inputArtifact, dependencies, subject, fingerprinterRegistry)

        then:
        workspaces.size() == 1
    }

    enum TransformationType {
        MUTABLE, IMMUTABLE
    }

    private static dependency(TransformationType type, File file) {
        return type == TransformationType.MUTABLE ? mutableDependency(file) : immutableDependency(file)
    }

    private workspaceDirectory(TransformationType type) {
        return type == TransformationType.MUTABLE ? mutableTransformsStoreDirectory : immutableTransformsStoreDirectory
    }

    private static TransformationSubject immutableDependency(File file) {
        return TransformationSubject.initial(file)
    }

    private static TransformationSubject mutableDependency(File file) {
        def artifactIdentifier = new ComponentFileArtifactIdentifier(
            new DefaultProjectComponentIdentifier(
                DefaultBuildIdentifier.ROOT,
                Path.path(":child"),
                Path.path(":child"),
                "child"
            ), file.getName())
        return TransformationSubject.initial(artifactIdentifier,
            file)
    }

    private Try<ImmutableList<File>> invoke(
        Transformer transformer,
        File inputArtifact,
        ArtifactTransformDependencies dependencies,
        TransformationSubject subject,
        FileCollectionFingerprinterRegistry fingerprinterRegistry
    ) {
        return invoker.createInvocation(transformer, inputArtifact, dependencies, subject, fingerprinterRegistry).invoke()
    }
}
