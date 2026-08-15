plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
    id("me.champeau.jmh") version "0.7.3"
}

repositories { mavenCentral() }

kotlin { jvmToolchain(17) }

dependencies {
    implementation(project(":differ-annotations"))
    ksp(project(":differ-processor"))
    implementation("org.javers:javers-core:7.11.7")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")
    implementation("com.flipkart.zjsonpatch:zjsonpatch:0.4.16")
    runtimeOnly("org.slf4j:slf4j-nop:2.0.16")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

jmh {
    warmupIterations.set(3)
    iterations.set(5)
    fork.set(1)
    timeOnIteration.set("1s")
    warmup.set("1s")
    benchmarkMode.set(listOf("avgt"))
    timeUnit.set("ns")
}
