plugins {
    id("poseidon.java-conventions")
}

dependencies {
    api(project(":datasets:api"))
    implementation("com.google.guava:guava:31.1-jre")
}
