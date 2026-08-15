package hana.differ.it

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FlagDocTest {

    @Test
    fun rootCaptureOffEmitsFlagNotValues() {
        val diff = FlagDocDiffer.diff(FlagDoc("1", "Ada"), FlagDoc("1", "Kim"))
        assertTrue(diff.hasChanged)
        assertEquals(FlagDocChange.Name, diff.changes.single())
    }
}
