package hana.differ.bench

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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

        assertFalse(differ.hasChanged)
        assertFalse(handwritten.hasChanged)
        assertEquals(0, handwritten.changeCount)
        assertEquals(0, differ.changes.size)
        assertFalse(javers.hasChanges())
        assertTrue(json.size() > 0)
    }

    @Test
    fun oneLinkChangedReversedNew() {
        val old = plant(4, flipMiddleInput = false, reverseLinks = false)
        val new = plant(4, flipMiddleInput = true, reverseLinks = true)
        val differ = PlantDiffer.diff(old, new)
        val handwritten = HandwrittenPlant.diff(old, new)
        val javers = JaversPlant.create().compare(old, new)
        val json = JsonPlant.diff(JsonPlant.mapper(), old, new)

        assertTrue(differ.hasChanged)
        assertTrue(handwritten.hasChanged)
        assertTrue(javers.hasChanges())
        assertTrue(json.size() > 0)
        assertEquals(differ.changes.size, handwritten.changeCount)
    }
}
