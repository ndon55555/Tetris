package model.game

import kotlin.browser.window
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds

@ExperimentalTime
actual fun timeStamp(): Duration = window.performance.now().milliseconds