rootProject.name = "app"

includeBuild("../anonymous-library") {
    dependencySubstitution {
        substitute(module("org.sample:number-utils")).with(project(":"))
    }
}
