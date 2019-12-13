package model.game

import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.nanoseconds

@ExperimentalTime
actual fun timeStamp(): Duration = System.nanoTime().nanoseconds