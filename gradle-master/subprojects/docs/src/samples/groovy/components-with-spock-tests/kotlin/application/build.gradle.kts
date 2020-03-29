plugins {
    groovy
    application
}

dependencies {
    implementation(project(":list"))
    implementation(project(":utilities"))
    implementation("org.codehaus.groovy:groovy-all:2.5.7")
    testImplementation("org.spockframework:spock-core:1.3-groovy-2.5")
}

application {
    mainClassName = "org.gradle.sample.Main"
}