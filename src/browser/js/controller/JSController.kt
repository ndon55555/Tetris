package controller

import kotlin.browser.window

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