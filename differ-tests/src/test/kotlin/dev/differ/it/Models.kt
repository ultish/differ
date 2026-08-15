package dev.differ.it

import dev.differ.Differ
import dev.differ.Tracked
import dev.differ.TrackedList
import dev.differ.TrackedNested

enum class ConnectionState { ENABLED, DISABLED }

data class Port(
    val id: String,
    @Tracked val name: String,
    @Tracked val type: String,
)

data class Connection(
    val id: String,
    @Tracked val state: ConnectionState,
    @Tracked val fromMachineId: String,
    @TrackedNested val fromPort: Port,
)

@Differ
data class Machine(
    val id: String,
    val name: String,
    @Tracked(captureValues = false) val setup: String,
    @TrackedList val inputConnections: List<Connection>,
)

fun machine(
    id: String = "m1",
    name: String = "Press",
    setup: String = """{"v":1}""",
    connections: List<Connection> = listOf(connection()),
) = Machine(id, name, setup, connections)

fun connection(
    id: String = "c1",
    state: ConnectionState = ConnectionState.ENABLED,
    fromMachineId: String = "m-from",
    portName: String = "out",
    portType: String = "power",
) = Connection(
    id = id,
    state = state,
    fromMachineId = fromMachineId,
    fromPort = Port(id = "p-$id", name = portName, type = portType),
)
