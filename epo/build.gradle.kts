plugins {
    id("poseidon.java-conventions")
}

dependencies {
    implementation(project(":POSEIDON"))
    implementation("com.beust:jcommander:1.81") // to parse command line arguments
}
