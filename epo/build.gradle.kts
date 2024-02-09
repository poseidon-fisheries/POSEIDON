import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("poseidon.java-conventions")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

tasks {
    named<ShadowJar>("shadowJar") {
        mergeServiceFiles()
    }
}

dependencies {
    implementation(project(":POSEIDON"))
    implementation("com.beust:jcommander:1.81") // to parse command line arguments
}
