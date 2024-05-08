import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("poseidon.java-conventions")
    id("jacoco")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.github.spotbugs") version "6.0.14"
}

tasks {
    named<ShadowJar>("shadowJar") {
        mergeServiceFiles()
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

//here we add all the dependencies notice that mason and geomason are not on maven so we need to add them separately
dependencies {

    api(project(":common"))
    api(project(":regulations"))
    api(project(":agents"))
    api(project(":geography"))

    //fx collections; needed for sanity
    implementation(fileTree("libs/fxbase") { include("*.jar") })

    // DO NOT use opencsv for new code, as we want to phase it out.
    implementation("com.opencsv:opencsv:3.8")

    //kd tree
    implementation(fileTree("libs/rednaxela") { include("*.jar") })

    //jung social network:
    implementation("net.sf.jung:jung-api:2.0.1")
    implementation("net.sf.jung:jung-graph-impl:2.0.1")
    implementation("net.sf.jung:jung-io:2.0.1")
    implementation("net.sf.jung:jung-algorithms:2.0.1")

    //jcommander, useful to parse command line arguments
    implementation("com.beust:jcommander:1.81")

    //commons math
    implementation("org.apache.commons:commons-math3:3.6.1")

    //discrete choosers
    implementation(fileTree("libs/discrete-choosers") { include("*.jar") })

    // maximizer
    api("de.openea:eva2:2.2.0")

    //testing:
    testImplementation("nl.jqno.equalsverifier:equalsverifier:3.7")

    // Reference implementation of JSR-385 for units of measure
    api("si.uom:si-quantity:2.1")
    api("si.uom:si-units:2.1")
}

spotbugs {
    ignoreFailures = true
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.jacocoTestReport {
    reports {
        xml.required = true
        html.required = true
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
}
