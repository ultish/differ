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
        notes: String = "good girl",
        nickname: String? = null,
        owner: Person = this.owner,
        vet: Person? = null,
        toys: Set<Toy> = setOf(ball),
        walks: List<Walk> = emptyList(),
        tricks: Map<String, Trick> = emptyMap(),
        tags: Map<String, String> = emptyMap(),
    ) = Pet("p1", name, notes, nickname, owner, vet, toys, walks, tricks, tags)

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

    @Test
    fun manyKindsChangeTogether() {
        val old = pet(
            name = "Hana",
            notes = "v1",
            nickname = null,
            owner = Person("o1", "Ada", 3),
            vet = null,
            toys = setOf(ball),
            walks = listOf(Walk("w1", "park")),
            tricks = mapOf("sit" to Trick("sit", 1)),
            tags = mapOf("color" to "red"),
        )
        val new = pet(
            name = "Hana-chan",
            notes = "v2",
            nickname = "pup",
            owner = Person("o1", "Ada", 4),
            vet = Person("v1", "Kim", 40),
            toys = setOf(Toy("t1", "squeaky"), rope),
            walks = listOf(Walk("w1", "beach"), Walk("w2", "lake")),
            tricks = mapOf("sit" to Trick("sit", 2), "paw" to Trick("paw", 1)),
            tags = mapOf("color" to "cream", "size" to "shiba"),
        )
        val diff = PetDiffer.diff(old, new)

        assertTrue(diff.hasChanged)
        assertTrue(diff.hasName)
        assertTrue(diff.hasNotes)
        assertTrue(diff.hasNickname)
        assertTrue(diff.hasOwnerAge)
        assertTrue(diff.hasVet)
        assertFalse(diff.hasVetAge)
        assertTrue(diff.hasToysName)
        assertTrue(diff.hasToysAdded)
        assertFalse(diff.hasToysRemoved)
        assertTrue(diff.hasWalksPark)
        assertTrue(diff.hasWalksAdded)
        assertFalse(diff.hasWalksRemoved)
        assertTrue(diff.hasTricksLevel)
        assertTrue(diff.hasTricksAdded)
        assertTrue(diff.hasTags)
        assertTrue(diff.hasTagsAdded)

        assertIs<PetChange.Name>(diff.changes.first { it is PetChange.Name }).also {
            assertEquals("Hana", it.old)
            assertEquals("Hana-chan", it.new)
        }
        assertEquals(PetChange.Notes, diff.changes.first { it is PetChange.Notes })
        assertIs<PetChange.Nickname>(diff.changes.first { it is PetChange.Nickname }).also {
            assertEquals(null, it.old)
            assertEquals("pup", it.new)
        }
        assertIs<PetChange.OwnerAge>(diff.changes.first { it is PetChange.OwnerAge }).also {
            assertEquals(3, it.old)
            assertEquals(4, it.new)
        }
        assertIs<PetChange.Vet>(diff.changes.first { it is PetChange.Vet }).also {
            assertEquals(null, it.old)
            assertEquals(Person("v1", "Kim", 40), it.new)
        }
        assertIs<PetChange.ToysName>(diff.changes.first { it is PetChange.ToysName }).also {
            assertEquals("t1", it.id)
            assertEquals("ball", it.old)
            assertEquals("squeaky", it.new)
        }
        assertIs<PetChange.ToysAdded>(diff.changes.first { it is PetChange.ToysAdded }).also {
            assertEquals("t2", it.id)
        }
        assertIs<PetChange.WalksPark>(diff.changes.first { it is PetChange.WalksPark }).also {
            assertEquals("w1", it.id)
            assertEquals("park", it.old)
            assertEquals("beach", it.new)
        }
        assertIs<PetChange.WalksAdded>(diff.changes.first { it is PetChange.WalksAdded }).also {
            assertEquals("w2", it.id)
        }
        assertIs<PetChange.TricksLevel>(diff.changes.first { it is PetChange.TricksLevel }).also {
            assertEquals("sit", it.id)
            assertEquals(1, it.old)
            assertEquals(2, it.new)
        }
        assertIs<PetChange.TricksAdded>(diff.changes.first { it is PetChange.TricksAdded }).also {
            assertEquals("paw", it.id)
        }
        assertIs<PetChange.Tags>(diff.changes.first { it is PetChange.Tags }).also {
            assertEquals("color", it.id)
            assertEquals("red", it.old)
            assertEquals("cream", it.new)
        }
        assertIs<PetChange.TagsAdded>(diff.changes.first { it is PetChange.TagsAdded }).also {
            assertEquals("size", it.id)
            assertEquals("shiba", it.item)
        }

        assertEquals("squeaky", diff.toys.getValue("t1").name?.new)
        assertEquals("beach", diff.walks.getValue("w1").park?.new)
        assertEquals(2, diff.tricks.getValue("sit").level?.new)
        assertEquals("cream", diff.tags.getValue("color").value?.new)
    }
}
