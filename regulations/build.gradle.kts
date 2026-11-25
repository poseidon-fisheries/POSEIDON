/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025-2026, University of Oxford.
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
    api(project(":geography"))
    api(libs.threeten.extra)
}

pitest {
    // Measured 2026-11-25 at approximately 67.6% (50 killed or timed out of 74
    // mutants); the floor sits two points below. Twenty-one mutants have no
    // coverage. The three survivors are a removed Preconditions check in
    // BetweenYearlyDates and two boundary changes in InYear, both at the
    // year-range edges. Bump this floor up, never down.
    mutationThreshold.set(65)
}
