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
    id("buildlogic.java-application-conventions")
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":regulations"))
    implementation(project(":biology"))
    implementation(project(":io"))
    implementation(project(":gui"))
    implementation(libs.jcommander)
}

tasks.shadowJar {
    mergeServiceFiles {
        // those exclusions prevent GeoTools from trying to load the CLib plugin, which crashes:
        exclude("com/sun/media/imageioimpl/plugins/jpeg/CLib*")
        exclude("META-INF/services/javax.imageio.spi.*")
    }
}
