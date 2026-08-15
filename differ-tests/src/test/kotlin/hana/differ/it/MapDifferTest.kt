package hana.differ.it

import hana.differ.ValueChange
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class MapDifferTest {

    private val ball = Toy("t1", "ball")
    private val rope = Toy("t2", "rope")

    @Test
    fun objectMapFieldChange() {
        val old = Chest(mapOf("t1" to ball), emptyMap())
        val new = Chest(mapOf("t1" to Toy("t1", "squeaky")), emptyMap())
        val diff = ChestDiffer.diff(old, new)
        assertTrue(diff.hasToysName)
        val change = assertIs<ChestChange.ToysName>(diff.changes.single())
        assertEquals("t1", change.id)
        assertEquals("ball", change.old)
        assertEquals("squeaky", change.new)
        assertEquals(ValueChange("ball", "squeaky"), diff.toys.getValue("t1").name)
    }

    @Test
    fun objectMapAddAndRemove() {
        val old = Chest(mapOf("t1" to ball), emptyMap())
        val new = Chest(mapOf("t2" to rope), emptyMap())
        val diff = ChestDiffer.diff(old, new)
        assertTrue(diff.hasToysAdded)
        assertTrue(diff.hasToysRemoved)
        assertIs<ChestChange.ToysAdded>(diff.changes[0]).also { assertEquals("t2", it.id) }
        assertIs<ChestChange.ToysRemoved>(diff.changes[1]).also { assertEquals("t1", it.id) }
    }

    @Test
    fun scalarMapValueChange() {
        val old = Chest(emptyMap(), mapOf("color" to "red"))
        val new = Chest(emptyMap(), mapOf("color" to "blue"))
        val diff = ChestDiffer.diff(old, new)
        assertTrue(diff.hasTags)
        val change = assertIs<ChestChange.Tags>(diff.changes.single())
        assertEquals("color", change.id)
        assertEquals("red", change.old)
        assertEquals("blue", change.new)
        assertEquals(ValueChange("red", "blue"), diff.tags.getValue("color").value)
    }

    @Test
    fun scalarMapAdd() {
        val old = Chest(emptyMap(), emptyMap())
        val new = Chest(emptyMap(), mapOf("color" to "red"))
        val diff = ChestDiffer.diff(old, new)
        assertTrue(diff.hasTagsAdded)
        assertIs<ChestChange.TagsAdded>(diff.changes.single()).also {
            assertEquals("color", it.id)
            assertEquals("red", it.item)
        }
    }

    @Test
    fun treeSetAndTreeMap() {
        val old = SortedChest(toyTreeSet(ball, rope), toyTreeMap(ball, rope))
        val new = SortedChest(
            toyTreeSet(rope, ball),
            toyTreeMap(Toy("t1", "squeaky"), rope),
        )
        val diff = SortedChestDiffer.diff(old, new)
        assertFalse(diff.hasToySetAdded)
        assertFalse(diff.hasToySetRemoved)
        assertTrue(diff.hasToyMapName)
        val change = assertIs<SortedChestChange.ToyMapName>(diff.changes.single())
        assertEquals("t1", change.id)
        assertEquals("ball", change.old)
        assertEquals("squeaky", change.new)
    }
}
