repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.12")
}

// tag::producer-project-dependency[]
dependencies {
    runtimeOnly(project(":producer"))
}
// end::producer-project-dependency[]
