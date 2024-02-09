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
    implementation(project(":common"))
    implementation(project(":simulations:api"))
    runtimeOnly(project(":simulations:adaptors"))
}
