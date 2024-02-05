plugins {
    id("poseidon.java-conventions")
}

dependencies {
    api("com.univocity:univocity-parsers:2.9.1")
    api(fileTree("$rootDir/libs/mason") { include("*.jar") })
    api(files("$rootDir/libs/geomason/geomason.1.5.jar"))
    api("com.vividsolutions:jts:1.13") // JTS Topology Suite, a geomason dependency
    implementation("org.apache.commons:commons-lang3:3.14.0")
}
