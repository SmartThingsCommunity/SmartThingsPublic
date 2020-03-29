/*
 * Copyright 2019 the original author or authors.
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

package org.gradle.api.internal.file

import com.google.common.collect.ImmutableSet
import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileVisitDetails
import org.gradle.api.file.FileVisitor
import org.gradle.api.internal.file.collections.FileSystemMirroringFileTree
import org.gradle.api.internal.file.collections.MinimalFileSet
import org.gradle.api.internal.provider.PropertyHost
import org.gradle.api.internal.provider.Providers
import org.gradle.api.internal.tasks.TaskDependencyFactory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskDependency
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet
import org.gradle.internal.Factory
import org.gradle.test.fixtures.file.TestNameTestDirectoryProvider
import org.junit.Assert
import org.junit.ClassRule
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.Callable

class DefaultFileCollectionFactoryTest extends Specification {
    @ClassRule
    @Shared
    TestNameTestDirectoryProvider tmpDir = new TestNameTestDirectoryProvider(getClass())
    def factory = new DefaultFileCollectionFactory(TestFiles.pathToFileResolver(tmpDir.testDirectory), Stub(TaskDependencyFactory), TestFiles.directoryFileTreeFactory(), Stub(Factory), Stub(PropertyHost), TestFiles.fileSystem())

    def "lazily queries contents of collection created from MinimalFileSet"() {
        def contents = Mock(MinimalFileSet)
        def file = new File("a")

        when:
        def collection = factory.create(contents)

        then:
        0 * contents._

        when:
        def files = collection.files

        then:
        files == [file] as Set
        1 * contents.files >> [file]
        0 * contents._
    }

    def "lazily queries dependencies of collection created from MinimalFileSet"() {
        def contents = Mock(MinimalFileSet)
        def builtBy = Mock(TaskDependency)
        def task = Stub(Task)

        when:
        def collection = factory.create(builtBy, contents)

        then:
        0 * builtBy._

        when:
        def tasks = collection.buildDependencies.getDependencies(null)

        then:
        tasks == [task] as Set
        1 * builtBy.getDependencies(_) >> [task]
        0 * _
    }

    def "constructs a configurable collection"() {
        expect:
        def collection = factory.configurableFiles("some collection")
        collection.files.empty
        collection.buildDependencies.getDependencies(null).empty
        collection.toString() == "some collection"
    }

    def "constructs an empty collection"() {
        expect:
        def collection = factory.empty()
        collection.isEmpty()
        collection.files.empty
        collection.buildDependencies.getDependencies(null).empty
        collection.visitStructure(new BrokenVisitor())
        collection.toString() == "file collection"

        def tree = collection.asFileTree
        tree.isEmpty()
        tree.files.empty
        tree.visit(new BrokenVisitor())
        tree.buildDependencies.getDependencies(null).empty
        tree.visitStructure(new BrokenVisitor())
        tree.matching {} is tree
        tree.matching(Stub(Action)) is tree
        tree.matching(Stub(PatternFilterable)) is tree
        tree.toString() == "file tree"
    }

    def "constructs empty collection with display name"() {
        expect:
        def collection = factory.empty("some collection")
        collection.files.empty
        collection.buildDependencies.getDependencies(null).empty
        collection.visitStructure(new BrokenVisitor())
        collection.toString() == "some collection"

        def tree = collection.asFileTree
        tree.files.empty
        tree.visit(new BrokenVisitor())
        tree.buildDependencies.getDependencies(null).empty
        tree.visitStructure(new BrokenVisitor())
        tree.toString() == "file tree"
    }

    def "constructs a collection with fixed contents"() {
        def file1 = new File("a")
        def file2 = new File("b")

        expect:
        def collection1 = factory.fixed("some collection", file1, file2)
        collection1.files == [file1, file2] as Set
        collection1.buildDependencies.getDependencies(null).empty
        collection1.toString() == "some collection"

        def collection2 = factory.fixed("some collection", [file1, file2])
        collection2.files == [file1, file2] as Set
        collection2.buildDependencies.getDependencies(null).empty
        collection2.toString() == "some collection"
    }

    def "returns empty collection when constructed with empty fixed array"() {
        expect:
        def collection = factory.fixed()
        collection.files.empty
        collection.buildDependencies.getDependencies(null).empty
        collection.visitStructure(new BrokenVisitor())
        collection.toString() == "file collection"
    }

    def "returns empty collection when constructed with display name and a fixed empty array"() {
        expect:
        def collection = factory.fixed("some collection")
        collection.files.empty
        collection.buildDependencies.getDependencies(null).empty
        collection.visitStructure(new BrokenVisitor())
        collection.toString() == "some collection"
    }

    def "returns empty collection when constructed with a fixed list containing nothing"() {
        expect:
        def collection = factory.fixed([])
        collection.files.empty
        collection.buildDependencies.getDependencies(null).empty
        collection.visitStructure(new BrokenVisitor())
        collection.toString() == "file collection"
    }

    def "returns empty collection when constructed with display name and a fixed list containing nothing"() {
        expect:
        def collection = factory.fixed("some collection", [])
        collection.files.empty
        collection.buildDependencies.getDependencies(null).empty
        collection.visitStructure(new BrokenVisitor())
        collection.toString() == "some collection"
    }

    def "returns empty collection when constructed with empty resolving array"() {
        expect:
        def collection = factory.resolving()
        collection.files.empty
        collection.buildDependencies.getDependencies(null).empty
        collection.visitStructure(new BrokenVisitor())
        collection.toString() == "file collection"
    }

    def "returns empty collection when constructed with display name and empty resolving array"() {
        expect:
        def collection = factory.resolving("some collection")
        collection.files.empty
        collection.buildDependencies.getDependencies(null).empty
        collection.visitStructure(new BrokenVisitor())
        collection.toString() == "some collection"
    }

    def "returns live collection when constructed with display name and a resolving list containing nothing"() {
        def contents = []

        expect:
        def collection = factory.resolving("some collection", contents)
        collection.files.empty
        collection.buildDependencies.getDependencies(null).empty
        collection.visitStructure(new BrokenVisitor())
        collection.toString() == "some collection"

        contents.add('a')
        !collection.files.empty
    }

    def "returns original file collection when constructed with a single collection"() {
        def original = Stub(FileCollectionInternal)

        expect:
        def collection = factory.resolving(original)
        collection.is(original)
    }

    def 'resolves specified files using FileResolver'() {
        def collection = factory.resolving('test files', 'abc', 'def')

        when:
        Set<File> files = collection.getFiles()

        then:
        collection.toString() == 'test files'
        files == [tmpDir.file('abc'), tmpDir.file('def')] as Set
    }

    def 'can use a Closure to specify a single file'() {
        def collection = factory.resolving('test files', [{ 'abc' }] as Object[])

        when:
        Set<File> files = collection.getFiles()

        then:
        collection.toString() == 'test files'
        files == [tmpDir.file('abc')] as Set
    }

    @Unroll
    def '#description can return null'() {
        def collection = factory.resolving('test files', input)

        when:
        Set<File> files = collection.getFiles()

        then:
        files.isEmpty()

        where:
        description | input
        'Closure'   | ({ null } as Object[])
        'Callable'  | (({ null } as Callable<Object>) as Object[])
    }

    def 'Provider can throw IllegalStateException'() {
        Provider provider = Mock()
        def exception = new IllegalStateException()
        def collection = factory.resolving('test files', provider)

        when:
        collection.getFiles()

        then:
        1 * provider.get() >> { throw exception }
        def thrown = thrown(IllegalStateException)
        exception == thrown
    }

    def 'lazily queries contents of a FileCollection'() {
        FileCollectionInternal fileCollection = Mock()

        when:
        def collection = factory.resolving('test files', fileCollection)

        then:
        0 * fileCollection._
        collection.toString() == 'test files'

        when:
        def files = collection.files

        then:
        1 * fileCollection.files >> { [tmpDir.file('abc')] as Set }
        files == [tmpDir.file('abc')] as Set
    }

    @Unroll
    def 'can use a #description to specify the contents of the collection'() {
        def collection = factory.resolving('test files', input)

        when:
        Set<File> files = collection.getFiles()

        then:
        files == [tmpDir.file('abc'), tmpDir.file('def')] as Set

        where:
        description        | input
        'closure'          | ({ ['abc', 'def'] } as Object[])
        'collection(list)' | ['abc', 'def']
        'array'            | (['abc', 'def'] as Object[])
        'FileCollection'   | fileCollectionOf(tmpDir.file('abc'), tmpDir.file('def'))
        'Callable'         | (({ ['abc', 'def'] } as Callable<Object>) as Object[])
        'Provider'         | providerReturning(['abc', 'def'])
        'nested objects'   | ({ [{ ['abc', { ['def'] as String[] }] }] } as Object[])
    }

    private FileCollection fileCollectionOf(final File... files) {
        return new AbstractFileCollection() {
            @Override
            String getDisplayName() {
                return 'test file collection'
            }

            @Override
            Set<File> getFiles() {
                return ImmutableSet.copyOf(files)
            }
        }
    }

    private Provider<Object> providerReturning(Object result) {
        return Providers.of(result)
    }

    @Unroll
    def 'can use a #description to specify the single content of the collection'() {
        def collection = factory.resolving('test files', input)

        when:
        Set<File> files = collection.getFiles()

        then:
        files == [tmpDir.file('abc')] as Set

        where:
        description | input
        'String'    | 'abc'
        'Path'      | tmpDir.file('abc').toPath()
        'URI'       | tmpDir.file('abc').toURI()
        'URL'       | tmpDir.file('abc').toURI().toURL()
    }

    static class BrokenVisitor implements FileCollectionStructureVisitor, FileVisitor {
        @Override
        void visitDir(FileVisitDetails dirDetails) {
            Assert.fail()
        }

        @Override
        void visitFile(FileVisitDetails fileDetails) {
            Assert.fail()
        }

        @Override
        void visitCollection(FileCollectionInternal.Source source, Iterable<File> contents) {
            Assert.fail()
        }

        @Override
        void visitGenericFileTree(FileTreeInternal fileTree, FileSystemMirroringFileTree sourceTree) {
            Assert.fail()
        }

        @Override
        void visitFileTree(File root, PatternSet patterns, FileTreeInternal fileTree) {
            Assert.fail()
        }

        @Override
        void visitFileTreeBackedByFile(File file, FileTreeInternal fileTree, FileSystemMirroringFileTree sourceTree) {
            Assert.fail()
        }
    }
}
