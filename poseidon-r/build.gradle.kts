plugins {
    id("poseidon.java-conventions")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

dependencies {
    compileOnly(project(":simulation:api"))
    runtimeOnly(project(":simulation:adaptors"))
}