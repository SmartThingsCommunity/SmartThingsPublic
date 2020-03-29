/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.api.tasks

import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import spock.lang.Unroll

import static org.gradle.util.GUtil.loadProperties

class WritePropertiesIntegrationTest extends AbstractIntegrationSpec {
    def "empty properties are written properly"() {
        given:
        buildFile << """
            task props(type: WriteProperties) {
                outputFile = file("output.properties")
            }
        """

        when:
        succeeds "props"
        then:
        file("output.properties").text == ""
    }

    def "empty properties with comment are written properly"() {
        given:
        buildFile << """
            task props(type: WriteProperties) {
                comment = "Line comment"
                outputFile = file("output.properties")
            }
        """

        when:
        succeeds "props"
        then:
        file("output.properties").text == normalize("""
            #Line comment
            """)
    }

    def "simple properties are written sorted alphabetically"() {
        given:
        buildFile << """
            task props(type: WriteProperties) {
                properties = [one: "1", two: "2", three: "three"]
                comment = "Line comment"
                outputFile = file("output.properties")
            }
        """

        when:
        succeeds "props"
        then:
        file("output.properties").text == normalize("""
            #Line comment
            one=1
            three=three
            two=2
            """)
    }

    @Unroll
    def "unicode characters are escaped when #description"() {
        given:
        buildFile << """
            task props(type: WriteProperties) {
                properties = [név: "Rezső"]
                comment = "Eső leső"
                $encoding
                outputFile = file("output.properties")
            }
        """

        when:
        succeeds "props"
        then:
        file("output.properties").text == normalize("""
            #Es\\u0151 les\\u0151
            n\\u00E9v=Rezs\\u0151
            """)

        where:
        encoding              | description
        null                  | "no encoding is set"
        "encoding = 'latin1'" | "latin1 encoding is used"
    }

    def "unicode characters are not escaped when encoding utf-8 encoding is used"() {
        given:
        buildFile << """
            task props(type: WriteProperties) {
                properties = [név: "Rezső"]
                encoding = "utf-8"
                comment = "Eső leső"
                outputFile = file("output.properties")
            }
        """

        when:
        succeeds "props"
        then:
        // Note Properties always escape Unicode in comments for some reason
        file("output.properties").getText("utf-8") == normalize("""
            #Es\\u0151 les\\u0151
            név=Rezső
            """)
    }

    def "specified line separator is used"() {
        given:
        buildFile << """
            task props(type: WriteProperties) {
                properties = [one: "1", two: "2", three: "three"]
                comment = "Line comment"
                lineSeparator = "EOL"
                outputFile = file("output.properties")
            }
        """

        when:
        succeeds "props"
        then:
        file("output.properties").text == normalize("""
            #Line comment
            one=1
            three=three
            two=2
            """).split("\n", -1).join("EOL")
    }

    @Unroll
    def "value cannot be '#propValue'"() {
        given:
        buildFile << """
            task props(type: WriteProperties) {
                property "someProp", $propValue
                outputFile = file("output.properties")
            }
        """
        when:
        fails "props"
        then:
        failure.assertHasCause("Property 'someProp' is not allowed to have a null value.")
        where:
        propValue << [ "null", "{ null }" ]
    }

    def "value can be provided"() {
        given:
        buildFile << """
            task props(type: WriteProperties) {
                property "provided", provider { "42" }
                outputFile = file("output.properties")
            }
        """
        when:
        succeeds "props"
        then:
        loadProperties(file('output.properties'))['provided'] == '42'
    }

    private static String normalize(String text) {
        return text.stripIndent().trim() + '\n'
    }
}
