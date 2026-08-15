package hana.differ.bench

fun plant(childCount: Int, flipMiddleInput: Boolean, reverseLinks: Boolean): Plant {
    val setup = """{"version":1,"cells":${childCount}}"""
    val site = site("s-main", "North", "Oslo")
    val failover = site("s-fail", "South", "Bergen")
    return Plant(
        id = "plant-1",
        name = "Press line",
        setup = setup,
        status = "live",
        site = site,
        failover = failover,
        inputLinks = links("in", childCount, flipMiddleInput, reverseLinks),
        outputLinks = links("out", childCount, flip = false, reverseLinks),
        alarms = alarms(childCount.coerceAtMost(32)),
        recipes = recipes(16),
        labels = mapOf(
            "line" to "A",
            "shift" to "night",
            "cell" to childCount.toString(),
        ),
    )
}

private fun site(id: String, region: String, city: String) = Site(
    id = id,
    name = "site-$id",
    region = region,
    address = Address(id = "addr-$id", street = "1 Dock", city = city, postcode = "0001"),
)

private fun links(prefix: String, count: Int, flip: Boolean, reverse: Boolean): List<Link> {
    val items = ArrayList<Link>(count)
    for (i in 0 until count) {
        val state = if (flip && i == count / 2) LinkState.DISABLED else LinkState.ENABLED
        items += Link(
            id = "$prefix-$i",
            state = state,
            fromMachineId = "m-from-$i",
            toMachineId = "m-to-$i",
            fromPort = Port("fp-$prefix-$i", "out", "power", 400),
            toPort = Port("tp-$prefix-$i", "in", "power", 400),
        )
    }
    if (reverse && count > 1) items.reverse()
    return items
}

private fun alarms(count: Int): Set<Alarm> =
    (0 until count).map { Alarm("a-$it", severity = it % 5, code = "E$it") }.toSet()

private fun recipes(count: Int): Map<String, Recipe> =
    (0 until count).associate { i ->
        val id = "r-$i"
        id to Recipe(id, version = i, body = """{"steps":$i}""")
    }
