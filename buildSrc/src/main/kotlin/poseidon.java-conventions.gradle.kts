/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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
plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    // centralize the dependency on Guava, since that's the
    // one library that we use almost everywhere
    implementation("com.google.guava:guava:33.2.0-jre")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito:mockito-core:4.11.0")

    // Google AutoService annotations, for generating ServiceLoader metadata automatically
    annotationProcessor("com.google.auto.service:auto-service:1.1.1")
    compileOnly("com.google.auto.service:auto-service-annotations:1.1.1")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    maxHeapSize = "4G" // GitHub actions was crashing on 6GB
    testLogging {
        events("passed", "skipped", "failed")
    }
    maxParallelForks = Runtime
        .getRuntime()
        .availableProcessors()
        .coerceAtMost(8)
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("-Werror", "-Xlint:-processing"))
}

tasks.withType<Copy>().all {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
