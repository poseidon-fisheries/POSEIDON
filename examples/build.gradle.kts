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
    id("com.google.protobuf") version "0.9.4"
    id("com.gradleup.shadow") version "8.3.5"
}

dependencies {
    implementation(project(":regulations"))
    implementation(project(":biology"))
    implementation(project(":io"))
    implementation(project(":gui"))

    implementation("org.jcommander:jcommander:2.0")

    // The following are all necessary for gRPC
    implementation("io.grpc:grpc-netty-shaded:1.68.1")
    implementation("io.grpc:grpc-protobuf:1.68.1")
    implementation("io.grpc:grpc-stub:1.68.1")
    implementation("com.google.protobuf:protobuf-java:4.28.3")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
}

tasks.shadowJar {
    mergeServiceFiles {
        // those exclusions prevent GeoTools from trying to load the CLib plugin, which crashes:
        exclude("com/sun/media/imageioimpl/plugins/jpeg/CLib*")
        exclude("META-INF/services/javax.imageio.spi.*")
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.28.3" // Specify Protoc compiler
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.68.1" // Specify gRPC plugin
        }
    }
    generateProtoTasks {
        all().configureEach {
            plugins {
                create("grpc")
            }
        }
    }
}
