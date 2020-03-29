// tag::dependency-reason[]
plugins {
    `java-library`
}

repositories {
    jcenter()
}

dependencies {
    implementation("org.ow2.asm:asm:7.1") {
        because("we require a JDK 9 compatible bytecode generator")
    }
}
// end::dependency-reason[]
