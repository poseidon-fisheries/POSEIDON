plugins {
    id("poseidon.java-conventions")
}

dependencies {
    compileOnly(project(":common:api"))
    implementation("org.apache.commons:commons-lang3:3.14.0")
}
