plugins {
    id("poseidon.java-conventions")
}

dependencies {
    api(project(":regulations:api"))
    api(files("$rootDir/libs/geomason/geomason.1.5.jar"))
}
