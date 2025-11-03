plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")
    // Apply Kotlin Serialization plugin from `gradle/libs.versions.toml`.
    alias(libs.plugins.kotlinPluginSerialization)
}

group = "com.firelord.ticketmaster"
version = "event-manager"

repositories {
    mavenCentral()
}

dependencies {
    // Mongo
    implementation("org.litote.kmongo:kmongo:5.2.0")

    // Elasticsearch
    implementation("co.elastic.clients:elasticsearch-java:8.15.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")

    // Configuration
    implementation("com.typesafe:config:1.4.3")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}