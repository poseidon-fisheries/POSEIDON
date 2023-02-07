plugins {
    id("poseidon.java-conventions")
}

dependencies {
    implementation(project(":POSEIDON"))
    implementation(project(":common"))
    compileOnly(project(":simulations:api"))
    compileOnly(project(":datasets:api"))
    runtimeOnly(project(":datasets:adaptors"))

    implementation("org.yaml:snakeyaml:1.29")
    implementation("com.google.guava:guava:31.1-jre")
}