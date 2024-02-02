plugins {
    id("poseidon.java-conventions")
}

dependencies {
    implementation(project(":datasets:api"))
    implementation(project(":datasets:core"))
    compileOnly(project(":common:api"))
    implementation(project(":common:core"))
    implementation(project(":POSEIDON"))
}
