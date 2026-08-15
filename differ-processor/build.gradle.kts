plugins {
    kotlin("jvm")
    id("com.vanniktech.maven.publish")
}

group = property("differGroup") as String
version = property("differVersion") as String

repositories { mavenCentral() }

kotlin { jvmToolchain(17) }

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:${property("kspVersion")}")
    implementation("com.squareup:kotlinpoet:${property("kotlinPoetVersion")}")
    implementation("com.squareup:kotlinpoet-ksp:${property("kotlinPoetVersion")}")
}
