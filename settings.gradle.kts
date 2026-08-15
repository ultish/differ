plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "differ"

include(":differ-annotations")
include(":differ-processor")
include(":differ-tests")
