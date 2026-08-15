package hana.differ.bench

import hana.differ.DifferSupport
import hana.differ.DuplicateChildIdException
import hana.differ.ValueChange
import java.util.HashMap

class Capture(val path: String, val id: String?, val old: Any?, val new: Any?)

object HandwrittenPlant {
    class LinkFields {
        var state: ValueChange<LinkState>? = null
        var fromMachineId: ValueChange<String>? = null
        var toMachineId: ValueChange<String>? = null
        var fromPortName: ValueChange<String>? = null
        var fromPortType: ValueChange<String>? = null
        var fromPortVoltage: ValueChange<Int>? = null
        var toPortName: ValueChange<String>? = null
        var toPortType: ValueChange<String>? = null
        var toPortVoltage: ValueChange<Int>? = null
    }

    class AlarmFields {
        var severity: ValueChange<Int>? = null
        var code: ValueChange<String>? = null
    }

    class RecipeFields {
        var version: ValueChange<Int>? = null
    }

    class Result(
        val hasChanged: Boolean,
        val changes: List<Capture>,
        val inputLinks: Map<String, LinkFields>,
        val outputLinks: Map<String, LinkFields>,
        val alarms: Map<String, AlarmFields>,
        val recipes: Map<String, RecipeFields>,
        val labels: Map<String, ValueChange<String>>,
    )

    fun diff(old: Plant, new: Plant): Result {
        val walk = Walk()
        walk.plant(old, new)
        return walk.result()
    }

    private class Walk {
        var changes: MutableList<Capture>? = null
        var inputLinks: HashMap<String, LinkFields>? = null
        var outputLinks: HashMap<String, LinkFields>? = null
        var alarms: HashMap<String, AlarmFields>? = null
        var recipes: HashMap<String, RecipeFields>? = null
        var labels: HashMap<String, ValueChange<String>>? = null

        fun result() = Result(
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

        private fun links(
            path: String,
            oldItems: List<Link>,
            newItems: List<Link>,
            start: HashMap<String, LinkFields>?,
        ): HashMap<String, LinkFields>? {
            val index = HashMap<String, Link>(oldItems.size * 2)
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
                    val held = DifferSupport.child(bag, item.id) { LinkFields() }
                    bag = held.map
                    held.value.state = ValueChange(prior.state, item.state)
                }
                if (prior.fromMachineId != item.fromMachineId) {
                    capture("$path.fromMachineId", item.id, prior.fromMachineId, item.fromMachineId)
                    val held = DifferSupport.child(bag, item.id) { LinkFields() }
                    bag = held.map
                    held.value.fromMachineId = ValueChange(prior.fromMachineId, item.fromMachineId)
                }
                if (prior.toMachineId != item.toMachineId) {
                    capture("$path.toMachineId", item.id, prior.toMachineId, item.toMachineId)
                    val held = DifferSupport.child(bag, item.id) { LinkFields() }
                    bag = held.map
                    held.value.toMachineId = ValueChange(prior.toMachineId, item.toMachineId)
                }
                if (prior.fromPort.name != item.fromPort.name) {
                    capture("$path.fromPort.name", item.id, prior.fromPort.name, item.fromPort.name)
                    val held = DifferSupport.child(bag, item.id) { LinkFields() }
                    bag = held.map
                    held.value.fromPortName = ValueChange(prior.fromPort.name, item.fromPort.name)
                }
                if (prior.fromPort.type != item.fromPort.type) {
                    capture("$path.fromPort.type", item.id, prior.fromPort.type, item.fromPort.type)
                    val held = DifferSupport.child(bag, item.id) { LinkFields() }
                    bag = held.map
                    held.value.fromPortType = ValueChange(prior.fromPort.type, item.fromPort.type)
                }
                if (prior.fromPort.voltage != item.fromPort.voltage) {
                    capture("$path.fromPort.voltage", item.id, prior.fromPort.voltage, item.fromPort.voltage)
                    val held = DifferSupport.child(bag, item.id) { LinkFields() }
                    bag = held.map
                    held.value.fromPortVoltage = ValueChange(prior.fromPort.voltage, item.fromPort.voltage)
                }
                if (prior.toPort.name != item.toPort.name) {
                    capture("$path.toPort.name", item.id, prior.toPort.name, item.toPort.name)
                    val held = DifferSupport.child(bag, item.id) { LinkFields() }
                    bag = held.map
                    held.value.toPortName = ValueChange(prior.toPort.name, item.toPort.name)
                }
                if (prior.toPort.type != item.toPort.type) {
                    capture("$path.toPort.type", item.id, prior.toPort.type, item.toPort.type)
                    val held = DifferSupport.child(bag, item.id) { LinkFields() }
                    bag = held.map
                    held.value.toPortType = ValueChange(prior.toPort.type, item.toPort.type)
                }
                if (prior.toPort.voltage != item.toPort.voltage) {
                    capture("$path.toPort.voltage", item.id, prior.toPort.voltage, item.toPort.voltage)
                    val held = DifferSupport.child(bag, item.id) { LinkFields() }
                    bag = held.map
                    held.value.toPortVoltage = ValueChange(prior.toPort.voltage, item.toPort.voltage)
                }
            }
            for (item in index.values) {
                capture(path, item.id, item, null)
            }
            return bag
        }

        private fun alarms(oldItems: Set<Alarm>, newItems: Set<Alarm>) {
            val index = HashMap<String, Alarm>(oldItems.size * 2)
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
                    val held = DifferSupport.child(alarms, item.id) { AlarmFields() }
                    alarms = held.map
                    held.value.severity = ValueChange(prior.severity, item.severity)
                }
                if (prior.code != item.code) {
                    capture("alarms.code", item.id, prior.code, item.code)
                    val held = DifferSupport.child(alarms, item.id) { AlarmFields() }
                    alarms = held.map
                    held.value.code = ValueChange(prior.code, item.code)
                }
            }
            for (item in index.values) {
                capture("alarms", item.id, item, null)
            }
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
                    val held = DifferSupport.child(recipes, key) { RecipeFields() }
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
