package com.gtohjs.machine.wildcard

import net.minecraftforge.event.OnDatapackSyncEvent
import net.minecraftforge.event.TagsUpdatedEvent
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.server.ServerStartedEvent
import net.minecraftforge.event.server.ServerStoppingEvent
import java.util.concurrent.atomic.AtomicLong

/** The existing Forge server-tick framework, with one budget for all providers. */
object WildcardRebuildScheduler {
    private val queue = RebuildQueue()
    private val cache = WildcardResultCache()
    private val participants = LinkedHashSet<WildcardRebuildCoordinator>()
    private val generation = AtomicLong()
    private var appliedGeneration = -1L
    private var tick = 0L
    private var ready = false

    @JvmStatic fun epoch(): Long = generation.get()
    @JvmStatic fun tickNumber(): Long = tick
    @JvmStatic fun isReady(): Boolean = ready
    @JvmField val HOST = object : WildcardRebuildCoordinator.Host {
        override fun epoch(): Long = generation.get()
        override fun tickNumber(): Long = tick
        override fun isReady(): Boolean = ready
        override fun offer(work: WildcardRebuildCoordinator) {
            participants.add(work)
            queue.offer(work)
        }
        override fun remove(work: WildcardRebuildCoordinator) {
            participants.remove(work)
            queue.remove(work)
        }
    }
    @JvmStatic fun cached(key: WildcardCacheKey): WildcardCacheEntry? = cache.get(key)
    @JvmStatic fun remember(key: WildcardCacheKey, result: WildcardCacheEntry) = cache.put(key, result)

    // Called only after the existing catalog/server-start checks have succeeded.
    @JvmStatic fun onServerStarted(event: ServerStartedEvent) {
        generation.incrementAndGet()
        cache.clear()
        ready = true
    }

    @JvmStatic fun onTagsUpdated(event: TagsUpdatedEvent) {
        if (event.updateCause == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) generation.incrementAndGet()
    }

    @JvmStatic fun onDatapackSync(event: OnDatapackSyncEvent) {
        if (event.player == null) generation.incrementAndGet()
    }

    @JvmStatic fun onServerTick(event: TickEvent.ServerTickEvent) {
        if (event.phase != TickEvent.Phase.END || !ready) return
        tick++
        val next = generation.get()
        if (next != appliedGeneration) {
            cache.clear()
            appliedGeneration = next
            participants.toList().forEach { it.ensureCurrent() }
        }
        queue.tick()
    }

    @JvmStatic fun onServerStopping(event: ServerStoppingEvent) {
        ready = false
        generation.incrementAndGet()
        participants.toList().forEach { it.detach() }
        participants.clear()
        queue.clear()
        cache.clear()
        tick = 0
    }
}
