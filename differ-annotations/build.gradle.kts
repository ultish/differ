plugins {
    kotlin("jvm")
    id("com.vanniktech.maven.publish")
}

group = property("differGroup") as String
version = property("differVersion") as String

repositories { mavenCentral() }

kotlin { jvmToolchain(17) }
