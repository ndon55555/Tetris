package controller

import kotlin.browser.window
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds

@ExperimentalTime
actual fun runAtFixedRate(period: Long, shouldContinue: () -> Boolean, event: () -> Unit) {
    var prevEventTime = window.performance.now()

    fun step(curTime: Double) {
        if (shouldContinue()) {
            if (curTime - prevEventTime >= period) {
                event()
                prevEventTime = curTime
            }

            window.requestAnimationFrame { timestamp -> step(timestamp) }
        }
    }

    window.requestAnimationFrame { timestamp -> step(timestamp) }
}

@ExperimentalTime
actual fun timeStamp(): Duration = window.performance.now().milliseconds