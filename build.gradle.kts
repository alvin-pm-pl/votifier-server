plugins {
    kotlin("jvm") version "1.8.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.minjae.votifier.server"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "jitpack"
        url = uri("https://jitpack.io")
    }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.github.NuVotifier.NuVotifier:nuvotifier-common:fcf6c93404")
    implementation("io.netty:netty-all:4.1.90.Final")
    implementation("ch.qos.logback:logback-classic:1.4.6")
    implementation("org.yaml:snakeyaml:2.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
    implementation("com.fasterxml.jackson.module:jackson-module-blackbird:2.14.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.14.2")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        manifest {
            attributes(mapOf("Main-Class" to "dev.minjae.votifier.server.BootstrapKt"))
        }
    }
}
