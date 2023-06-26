plugins {
    id("poseidon.java-conventions")
}

dependencies {
    implementation("com.google.guava:guava:31.1-jre")
    compileOnly(project(":common:api"))
}