package hana.differ.it

import hana.differ.Differ
import hana.differ.Tracked
import hana.differ.TrackedList
import hana.differ.TrackedNested

@Differ
data class OptionalDoc(
    @Tracked val title: String?,
    @TrackedNested val port: Port?,
    @TrackedList val connections: List<Connection>?,
)

data class OptionalRow(
    val id: String,
    @TrackedNested val fromPort: Port?,
)

@Differ
data class OptionalRows(
    @TrackedList val rows: List<OptionalRow>,
)
