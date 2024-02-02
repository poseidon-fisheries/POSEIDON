plugins {
    id("poseidon.java-conventions")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

dependencies {
    implementation(project(":common:api"))
    implementation(project(":common:core"))
    implementation(project(":simulations:api"))
    runtimeOnly(project(":simulations:adaptors"))
}
