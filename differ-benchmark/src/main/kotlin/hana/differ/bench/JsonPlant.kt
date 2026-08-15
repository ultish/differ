package hana.differ.bench

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.flipkart.zjsonpatch.JsonDiff

object JsonPlant {
    fun mapper(): ObjectMapper = jacksonObjectMapper()

    fun diff(mapper: ObjectMapper, old: Plant, new: Plant): JsonNode {
        val oldNode = mapper.valueToTree<JsonNode>(old)
        val newNode = mapper.valueToTree<JsonNode>(new)
        return JsonDiff.asJson(oldNode, newNode)
    }
}
