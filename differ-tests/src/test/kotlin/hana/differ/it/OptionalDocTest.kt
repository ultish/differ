package hana.differ.it

import hana.differ.ValueChange
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OptionalDocTest {

    private val port = Port("p1", "out", "power")

    private fun doc(
        title: String? = null,
        port: Port? = null,
        connections: List<Connection>? = null,
    ) = OptionalDoc(title, port, connections)

    @Test
    fun bothNullIsUnchanged() {
        val diff = OptionalDocDiffer.diff(doc(), doc())
        assertFalse(diff.hasChanged)
        assertTrue(diff.changes.isEmpty())
    }

    @Test
    fun nullableScalarToNull() {
        val diff = OptionalDocDiffer.diff(doc(title = "a"), doc(title = null))
        assertTrue(diff.hasTitle)
        val change = assertIs<OptionalDocChange.Title>(diff.changes.single())
        assertEquals("a", change.old)
        assertNull(change.new)
    }

    @Test
    fun nullableScalarFromNull() {
        val diff = OptionalDocDiffer.diff(doc(title = null), doc(title = "a"))
        val change = assertIs<OptionalDocChange.Title>(diff.changes.single())
        assertNull(change.old)
        assertEquals("a", change.new)
    }

    @Test
    fun nestedBothNullIsUnchanged() {
        val diff = OptionalDocDiffer.diff(doc(port = null), doc(port = null))
        assertFalse(diff.hasChanged)
    }

    @Test
    fun nestedObjectToNullIsPresence() {
        val diff = OptionalDocDiffer.diff(doc(port = port), doc(port = null))
        assertTrue(diff.hasPort)
        assertFalse(diff.hasPortName)
        val change = assertIs<OptionalDocChange.Port>(diff.changes.single())
        assertEquals(port, change.old)
        assertNull(change.new)
    }

    @Test
    fun nestedObjectFromNullIsPresence() {
        val diff = OptionalDocDiffer.diff(doc(port = null), doc(port = port))
        val change = assertIs<OptionalDocChange.Port>(diff.changes.single())
        assertNull(change.old)
        assertEquals(port, change.new)
    }

    @Test
    fun nestedBothPresentIsLeafChangeOnly() {
        val other = Port("p1", "in", "power")
        val diff = OptionalDocDiffer.diff(doc(port = port), doc(port = other))
        assertTrue(diff.hasPortName)
        assertFalse(diff.hasPort)
        val change = assertIs<OptionalDocChange.PortName>(diff.changes.single())
        assertEquals("out", change.old)
        assertEquals("in", change.new)
    }

    @Test
    fun nullListEqualsEmptyList() {
        val empty = OptionalDocDiffer.diff(doc(connections = null), doc(connections = emptyList()))
        assertFalse(empty.hasChanged)
        val reverse = OptionalDocDiffer.diff(doc(connections = emptyList()), doc(connections = null))
        assertFalse(reverse.hasChanged)
    }

    @Test
    fun nullListVsItemIsAdd() {
        val child = connection()
        val diff = OptionalDocDiffer.diff(doc(connections = null), doc(connections = listOf(child)))
        assertTrue(diff.hasConnectionsAdded)
        val change = assertIs<OptionalDocChange.ConnectionsAdded>(diff.changes.single())
        assertEquals("c1", change.id)
    }

    @Test
    fun itemVsNullListIsRemove() {
        val child = connection()
        val diff = OptionalDocDiffer.diff(doc(connections = listOf(child)), doc(connections = null))
        assertTrue(diff.hasConnectionsRemoved)
        val change = assertIs<OptionalDocChange.ConnectionsRemoved>(diff.changes.single())
        assertEquals("c1", change.id)
        assertEquals(child, change.item)
    }

    @Test
    fun nestedPresenceUnderListUpdatesTheMap() {
        val wrapOld = OptionalRows(listOf(OptionalRow("r1", port)))
        val wrapNew = OptionalRows(listOf(OptionalRow("r1", null)))
        val diff = OptionalRowsDiffer.diff(wrapOld, wrapNew)
        assertTrue(diff.hasRowsFromPort)
        val change = assertIs<OptionalRowsChange.RowsFromPort>(diff.changes.single())
        assertEquals("r1", change.id)
        assertEquals(port, change.old)
        assertNull(change.new)
        assertEquals(ValueChange(port, null), diff.rows.getValue("r1").fromPort)
    }
}
