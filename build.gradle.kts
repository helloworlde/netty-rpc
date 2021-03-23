subprojects {
    apply(plugin = "java")
    apply(plugin = "application")


    group = "io.github.helloworlde"
    version = "0.0.1"

    repositories {
        mavenCentral()
        maven {
            setUrl("https://maven.aliyun.com/repository/apache-snapshots")
        }
        maven {
            setUrl("https://maven.aliyun.com/repository/public")
        }
        jcenter()
    }

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}