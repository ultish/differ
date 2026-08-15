package hana.differ.bench

import org.javers.core.Javers
import org.javers.core.JaversBuilder
import org.javers.core.MappingStyle
import org.javers.core.diff.Diff
import org.javers.core.diff.ListCompareAlgorithm
import org.javers.core.diff.changetype.PropertyChange
import org.javers.core.metamodel.clazz.EntityDefinition

object JaversPlant {
    fun create(): Javers =
        JaversBuilder.javers()
            .withMappingStyle(MappingStyle.FIELD)
            .withListCompareAlgorithm(ListCompareAlgorithm.AS_SET)
            .registerEntity(EntityDefinition(Plant::class.java, "id"))
            .registerEntity(EntityDefinition(Link::class.java, "id"))
            .registerEntity(EntityDefinition(Alarm::class.java, "id"))
            .registerValueObject(Site::class.java)
            .registerValueObject(Address::class.java)
            .registerValueObject(Port::class.java)
            .registerValueObject(Recipe::class.java)
            .build()

    fun consume(diff: Diff): Boolean {
        var seen = false
        for (change in diff.changes) {
            if (change is PropertyChange<*>) {
                val left = change.left
                val right = change.right
                seen = seen or (left != right)
            }
        }
        return seen or diff.hasChanges()
    }
}
