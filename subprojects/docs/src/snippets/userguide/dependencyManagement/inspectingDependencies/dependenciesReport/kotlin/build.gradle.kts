// tag::dependency-declaration[]
repositories {
    jcenter()
}

configurations {
    create("scm")
}

dependencies {
    "scm"("org.eclipse.jgit:org.eclipse.jgit:4.9.2.201712150930-r")
}
// end::dependency-declaration[]
