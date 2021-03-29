plugins {
    java
    idea
    id("io.freefair.lombok") version "5.3.0"
    `maven-publish`
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "application")
    apply(plugin = "maven-publish")
    apply(plugin = "io.freefair.lombok")

    group = "io.github.helloworlde"
    version = "0.0.1-SNAPSHOT"

    var nettyVersion = "4.1.59.Final"
    val slf4jVersion = "1.7.25"
    val jacksonVersion = "2.12.2"
    val consulVersion = "1.5.1"

    repositories {
        mavenLocal()
        mavenCentral()
        maven {
            setUrl("https://maven.aliyun.com/repository/apache-snapshots")
        }
        maven {
            setUrl("https://maven.aliyun.com/repository/public")
        }
        jcenter()
    }

    dependencies {
        implementation("io.netty:netty-all:${nettyVersion}")

        implementation("org.slf4j:slf4j-api:${slf4jVersion}")
        implementation("org.slf4j:slf4j-simple:${slf4jVersion}")
        implementation("com.orbitz.consul:consul-client:${consulVersion}")

        implementation("com.fasterxml.jackson.core:jackson-core:${jacksonVersion}")
        implementation("com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}")
        implementation("com.fasterxml.jackson.core:jackson-annotations:${jacksonVersion}")
    }

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    java {
        withJavadocJar()
        withSourcesJar()
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
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
}