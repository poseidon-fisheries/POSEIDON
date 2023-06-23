plugins {
    id("poseidon.java-conventions")
}

dependencies {
    implementation(project(":regulations:api"))
    implementation("com.google.guava:guava:31.1-jre")
}
