package hana.differ.it

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RemovedBitMaskTest {

    @Test
    fun removedBitInTheTopOfAFullCompactMaskStaysClear() {
        val ints = Array(62) { 0 }
        val diff = Top64Differ.diff(null, top64(ints, listOf(Top64Item("a"))))
        assertTrue(diff.hasChanged)
        assertTrue(diff.hasF00)
        assertTrue(diff.hasF61)
        assertTrue(diff.hasItemsAdded)
        assertFalse(diff.hasItemsRemoved)
    }

    @Test
    fun removedBitInAWideMaskStaysClear() {
        val diff = Wide72Differ.diff(null, wide72(listOf(Top64Item("a"))))
        assertTrue(diff.hasChanged)
        assertTrue(diff.hasF00)
        assertTrue(diff.hasF69)
        assertTrue(diff.hasItemsAdded)
        assertFalse(diff.hasItemsRemoved)
    }

    private fun top64(ints: Array<Int>, items: List<Top64Item>): Top64 {
        val c = Top64::class.java.constructors.single()
        return c.newInstance(*ints, items) as Top64
    }

    private fun wide72(items: List<Top64Item>): Wide72 {
        val c = Wide72::class.java.constructors.single()
        return c.newInstance(*Array<Any>(70) { 0 }, items) as Wide72
    }
}
