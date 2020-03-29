/*
 * Copyright 2010 the original author or authors.
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

import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileVisitorUtil
import org.gradle.api.internal.TaskInternal
import org.gradle.api.internal.provider.ProviderInternal
import org.gradle.api.internal.tasks.TaskDependencyInternal
import org.gradle.api.internal.tasks.TaskDependencyResolveContext
import org.gradle.api.specs.Spec
import org.gradle.util.GUtil
import org.gradle.util.TestUtil

import static org.gradle.util.Matchers.isEmpty
import static org.gradle.util.WrapUtil.toLinkedSet
import static org.gradle.util.WrapUtil.toList
import static org.gradle.util.WrapUtil.toSet
import static org.hamcrest.CoreMatchers.equalTo
import static org.hamcrest.core.IsInstanceOf.instanceOf
import static org.junit.Assert.assertThat

class AbstractFileCollectionTest extends FileCollectionSpec {
    final TaskDependencyInternal dependency = Mock(TaskDependencyInternal.class)

    @Override
    AbstractFileCollection containing(File... files) {
        return new TestFileCollection(files)
    }

    void canGetSingleFile() {
        def file = new File("f1")
        def collection = new TestFileCollection(file)

        expect:
        collection.getSingleFile().is(file)
    }

    void failsToGetSingleFileWhenCollectionContainsMultipleFiles() {
        File file1 = new File("f1")
        File file2 = new File("f2")
        TestFileCollection collection = new TestFileCollection(file1, file2)

        expect:
        try {
            collection.getSingleFile()
            fail()
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), equalTo("Expected collection-display-name to contain exactly one file, however, it contains more than one file."))
        }
    }

    void failsToGetSingleFileWhenCollectionIsEmpty() {
        TestFileCollection collection = new TestFileCollection()

        expect:
        try {
            collection.getSingleFile()
            fail()
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), equalTo("Expected collection-display-name to contain exactly one file, however, it contains no files."))
        }
    }

    void containsFile() {
        def file1 = new File("f1")
        def collection = new TestFileCollection(file1)

        expect:
        collection.contains(file1)
        !collection.contains(new File("f2"))
    }

    void canGetFilesAsAPath() {
        File file1 = new File("f1")
        File file2 = new File("f2")
        TestFileCollection collection = new TestFileCollection(file1, file2)

        expect:
        assertThat(collection.getAsPath(), equalTo(file1.path + File.pathSeparator + file2.path))
    }

    void canAddCollectionsTogether() {
        File file1 = new File("f1")
        File file2 = new File("f2")
        File file3 = new File("f3")
        TestFileCollection collection1 = new TestFileCollection(file1, file2)
        TestFileCollection collection2 = new TestFileCollection(file2, file3)

        when:
        FileCollection sum = collection1.plus(collection2)

        then:
        assertThat(sum, instanceOf(UnionFileCollection.class))
        assertThat(sum.getFiles(), equalTo(toLinkedSet(file1, file2, file3)))
    }

    def "can add collections using + operator"() {
        File file1 = new File("f1")
        File file2 = new File("f2")
        File file3 = new File("f3")
        TestFileCollection collection1 = new TestFileCollection(file1, file2)
        TestFileCollection collection2 = new TestFileCollection(file2, file3)

        when:
        FileCollection sum = collection1 + collection2

        then:
        sum instanceof UnionFileCollection
        sum.getFiles() == toLinkedSet(file1, file2, file3)
    }

    def "can add a list of collections"() {
        File file1 = new File("f1")
        File file2 = new File("f2")
        File file3 = new File("f3")
        TestFileCollection collection1 = new TestFileCollection(file1, file2)
        TestFileCollection collection2 = new TestFileCollection(file2, file3)

        when:
        FileCollection sum = collection1.plus(collection2)

        then:
        sum instanceof UnionFileCollection
        sum.getFiles() == toLinkedSet(file1, file2, file3)
    }

    def "can add list of collections using + operator"() {
        File file1 = new File("f1")
        File file2 = new File("f2")
        File file3 = new File("f3")
        TestFileCollection collection1 = new TestFileCollection(file1, file2)
        TestFileCollection collection2 = new TestFileCollection(file2, file3)

        when:
        FileCollection sum = collection1 + collection2

        then:
        sum instanceof UnionFileCollection
        sum.getFiles() == toLinkedSet(file1, file2, file3)
    }

    void canSubtractCollections() {
        File file1 = new File("f1")
        File file2 = new File("f2")
        File file3 = new File("f3")
        TestFileCollection collection1 = new TestFileCollection(file1, file2)
        TestFileCollection collection2 = new TestFileCollection(file2, file3)

        when:
        FileCollection difference = collection1.minus(collection2)

        then:
        assertThat(difference.getFiles(), equalTo(toLinkedSet(file1)))
    }

    def "can subtract a collection using - operator"() {
        File file1 = new File("f1")
        File file2 = new File("f2")
        File file3 = new File("f3")
        TestFileCollection collection1 = new TestFileCollection(file1, file2)
        TestFileCollection collection2 = new TestFileCollection(file2, file3)

        when:
        FileCollection difference = collection1 - collection2

        then:
        difference.files == toLinkedSet(file1)
    }

    def "can subtract a list of collection"() {
        File file1 = new File("f1")
        File file2 = new File("f2")
        File file3 = new File("f3")
        TestFileCollection collection1 = new TestFileCollection(file1, file2)
        TestFileCollection collection2 = new TestFileCollection(file2, file3)

        when:
        FileCollection difference = collection1.minus(collection2)

        then:
        difference.files == toLinkedSet(file1)
    }

    def "can subtract a list of collections using - operator"() {
        File file1 = new File("f1")
        File file2 = new File("f2")
        File file3 = new File("f3")
        TestFileCollection collection1 = new TestFileCollection(file1, file2)
        TestFileCollection collection2 = new TestFileCollection(file2, file3)

        when:
        FileCollection difference = collection1 - collection2

        then:
        difference.files == toLinkedSet(file1)
    }

    void canConvertToCollectionTypes() {
        File file = new File("f1")
        TestFileCollection collection = new TestFileCollection(file)

        expect:
        collection as Collection == toList(file)
        collection as Set == toLinkedSet(file)
        collection as List == toList(file)
    }

    void toFileTreeReturnsSingletonTreeForEachFileInCollection() {
        File file = testDir.createFile("f1")
        File file2 = testDir.createFile("f2")

        TestFileCollection collection = new TestFileCollection(file, file2)
        FileTree tree = collection.getAsFileTree()

        expect:
        FileVisitorUtil.assertVisits(tree, GUtil.map("f1", file, "f2", file2))
    }

    void canFilterContentsOfCollectionUsingSpec() {
        File file1 = new File("f1")
        File file2 = new File("f2")

        TestFileCollection collection = new TestFileCollection(file1, file2)
        FileCollection filtered = collection.filter(new Spec<File>() {
            boolean isSatisfiedBy(File element) {
                return element.getName().equals("f1")
            }
        })

        expect:
        assertThat(filtered.getFiles(), equalTo(toSet(file1)))
    }

    void canFilterContentsOfCollectionUsingClosure() {
        File file1 = new File("f1")
        File file2 = new File("f2")

        TestFileCollection collection = new TestFileCollection(file1, file2)
        FileCollection filtered = collection.filter(TestUtil.toClosure("{f -> f.name == 'f1'}"))

        expect:
        assertThat(filtered.getFiles(), equalTo(toSet(file1)))
    }

    void filteredCollectionIsLive() {
        File file1 = new File("f1")
        File file2 = new File("f2")
        File file3 = new File("dir/f1")
        TestFileCollection collection = new TestFileCollection(file1, file2)

        when:
        FileCollection filtered = collection.filter(TestUtil.toClosure("{f -> f.name == 'f1'}"))

        then:
        assertThat(filtered.getFiles(), equalTo(toSet(file1)))

        when:
        collection.files.add(file3)

        then:
        assertThat(filtered.getFiles(), equalTo(toSet(file1, file3)))
    }

    void hasNoDependencies() {
        expect:
        assertThat(new TestFileCollection().getBuildDependencies().getDependencies(null), isEmpty())
    }

    void fileTreeHasSameDependenciesAsThis() {
        TestFileCollectionWithDependency collection = new TestFileCollectionWithDependency()
        collection.files.add(new File("f1"))

        expect:
        assertHasSameDependencies(collection.getAsFileTree())
        assertHasSameDependencies(collection.getAsFileTree().matching(TestUtil.TEST_CLOSURE))
    }

    void filteredCollectionHasSameDependenciesAsThis() {
        TestFileCollectionWithDependency collection = new TestFileCollectionWithDependency()

        expect:
        assertHasSameDependencies(collection.filter(TestUtil.toClosure("{true}")))
    }

    void elementsProviderHasNoDependenciesWhenThisHasNoDependencies() {
        def collection = new TestFileCollection()
        def context = Mock(TaskDependencyResolveContext)

        ProviderInternal elements = collection.elements

        when:
        def visited = elements.maybeVisitBuildDependencies(context)

        then:
        visited
        1 * context.add(collection)
        0 * context._

        expect:
        !elements.valueProducedByTask
    }

    void elementsProviderHasSameDependenciesAsThis() {
        def collection = new TestFileCollectionWithDependency()
        def context = Mock(TaskDependencyResolveContext)
        def task = Mock(TaskInternal)
        _ * dependency.visitDependencies(_) >> { TaskDependencyResolveContext c -> c.add(task) }

        ProviderInternal elements = collection.elements

        when:
        def visited = elements.maybeVisitBuildDependencies(context)

        then:
        visited
        1 * context.add(collection)
        0 * context._

        expect:
        elements.valueProducedByTask
    }

    void visitsSelfAsLeafCollection() {
        def collection = new TestFileCollection()
        def visitor = Mock(FileCollectionStructureVisitor)

        when:
        collection.visitStructure(visitor)

        then:
        1 * visitor.prepareForVisit(FileCollectionInternal.OTHER) >> FileCollectionStructureVisitor.VisitType.Visit
        1 * visitor.visitCollection(FileCollectionInternal.OTHER, collection)
        0 * visitor._
    }

    void doesNotVisitSelfWhenVisitorIsNotInterested() {
        def collection = new TestFileCollection()
        def visitor = Mock(FileCollectionStructureVisitor)

        when:
        collection.visitStructure(visitor)

        then:
        1 * visitor.prepareForVisit(FileCollectionInternal.OTHER) >> FileCollectionStructureVisitor.VisitType.NoContents
        0 * visitor._
    }

    private void assertHasSameDependencies(FileCollection tree) {
        final Task task = Mock(Task.class)
        final Task depTask = Mock(Task.class)
        1 * dependency.visitDependencies(_) >> { TaskDependencyResolveContext c -> c.add(depTask) }
        0 * dependency._

        assertThat(tree.getBuildDependencies().getDependencies(task), equalTo((Object) toSet(depTask)))
    }

    static class TestFileCollection extends AbstractFileCollection {
        Set<File> files = new LinkedHashSet<File>()

        TestFileCollection(File... files) {
            this.files.addAll(Arrays.asList(files))
        }

        @Override
        String getDisplayName() {
            return "collection-display-name"
        }

        @Override
        Set<File> getFiles() {
            return files
        }
    }

    private class TestFileCollectionWithDependency extends TestFileCollection {
        TestFileCollectionWithDependency(File... files) {
            super(files)
        }

        @Override
        void visitDependencies(TaskDependencyResolveContext context) {
            context.add(dependency)
        }
    }
}
