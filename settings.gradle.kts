rootProject.name = "POSEIDON"
include("agents:api")
include("agents:core")
include("common:api")
include("common:core")
include("datasets:adaptors")
include("datasets:api")
include("datasets:core")
include("POSEIDON")
include("poseidon-r")
include("regulations:api")
include("regulations:core")
include("simulations:adaptors")
include("simulations:api")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.6.0"
}
include("epo")
