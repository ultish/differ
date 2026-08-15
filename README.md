# Differ

Compile-time, ID-keyed diffs for Kotlin data classes.

Site: [ultish.github.io/differ](https://ultish.github.io/differ/)

Annotate the fields you care about. KSP generates a `*Differ` that compares two values of the same type. Detection is a bit mask. Values are sealed events for the fields that actually changed. Child lists are matched by id, never by index.

Wire formats stay at the boundary. Avro and Mongo can keep UUID-as-string. Convert once, then compare `Machine` to `Machine`.

## Install

```kotlin
plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    implementation("io.github.ultish:differ-annotations:0.1.5")
    ksp("io.github.ultish:differ-processor:0.1.5")
}
```

The Kotlin package is still `hana.differ`. The Maven group is `io.github.ultish` so Central will accept it.

### Maven Local

```sh
./gradlew publishToMavenLocal
```

### Maven Central

Claim the `io.github.ultish` namespace at [central.sonatype.org](https://central.sonatype.org/). Generate a portal user token. Create a GPG key and upload the public key to a keyserver. Then add these GitHub Actions secrets:

- `MAVEN_CENTRAL_USERNAME`
- `MAVEN_CENTRAL_PASSWORD`
- `SIGNING_KEY` (ascii-armored private key from `gpg --export-secret-keys --armor`)
- `SIGNING_PASSWORD`

Run the `publish` workflow, or `./gradlew publishAndReleaseToMavenCentral` locally with the same values in `~/.gradle/gradle.properties`. Consumers then depend on Maven Central with no extra repo and no token.

Until the first Central publish lands, use Maven Local.

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

The mask is a `LongArray`, so a fat document is not capped at 64 slots. Add/remove, nullable nested presence, and every nested leaf still each take one slot. Same-type comparison only. Identity defaults to a property named `id`.

`@Tracked` nullable scalars compare with `!=`, so `null` is a normal old/new. `@TrackedNested` on `T?` records presence: both null is unchanged, one null is a single event with the whole object, both present walks the child fields. `@TrackedList` is any `List` or `Set`, including `SortedSet` / `TreeSet`. A Set is copied into a list for the walk. Matching is by `matchBy`, never by order.

`@TrackedMap` is any `Map`, including `SortedMap` / `TreeMap`. The map key is the id. Object values walk `@Tracked` fields. Scalar values (`String`, numbers, enums) compare with `!=`.

`List` / `Set` / `Map` that are nullable treat `null` as empty. Primitive arrays (`IntArray`, …) are not keyed collections. Mark them `@Tracked` and compare the whole array.
