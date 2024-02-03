plugins {
    id("poseidon.java-conventions")
}

dependencies {
    implementation("org.apache.commons:commons-lang3:3.14.0")
    api(fileTree("$rootDir/libs/mason") { include("*.jar") })
    api(files("$rootDir/libs/geomason/geomason.1.5.jar"))
    implementation("com.vividsolutions:jts:1.13") // JTS Topology Suite, a geomason dependency
    api("com.univocity:univocity-parsers:2.9.1")
}
