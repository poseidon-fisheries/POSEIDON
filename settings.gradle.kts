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
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "POSEIDON"
include("core")
include("geography")
include("io")
include("biology")
include("examples")
include("gui")
include("agents")
include("regulations")

dependencyResolutionManagement {
    repositories {
        maven {
            // needs to come before Maven Central, otherwise we fail to find javax.media:jai_core:1.1.3
            url = uri("https://repo.osgeo.org/repository/geotools-releases/")
        }
        mavenCentral()
        maven { url = uri("https://maven.geo-solutions.it/") }
        maven {
            // needs to come after mavenCentral otherwise we fail to find
            // flatlaf-3.5.1-macos-arm64.dylib and flatlaf-3.5.1-macos-x86_64.dylib
            url = uri("https://nexus.geomatys.com/repository/maven-public/")
        }
    }
}
