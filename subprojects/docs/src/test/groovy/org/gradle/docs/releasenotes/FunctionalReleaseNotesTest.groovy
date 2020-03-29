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

package org.gradle.docs.releasenotes

import geb.Browser
import geb.Configuration
import geb.spock.GebReportingSpec
import groovy.json.JsonSlurper
import org.gradle.util.GradleVersion
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import spock.lang.Ignore
import spock.lang.IgnoreIf
import spock.lang.Shared

/**
 * These tests actually open the release notes in a browser and test the JS.
 */
@Ignore
// @IgnoreIf({ !FunctionalReleaseNotesTest.canReachServices() })
class FunctionalReleaseNotesTest extends GebReportingSpec {

    static private final String FIXED_ISSUES_URL = "https://services.gradle.org/fixed-issues/${GradleVersion.current().baseVersion.version}"
    static private final String KNOWN_ISSUES_URL = "https://services.gradle.org/known-issues/${GradleVersion.current().baseVersion.version}"

    private String version = GradleVersion.current().baseVersion.version

    static boolean canReachServices() {
        try {
            HttpURLConnection connection = FIXED_ISSUES_URL.toURL().openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connect()
            connection.responseCode == 200
        } catch (IOException ignore) {
            false
        }
    }

    @Shared url = new ReleaseNotesTestContext().renderedFile.toURL().toString()

    def setup() {
        to ReleaseNotesPage
    }

    ReleaseNotesPage getPage() {
        browser.page as ReleaseNotesPage
    }

    @Override
    Browser createBrowser() {
        new Browser(driver: new HtmlUnitDriver(true), new Configuration(reportsDir: new File("build/geb-reports")))
    }

    List<Map> fixedIssues() {
        parseIssues(FIXED_ISSUES_URL)
    }

    List<Map> knownIssues() {
        parseIssues(KNOWN_ISSUES_URL)
    }

    private List<Map<String, String>> parseIssues(String url) {
        def result = new JsonSlurper().parseText(new URL(url).text) as List<Map>
        result.each { json ->
            json.summary = json.summary.replace('\n', '').replace('\t', '').replaceAll('\\s+', ' ').trim()
        }
    }

    def "has fixed issues"() {
        when:
        def fixed = fixedIssues()
        def numFixedIssues = fixed.size()

        then:
        waitFor { page.fixedIssuesParagraph.text() == "$numFixedIssues issues have been fixed in Gradle $version." }
        if (numFixedIssues == 0) {
            return
        }

        waitFor { page.fixedIssuesListItems.size() == numFixedIssues }
        fixed.eachWithIndex { json, i ->
            def issue = page.fixedIssuesListItems[i]
            assert issue.text() == "[$json.key] - ${json.summary}"
            assert issue.find("a").attr("href") == json.link
        }
    }

    def "has known issues"() {
        when:
        def knownIssues = knownIssues()

        then:
        if (knownIssues.size() == 0) {
            waitFor { page.knownIssuesParagraph.text() == "There are no known issues of Gradle ${version} at this time." }
            return
        } else {
            waitFor { page.knownIssuesParagraph.text() == "${knownIssues.size()} issues are known to affect Gradle $version." }
        }

        page.knownIssuesListItems.size() == knownIssues.size()
        knownIssues.eachWithIndex { json, i ->
            def issue = page.knownIssuesListItems[i]
            assert issue.text() == "[$json.key] - ${json.summary}"
            assert issue.find("a").attr("href") == json.link
        }
    }

    def cleanupSpec() {
        browser.quit()
    }
}
