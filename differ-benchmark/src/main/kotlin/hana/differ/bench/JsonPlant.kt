package hana.differ.bench

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import hana.differ.DifferSupport
import hana.differ.DuplicateChildIdException
import java.util.HashMap
import java.util.HashSet

object JsonPlant {
    class Result(val hasChanged: Boolean, val changes: List<Capture>)

    fun mapper(): ObjectMapper = jacksonObjectMapper()

    fun diff(mapper: ObjectMapper, old: Plant, new: Plant): Result {
        val oldNode = mapper.valueToTree<JsonNode>(old)
        val newNode = mapper.valueToTree<JsonNode>(new)
        val walk = Walk()
        walk.node("", oldNode, newNode, null)
        return Result(walk.changes != null, walk.changes ?: emptyList())
    }

    private class Walk {
        var changes: MutableList<Capture>? = null

        fun node(path: String, old: JsonNode, new: JsonNode, id: String?) {
            when {
                old.isObject && new.isObject -> obj(path, old, new, id)
                old.isArray && new.isArray -> arr(path, old, new)
                old != new -> capture(path, id, old, new)
            }
        }

        private fun obj(path: String, old: JsonNode, new: JsonNode, id: String?) {
            val names = LinkedHashSet<String>()
            old.fieldNames().forEachRemaining { names += it }
            new.fieldNames().forEachRemaining { names += it }
            for (name in names) {
                if (name == "id") continue
                val childPath = if (path.isEmpty()) name else "$path.$name"
                val prior = old.get(name)
                val next = new.get(name)
                when {
                    prior == null || prior.isNull -> capture(childPath, name, null, next)
                    next == null || next.isNull -> capture(childPath, name, prior, null)
                    prior.isObject && next.isObject -> {
                        val childId = textId(next) ?: textId(prior) ?: name
                        obj(childPath, prior, next, childId)
                    }
                    prior.isArray && next.isArray -> arr(childPath, prior, next)
                    prior != next -> capture(childPath, id ?: name, prior, next)
                }
            }
        }

        private fun arr(path: String, old: JsonNode, new: JsonNode) {
            val sample = when {
                new.size() > 0 -> new[0]
                old.size() > 0 -> old[0]
                else -> return
            }
            if (sample.isObject && sample.has("id")) {
                keyed(path, old, new)
                return
            }
            val shared = minOf(old.size(), new.size())
            for (i in 0 until shared) {
                node("$path[$i]", old[i], new[i], null)
            }
            for (i in shared until new.size()) capture(path, null, null, new[i])
            for (i in shared until old.size()) capture(path, null, old[i], null)
        }

        private fun keyed(path: String, old: JsonNode, new: JsonNode) {
            val index = HashMap<String, JsonNode>(old.size() * 2)
            for (item in old) {
                val key = item.get("id").asText()
                if (index.put(key, item) != null) throw DuplicateChildIdException(key)
            }
            val seen = HashSet<String>(new.size())
            for (item in new) {
                val key = item.get("id").asText()
                val prior = index[key]
                if (prior == null) {
                    capture(path, key, null, item)
                    continue
                }
                seen += key
                obj(path, prior, item, key)
            }
            for ((key, prior) in index) {
                if (key !in seen) capture(path, key, prior, null)
            }
        }

        private fun capture(path: String, id: String?, old: Any?, new: Any?) {
            changes = DifferSupport.add(changes, Capture(path, id, old, new))
        }
    }

    private fun textId(node: JsonNode): String? {
        val id = node.get("id") ?: return null
        if (!id.isTextual) return null
        return id.asText()
    }
}
