plugins {
    scala
}

repositories {
    mavenCentral()
}

// tag::scala-test-dependency[]
dependencies {
    testImplementation("org.scala-lang:scala-library:2.11.1")
}
// end::scala-test-dependency[]
