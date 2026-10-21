package hana.differ

/** Old and new values for one changed field. */
data class ValueChange<T>(val old: T, val new: T)

/** Two children in a [@TrackedList] reported the same identity. */
class DuplicateChildIdException(id: String) : IllegalArgumentException("Duplicate child id: $id")

/**
 * Helpers the generated `*Differ.diff` calls. Not a public integration point.
 */
object DifferSupport {
    fun bits(slots: Int): LongArray = LongArray((slots + 63) ushr 6)

    fun allLong(slots: Int): Long = if (slots == 0) 0L else -1L ushr (64 - slots)

    fun allBits(slots: Int): LongArray {
        val mask = bits(slots)
        for (slot in 0 until slots) set(mask, slot)
        return mask
    }

    fun set(mask: LongArray, slot: Int) {
        val word = slot ushr 6
        mask[word] = mask[word] or (1L shl (slot and 63))
    }

    fun clear(mask: LongArray, slot: Int) {
        val word = slot ushr 6
        mask[word] = mask[word] and (1L shl (slot and 63)).inv()
    }

    fun test(mask: LongArray, slot: Int): Boolean =
        mask[slot ushr 6] and (1L shl (slot and 63)) != 0L

    fun any(mask: LongArray): Boolean {
        for (word in mask) if (word != 0L) return true
        return false
    }

    fun <T> add(current: MutableList<T>?, item: T): MutableList<T> =
        (current ?: ArrayList(2)).also { it += item }

    class HeldMap<K, V>(val map: HashMap<K, V>, val value: V)

    fun <K, V> child(current: HashMap<K, V>?, key: K, create: () -> V): HeldMap<K, V> {
        val map = current ?: HashMap(4)
        return HeldMap(map, map.getOrPut(key, create))
    }
}
