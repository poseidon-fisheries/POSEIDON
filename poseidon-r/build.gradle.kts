plugins {
    id("poseidon.java-conventions")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

dependencies {
    implementation("com.google.guava:guava:31.1-jre")
    implementation(project(":common"))
    implementation(project(":simulations:api"))
    runtimeOnly(project(":simulations:adaptors"))
}