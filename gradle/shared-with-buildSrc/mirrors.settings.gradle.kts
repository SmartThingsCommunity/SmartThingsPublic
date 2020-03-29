/*
 * Copyright 2018 the original author or authors.
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
import org.gradle.api.internal.artifacts.BaseRepositoryFactory.PLUGIN_PORTAL_OVERRIDE_URL_PROPERTY
import org.gradle.api.internal.GradleInternal
import org.gradle.internal.nativeintegration.network.HostnameLookup

val originalUrls: Map<String, String> = mapOf(
    "jcenter" to "https://jcenter.bintray.com/",
    "mavencentral" to "https://repo.maven.apache.org/maven2/",
    "google" to "https://dl.google.com/dl/android/maven2/",
    "gradle" to "https://repo.gradle.org/gradle/repo",
    "gradleplugins" to "https://plugins.gradle.org/m2",
    "gradlejavascript" to "https://repo.gradle.org/gradle/javascript-public",
    "gradle-libs" to "https://repo.gradle.org/gradle/libs",
    "gradle-releases" to "https://repo.gradle.org/gradle/libs-releases",
    "gradle-snapshots" to "https://repo.gradle.org/gradle/libs-snapshots",
    "gradle-enterprise-plugin-rc" to "https://repo.gradle.org/gradle/enterprise-libs-release-candidates-local",
    "kotlinx" to "https://kotlin.bintray.com/kotlinx/",
    "kotlineap" to "https://dl.bintray.com/kotlin/kotlin-eap/"
)

val mirrorUrls: Map<String, String> =
    System.getenv("REPO_MIRROR_URLS")?.ifBlank { null }?.split(',')?.associate { nameToUrl ->
        val (name, url) = nameToUrl.split(':', limit = 2)
        name to url
    } ?: emptyMap()


fun isEc2Agent() = (gradle as GradleInternal).services.get(HostnameLookup::class.java).hostname.startsWith("ip-")

fun isMacAgent() = System.getProperty("os.name").toLowerCase().contains("mac")

fun ignoreMirrors() = System.getenv("IGNORE_MIRROR")?.toBoolean() ?: false

fun withMirrors(handler: RepositoryHandler) {
    if ("CI" !in System.getenv() || isEc2Agent()) {
        return
    }
    handler.all {
        if (this is MavenArtifactRepository) {
            originalUrls.forEach { name, originalUrl ->
                if (normalizeUrl(originalUrl) == normalizeUrl(this.url.toString()) && mirrorUrls.containsKey(name)) {
                    this.setUrl(mirrorUrls.get(name))
                }
            }
        }
    }
}

fun normalizeUrl(url: String): String {
    val result = url.replace("https://", "http://")
    return if (result.endsWith("/")) result else "$result/"
}

if (System.getProperty(PLUGIN_PORTAL_OVERRIDE_URL_PROPERTY) == null && !isEc2Agent() && !isMacAgent() && !ignoreMirrors()) {
    // https://github.com/gradle/gradle-private/issues/2725
    // https://github.com/gradle/gradle-private/issues/2951
    System.setProperty(PLUGIN_PORTAL_OVERRIDE_URL_PROPERTY, "https://dev12.gradle.org/artifactory/gradle-plugins/")
    gradle.buildFinished {
        System.clearProperty(PLUGIN_PORTAL_OVERRIDE_URL_PROPERTY)
    }
}

gradle.allprojects {
    buildscript.configurations["classpath"].incoming.beforeResolve {
        withMirrors(buildscript.repositories)
    }
    afterEvaluate {
        withMirrors(repositories)
    }
}

gradle.settingsEvaluated {
    withMirrors(settings.pluginManagement.repositories)
}
