package controller

import kotlin.browser.window

actual fun runAtFixedRate(period: Long, shouldContinue: () -> Boolean, event: () -> Unit) {
    var intervalId: Int? = null
    intervalId = window.setInterval({
        if (shouldContinue()) {
            event()
        } else {
            intervalId?.let {
                window.clearInterval(it)
            }
        }
    }, period.toInt())
}