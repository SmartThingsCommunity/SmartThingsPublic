/*
 * Copyright 2013 the original author or authors.
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

package org.gradle.api.publish.ivy.internal.artifact

import com.google.common.collect.ImmutableSet
import org.gradle.api.Task
import org.gradle.api.internal.artifacts.PublishArtifactInternal
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.internal.tasks.DefaultTaskDependency
import org.gradle.api.publish.ivy.IvyArtifact
import org.gradle.api.publish.ivy.internal.publisher.IvyPublicationIdentity
import org.gradle.api.tasks.bundling.Jar
import org.gradle.internal.reflect.Instantiator
import org.gradle.internal.typeconversion.NotationParser
import org.gradle.test.fixtures.AbstractProjectBuilderSpec
import org.gradle.util.TestUtil

public class IvyArtifactNotationParserFactoryTest extends AbstractProjectBuilderSpec {
    Instantiator instantiator = TestUtil.instantiatorFactory().decorateLenient()
    def fileNotationParser = Mock(NotationParser)
    def task = Mock(Task)
    def taskDependency = new DefaultTaskDependency(null, ImmutableSet.of(task))
    def publishArtifact = Stub(PublishArtifactInternal) {
        getName() >> 'name'
        getExtension() >> 'extension'
        getType() >> 'type'
        getFile() >> new File('foo')
        getBuildDependencies() >> taskDependency
    }
    def dependencies = Collections.singleton(Mock(Task))

    NotationParser<Object, IvyArtifact> parser

    def "setup"() {
        def fileResolver = Stub(FileResolver) {
            asNotationParser() >> fileNotationParser
        }
        def identity = Stub(IvyPublicationIdentity) {
            getModule() >> 'pub-name'
        }
        parser = new IvyArtifactNotationParserFactory(instantiator, fileResolver, identity).create()
    }

    def "directly returns IvyArtifact input"() {
        when:
        def ivyArtifact = Mock(IvyArtifact)

        then:
        parser.parseNotation(ivyArtifact) == ivyArtifact
    }

    def "creates IvyArtifact for PublishArtifact"() {
        when:
        def ivyArtifact = parser.parseNotation(publishArtifact)

        then:
        ivyArtifact.name == 'pub-name'
        ivyArtifact.extension == publishArtifact.extension
        ivyArtifact.type == publishArtifact.type
        ivyArtifact.file == publishArtifact.file
        ivyArtifact.buildDependencies.getDependencies(task) == dependencies
    }

    def "creates IvyArtifact for source map notation"() {
        when:
        IvyArtifact ivyArtifact = parser.parseNotation(source: publishArtifact)

        then:
        ivyArtifact.name == 'pub-name'
        ivyArtifact.extension == publishArtifact.extension
        ivyArtifact.type == publishArtifact.type
        ivyArtifact.file == publishArtifact.file
        ivyArtifact.buildDependencies.getDependencies(task) == dependencies
    }

    def "creates IvyArtifact for source map notation with file"() {
        given:
        File file = new File('some-file-1.2.zip')

        when:
        def ivyArtifact = parser.parseNotation(source: 'some-file')

        then:
        fileNotationParser.parseNotation('some-file') >> file

        and:
        ivyArtifact.name == 'pub-name'
        ivyArtifact.extension == "zip"
        ivyArtifact.type == "zip"
        ivyArtifact.file == file
    }


    def "creates and configures IvyArtifact for source map notation"() {
        when:
        IvyArtifact ivyArtifact = parser.parseNotation(source: publishArtifact, name: 'the-name', extension: "the-ext", type: "the-type")

        then:
        ivyArtifact.file == publishArtifact.file
        ivyArtifact.name == "the-name"
        ivyArtifact.extension == "the-ext"
        ivyArtifact.type == "the-type"
        ivyArtifact.buildDependencies.getDependencies(task) == dependencies
    }

    def "creates IvyArtifact for ArchivePublishArtifact"() {
        when:
        def rootProject = TestUtil.createRootProject(temporaryFolder.testDirectory)
        def archive = rootProject.task('foo', type: Jar, {})
        archive.setBaseName("base-name")
        archive.setExtension('extension')
        archive.setDestinationDir(rootProject.buildDir)

        IvyArtifact ivyArtifact = parser.parseNotation(archive)

        then:
        ivyArtifact.name == 'pub-name'
        ivyArtifact.extension == "extension"
        ivyArtifact.classifier == null
        ivyArtifact.file == archive.archivePath
        ivyArtifact.buildDependencies.getDependencies(null) == [archive] as Set
    }

    def "creates IvyArtifact for file notation"() {
        given:
        File file = new File(fileName)

        when:
        IvyArtifact ivyArtifact = parser.parseNotation('some-file')

        then:
        fileNotationParser.parseNotation('some-file') >> file

        and:
        ivyArtifact.name == 'pub-name'
        ivyArtifact.extension == extension
        ivyArtifact.type == type
        ivyArtifact.classifier == null
        ivyArtifact.file == file

        where:
        fileName                       | extension | type
        "some-file-1.2.zip"            | "zip"     | "zip"
        "some-file"                    | ""        | ""
        "some-file-1.2-classifier.zip" | "zip"     | "zip"
    }

}
