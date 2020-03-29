plugins {
    scala
}

repositories {
    mavenCentral()
}

// tag::compiler-plugin[]
dependencies {
    implementation("org.scala-lang:scala-library:2.13.1")
    scalaCompilerPlugins("org.typelevel:kind-projector_2.13.1:0.11.0")
}
// end::compiler-plugin[]
