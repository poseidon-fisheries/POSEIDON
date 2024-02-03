plugins {
    id("poseidon.java-conventions")
}

dependencies {
    api(fileTree("$rootDir/libs/mason") {
        include("*.jar")
    })
}
