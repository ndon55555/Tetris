package controller.config

import controller.Command

class GameConfiguration {
    // All time values are in milliseconds
    var showGhost: Boolean = true
    var autoRepeatRate: Int = 30
        set(value) {
            if (value <= 0) {
                throw IllegalArgumentException("Auto repeat rate must be positive")
            }

            field = value
        }

    var delayedAutoShift: Int = 140
        set(value) {
            if (value < 0) {
                throw IllegalArgumentException("Delayed auto shift must be nonnegative")
            }

            field = value
        }

    var autoDropDelay: Int = 1000
        set(value) {
            if (value <= 0) {
                throw IllegalArgumentException("Auto drop delay must be positive")
            }

            field = value
        }

    var previewPieces = 5
        set(value) {
            if (value !in 0..6) {
                throw IllegalArgumentException("Number of preview pieces should be between 0 and 6, inclusive.")
            }

            field = value
        }

    var lockDelay = 500
        set(value) {
            if (value < 0) {
                throw IllegalArgumentException("Lock delay must be nonnegative.")
            }

            field = value
        }

    var keyToCommand = mutableMapOf( // TODO need a better system for this
        "z" to Command.ROTATE_CCW,
        "up" to Command.ROTATE_CW,
        "left" to Command.LEFT,
        "right" to Command.RIGHT,
        "down" to Command.SOFT_DROP,
        "space" to Command.HARD_DROP,
        "shift" to Command.HOLD
    ).withDefault { Command.DO_NOTHING }

    var rotationSystem: RotationSystem = SuperRotation()

    var generator: StandardTetriminoGenerator = RandomBagOf7()
}