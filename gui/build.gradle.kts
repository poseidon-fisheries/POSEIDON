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
    id("buildlogic.java-application-conventions")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":io"))
    implementation(project(":biology"))
    implementation(project(":agents"))

    val flatlafVersion = "3.5.1"
    implementation("com.formdev:flatlaf:${flatlafVersion}")
    implementation("com.formdev:flatlaf:${flatlafVersion}:linux-x86_64@so")
    implementation("com.formdev:flatlaf:${flatlafVersion}:macos-arm64@dylib")
    implementation("com.formdev:flatlaf:${flatlafVersion}:macos-x86_64@dylib")
    implementation("com.formdev:flatlaf:${flatlafVersion}:windows-x86_64@dll")

    // ARM 64-bit (not needed if your application does not support Windows on ARM)
    implementation("com.formdev:flatlaf:${flatlafVersion}:windows-arm64@dll")

    // Apache batik for SVG handling
    implementation("org.apache.xmlgraphics:batik-transcoder:1.17")
    implementation("org.apache.xmlgraphics:batik-dom:1.17")
    implementation("org.apache.xmlgraphics:batik-awt-util:1.17")
}
