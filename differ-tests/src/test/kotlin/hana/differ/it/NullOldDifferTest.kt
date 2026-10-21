package hana.differ.it

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class NullOldDifferTest {

    private fun pet(vet: Person? = null) = Pet(
        id = "p1",
        name = "Hana",
        notes = "good girl",
        nickname = null,
        owner = Person("o1", "Ada", 3),
        vet = vet,
        toys = setOf(Toy("t1", "ball"), Toy("t2", "rope")),
        walks = listOf(Walk("w1", "park")),
        tricks = mapOf("sit" to Trick("sit", 2)),
        tags = mapOf("color" to "brown"),
    )

    @Test
    fun everyBitIsSet() {
        val diff = PetDiffer.diff(null, pet(vet = Person("v1", "Bo", 40)))
        assertTrue(diff.hasChanged)
        assertTrue(diff.hasName)
        assertTrue(diff.hasNotes)
        assertTrue(diff.hasNickname)
        assertTrue(diff.hasOwnerAge)
        assertTrue(diff.hasVet)
        assertTrue(diff.hasVetAge)
        assertTrue(diff.hasToysAdded)
        assertTrue(diff.hasToysRemoved)
        assertTrue(diff.hasToysName)
        assertTrue(diff.hasWalksAdded)
        assertTrue(diff.hasWalksRemoved)
        assertTrue(diff.hasWalksPark)
        assertTrue(diff.hasTricksAdded)
        assertTrue(diff.hasTricksRemoved)
        assertTrue(diff.hasTricksLevel)
        assertTrue(diff.hasTagsAdded)
        assertTrue(diff.hasTagsRemoved)
        assertTrue(diff.hasTags)
    }

    @Test
    fun scalarsEmitEventsWithoutOldValue() {
        val changes = PetDiffer.diff(null, pet()).changes
        assertEquals("Hana", changes.filterIsInstance<PetChange.NameInitial>().single().new)
        assertEquals(3, changes.filterIsInstance<PetChange.OwnerAgeInitial>().single().new)
        assertEquals(null, changes.filterIsInstance<PetChange.NicknameInitial>().single().new)
        assertEquals(1, changes.filterIsInstance<PetChange.Notes>().size)
    }

    @Test
    fun nestedObjectEmitsPresenceAdded() {
        val vet = Person("v1", "Bo", 40)
        val changes = PetDiffer.diff(null, pet(vet = vet)).changes
        val presence = changes.filterIsInstance<PetChange.Vet>().single()
        assertEquals(null, presence.old)
        assertEquals(vet, presence.new)
        assertEquals(40, changes.filterIsInstance<PetChange.VetAgeInitial>().single().new)
    }

    @Test
    fun absentNestedObjectEmitsNoPresenceEvent() {
        val changes = PetDiffer.diff(null, pet(vet = null)).changes
        assertTrue(changes.none { it is PetChange.Vet })
    }

    @Test
    fun collectionsReportEveryEntryAsAdded() {
        val changes = PetDiffer.diff(null, pet()).changes
        assertEquals(setOf("t1", "t2"), changes.filterIsInstance<PetChange.ToysAdded>().map { it.id }.toSet())
        assertEquals("park", changes.filterIsInstance<PetChange.WalksAdded>().single().item.park)
        assertEquals(2, changes.filterIsInstance<PetChange.TricksAdded>().single().item.level)
        val tag = changes.filterIsInstance<PetChange.TagsAdded>().single()
        assertEquals("color", tag.id)
        assertEquals("brown", tag.item)
        assertTrue(changes.none { it is PetChange.ToysRemoved || it is PetChange.TagsRemoved })
    }

    @Test
    fun nonNullOldStillDiffs() {
        val diff = PetDiffer.diff(pet(), pet())
        assertEquals(false, diff.hasChanged)
        assertIs<PetChange.Name>(PetDiffer.diff(pet(), pet().copy(name = "Rex")).changes.single())
    }
}
