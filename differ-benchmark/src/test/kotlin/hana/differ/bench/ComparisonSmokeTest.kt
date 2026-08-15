package hana.differ.bench

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ComparisonSmokeTest {
    @Test
    fun unchangedReversedNew() {
        val old = plant(4, flipMiddleInput = false, reverseLinks = false)
        val new = plant(4, flipMiddleInput = false, reverseLinks = true)
        val differ = PlantDiffer.diff(old, new)
        val handwritten = HandwrittenPlant.diff(old, new)
        val javers = JaversPlant.create().compare(old, new)
        val json = JsonPlant.diff(JsonPlant.mapper(), old, new)
        val flag = PlantFlagDiffer.diff(old.toFlag(), new.toFlag())

        assertFalse(differ.hasChanged)
        assertFalse(handwritten.hasChanged)
        assertFalse(JaversPlant.consume(javers))
        assertFalse(json.hasChanged)
        assertFalse(flag.hasChanged)
        assertTrue(differ.changes.isEmpty())
        assertTrue(handwritten.changes.isEmpty())
        assertTrue(json.changes.isEmpty())
        assertTrue(flag.changes.isEmpty())
        assertFalse(OpenIndexPlant.diff(old, new).hasChanged)
        assertFalse(OpenIndexPlant.diff(old, new, IndexPool()).hasChanged)
    }

    @Test
    fun oneLinkChangedReversedNew() {
        val old = plant(4, flipMiddleInput = false, reverseLinks = false)
        val new = plant(4, flipMiddleInput = true, reverseLinks = true)
        val differ = PlantDiffer.diff(old, new)
        val handwritten = HandwrittenPlant.diff(old, new)
        val javers = JaversPlant.create().compare(old, new)
        val json = JsonPlant.diff(JsonPlant.mapper(), old, new)
        val flag = PlantFlagDiffer.diff(old.toFlag(), new.toFlag())

        assertTrue(differ.hasChanged)
        assertTrue(handwritten.hasChanged)
        assertTrue(JaversPlant.consume(javers))
        assertTrue(json.hasChanged)
        assertTrue(flag.hasChanged)
        assertEquals(differ.changes.size, handwritten.changes.size)

        val capture = handwritten.changes.single { it.old == LinkState.ENABLED && it.new == LinkState.DISABLED }
        assertEquals(LinkState.ENABLED, capture.old)
        assertEquals(LinkState.DISABLED, capture.new)

        val state = assertIs<PlantChange.InputLinksState>(differ.changes.single { it is PlantChange.InputLinksState })
        assertEquals(LinkState.ENABLED, state.old)
        assertEquals(LinkState.DISABLED, state.new)

        assertEquals(PlantFlagChange.InputLinksState, flag.changes.single { it is PlantFlagChange.InputLinksState })

        val open = OpenIndexPlant.diff(old, new)
        val reused = OpenIndexPlant.diff(old, new, IndexPool())
        assertEquals(handwritten.changes.size, open.changes.size)
        assertEquals(handwritten.changes.size, reused.changes.size)
        val openState = open.changes.single { it.old == LinkState.ENABLED && it.new == LinkState.DISABLED }
        assertEquals(LinkState.ENABLED, openState.old)
        assertEquals(LinkState.DISABLED, openState.new)
        assertTrue(
            json.changes.any { change ->
                (change.id == "in-2" || change.path.contains("inputLinks")) &&
                    change.path.contains("state")
            },
        )
    }

    @Test
    fun addAndRemoveMatchHandwritten() {
        val old = plant(4, flipMiddleInput = false, reverseLinks = false)
        val grown = plant(4, flipMiddleInput = false, reverseLinks = true)
        val sample = grown.inputLinks.first()
        val new = grown.copy(
            inputLinks = grown.inputLinks.filter { it.id != "in-0" } +
                Link("in-new", LinkState.ENABLED, "m", "n", sample.fromPort, sample.toPort),
        )
        val handwritten = HandwrittenPlant.diff(old, new)
        val open = OpenIndexPlant.diff(old, new)
        assertEquals(handwritten.changes.size, open.changes.size)
        assertTrue(handwritten.changes.any { it.id == "in-0" && it.new == null })
        assertTrue(open.changes.any { it.id == "in-0" && it.new == null })
        assertTrue(handwritten.changes.any { it.id == "in-new" && it.old == null })
        assertTrue(open.changes.any { it.id == "in-new" && it.old == null })
    }
}
