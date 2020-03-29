/*
 * Copyright 2015 the original author or authors.
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
import org.gradle.api.internal.file.collections.FileCollectionResolveContext
import org.gradle.api.internal.file.collections.FileSystemMirroringFileTree
import org.gradle.api.internal.tasks.TaskDependencyContainer
import org.gradle.api.internal.tasks.TaskDependencyResolveContext

class CompositeFileCollectionSpec extends FileCollectionSpec {
    @Override
    AbstractFileCollection containing(File... files) {
        return new TestCollection() {
            @Override
            void visitContents(FileCollectionResolveContext context) {
                context.addAll(files as List)
            }
        }
    }

    def "visits contents on each query"() {
        def visited = 0;
        def collection = new TestCollection() {
            @Override
            void visitContents(FileCollectionResolveContext context) {
                visited++
                context.add(new File("foo").absoluteFile)
            }
        }

        when:
        def sourceCollections = collection.sourceCollections

        then:
        sourceCollections.size() == 1
        visited == 1

        when:
        def files = collection.files

        then:
        files.size() == 1
        visited == 2
    }

    def "visits contents when task dependencies are queried"() {
        def visited = 0;
        def task = Stub(Task)
        def dependency = Stub(Task)
        def child = collectionDependsOn(dependency)
        def collection = new TestCollection() {
            @Override
            void visitContents(FileCollectionResolveContext context) {
                visited++
                context.add(child)
            }
        }

        when:
        def dependencies = collection.buildDependencies

        then:
        visited == 0

        when:
        def deps = dependencies.getDependencies(task)

        then:
        visited == 1
        deps as List == [dependency]

        when:
        deps = dependencies.getDependencies(Stub(Task))

        then:
        visited == 2
        deps as List == [dependency]
    }

    def "subtype can avoid creating content when task dependencies are queried"() {
        def visited = 0;
        def task = Stub(Task)
        def dependency = Stub(Task)
        def collection = new TestCollection() {
            @Override
            void visitDependencies(TaskDependencyResolveContext context) {
                visited++
                context.add(dependency)
            }
        }

        when:
        def dependencies = collection.buildDependencies

        then:
        visited == 0

        when:
        def deps = dependencies.getDependencies(task)

        then:
        visited == 1
        deps as List == [dependency]

        when:
        deps = dependencies.getDependencies(Stub(Task))

        then:
        visited == 2
        deps as List == [dependency]
    }

    def "collection dependencies are live"() {
        def task = Stub(Task)
        def dependency1 = Stub(Task)
        def dependency2 = Stub(Task)
        def dependencySource = Mock(TaskDependencyContainer)

        def collection = new TestCollection() {
            @Override
            void visitDependencies(TaskDependencyResolveContext context) {
                context.add(dependencySource)
            }
        }

        given:
        1 * dependencySource.visitDependencies(_) >> { TaskDependencyResolveContext context -> context.add(dependency1) }
        1 * dependencySource.visitDependencies(_) >> { TaskDependencyResolveContext context -> context.add(dependency2) }
        1 * dependencySource.visitDependencies(_) >> { TaskDependencyResolveContext context -> context.add(dependency1); context.add(dependency2) }
        def dependencies = collection.buildDependencies

        expect:
        dependencies.getDependencies(task) as List == [dependency1]
        dependencies.getDependencies(task) as List == [dependency2]
        dependencies.getDependencies(task) as List == [dependency1, dependency2]
    }

    def "filtered collection has same live dependencies as original collection"() {
        def task = Stub(Task)
        def dependency1 = Stub(Task)
        def dependency2 = Stub(Task)
        def dependencySource = Mock(TaskDependencyContainer)

        def collection = new TestCollection() {
            @Override
            void visitDependencies(TaskDependencyResolveContext context) {
                context.add(dependencySource)
            }
        }

        given:
        1 * dependencySource.visitDependencies(_) >> { TaskDependencyResolveContext context -> context.add(dependency1) }
        1 * dependencySource.visitDependencies(_) >> { TaskDependencyResolveContext context -> context.add(dependency2) }

        def dependencies = collection.filter { false }.buildDependencies

        expect:
        dependencies.getDependencies(task) as List == [dependency1]
        dependencies.getDependencies(task) as List == [dependency2]
    }

    def "FileTree view has same live dependencies as original collection"() {
        def task = Stub(Task)
        def dependency1 = Stub(Task)
        def dependency2 = Stub(Task)
        def dependencySource = Mock(TaskDependencyContainer)

        def collection = new TestCollection() {
            @Override
            void visitDependencies(TaskDependencyResolveContext context) {
                context.add(dependencySource)
            }
        }

        given:
        1 * dependencySource.visitDependencies(_) >> { TaskDependencyResolveContext context -> context.add(dependency1) }
        1 * dependencySource.visitDependencies(_) >> { TaskDependencyResolveContext context -> context.add(dependency2) }

        def dependencies = collection.asFileTree.buildDependencies

        expect:
        dependencies.getDependencies(task) as List == [dependency1]
        dependencies.getDependencies(task) as List == [dependency2]
    }

    def "visits content of tree of collections"() {
        def child1 = new TestCollection() {
            @Override
            void visitContents(FileCollectionResolveContext context) {
                context.add(new File("1").absoluteFile)
            }
        }
        def child2 = new TestCollection() {
            @Override
            void visitContents(FileCollectionResolveContext context) {
                context.add(child1)
            }
        }
        def child3 = new TestCollection() {
            @Override
            void visitContents(FileCollectionResolveContext context) {
                context.add(new File("2").absoluteFile)
            }
        }
        def collection = new TestCollection() {
            @Override
            void visitContents(FileCollectionResolveContext context) {
                context.add(child2)
                context.add(child3)
            }
        }

        expect:
        collection.files.size() == 2
    }

    def "visits content of tree of collections when dependencies are queried"() {
        def task = Stub(Task)
        def dependency1 = Stub(Task)
        def dependency2 = Stub(Task)
        def child1 = new TestCollection() {
            @Override
            void visitContents(FileCollectionResolveContext context) {
                context.add(collectionDependsOn(dependency1))
            }
        }
        def child2 = new TestCollection() {
            @Override
            void visitContents(FileCollectionResolveContext context) {
                context.add(child1)
            }
        }
        def child3 = new TestCollection() {
            @Override
            void visitContents(FileCollectionResolveContext context) {
                context.add(child2)
                context.add(collectionDependsOn(dependency2))
            }
        }
        def collection = new TestCollection() {
            @Override
            void visitContents(FileCollectionResolveContext context) {
                context.add(child3)
            }
        }

        expect:
        collection.buildDependencies.getDependencies(task) == [dependency1, dependency2] as LinkedHashSet
    }

    def "descendant can avoid visiting content when task dependencies are queried"() {
        def task = Stub(Task)
        def dependency1 = Stub(Task)
        def dependency2 = Stub(Task)
        def child1 = new TestCollection() {
            @Override
            void visitDependencies(TaskDependencyResolveContext context) {
                context.add(dependency1)
            }
        }
        def child2 = new TestCollection() {
            @Override
            void visitContents(FileCollectionResolveContext context) {
                context.add(child1)
            }
        }
        def child3 = new TestCollection() {
            @Override
            void visitContents(FileCollectionResolveContext context) {
                context.add(child2)
                context.add(collectionDependsOn(dependency2))
            }
        }
        def collection = new TestCollection() {
            @Override
            void visitContents(FileCollectionResolveContext context) {
                context.add(child3)
            }
        }

        expect:
        collection.buildDependencies.getDependencies(task) == [dependency1, dependency2] as LinkedHashSet
    }

    def "can visit structure"() {
        def child1 = Stub(FileCollectionInternal)
        def child2 = Stub(FileTreeInternal)
        def child2Source = Stub(FileSystemMirroringFileTree)
        def source = Stub(FileCollectionInternal.Source)

        def tree = new TestCollection() {
            @Override
            void visitContents(FileCollectionResolveContext context) {
                context.add(child1)
                context.add(child2)
            }
        }
        def visitor = Mock(FileCollectionStructureVisitor)

        when:
        tree.visitStructure(visitor)

        then:
        child1.visitStructure(visitor) >> { FileCollectionStructureVisitor v -> v.visitCollection(source, child1) }
        child2.visitStructure(visitor) >> { FileCollectionStructureVisitor v -> v.visitGenericFileTree(child2, child2Source) }
        1 * visitor.visitCollection(source, child1)
        1 * visitor.visitGenericFileTree(child2, child2Source)
        0 * visitor._
    }

    def collectionDependsOn(Task... tasks) {
        def collection = Stub(FileCollectionInternal)
        collection.visitDependencies(_) >> { TaskDependencyResolveContext context ->
            for (t in tasks) {
                context.add(t)
            }
        }
        return collection
    }

    private static abstract class TestCollection extends CompositeFileCollection {
        @Override
        String getDisplayName() {
            return "<display-name>"
        }

        @Override
        void visitContents(FileCollectionResolveContext context) {
            throw new UnsupportedOperationException()
        }
    }
}
