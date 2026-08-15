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
