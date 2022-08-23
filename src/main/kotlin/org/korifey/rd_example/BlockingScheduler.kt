package org.korifey.rd_example

import com.jetbrains.rd.util.catch
import com.jetbrains.rd.util.lifetime.Lifetime
import com.jetbrains.rd.util.lifetime.isAlive
import com.jetbrains.rd.util.reactive.IScheduler
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Create scheduler upon current thread and pump messages while [lifetime] is alive.
 * [initializationAction] usually serves to create protocol based on this scheduler and subscribe on signals
 * right on scheduler's thread. This ensures we don't have race condition with event from other process and don't miss
 * this event.
 */
fun pumpCurrentThread(lifetime: Lifetime, initializationAction: (IScheduler) -> Unit) {
    val actions = ConcurrentLinkedQueue<() -> Unit>()
    val currentThread = Thread.currentThread()
    val scheduler = object : IScheduler {
        override val isActive: Boolean
            get() = currentThread == Thread.currentThread()

        override fun flush() {
            while (true) {
                val action = actions.poll() ?: return
                if (lifetime.isAlive)
                    catch { action() }
            }
        }

        override fun queue(action: () -> Unit) {
            if (lifetime.isAlive)
                actions.add (action)
        }
    }

    initializationAction(scheduler)

    j@while (lifetime.isAlive) {
        val action = actions.poll()
        if (action == null) {
            Thread.yield()
            continue@j
        }
        catch { action() }
    }
}