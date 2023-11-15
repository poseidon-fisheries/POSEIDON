plugins {
    id("poseidon.java-conventions")
}

dependencies {
    implementation(project(":POSEIDON"))
    implementation(project(":regulations:core"))
    implementation("com.google.guava:guava:31.1-jre")
}