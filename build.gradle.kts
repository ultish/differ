plugins {
    kotlin("jvm") version "2.4.10" apply false
    id("com.google.devtools.ksp") version "2.3.11" apply false
}

subprojects {
    pluginManager.withPlugin("maven-publish") {
        extensions.configure<PublishingExtension> {
            repositories {
                maven {
                    name = "GitHubPackages"
                    val repository = providers.environmentVariable("GITHUB_REPOSITORY")
                        .orElse("OWNER/differ")
                    url = uri(repository.map { "https://maven.pkg.github.com/$it" })
                    credentials {
                        username = providers.environmentVariable("GITHUB_ACTOR").orElse("").get()
                        password = providers.environmentVariable("GITHUB_TOKEN").orElse("").get()
                    }
                }
            }
        }
    }
}
