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
    alias(libs.plugins.protobuf)
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":regulations"))
    implementation(project(":biology"))
    implementation(project(":io"))
    runtimeOnly(project(":examples")) // temporary, to load western med scenario
    implementation(libs.protobuf.java.util)
    implementation(libs.jcommander)
    implementation(libs.bundles.grpc)
    implementation(libs.commons.beanutils)
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
        artifact = libs.protoc.get().toString()
    }
    plugins {
        create("grpc") {
            artifact = libs.protocGenGrpcJava.get().toString()
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
