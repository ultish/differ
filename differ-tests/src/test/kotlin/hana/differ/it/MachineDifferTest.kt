package hana.differ.it

import hana.differ.DuplicateChildIdException
import hana.differ.ValueChange
import kotlin.test.*

class MachineDifferTest {

    @Test
    fun unchangedIsCheapAndEmpty() {
        val a = machine()
        val b = machine()
        val diff = MachineDiffer.diff(a, b)
        assertFalse(diff.hasChanged)
        assertTrue(diff.changes.isEmpty())
        assertTrue(diff.inputConnections.isEmpty())
    }

    @Test
    fun reorderWithSameIdsIsUnchanged() {
        val old = machine(connections = listOf(connection("c1"), connection("c2")))
        val new = machine(connections = listOf(connection("c2"), connection("c1")))
        val diff = MachineDiffer.diff(old, new)
        assertFalse(diff.hasChanged)
    }

    @Test
    fun untaggedNameIsInvisible() {
        val diff = MachineDiffer.diff(machine(name = "A"), machine(name = "B"))
        assertFalse(diff.hasChanged)
    }

    @Test
    fun setupIsDetectedWithoutValues() {
        val diff = MachineDiffer.diff(machine(setup = """{"v":1}"""), machine(setup = """{"v":2}"""))
        assertTrue(diff.hasChanged)
        assertTrue(diff.hasSetup)
        assertEquals(listOf(MachineChange.Setup), diff.changes)
        assertTrue(diff.inputConnections.isEmpty())
    }

    @Test
    fun stateChangeIsTypedAndKeyed() {
        val old = machine()
        val new = machine(connections = listOf(connection(state = ConnectionState.DISABLED)))
        val diff = MachineDiffer.diff(old, new)



        assertTrue(diff.hasChanged)
        assertTrue(diff.hasInputConnectionsState)
        assertFalse(diff.hasSetup)

        val change = assertIs<MachineChange.InputConnectionsState>(diff.changes.single())
        assertEquals("c1", change.id)
        assertEquals(ConnectionState.ENABLED, change.old)
        assertEquals(ConnectionState.DISABLED, change.new)

        val delta = diff.inputConnections.getValue("c1")
        assertEquals(ValueChange(ConnectionState.ENABLED, ConnectionState.DISABLED), delta.state)
        assertNull(delta.fromMachineId)
        assertNull(delta.fromPortName)
    }

    @Test
    fun onlyChangedChildrenAppearInTheMap() {
        val old = machine(connections = listOf(connection("c1"), connection("c2")))
        val new = machine(
            connections = listOf(
                connection("c1"),
                connection("c2", state = ConnectionState.DISABLED),
            ),
        )
        val diff = MachineDiffer.diff(old, new)
        assertEquals(setOf("c2"), diff.inputConnections.keys)
        assertEquals(1, diff.changes.size)
    }

    @Test
    fun nestedPortName() {
        val old = machine()
        val new = machine(connections = listOf(connection(portName = "out-2")))
        val diff = MachineDiffer.diff(old, new)
        assertTrue(diff.hasInputConnectionsFromPortName)
        val change = assertIs<MachineChange.InputConnectionsFromPortName>(diff.changes.single())
        assertEquals("c1", change.id)
        assertEquals("out", change.old)
        assertEquals("out-2", change.new)
        assertEquals(ValueChange("out", "out-2"), diff.inputConnections.getValue("c1").fromPortName)
    }

    @Test
    fun addedAndRemoved() {
        val old = machine(connections = listOf(connection("c1")))
        val new = machine(connections = listOf(connection("c2")))
        val diff = MachineDiffer.diff(old, new)
        assertTrue(diff.hasInputConnectionsAdded)
        assertTrue(diff.hasInputConnectionsRemoved)
        assertIs<MachineChange.InputConnectionsAdded>(diff.changes[0]).also {
            assertEquals("c2", it.id)
            assertEquals("c2", it.item.id)
        }
        assertIs<MachineChange.InputConnectionsRemoved>(diff.changes[1]).also {
            assertEquals("c1", it.id)
        }
        assertTrue(diff.inputConnections.isEmpty())
    }

    @Test
    fun duplicateIdThrows() {
        val bad = machine(connections = listOf(connection("c1"), connection("c1")))
        assertFailsWith<DuplicateChildIdException> {
            MachineDiffer.diff(bad, machine())
        }
    }
}
