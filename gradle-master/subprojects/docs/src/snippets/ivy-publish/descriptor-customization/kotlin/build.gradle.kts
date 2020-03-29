plugins {
    `ivy-publish`
}

group = "org.gradle.sample"
version = "1.0"

publishing {
// tag::customize-descriptor[]
// tag::versions-resolved[]
    publications {
        create<IvyPublication>("ivyCustom") {
            // end::versions-resolved[]
            descriptor {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
                author {
                    name.set("Jane Doe")
                    url.set("http://example.com/users/jane")
                }
                description {
                    text.set("A concise description of my library")
                    homepage.set("http://www.example.com/library")
                }
            }
// tag::versions-resolved[]
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
        }
    }
// end::versions-resolved[]
// end::customize-descriptor[]
    repositories {
        ivy {
            // change to point to your repo, e.g. http://my.org/repo
            url = uri("$buildDir/repo")
        }
    }
}

// tag::generate[]
tasks.named<GenerateIvyDescriptor>("generateDescriptorFileForIvyCustomPublication") {
    destination = file("$buildDir/generated-ivy.xml")
}
// end::generate[]
