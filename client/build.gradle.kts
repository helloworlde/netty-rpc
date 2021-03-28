plugins {
    java
    idea
    id("io.freefair.lombok") version "5.3.0"
}

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}

var nettyVersion = "4.1.59.Final"
val slf4jVersion = "1.7.25"
val jacksonVersion = "2.12.2"
val consulVersion = "1.5.1"

dependencies {
    implementation(project(":core"))
    implementation("io.netty:netty-all:${nettyVersion}")

    implementation("org.slf4j:slf4j-api:${slf4jVersion}")
    implementation("org.slf4j:slf4j-simple:${slf4jVersion}")
    implementation("com.orbitz.consul:consul-client:${consulVersion}")

    implementation("com.fasterxml.jackson.core:jackson-core:${jacksonVersion}")
    implementation("com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}")
    implementation("com.fasterxml.jackson.core:jackson-annotations:${jacksonVersion}")

}
