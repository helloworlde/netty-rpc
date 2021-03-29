plugins {
    java
    idea
    id("io.freefair.lombok") version "5.3.0"
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "application")
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
}