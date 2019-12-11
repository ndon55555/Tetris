package controller

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.nanoseconds

actual fun runAtFixedRate(period: Long, shouldContinue: () -> Boolean, event: () -> Unit) {
    Executors.newSingleThreadScheduledExecutor { r ->
        object : Thread() {
            override fun run() {
                if (!shouldContinue()) {
                    this.interrupt()
                } else {
                    r.run()
                }
            }
        }.apply { isDaemon = true }
    }.scheduleAtFixedRate(event, 0, period, TimeUnit.MILLISECONDS)
}

@ExperimentalTime
actual fun timeStamp(): Duration = System.nanoTime().nanoseconds