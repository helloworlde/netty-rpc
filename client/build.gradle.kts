plugins {
    `maven-publish`
}
apply(plugin = "maven-publish")

base.archivesBaseName = "netty-rpc-client"

dependencies {
    implementation(project(":core"))
}


publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = tasks.jar.get().archiveBaseName.get()
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                description.set("Netty RPC Core")
                url.set("https://maven.pkg.github.com/helloworlde/netty-rpc")
                properties.set(
                    mapOf(
                        "myProp" to "value",
                        "prop.with.dots" to "anotherValue"
                    )
                )
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("helloworlde")
                        name.set("helloworlde")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/helloworlde/netty-rpc.git")
                    developerConnection.set("scm:git:ssh://github.com/helloworlde/netty-rpc.git")
                    url.set("http://github.com/helloworlde/netty-rpc")
                }
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/helloworlde/netty-rpc")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}