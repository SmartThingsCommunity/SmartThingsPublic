configurations.create("runtime")

// tag::copy-dependencies[]
tasks.register<Sync>("libs") {
    from(configurations["runtime"])
    into("$buildDir/libs")
}
// end::copy-dependencies[]
