// tag::apply-plugin[]
plugins {
    `java-library`
}
// end::apply-plugin[]

// tag::import-build[]
ant.importBuild("build.xml") { oldTargetName ->
    if (oldTargetName == "build") "ant_build" else oldTargetName  // <1>
}
// end::import-build[]

// tag::source-sets[]
sourceSets {
    main {
        java.setSrcDirs(listOf(ant.properties["src.dir"]))
        java.outputDir = file(ant.properties["classes.dir"] ?: "$buildDir/classes")
    }
}
// end::source-sets[]

// tag::task-dependencies[]
tasks {
    compileJava {
        dependsOn("prepare")  // <1>
    }
    named("package") {
        setDependsOn(listOf(compileJava))  // <2>
    }
    assemble {
        setDependsOn(listOf("package"))  // <3>
    }
}
// end::task-dependencies[]
