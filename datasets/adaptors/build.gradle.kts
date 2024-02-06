plugins {
    id("poseidon.java-conventions")
}

dependencies {
    implementation(project(":datasets:api"))
    implementation(project(":datasets:core"))
    implementation(project(":common"))
    implementation(project(":POSEIDON"))
    implementation(project(":epo"))
}
