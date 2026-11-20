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
    implementation(project(":io"))
    implementation(libs.gdx.ai)
    api(libs.jts.core)
    implementation(libs.gt.api)
    implementation(libs.gt.main)
    implementation(libs.gt.coverage)
    runtimeOnly(libs.gt.shapefile)
    runtimeOnly(libs.gt.arcgrid)
    implementation(libs.cdm.core)
}

pitest {
    // Measured 2026-11-20 at approximately 97% (745 killed and two timeouts
    // out of 771 mutants). Survivors cover equivalent paths (cell-key and step
    // encodings, the A* heuristic, envelope tie-breaks), tolerance boundaries,
    // diagnostic logging and resource disposal. Bump this floor up, never down.
    mutationThreshold.set(94)
}
