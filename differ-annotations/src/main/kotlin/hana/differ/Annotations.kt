package hana.differ

/**
 * Generate a `*Differ` for this class. Both sides of the comparison are this
 * type. Convert wire formats at the boundary. The generated walk is same-type
 * only.
 *
 * [name] overrides the generated object (`MachineDiffer` by default).
 *
 * [captureValues] defaults to true. When false, every scalar in this differ
 * is treated as [@Tracked] with `captureValues = false`, including inlined
 * nested, list, and map leaves. A per-field `true` cannot override a false
 * root.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Differ(val name: String = "", val captureValues: Boolean = true)

/**
 * Compare this property. Untagged properties are invisible.
 *
 * [captureValues] controls only whether a change produces a typed event with
 * old/new. Detection always runs. Use `false` for blobs you must flag but
 * must not copy.
 *
 * [skipWhenNew] defaults to false. When true and `diff(old, new)` gets a `null`
 * old value, this property sets no `has*` bit and emits no change event, so a
 * default-valued flag does not read as a change on a first save. With a
 * non-null old value the property is compared as usual. Works on root
 * properties, on [@TrackedList] / [@TrackedMap] element properties and on
 * [@TrackedNested] properties.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Tracked(val captureValues: Boolean = true, val skipWhenNew: Boolean = false)

/**
 * Compare this [List] or [Set] by [matchBy], never by iteration order.
 *
 * `SortedSet` / `TreeSet` count as a Set. The element type does not need
 * [@Differ]. Its [@Tracked] / [@TrackedNested] / [@TrackedList] properties
 * are inlined into the parent differ.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class TrackedList(val matchBy: String = "id")

/**
 * Compare this [Map] by key. The key is the identity. No [TrackedList.matchBy].
 *
 * `SortedMap` / `TreeMap` count as a Map. Object values walk their [@Tracked]
 * fields. Scalar values (`String`, numbers, enums) compare with `!=`.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class TrackedMap

/**
 * Descend into this nested object. Its type does not need [@Differ].
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class TrackedNested
