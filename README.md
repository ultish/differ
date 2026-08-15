# Differ

Compile-time, ID-keyed diffs for Kotlin data classes.

Annotate the fields you care about. KSP generates a `*Differ` that compares two values of the same type. Detection is a bit mask. Values are sealed events for the fields that actually changed. Child lists are matched by id, never by index.

Wire formats stay at the boundary. Avro and Mongo can keep UUID-as-string. Convert once, then compare `Machine` to `Machine`.

## Install

```kotlin
plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    implementation("hana.differ:differ-annotations:0.1.1")
    ksp("hana.differ:differ-processor:0.1.1")
}
```

### Maven Local

```sh
./gradlew publishToMavenLocal
```

### GitHub Packages

Push the repo to GitHub, then publish a GitHub Release (or run the `publish` workflow). Actions uploads `differ-annotations` and `differ-processor` with `GITHUB_TOKEN`. No extra secrets.

Consumers need a GitHub token even for a public package. In `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/ultish/differ")
            credentials {
                username = providers.gradleProperty("gpr.user").orElse(providers.environmentVariable("GITHUB_ACTOR"))
                password = providers.gradleProperty("gpr.key").orElse(providers.environmentVariable("GITHUB_TOKEN"))
            }
        }
    }
}
```

Maven Central is a different host. It needs a verified group id, GPG signing, and Central Portal credentials in Actions secrets. This repo does not do that yet.

## Usage

```kotlin
import hana.differ.Differ
import hana.differ.Tracked
import hana.differ.TrackedList
import hana.differ.TrackedNested

@Differ
data class Machine(
    val id: String,
    val name: String,
    @Tracked(captureValues = false) val setup: String,
    @TrackedList val inputConnections: List<Connection>,
)

data class Connection(
    val id: String,
    @Tracked val state: ConnectionState,
    @TrackedNested val fromPort: Port,
)
```

`Connection` and `Port` do not take `@Differ`. Nested `@Tracked` fields are inlined into `MachineDiffer`.

```kotlin
val diff = MachineDiffer.diff(stored, incoming)

if (!diff.hasChanged) return

if (diff.hasSetup) reloadSetup()

if (diff.hasInputConnectionsState) {
    for (change in diff.changes) {
        if (change is MachineChange.InputConnectionsState) {
            change.id
            change.old
            change.new
        }
    }
}

diff.inputConnections["c1"]?.state?.new
```

`hasChanged` / `hasInputConnectionsState` are bit tests. `changes` and `inputConnections` contain only children that changed. You do not walk the original list again.

`@Tracked(captureValues = false)` still sets `hasSetup` and still emits `MachineChange.Setup`. It does not copy old/new.

A rename of a tracked property fails the build. A new untagged field is invisible.

## Modules

`differ-annotations` is the runtime classpath (`@Differ`, `@Tracked`, `ValueChange`). `differ-processor` is KSP only. `differ-tests` is this repo's proof, not an artifact.

## Limits

One `Long` mask, 64 tracked slots including each list's add/remove, each nullable nested object's presence, and every nested leaf. Same-type comparison only. Identity defaults to a property named `id`.

`@Tracked` nullable scalars compare with `!=`, so `null` is a normal old/new. `@TrackedNested` on `T?` records presence: both null is unchanged, one null is a single event with the whole object, both present walks the child fields. `@TrackedList` on `List<T>?` treats `null` as empty.
