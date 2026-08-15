package hana.differ.it

import hana.differ.Differ
import hana.differ.Tracked
import hana.differ.TrackedList
import hana.differ.TrackedNested


@Differ
data class Pet(
    val id: String,
    @Tracked val name: String,
    @TrackedNested val owner: Person,
    @TrackedList val toys: Set<Toy>
)

data class Person(val id: String, val name: String, @Tracked val age: Int)

data class Toy(val id: String, @Tracked val name: String)

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
