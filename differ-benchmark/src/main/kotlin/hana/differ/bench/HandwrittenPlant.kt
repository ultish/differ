package hana.differ.bench

import hana.differ.DuplicateChildIdException
import java.util.HashMap

object HandwrittenPlant {
    class Result(val hasChanged: Boolean, val changeCount: Int)

    fun diff(old: Plant, new: Plant): Result {
        var n = 0
        if (old.name != new.name) n++
        if (old.setup != new.setup) n++
        if (old.status != new.status) n++
        n += siteFields(old.site, new.site)
        val oldFail = old.failover
        val newFail = new.failover
        if (oldFail != null && newFail != null) {
            n += siteFields(oldFail, newFail)
        } else if (oldFail != null || newFail != null) {
            n++
        }
        n += links(old.inputLinks, new.inputLinks)
        n += links(old.outputLinks, new.outputLinks)
        n += alarms(old.alarms, new.alarms)
        n += recipes(old.recipes, new.recipes)
        n += labels(old.labels, new.labels)
        return Result(n != 0, n)
    }

    private fun siteFields(old: Site, new: Site): Int {
        var n = 0
        if (old.name != new.name) n++
        if (old.region != new.region) n++
        if (old.address.street != new.address.street) n++
        if (old.address.city != new.address.city) n++
        if (old.address.postcode != new.address.postcode) n++
        return n
    }

    private fun links(oldItems: List<Link>, newItems: List<Link>): Int {
        val index = HashMap<String, Link>(oldItems.size * 2)
        for (item in oldItems) {
            if (index.put(item.id, item) != null) throw DuplicateChildIdException(item.id)
        }
        var n = 0
        for (item in newItems) {
            val prior = index.remove(item.id)
            if (prior == null) {
                n++
                continue
            }
            if (prior.state != item.state) n++
            if (prior.fromMachineId != item.fromMachineId) n++
            if (prior.toMachineId != item.toMachineId) n++
            if (prior.fromPort.name != item.fromPort.name) n++
            if (prior.fromPort.type != item.fromPort.type) n++
            if (prior.fromPort.voltage != item.fromPort.voltage) n++
            if (prior.toPort.name != item.toPort.name) n++
            if (prior.toPort.type != item.toPort.type) n++
            if (prior.toPort.voltage != item.toPort.voltage) n++
        }
        n += index.size
        return n
    }

    private fun alarms(oldItems: Set<Alarm>, newItems: Set<Alarm>): Int {
        val index = HashMap<String, Alarm>(oldItems.size * 2)
        for (item in oldItems) {
            if (index.put(item.id, item) != null) throw DuplicateChildIdException(item.id)
        }
        var n = 0
        for (item in newItems) {
            val prior = index.remove(item.id)
            if (prior == null) {
                n++
                continue
            }
            if (prior.severity != item.severity) n++
            if (prior.code != item.code) n++
        }
        n += index.size
        return n
    }

    private fun recipes(oldMap: Map<String, Recipe>, newMap: Map<String, Recipe>): Int {
        var n = 0
        for ((k, item) in newMap) {
            val prior = oldMap[k]
            if (prior == null) {
                n++
                continue
            }
            if (prior.version != item.version) n++
            if (prior.body != item.body) n++
        }
        for ((k, _) in oldMap) {
            if (k !in newMap) n++
        }
        return n
    }

    private fun labels(oldMap: Map<String, String>, newMap: Map<String, String>): Int {
        var n = 0
        for ((k, item) in newMap) {
            val prior = oldMap[k]
            if (prior == null) {
                n++
                continue
            }
            if (prior != item) n++
        }
        for ((k, _) in oldMap) {
            if (k !in newMap) n++
        }
        return n
    }
}
