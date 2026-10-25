package com.gtohjs.machine.wildcard

import java.util.function.LongSupplier

/** Runs real production primitives without initializing Minecraft or a mod registry. */
object OptimizationContractChecks {
    @JvmStatic fun main(args: Array<String>) {
        queueContracts()
        routingContracts()
        cacheContracts()
        lifecycleContracts()
        println("Optimization contracts passed: fairness/budget, routing, cache bounds, cancellation/epochs/retry/publication")
    }

    private fun queueContracts() {
        val order = ArrayList<String>()
        fun work(name: String) = object : RebuildQueue.Work {
            override fun isValid() = true
            override fun isRunnable() = true
            override fun step(): Boolean { order.add(name); return false }
            override fun failed(error: Exception) { throw error }
        }
        val queue = RebuildQueue(100, 4, LongSupplier { 0 })
        val first = work("a")
        queue.offer(first)
        queue.offer(first)
        queue.offer(work("b"))
        check(queue.tick() == 4)
        check(order == listOf("a", "b", "a", "b"))
        queue.remove(first)
        order.clear()
        queue.tick()
        check(order == listOf("b", "b", "b", "b"))
        queue.clear()
        check(queue.tick() == 0)

        var nanos = 0L
        var calls = 0
        val timed = RebuildQueue(2, 128, LongSupplier { nanos })
        timed.offer(object : RebuildQueue.Work {
            override fun isValid() = true
            override fun isRunnable() = true
            override fun step(): Boolean { calls++; nanos += 3; return false }
            override fun failed(error: Exception) { throw error }
        })
        timed.tick()
        check(calls == 1) // A native call may overrun, but a second call must wait.
    }

    private data class Pattern(val id: String)
    private fun routingContracts() {
        val routes = PatternRoutes<Pattern>()
        val first = Pattern("same")
        val second = Pattern("same")
        routes.add(first, 2)
        check(routes.source(Pattern("same")) == 2)
        routes.add(second, 5)
        check(routes.source(first) == 2)
        check(routes.source(second) == 5)
        check(routes.source(Pattern("same")) == null)
        check(routes.source(Pattern("missing")) == null)
        val sameSlot = PatternRoutes<Pattern>()
        sameSlot.add(first, 2)
        sameSlot.add(second, 2)
        check(sameSlot.source(Pattern("same")) == 2)
    }

    private fun cacheContracts() {
        fun key(value: Int) = WildcardCacheKey(byteArrayOf(value.toByte()))
        fun entry(count: Int, size: Int = 1) = WildcardCacheEntry(
            List(count) { EncodedWildcardPattern(it, ByteArray(size)) }, "test:type", count.toLong() * size)
        val cache = WildcardResultCache(2, 3, 32)
        cache.put(key(1), entry(1))
        cache.put(key(2), entry(1))
        check(cache.get(key(1)) != null) // Touch the first entry.
        cache.put(key(3), entry(1))
        check(cache.get(key(2)) == null && cache.get(key(1)) != null)
        cache.put(key(4), entry(3))
        check(cache.get(key(1)) == null && cache.get(key(3)) == null)
        check(cache.get(key(4))?.patterns?.size == 3)
        cache.put(key(5), entry(4)) // Oversize entries leave existing data intact.
        check(cache.get(key(5)) == null && cache.get(key(4)) != null)
        cache.put(key(6), entry(1, 40))
        check(cache.get(key(6)) == null)
        cache.put(key(4), entry(1)) // Replacement accounting must subtract old weight.
        cache.put(key(7), entry(2))
        check(cache.get(key(4)) != null && cache.get(key(7)) != null)
        cache.clear()
        check(cache.get(key(7)) == null)
        val payload = byteArrayOf(1, 2)
        val original = WildcardCacheKey(payload)
        val encoded = EncodedWildcardPattern(0, payload)
        payload[0] = 9
        check(original == WildcardCacheKey(byteArrayOf(1, 2)))
        val copy = encoded.payload()
        copy[0] = 8
        check(encoded.payload()[0] == 1.toByte())
    }

    private class Host : WildcardRebuildCoordinator.Host {
        val queue = RebuildQueue(100, 1, LongSupplier { 0 })
        var generation = 1L
        var tick = 0L
        var ready = false
        override fun epoch() = generation
        override fun tickNumber() = tick
        override fun isReady() = ready
        override fun offer(work: WildcardRebuildCoordinator) = queue.offer(work)
        override fun remove(work: WildcardRebuildCoordinator) = queue.remove(work)
    }

    private class Driver : WildcardRebuildCoordinator.Driver {
        var attached = true
        var creates = 0
        var advances = 0
        var cancellations = 0
        var clears = 0
        var publications = 0
        var failures = 0
        var throwNext = false
        var revision = -1L
        var epoch = -1L
        var onCreate: () -> Unit = {}
        override fun isAttached() = attached
        override fun clearPublished() { clears++ }
        override fun rebuildFailed(error: Exception) { failures++ }
        override fun published() { publications++ }
        override fun createTask(revision: Long, epoch: Long): WildcardRebuildCoordinator.Task {
            creates++
            this.revision = revision
            this.epoch = epoch
            onCreate()
            return object : WildcardRebuildCoordinator.Task {
                private var remaining = 2
                override fun advance(): Boolean {
                    advances++
                    if (throwNext) { throwNext = false; error("expected failure") }
                    return --remaining == 0
                }
                override fun cancel() { cancellations++ }
            }
        }
    }

    private fun lifecycleContracts() {
        val host = Host()
        val driver = Driver()
        val coordinator = WildcardRebuildCoordinator(driver, host)
        coordinator.request()
        coordinator.request() // Coalescing must not create two jobs.
        host.queue.tick()
        check(driver.creates == 0)
        host.ready = true
        host.queue.tick()
        check(driver.creates == 1 && !coordinator.isPublished())
        val oldRevision = driver.revision
        host.queue.tick()
        check(driver.publications == 0)
        coordinator.request()
        check(driver.cancellations == 1 && !coordinator.isCurrent(oldRevision, driver.epoch))
        repeat(3) { host.queue.tick() }
        check(coordinator.isPublished() && driver.publications == 1)
        host.generation++
        check(!coordinator.isPublished())
        coordinator.ensureCurrent()
        repeat(3) { host.queue.tick() }
        check(driver.publications == 2 && coordinator.isPublished())

        coordinator.request()
        host.queue.tick()
        driver.throwNext = true
        host.queue.tick()
        check(driver.failures == 1 && !coordinator.isPublished())
        val beforeRetry = driver.creates
        host.queue.tick()
        check(driver.creates == beforeRetry)
        host.tick += 20
        repeat(3) { host.queue.tick() }
        check(coordinator.isPublished() && driver.publications == 3)

        coordinator.request()
        host.queue.tick()
        coordinator.detach()
        host.queue.tick()
        check(!coordinator.isPublished() && driver.publications == 3)

        // Native source discovery may synchronously issue a fresh request.
        var reentered = false
        driver.onCreate = {
            if (!reentered) { reentered = true; coordinator.request() }
        }
        coordinator.request()
        host.queue.tick()
        check(!coordinator.isPublished())
        repeat(3) { host.queue.tick() }
        check(coordinator.isPublished() && driver.publications == 4)
    }
}
