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
    id("poseidon.java-conventions")
}

dependencies {
    api("com.univocity:univocity-parsers:2.9.1")
    api(fileTree("$rootDir/libs/mason") { include("*.jar") })
    api(files("$rootDir/libs/geomason/geomason.1.5.jar"))
    api("com.vividsolutions:jts:1.13") // JTS Topology Suite, a geomason dependency
    // We need to stay on SnakeYAML 1.33 despite the vulnerability
    // because Eva2 is not compatible with SnakeYAML 2.x.
    api("org.yaml:snakeyaml:1.33")
    implementation("org.apache.commons:commons-lang3:3.14.0")
}
