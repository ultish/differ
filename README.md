# Differ

Compile-time, ID-keyed diffs for Kotlin data classes.

Site: [ultish.github.io/differ](https://ultish.github.io/differ/)

Annotate the fields you care about. KSP generates a `*Differ` that compares two values of the same type. Detection is a bit test. Values are sealed events for what actually changed. Children are matched by id, never by position.

Convert Avro and Mongo at the boundary. The walk is same-type only, `Pet` versus `Pet`.

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

Maven Central. No extra repository. Imports are `hana.differ`.

## Usage

```kotlin
import hana.differ.Differ
import hana.differ.Tracked
import hana.differ.TrackedList
import hana.differ.TrackedMap
import hana.differ.TrackedNested

@Differ
data class Pet(
    val id: String,
    @Tracked val name: String,
    @Tracked(captureValues = false) val notes: String,
    @Tracked val nickname: String?,
    @TrackedNested val owner: Person,
    @TrackedNested val vet: Person?,
    @TrackedList val toys: Set<Toy>,
    @TrackedList val walks: List<Walk>,
    @TrackedMap val tricks: Map<String, Trick>,
    @TrackedMap val tags: Map<String, String>,
)
```

`Person`, `Toy`, `Walk`, and `Trick` do not take `@Differ`. Their tagged fields fold into `PetDiffer`.

```kotlin
val diff = PetDiffer.diff(stored, incoming)

if (!diff.hasChanged) return

if (diff.hasName) { /* bit test */ }

for (change in diff.changes) {
    when (change) {
        is PetChange.ToysName -> {
            change.id
            change.old
            change.new
        }
        is PetChange.Vet -> change.new
        else -> {}
    }
}

diff.toys["t1"]?.name?.new
diff.tags["color"]?.value?.new
```

`hasChanged` and `hasToysName` are bit tests. `changes` and the keyed maps contain only children that changed. You do not walk the original collection again.

`@Tracked(captureValues = false)` still sets the bit and still emits a flag event. It does not copy old/new.

A rename of a tagged property fails the build. A new untagged field is invisible.

## Collections

`@TrackedList` is any `List` or `Set`, including `SortedSet` and `TreeSet`. A set is copied for the walk. Matching is by `matchBy` (default `id`), never by order.

`@TrackedMap` is any `Map`, including `SortedMap` and `TreeMap`. The map key is the id. Object values walk `@Tracked` fields. Scalar values (`String`, numbers, enums) compare with `!=`.

Nullable `List` / `Set` / `Map` treat `null` as empty.

`@Tracked` nullable scalars compare with `!=`. `@TrackedNested` on `T?` records presence. Both null is unchanged. One null is a single event with the whole object. Both present walks the child fields.

Primitive arrays (`IntArray` and the rest) are not keyed collections. Mark them `@Tracked` and compare the whole array.

## Modules

`differ-annotations` is the runtime (`@Differ`, `@Tracked`, `ValueChange`). `differ-processor` is KSP only. `differ-tests` is this repo's proof, not an artifact.

## Benchmark

A fat `Plant` (nested site and address, optional failover, two reversed link lists, alarm set, recipe map, label map). On Java 17, 256 keyed children, unchanged: handwritten 13 µs, this library 21 µs, JSON 0.82 ms, JaVers 3.7 ms. The hand walk is faster. Differ is the one you do not maintain.

```sh
./gradlew :differ-benchmark:jmh
```

`-Pjmh.includes=PlantBenchmark` or `IndexBenchmark` to limit.

## Limits

The mask is a `Long`, then a `LongArray` past 64 slots. Add/remove, nullable nested presence, and every nested leaf each take one slot. Same-type comparison only.
