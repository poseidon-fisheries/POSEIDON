/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

plugins {
    id("buildlogic.java-library-conventions")
}

dependencies {
    api(project(":core"))
    implementation(project(":io"))
    implementation("com.badlogicgames.gdx:gdx-ai:1.8.2")
    implementation("edu.ucar:cdm-core:5.7.0")
    implementation("edu.ucar:netcdf4:5.7.0")
    val sisVersion = 1.4
    implementation("org.apache.sis.storage:sis-storage:${sisVersion}")
    implementation("org.apache.sis.core:sis-feature:${sisVersion}")
    runtimeOnly("org.apache.sis.non-free:sis-embedded-data:${sisVersion}")
    // We are temporarily using geotoolkit for loading shapefiles, in the hope
    // that this feature will eventually be ported to SIS with a similar API
    api("org.geotoolkit:geotk-feature-shapefile:24.11.19")
}
