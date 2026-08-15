package hana.differ.it

import hana.differ.Differ
import hana.differ.TrackedList
import hana.differ.TrackedMap
import java.util.SortedMap
import java.util.SortedSet
import java.util.TreeMap
import java.util.TreeSet

@Differ
data class Chest(
    @TrackedMap val toys: Map<String, Toy>,
    @TrackedMap val tags: Map<String, String>,
)

@Differ
data class SortedChest(
    @TrackedList val toySet: SortedSet<Toy>,
    @TrackedMap val toyMap: SortedMap<String, Toy>,
)

fun toyTreeSet(vararg toys: Toy): SortedSet<Toy> =
    TreeSet(compareBy<Toy> { it.id }).apply { addAll(toys) }

fun toyTreeMap(vararg toys: Toy): SortedMap<String, Toy> =
    TreeMap<String, Toy>().apply { toys.forEach { put(it.id, it) } }
