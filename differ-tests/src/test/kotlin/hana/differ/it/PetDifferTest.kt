package hana.differ.it

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PetDifferTest {

    private val owner = Person("o1", "Ada", 3)
    private val ball = Toy("t1", "ball")
    private val rope = Toy("t2", "rope")

    private fun pet(
        name: String = "Hana",
        owner: Person = this.owner,
        toys: Set<Toy> = setOf(ball),
    ) = Pet("p1", name, owner, toys)

    @Test
    fun setReorderIsUnchanged() {
        val diff = PetDiffer.diff(pet(toys = setOf(ball, rope)), pet(toys = setOf(rope, ball)))
        assertFalse(diff.hasChanged)
    }

    @Test
    fun setAddAndRemove() {
        val diff = PetDiffer.diff(pet(toys = setOf(ball)), pet(toys = setOf(rope)))
        assertTrue(diff.hasToysAdded)
        assertTrue(diff.hasToysRemoved)
        assertIs<PetChange.ToysAdded>(diff.changes[0]).also { assertEquals("t2", it.id) }
        assertIs<PetChange.ToysRemoved>(diff.changes[1]).also { assertEquals("t1", it.id) }
    }

    @Test
    fun setFieldChange() {
        val diff = PetDiffer.diff(pet(), pet(toys = setOf(Toy("t1", "squeaky"))))
        assertTrue(diff.hasToysName)
        val change = assertIs<PetChange.ToysName>(diff.changes.single())
        assertEquals("t1", change.id)
        assertEquals("ball", change.old)
        assertEquals("squeaky", change.new)
    }

    @Test
    fun nestedOwnerStillWorks() {
        val diff = PetDiffer.diff(pet(), pet(owner = Person("o1", "Ada", 4)))
        assertTrue(diff.hasOwnerAge)
        val change = assertIs<PetChange.OwnerAge>(diff.changes.single())
        assertEquals(3, change.old)
        assertEquals(4, change.new)
    }
}
