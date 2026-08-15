package hana.differ.bench

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
class IdIndexTest {
    @Test
    fun putGetRemove() {
        val index = IdIndex<Int>(4)
        assertNull(index.put("a", 1))
        assertNull(index.put("b", 2))
        assertEquals(1, index.put("a", 9))
        assertEquals(1, index.remove("a"))
        assertNull(index.remove("a"))
        assertEquals(2, index.remove("b"))
    }

    @Test
    fun remainingAfterPartialRemove() {
        val index = IdIndex<String>(8)
        index.put("in-0", "A")
        index.put("in-1", "B")
        index.put("in-2", "C")
        index.remove("in-1")
        val left = ArrayList<String>()
        index.forEachRemaining { left += it }
        assertEquals(setOf("A", "C"), left.toSet())
    }

    @Test
    fun clearThenReuse() {
        val index = IdIndex<Int>(2)
        index.put("x", 1)
        index.clear()
        assertNull(index.remove("x"))
        assertNull(index.put("x", 2))
        assertEquals(2, index.remove("x"))
    }

    @Test
    fun growAndMatchMany() {
        val index = IdIndex<Int>(0)
        index.ensure(256)
        for (i in 0 until 256) {
            assertNull(index.put("in-$i", i))
        }
        assertEquals(200, index.remove("in-200"))
        var n = 0
        index.forEachRemaining { n++ }
        assertEquals(255, n)
    }

    @Test
    fun emptyRemove() {
        val index = IdIndex<Int>(4)
        assertNull(index.remove("missing"))
        var n = 0
        index.forEachRemaining { n++ }
        assertEquals(0, n)
    }
}
