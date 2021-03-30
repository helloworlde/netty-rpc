var nettyVersion = "4.1.59.Final"
val slf4jVersion = "1.7.25"
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
    implementation(project(":client"))
    implementation(project(":server"))

//
//    implementation("io.github.helloworlde:netty-rpc-core:${nettyRpcVersion}")
//    implementation("io.github.helloworlde:netty-rpc-client:${nettyRpcVersion}")
//    implementation("io.github.helloworlde:netty-rpc-server:${nettyRpcVersion}")

    implementation("org.slf4j:slf4j-api:${slf4jVersion}")
    implementation("org.slf4j:slf4j-simple:${slf4jVersion}")
}
