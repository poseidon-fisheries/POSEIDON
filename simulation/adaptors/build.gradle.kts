plugins {
    id("poseidon.java-conventions")
}

dependencies {
    implementation(project(":simulation:api"))
    implementation(project(":POSEIDON"))
    implementation("org.yaml:snakeyaml:1.29")
    implementation("com.google.guava:guava:31.1-jre")
}