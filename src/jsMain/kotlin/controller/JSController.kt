package controller

import kotlin.browser.window
import kotlin.js.Date
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds

actual fun runAtFixedRate(period: Long, shouldContinue: () -> Boolean, event: () -> Unit) {
    while (shouldContinue()) {
        event()
        window.setTimeout({}, period.toInt())
    }
}

@ExperimentalTime
actual fun timeStamp(): Duration = Date.now().milliseconds