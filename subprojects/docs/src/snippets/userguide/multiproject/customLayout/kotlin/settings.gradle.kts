include ("projectA", "projectB")

// tag::lookup-project[]
println(rootProject.name)
println(project(":projectA").name)
// end::lookup-project[]

// tag::change-project[]
rootProject.name = "main"
project(":projectA").projectDir = File(settingsDir, "../my-project-a")
project(":projectA").buildFileName = "projectA.gradle"
// end::change-project[]
