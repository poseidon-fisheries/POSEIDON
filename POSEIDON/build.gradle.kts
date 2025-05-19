/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

    //kd tree
    implementation(fileTree("libs/rednaxela") { include("*.jar") })

    //jung social network:
    implementation("net.sf.jung:jung-api:2.0.1")
    implementation("net.sf.jung:jung-graph-impl:2.0.1")
    implementation("net.sf.jung:jung-io:2.0.1")
    implementation("net.sf.jung:jung-algorithms:2.0.1")

    //jcommander, useful to parse command line arguments
    implementation("com.beust:jcommander:1.82")

    // Apache commons utilities
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("commons-beanutils:commons-beanutils:1.9.4")

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
