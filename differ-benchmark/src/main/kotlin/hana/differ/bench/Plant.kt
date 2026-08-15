package hana.differ.bench

import hana.differ.Differ
import hana.differ.Tracked
import hana.differ.TrackedList
import hana.differ.TrackedMap
import hana.differ.TrackedNested

enum class LinkState { ENABLED, DISABLED }

data class Address(
    val id: String,
    @Tracked val street: String,
    @Tracked val city: String,
    @Tracked val postcode: String,
)

data class Site(
    val id: String,
    @Tracked val name: String,
    @Tracked val region: String,
    @TrackedNested val address: Address,
)

data class Port(
    val id: String,
    @Tracked val name: String,
    @Tracked val type: String,
    @Tracked val voltage: Int,
)

data class Link(
    val id: String,
    @Tracked val state: LinkState,
    @Tracked val fromMachineId: String,
    @Tracked val toMachineId: String,
    @TrackedNested val fromPort: Port,
    @TrackedNested val toPort: Port,
)

data class Alarm(
    val id: String,
    @Tracked val severity: Int,
    @Tracked val code: String,
)

data class Recipe(
    val id: String,
    @Tracked val version: Int,
    @Tracked(captureValues = false) val body: String,
)

@Differ
data class Plant(
    val id: String,
    @Tracked val name: String,
    @Tracked(captureValues = false) val setup: String,
    @Tracked val status: String?,
    @TrackedNested val site: Site,
    @TrackedNested val failover: Site?,
    @TrackedList val inputLinks: List<Link>,
    @TrackedList val outputLinks: List<Link>,
    @TrackedList val alarms: Set<Alarm>,
    @TrackedMap val recipes: Map<String, Recipe>,
    @TrackedMap val labels: Map<String, String>,
)
