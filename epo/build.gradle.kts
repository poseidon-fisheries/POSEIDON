import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("poseidon.java-conventions")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

tasks {
    named<ShadowJar>("shadowJar") {
        mergeServiceFiles()
    }
}

dependencies {
    implementation(project(":POSEIDON"))
    implementation("com.beust:jcommander:1.82") // to parse command line arguments
    implementation("commons-beanutils:commons-beanutils:1.9.4")
}
