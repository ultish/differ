plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

repositories { mavenCentral() }

kotlin { jvmToolchain(17) }

dependencies {
    testImplementation(project(":differ-annotations"))
    kspTest(project(":differ-processor"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
