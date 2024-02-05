plugins {
    id("poseidon.java-conventions")
}

dependencies {
    api(project(":agents:api"))
    implementation(project(":common"))
    annotationProcessor("com.google.auto.service:auto-service:1.1.1")
    compileOnly("com.google.auto.service:auto-service-annotations:1.1.1")
}
