plugins {
    id("poseidon.java-conventions")
}

dependencies {
    implementation("com.google.guava:guava:31.1-jre")
    api(project(":common:api"))
    api(project(":agents:api"))
}