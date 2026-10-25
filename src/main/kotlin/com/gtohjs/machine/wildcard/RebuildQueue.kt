package com.gtohjs.machine.wildcard

import java.util.ArrayDeque
import java.util.function.LongSupplier
import java.util.IdentityHashMap

/** Main-thread cooperative work. A single native search cannot be preempted. */
class RebuildQueue @JvmOverloads constructor(
    private val budgetNanos: Long = 2_000_000,
    private val maxSteps: Int = 128,
    private val clock: LongSupplier = LongSupplier(System::nanoTime)
) {
    interface Work {
        fun isValid(): Boolean
        fun isRunnable(): Boolean
        /** One search, encoding, or restoration unit; true means complete. */
        fun step(): Boolean
        fun failed(error: Exception)
    }

    private val queue = ArrayDeque<Work>()
    private val queued = HashSet<Work>()

    fun offer(work: Work) {
        if (queued.add(work)) queue.addLast(work)
    }

    fun remove(work: Work) {
        if (queued.remove(work)) queue.remove(work)
    }

    fun clear() {
        queue.clear()
        queued.clear()
    }

    fun tick(): Int {
        val start = clock.asLong
        var visits = 0
        while (queue.isNotEmpty() && visits < maxSteps && clock.asLong - start < budgetNanos) {
            val work = queue.removeFirst()
            queued.remove(work)
            visits++
            if (!work.isValid()) continue
            if (!work.isRunnable()) {
                offer(work)
                continue
            }
            try {
                if (!work.step() && work.isValid()) offer(work)
            } catch (error: Exception) {
                work.failed(error)
                if (work.isValid()) offer(work)
            }
        }
        return visits
    }
}

/** Immutable content key; callers may reuse their encoding buffers. */
class WildcardCacheKey(payload: ByteArray) {
    private val bytes = payload.copyOf()
    private val hash = bytes.contentHashCode()
    val size: Int get() = bytes.size
    override fun hashCode(): Int = hash
    override fun equals(other: Any?): Boolean = other is WildcardCacheKey && bytes.contentEquals(other.bytes)
}

/** Exact objects keep their slot; an equivalent copy must have one unique route. */
class PatternRoutes<P : Any> {
    private val exact = IdentityHashMap<P, Int>()
    private val equivalent = HashMap<P, Int>()
    private val ambiguous = HashSet<P>()

    fun add(pattern: P, source: Int) {
        exact[pattern] = source
        if (pattern in ambiguous) return
        val previous = equivalent.putIfAbsent(pattern, source)
        if (previous != null && previous != source) {
            equivalent.remove(pattern)
            ambiguous.add(pattern)
        }
    }

    fun source(pattern: P): Int? = exact[pattern] ?: if (pattern in ambiguous) null else equivalent[pattern]
}

class EncodedWildcardPattern(val sourceSlot: Int, payload: ByteArray) {
    private val bytes = payload.copyOf()
    val size: Int get() = bytes.size
    fun payload(): ByteArray = bytes.copyOf()
}

class WildcardCacheEntry(
    val patterns: List<EncodedWildcardPattern>,
    val selectedType: String?,
    val payloadBytes: Long
)

/** Limits apply to the shared cache, not live providers' currently published lists. */
class WildcardResultCache @JvmOverloads constructor(
    private val maxSignatures: Int = MAX_SIGNATURES,
    private val maxPatterns: Int = MAX_PATTERNS,
    private val maxBytes: Long = MAX_BYTES
) {
    companion object {
        const val MAX_SIGNATURES = 32
        const val MAX_PATTERNS = 32768
        const val MAX_BYTES = 32L * 1024 * 1024
    }
    private val entries = LinkedHashMap<WildcardCacheKey, WildcardCacheEntry>(16, .75f, true)
    private var patterns = 0
    private var bytes = 0L

    fun get(key: WildcardCacheKey): WildcardCacheEntry? = entries[key]

    fun put(key: WildcardCacheKey, value: WildcardCacheEntry) {
        if (value.patterns.size > maxPatterns || value.payloadBytes + key.size > maxBytes) return
        entries.remove(key)?.let { subtract(key, it) }
        entries[key] = value
        patterns += value.patterns.size
        bytes += value.payloadBytes + key.size
        val iterator = entries.entries.iterator()
        while (entries.size > maxSignatures || patterns > maxPatterns || bytes > maxBytes) {
            val oldest = iterator.next()
            subtract(oldest.key, oldest.value)
            iterator.remove()
        }
    }

    private fun subtract(key: WildcardCacheKey, entry: WildcardCacheEntry) {
        patterns -= entry.patterns.size
        bytes -= entry.payloadBytes + key.size
    }

    fun clear() {
        entries.clear()
        patterns = 0
        bytes = 0
    }
}
