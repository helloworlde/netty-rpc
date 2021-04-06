plugins {
    id("org.springframework.boot") version "2.4.4"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
}


val nettyRpcVersion = "0.0.1-SNAPSHOT"

repositories {
    mavenLocal()
    maven {
        setUrl("https://maven.pkg.github.com/helloworlde/netty-rpc")
    }
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
    implementation(project(":example:sdk"))
    implementation(project(":spring-boot-starter-server"))
    implementation(project(":spring-boot-starter-opentelemetry"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
