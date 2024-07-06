val app_version: String by project

val kotlinx_coroutines_version: String by project
val kotlinx_serialization_version: String by project
val discord4j_version: String by project
val logback_version: String by project


plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"
    application

    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "brauterfallet.no"
version = app_version

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinx_coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinx_coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinx_serialization_version")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-hocon:$kotlinx_serialization_version")
    implementation("com.discord4j:discord4j-core:$discord4j_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
}

application {
    mainClass.set("MainKt")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register("deleteApplicationProperties") {
    doLast {
        val propertiesFile = file("build/resources/main/application.properties")
        if (propertiesFile.exists()) {
            delete(propertiesFile)

            println("Deleted application.properties file")
        }
    }
}

tasks.named("processResources") {
    dependsOn("deleteApplicationProperties")
}

tasks.processResources {
    filesMatching("application.properties") {
        expand("version" to version)
    }
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "MainKt"
    }
}
kotlin {
    jvmToolchain(21)
}