package com.gtohjs.machine.wildcard

/** Owns task lifetime without a nullable task/result/dirty-flag combination. */
class WildcardRebuildCoordinator(private val driver: Driver, private val host: Host) : RebuildQueue.Work {
    interface Host {
        fun epoch(): Long
        fun tickNumber(): Long
        fun isReady(): Boolean
        fun offer(work: WildcardRebuildCoordinator)
        fun remove(work: WildcardRebuildCoordinator)
    }
    interface Driver {
        fun isAttached(): Boolean
        fun createTask(revision: Long, epoch: Long): Task?
        fun clearPublished()
        fun rebuildFailed(error: Exception)
        fun published()
    }

    interface Task {
        fun advance(): Boolean
        fun cancel()
    }

    private sealed interface State {
        data object Detached : State
        data object Pending : State
        data class Running(val task: Task) : State
        data object Published : State
        data class Retry(val tick: Long) : State
    }

    private var state: State = State.Detached
    private var revision = 0L
    private var epoch = -1L
    private var failures = 0

    fun request() {
        cancelTask()
        revision++
        epoch = host.epoch()
        failures = 0
        driver.clearPublished()
        state = State.Pending
        if (driver.isAttached()) host.offer(this)
    }

    fun ensureCurrent() {
        if (epoch != host.epoch()) request()
    }

    fun isCurrent(expectedRevision: Long, expectedEpoch: Long): Boolean =
        driver.isAttached() && revision == expectedRevision && epoch == expectedEpoch &&
            epoch == host.epoch()

    fun isPublished(): Boolean = state === State.Published && epoch == host.epoch()

    fun detach() {
        cancelTask()
        revision++
        state = State.Detached
        driver.clearPublished()
        host.remove(this)
    }

    private fun cancelTask() {
        (state as? State.Running)?.task?.cancel()
    }

    override fun isValid(): Boolean = driver.isAttached() && state !== State.Detached && state !== State.Published

    override fun isRunnable(): Boolean = host.isReady() &&
        ((state as? State.Retry)?.tick ?: 0L) <= host.tickNumber()

    override fun step(): Boolean {
        ensureCurrent()
        when (val current = state) {
            State.Pending, is State.Retry -> {
                val expectedRevision = revision
                val expectedEpoch = epoch
                val task = driver.createTask(revision, epoch)
                if (revision != expectedRevision || epoch != expectedEpoch || epoch != host.epoch()) {
                    task?.cancel()
                    ensureCurrent()
                    return false
                }
                if (task == null) {
                    state = State.Retry(host.tickNumber() + 20)
                } else {
                    state = State.Running(task)
                }
            }
            is State.Running -> if (current.task.advance()) {
                if (state !== current || epoch != host.epoch()) {
                    ensureCurrent()
                    return false
                }
                state = State.Published
                failures = 0
                driver.published()
                return true
            }
            State.Detached, State.Published -> return true
        }
        return false
    }

    override fun failed(error: Exception) {
        cancelTask()
        driver.clearPublished()
        failures = (failures + 1).coerceAtMost(5)
        state = State.Retry(host.tickNumber() + (20L shl (failures - 1)).coerceAtMost(200))
        driver.rebuildFailed(error)
    }
}
