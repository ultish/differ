package hana.differ.it

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SkipWhenNewTest {

    private fun doc(reviewed: Boolean = false, flagged: Boolean = false, verified: Boolean = false) = SkipDoc(
        title = "t",
        reviewed = reviewed,
        lines = listOf(SkipLine("l1", "first", flagged)),
        detail = SkipDetail("Oslo", verified),
    )

    @Test
    fun rootPropertyIsSkippedForNullOld() {
        val diff = SkipDocDiffer.diff(null, doc(reviewed = true))
        assertTrue(diff.hasChanged)
        assertTrue(diff.hasTitle)
        assertFalse(diff.hasReviewed)
        assertTrue(diff.changes.filterIsInstance<SkipDocChange.TitleInitial>().isNotEmpty())
        assertTrue(diff.changes.none { it is SkipDocChange.Reviewed || it is SkipDocChange.ReviewedInitial })
    }

    @Test
    fun rootPropertyStillDiffsAgainstNonNullOld() {
        val diff = SkipDocDiffer.diff(doc(reviewed = false), doc(reviewed = true))
        assertTrue(diff.hasReviewed)
        val change = diff.changes.filterIsInstance<SkipDocChange.Reviewed>().single()
        assertEquals(false, change.old)
        assertEquals(true, change.new)
    }

    @Test
    fun listElementPropertyIsSkippedForNullOld() {
        val diff = SkipDocDiffer.diff(null, doc(flagged = true))
        assertTrue(diff.hasLinesAdded)
        assertTrue(diff.hasLinesLabel)
        assertFalse(diff.hasLinesFlagged)
        assertTrue(diff.changes.none { it.javaClass.simpleName.startsWith("LinesFlagged") })
    }

    @Test
    fun listElementPropertyStillDiffsAgainstNonNullOld() {
        val diff = SkipDocDiffer.diff(doc(flagged = false), doc(flagged = true))
        assertTrue(diff.hasLinesFlagged)
        val flagged = diff.lines.getValue("l1").flagged
        assertEquals(false, flagged?.old)
        assertEquals(true, flagged?.new)
    }

    @Test
    fun nestedPropertyIsSkippedForNullOld() {
        val diff = SkipDocDiffer.diff(null, doc(verified = true))
        assertTrue(diff.hasDetailCity)
        assertFalse(diff.hasDetailVerified)
        assertTrue(diff.changes.none { it is SkipDocChange.DetailVerified || it is SkipDocChange.DetailVerifiedInitial })
    }

    @Test
    fun nestedPropertyStillDiffsAgainstNonNullOld() {
        val diff = SkipDocDiffer.diff(doc(verified = false), doc(verified = true))
        assertTrue(diff.hasDetailVerified)
        assertIs<SkipDocChange.DetailVerified>(diff.changes.single())
    }
}
