plugins {
    id("poseidon.java-conventions")
}

dependencies {
    api(project(":regulations:api"))
    implementation("com.google.guava:guava:31.1-jre")
}
