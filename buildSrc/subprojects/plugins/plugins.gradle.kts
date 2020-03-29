dependencies {
    implementation(project(":binaryCompatibility"))
    implementation(project(":build"))
    implementation(project(":configuration"))
    implementation(project(":kotlinDsl"))
    implementation(project(":performance"))
    implementation(project(":versioning"))

    implementation("org.jsoup:jsoup:1.11.3")
    implementation("com.google.guava:guava")
    implementation("org.ow2.asm:asm:7.1")
    implementation("org.ow2.asm:asm-commons:7.1")
    implementation("com.google.code.gson:gson:2.7")
    implementation("org.gradle:test-retry-gradle-plugin:1.1.3")

    testImplementation("org.junit.jupiter:junit-jupiter-params:5.6.0")
    testImplementation("junit:junit:4.12")
    testImplementation("io.mockk:mockk:1.8.13")
}

gradlePlugin {
    plugins {
        register("buildTypes") {
            id = "gradlebuild.build-types"
            implementationClass = "org.gradle.plugins.buildtypes.BuildTypesPlugin"
        }
        register("buildVersion") {
            id = "gradlebuild.build-version"
            implementationClass = "org.gradle.gradlebuild.versioning.BuildVersionPlugin"
        }
        register("performanceTest") {
            id = "gradlebuild.performance-test"
            implementationClass = "org.gradle.plugins.performance.PerformanceTestPlugin"
        }
        register("unitTestAndCompile") {
            id = "gradlebuild.unittest-and-compile"
            implementationClass = "org.gradle.gradlebuild.unittestandcompile.UnitTestAndCompilePlugin"
        }
        register("install") {
            id = "gradlebuild.install"
            implementationClass = "org.gradle.plugins.install.InstallPlugin"
        }
        register("generateSubprojectsInfo") {
            id = "gradlebuild.generate-subprojects-info"
            implementationClass = "org.gradle.build.GenerateSubprojectsInfoPlugin"
        }
    }
}

tasks.withType<Test> {
    environment("BUILD_BRANCH", "myBranch")
}
