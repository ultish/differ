package hana.differ.bench

import hana.differ.DifferSupport
import hana.differ.DuplicateChildIdException
import hana.differ.ValueChange
import java.util.HashMap

object OpenIndexPlant {
    fun diff(old: Plant, new: Plant, pool: IndexPool? = null): HandwrittenPlant.Result {
        val walk = Walk(pool)
        walk.plant(old, new)
        return walk.result()
    }

    private class Walk(private val pool: IndexPool?) {
        var changes: MutableList<Capture>? = null
        var inputLinks: HashMap<String, HandwrittenPlant.LinkFields>? = null
        var outputLinks: HashMap<String, HandwrittenPlant.LinkFields>? = null
        var alarms: HashMap<String, HandwrittenPlant.AlarmFields>? = null
        var recipes: HashMap<String, HandwrittenPlant.RecipeFields>? = null
        var labels: HashMap<String, ValueChange<String>>? = null

        fun result() = HandwrittenPlant.Result(
            hasChanged = changes != null,
            changes = changes ?: emptyList(),
            inputLinks = inputLinks ?: emptyMap(),
            outputLinks = outputLinks ?: emptyMap(),
            alarms = alarms ?: emptyMap(),
            recipes = recipes ?: emptyMap(),
            labels = labels ?: emptyMap(),
        )

        fun plant(old: Plant, new: Plant) {
            if (old.name != new.name) capture("name", null, old.name, new.name)
            if (old.setup != new.setup) capture("setup", null, null, null)
            if (old.status != new.status) capture("status", null, old.status, new.status)
            site("site", old.site, new.site)
            val oldFail = old.failover
            val newFail = new.failover
            if (oldFail != null && newFail != null) {
                site("failover", oldFail, newFail)
            } else if (oldFail != null || newFail != null) {
                capture("failover", null, oldFail, newFail)
            }
            inputLinks = links("inputLinks", old.inputLinks, new.inputLinks, inputLinks)
            outputLinks = links("outputLinks", old.outputLinks, new.outputLinks, outputLinks)
            alarms(old.alarms, new.alarms)
            recipes(old.recipes, new.recipes)
            labels(old.labels, new.labels)
        }

        private fun site(prefix: String, old: Site, new: Site) {
            if (old.name != new.name) capture("$prefix.name", null, old.name, new.name)
            if (old.region != new.region) capture("$prefix.region", null, old.region, new.region)
            if (old.address.street != new.address.street) {
                capture("$prefix.address.street", null, old.address.street, new.address.street)
            }
            if (old.address.city != new.address.city) {
                capture("$prefix.address.city", null, old.address.city, new.address.city)
            }
            if (old.address.postcode != new.address.postcode) {
                capture("$prefix.address.postcode", null, old.address.postcode, new.address.postcode)
            }
        }

        private fun linkIndex(size: Int): IdIndex<Link> {
            val pooled = pool?.links
            if (pooled != null) {
                pooled.ensure(size)
                pooled.clear()
                return pooled
            }
            return IdIndex(size)
        }

        private fun alarmIndex(size: Int): IdIndex<Alarm> {
            val pooled = pool?.alarms
            if (pooled != null) {
                pooled.ensure(size)
                pooled.clear()
                return pooled
            }
            return IdIndex(size)
        }

        private fun links(
            path: String,
            oldItems: List<Link>,
            newItems: List<Link>,
            start: HashMap<String, HandwrittenPlant.LinkFields>?,
        ): HashMap<String, HandwrittenPlant.LinkFields>? {
            val index = linkIndex(oldItems.size)
            for (item in oldItems) {
                if (index.put(item.id, item) != null) throw DuplicateChildIdException(item.id)
            }
            var bag = start
            for (item in newItems) {
                val prior = index.remove(item.id)
                if (prior == null) {
                    capture(path, item.id, null, item)
                    continue
                }
                if (prior.state != item.state) {
                    capture("$path.state", item.id, prior.state, item.state)
                    val held = DifferSupport.child(bag, item.id) { HandwrittenPlant.LinkFields() }
                    bag = held.map
                    held.value.state = ValueChange(prior.state, item.state)
                }
                if (prior.fromMachineId != item.fromMachineId) {
                    capture("$path.fromMachineId", item.id, prior.fromMachineId, item.fromMachineId)
                    val held = DifferSupport.child(bag, item.id) { HandwrittenPlant.LinkFields() }
                    bag = held.map
                    held.value.fromMachineId = ValueChange(prior.fromMachineId, item.fromMachineId)
                }
                if (prior.toMachineId != item.toMachineId) {
                    capture("$path.toMachineId", item.id, prior.toMachineId, item.toMachineId)
                    val held = DifferSupport.child(bag, item.id) { HandwrittenPlant.LinkFields() }
                    bag = held.map
                    held.value.toMachineId = ValueChange(prior.toMachineId, item.toMachineId)
                }
                if (prior.fromPort.name != item.fromPort.name) {
                    capture("$path.fromPort.name", item.id, prior.fromPort.name, item.fromPort.name)
                    val held = DifferSupport.child(bag, item.id) { HandwrittenPlant.LinkFields() }
                    bag = held.map
                    held.value.fromPortName = ValueChange(prior.fromPort.name, item.fromPort.name)
                }
                if (prior.fromPort.type != item.fromPort.type) {
                    capture("$path.fromPort.type", item.id, prior.fromPort.type, item.fromPort.type)
                    val held = DifferSupport.child(bag, item.id) { HandwrittenPlant.LinkFields() }
                    bag = held.map
                    held.value.fromPortType = ValueChange(prior.fromPort.type, item.fromPort.type)
                }
                if (prior.fromPort.voltage != item.fromPort.voltage) {
                    capture("$path.fromPort.voltage", item.id, prior.fromPort.voltage, item.fromPort.voltage)
                    val held = DifferSupport.child(bag, item.id) { HandwrittenPlant.LinkFields() }
                    bag = held.map
                    held.value.fromPortVoltage = ValueChange(prior.fromPort.voltage, item.fromPort.voltage)
                }
                if (prior.toPort.name != item.toPort.name) {
                    capture("$path.toPort.name", item.id, prior.toPort.name, item.toPort.name)
                    val held = DifferSupport.child(bag, item.id) { HandwrittenPlant.LinkFields() }
                    bag = held.map
                    held.value.toPortName = ValueChange(prior.toPort.name, item.toPort.name)
                }
                if (prior.toPort.type != item.toPort.type) {
                    capture("$path.toPort.type", item.id, prior.toPort.type, item.toPort.type)
                    val held = DifferSupport.child(bag, item.id) { HandwrittenPlant.LinkFields() }
                    bag = held.map
                    held.value.toPortType = ValueChange(prior.toPort.type, item.toPort.type)
                }
                if (prior.toPort.voltage != item.toPort.voltage) {
                    capture("$path.toPort.voltage", item.id, prior.toPort.voltage, item.toPort.voltage)
                    val held = DifferSupport.child(bag, item.id) { HandwrittenPlant.LinkFields() }
                    bag = held.map
                    held.value.toPortVoltage = ValueChange(prior.toPort.voltage, item.toPort.voltage)
                }
            }
            index.forEachRemaining { item -> capture(path, item.id, item, null) }
            return bag
        }

        private fun alarms(oldItems: Set<Alarm>, newItems: Set<Alarm>) {
            val index = alarmIndex(oldItems.size)
            for (item in oldItems) {
                if (index.put(item.id, item) != null) throw DuplicateChildIdException(item.id)
            }
            for (item in newItems) {
                val prior = index.remove(item.id)
                if (prior == null) {
                    capture("alarms", item.id, null, item)
                    continue
                }
                if (prior.severity != item.severity) {
                    capture("alarms.severity", item.id, prior.severity, item.severity)
                    val held = DifferSupport.child(alarms, item.id) { HandwrittenPlant.AlarmFields() }
                    alarms = held.map
                    held.value.severity = ValueChange(prior.severity, item.severity)
                }
                if (prior.code != item.code) {
                    capture("alarms.code", item.id, prior.code, item.code)
                    val held = DifferSupport.child(alarms, item.id) { HandwrittenPlant.AlarmFields() }
                    alarms = held.map
                    held.value.code = ValueChange(prior.code, item.code)
                }
            }
            index.forEachRemaining { item -> capture("alarms", item.id, item, null) }
        }

        private fun recipes(oldMap: Map<String, Recipe>, newMap: Map<String, Recipe>) {
            for ((key, item) in newMap) {
                val prior = oldMap[key]
                if (prior == null) {
                    capture("recipes", key, null, item)
                    continue
                }
                if (prior.version != item.version) {
                    capture("recipes.version", key, prior.version, item.version)
                    val held = DifferSupport.child(recipes, key) { HandwrittenPlant.RecipeFields() }
                    recipes = held.map
                    held.value.version = ValueChange(prior.version, item.version)
                }
                if (prior.body != item.body) capture("recipes.body", key, null, null)
            }
            for ((key, prior) in oldMap) {
                if (key !in newMap) capture("recipes", key, prior, null)
            }
        }

        private fun labels(oldMap: Map<String, String>, newMap: Map<String, String>) {
            for ((key, item) in newMap) {
                val prior = oldMap[key]
                if (prior == null) {
                    capture("labels", key, null, item)
                    continue
                }
                if (prior != item) {
                    capture("labels", key, prior, item)
                    val map = labels ?: HashMap(4)
                    map[key] = ValueChange(prior, item)
                    labels = map
                }
            }
            for ((key, prior) in oldMap) {
                if (key !in newMap) capture("labels", key, prior, null)
            }
        }

        private fun capture(path: String, id: String?, old: Any?, new: Any?) {
            changes = DifferSupport.add(changes, Capture(path, id, old, new))
        }
    }
}
