package hana.differ.it

import hana.differ.Differ
import hana.differ.Tracked
import hana.differ.TrackedList
import hana.differ.TrackedNested

data class SkipLine(val id: String, @Tracked val label: String, @Tracked(skipWhenNew = true) val flagged: Boolean)

data class SkipDetail(@Tracked val city: String, @Tracked(skipWhenNew = true) val verified: Boolean)

@Differ
data class SkipDoc(
    @Tracked val title: String,
    @Tracked(skipWhenNew = true) val reviewed: Boolean,
    @TrackedList val lines: List<SkipLine>,
    @TrackedNested val detail: SkipDetail,
)
