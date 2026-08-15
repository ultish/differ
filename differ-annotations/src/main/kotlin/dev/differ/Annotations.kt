package dev.differ

/**
 * Generate a `*Differ` for this class. Both sides of the comparison are this
 * type. Convert wire formats at the boundary. The generated walk is same-type
 * only.
 *
 * [name] overrides the generated object (`MachineDiffer` by default).
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Differ(val name: String = "")

/**
 * Compare this property. Untagged properties are invisible.
 *
 * [captureValues] controls only whether a change produces a typed event with
 * old/new. Detection always runs. Use `false` for blobs you must flag but
 * must not copy.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Tracked(val captureValues: Boolean = true)

/**
 * Compare this collection by [matchBy], never by index.
 *
 * The element type does not need [@Differ]. Its [@Tracked] / [@TrackedNested] /
 * [@TrackedList] properties are inlined into the parent differ.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class TrackedList(val matchBy: String = "id")

/**
 * Descend into this nested object. Its type does not need [@Differ].
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class TrackedNested
