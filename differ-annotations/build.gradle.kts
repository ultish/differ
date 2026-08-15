plugins {
    kotlin("jvm")
    `maven-publish`
}

group = property("differGroup") as String
version = property("differVersion") as String

repositories { mavenCentral() }

kotlin { jvmToolchain(17) }

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
