/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2026, University of Oxford.
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
    id("buildlogic.java-library-conventions")
}

dependencies {
    testImplementation(testFixtures(project(":core")))
    api(project(":core"))
    api(libs.tablesaw.core)
    api(libs.snakeyaml)
    implementation(libs.commons.io)
    implementation(libs.jcommander)
    constraints {
        api(libs.classgraph) {
            because("classgraph 4.8.180 avoids JDK 24+ sun.misc.Unsafe::invokeCleaner warnings")
        }
    }
    runtimeOnly(libs.slf4j.jdk14) // tablesaw uses slf4j, which we reroute to j.u.Logging
}

pitest {
    // Measured 2026-11-25 at approximately 78.9% (101 killed or timed out of
    // 128 mutants); the floor sits two points below. Twenty mutants have no
    // coverage, and the seven survivors are YAML loader and dumper settings
    // (ScenarioLoader's alias limit and class-name checks, DumperOptions'
    // pretty flow, the read-only property setting in BuilderConstructor), a
    // logger call in CsvTableWriter, and a type-description registration.
    // Bump this floor up, never down.
    mutationThreshold.set(76)
}
