/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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
    api(project(":core"))
    api(libs.jenetics)
}

pitest {
    // Measured 2026-11-25 at approximately 90.7% (88 killed or timed out of 97
    // mutants); the floor sits two points below. Six mutants have no coverage.
    // The three survivors are in CalibrationProblem: a long subtraction
    // replaced by addition, and removed calls to TemporalSchedule::stepFor and
    // Simulation::finish. Bump this floor up, never down.
    mutationThreshold.set(88)
}
