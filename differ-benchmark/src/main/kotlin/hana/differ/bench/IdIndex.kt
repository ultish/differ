package hana.differ.bench

class IdIndex<T : Any>(expected: Int = 0) {
    private var cap = capacity(expected)
    private var mask = cap - 1
    private var keys = arrayOfNulls<String>(cap)
    private var hashes = IntArray(cap)
    private var vals = arrayOfNulls<Any>(cap)
    private var state = ByteArray(cap)

    fun ensure(expected: Int) {
        val need = capacity(expected)
        if (need <= cap) return
        cap = need
        mask = cap - 1
        keys = arrayOfNulls(cap)
        hashes = IntArray(cap)
        vals = arrayOfNulls(cap)
        state = ByteArray(cap)
    }

    fun clear() {
        state.fill(EMPTY)
    }

    fun put(id: String, value: T): T? {
        val h = mix(id)
        var i = h and mask
        while (true) {
            val slot = state[i]
            if (slot == LIVE) {
                if (hashes[i] == h && keys[i] == id) {
                    @Suppress("UNCHECKED_CAST")
                    return vals[i] as T
                }
            } else {
                keys[i] = id
                hashes[i] = h
                vals[i] = value
                state[i] = LIVE
                return null
            }
            i = (i + 1) and mask
        }
    }

    fun remove(id: String): T? {
        val h = mix(id)
        var i = h and mask
        while (true) {
            val slot = state[i]
            if (slot == EMPTY) return null
            if (slot == LIVE && hashes[i] == h && keys[i] == id) {
                @Suppress("UNCHECKED_CAST")
                val prior = vals[i] as T
                state[i] = TOMB
                vals[i] = null
                return prior
            }
            i = (i + 1) and mask
        }
    }

    fun forEachRemaining(action: (T) -> Unit) {
        for (i in 0 until cap) {
            if (state[i] != LIVE) continue
            @Suppress("UNCHECKED_CAST")
            action(vals[i] as T)
        }
    }

    private companion object {
        const val EMPTY: Byte = 0
        const val LIVE: Byte = 1
        const val TOMB: Byte = 2

        fun capacity(expected: Int): Int {
            val want = (expected * 2).coerceAtLeast(4)
            var n = 4
            while (n < want) n = n shl 1
            return n
        }

        fun mix(id: String): Int {
            val h = id.hashCode()
            return h xor (h ushr 16)
        }
    }
}

class IndexPool {
    val links = IdIndex<Link>()
    val alarms = IdIndex<Alarm>()
}
