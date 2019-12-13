package controller

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

actual fun runAtFixedRate(period: Long, shouldContinue: () -> Boolean, event: () -> Unit) {
    // wrapping gameLoop in an object with stopGameLoop allows gameLoop to stop itself
    object {
        val gameLoop = Executors.newSingleThreadScheduledExecutor {
            Thread(it).apply { isDaemon = true }
        }.scheduleAtFixedRate({
            if (shouldContinue()) {
                event()
            } else {
                stopGameLoop()
            }
        }, 0, period, TimeUnit.MILLISECONDS)

        fun stopGameLoop() {
            gameLoop.cancel(true)
        }
    }
}