plugins {
    id("poseidon.java-conventions")
}

dependencies {
    implementation(project(":datasets:api"))
    implementation("com.google.guava:guava:31.1-jre")
}