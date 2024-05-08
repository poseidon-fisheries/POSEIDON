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
 */
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("poseidon.java-conventions")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

tasks {
    named<ShadowJar>("shadowJar") {
        mergeServiceFiles()
    }
}
dependencies {
    implementation(project(":common"))
    implementation(project(":simulations:api"))
    runtimeOnly(project(":simulations:adaptors"))
    runtimeOnly(project(":poseidon-r:adaptors"))
}
