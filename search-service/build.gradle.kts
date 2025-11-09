plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")
    // Apply Kotlin Serialization plugin from `gradle/libs.versions.toml`.
    alias(libs.plugins.kotlinPluginSerialization)
}

group = "org.example"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    // Elasticsearch
    implementation("co.elastic.clients:elasticsearch-java:8.15.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")

    // Configuration
    implementation("com.typesafe:config:1.4.3")

    // logger
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("org.slf4j:slf4j-api:2.0.9")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}