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
    api(project(":regulations"))
    api(project(":biology"))
    api(project(":geography"))
    api(project(":io"))
    api(libs.joda.money)
    api(libs.gdx.ai)
    implementation(libs.streamex)
    implementation(libs.commons.beanutils)
    implementation(libs.fastutil.core)
}

pitest {
    // Measured 2026-11-20 at approximately 98.6% (1,031 killed or timed out of
    // 1,046 mutants); the floor sits two points below. No mutant is uncovered:
    // the newInstance methods of simple factories run through the shared
    // FactoryProductsTest. That non-null sweep, not tests of how each factory
    // wires its inputs, kills the null-return mutants of the newInstance
    // methods. The 15 survivors are equivalent paths, tolerance
    // boundaries and redundant calls. Bump this floor up, never down.
    mutationThreshold.set(96)
}
