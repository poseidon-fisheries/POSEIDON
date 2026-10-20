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

buildscript {
    dependencies {
        // node-gradle (latest release, 7.1.0) brings a vulnerable Jackson 2 onto the build
        // classpath; drop once a node-gradle release brings 2.18.11 or later
        classpath(platform(libs.jackson2.bom))
    }
}

plugins {
    base
    alias(libs.plugins.node.gradle)
    alias(libs.plugins.spotless)
}

node {
    download = true
    version = libs.versions.nodejs
    // Downloaded through the Node.js repository declared in settings.gradle.kts
    distBaseUrl = null
}

// Prettier versions come from package.json, which IntelliJ's Prettier integration also uses, so
// that formatting in the IDE and in the build always agree
@Suppress("UNCHECKED_CAST")
val npmPackages = (groovy.json.JsonSlurper().parse(file("package.json")) as Map<String, Any>)
    .getValue("devDependencies") as Map<String, String>
val isWindows = System.getProperty("os.name").startsWith("Windows")

spotless {
    java {
        target(subprojects.map { "${it.name}/src/**/*.java" })
        prettier(npmPackages)
            .nodeExecutable(
                tasks.nodeSetup.get().nodeDir.get()
                    .file(if (isWindows) "node.exe" else "bin/node").asFile
            )
            .npmExecutable(
                tasks.npmSetup.get().npmDir.get()
                    .file(if (isWindows) "npm.cmd" else "bin/npm").asFile
            )
            .configFile(".prettierrc")
            .npmInstallCache()
        // Existing years are kept as they are; IntelliJ's copyright profile updates them on edit
        licenseHeader(
            """
            /*
             * POSEIDON: an agent-based model of fisheries
             * Copyright (c) ${'$'}YEAR, University of Oxford.
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
            """.trimIndent() + "\n\n"
        )
    }
}

tasks.named("spotlessJava") {
    dependsOn(tasks.nodeSetup, tasks.npmSetup)
}

tasks.named("check") {
    dependsOn(subprojects.map { it.tasks.named("test") })
}

allprojects {
    // Missing comments are not reported, and any other Javadoc problem fails the build
    tasks.withType<Javadoc>().configureEach {
        (options as StandardJavadocDocletOptions).apply {
            addBooleanOption("Xdoclint:all,-missing", true)
            addBooleanOption("Werror", true)
        }
    }
}

subprojects {
    tasks.withType<Jar>().configureEach {
        archiveBaseName.set("${rootProject.name}-${project.name}")
    }
}

tasks.register<Javadoc>("aggregateJavadoc") {
    group = "documentation"
    description = "Generates aggregated Javadoc across all subprojects."
    dependsOn(subprojects.map { it.tasks.named("javadoc") })
    subprojects.forEach { sub ->
        source(sub.extensions.getByType<SourceSetContainer>()["main"].allJava)
    }
    classpath = files(subprojects.map { it.configurations.getByName("compileClasspath") })
    setDestinationDir(layout.buildDirectory.dir("docs/javadoc").get().asFile)
}
