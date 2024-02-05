rootProject.name = "POSEIDON"
include("agents")
include("common")
include("datasets:adaptors")
include("datasets:api")
include("datasets:core")
include("POSEIDON")
include("poseidon-r")
include("regulations")
include("simulations:adaptors")
include("simulations:api")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.6.0"
}
include("epo")
