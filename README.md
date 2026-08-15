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
    implementation("dev.differ:differ-annotations:0.1.0")
    ksp("dev.differ:differ-processor:0.1.0")
}
```

Publish the two artifacts to Maven Local with `./gradlew publishToMavenLocal`.

## Usage

```kotlin
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

One `Long` mask, 64 tracked slots including each list's add/remove and every nested leaf. Same-type comparison only. Identity defaults to a property named `id`.
