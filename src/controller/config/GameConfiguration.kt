package controller.config

import controller.Command
import java.awt.event.KeyEvent

class GameConfiguration {
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

    var keyToCommand = mutableMapOf(
        KeyEvent.VK_Z to Command.ROTATE_CCW,
        KeyEvent.VK_UP to Command.ROTATE_CW,
        KeyEvent.VK_LEFT to Command.LEFT,
        KeyEvent.VK_RIGHT to Command.RIGHT,
        KeyEvent.VK_DOWN to Command.SOFT_DROP,
        KeyEvent.VK_SPACE to Command.HARD_DROP,
        KeyEvent.VK_SHIFT to Command.HOLD
    ).withDefault { Command.DO_NOTHING }

    var rotationSystem: RotationSystem = SuperRotation()

    var generator: StandardTetriminoGenerator = RandomBagOf7()
}