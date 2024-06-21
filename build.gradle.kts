val discord4j_version: String by project

plugins {
    kotlin("jvm") version "2.0.0"
    application
}

group = "brauterfallet.no"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("com.discord4j:discord4j-core:$discord4j_version")
}

application {
    mainClass.set("MainKt")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}