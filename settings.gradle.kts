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

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "POSEIDON"
include("core")
include("calibration")
include("geography")
include("io")
include("biology")
include("examples")
include("gui")
include("fishing")
include("regulations")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // This repository list is intentionally mirrored in the
        // parent SURIMI-POSEIDON/settings.gradle.kts. Keep both in sync.
        mavenCentral()
        maven("https://repo.osgeo.org/repository/geotools-releases/")
        maven("https://jitpack.io")
        maven("https://maven.geo-solutions.it/")
        maven {
            url = uri("https://nexus.geomatys.com/repository/maven-public/")
            // Only for cdm-core: left unfiltered, this repository would also answer for FlatLaf, but it
            // lacks the macOS native artifacts (flatlaf-<version>-macos-arm64.dylib / -macos-x86_64.dylib)
            content { includeGroup("edu.ucar") }
        }
        ivy {
            // Node.js distributions, downloaded by the node-gradle plugin to run Prettier. Declared
            // here rather than by the plugin because project repositories are forbidden above.
            name = "Node.js"
            url = uri("https://nodejs.org/dist/")
            patternLayout { artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]") }
            metadataSources { artifact() }
            content { includeModule("org.nodejs", "node") }
        }
    }
}
