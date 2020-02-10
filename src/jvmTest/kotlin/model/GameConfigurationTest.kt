package model

import model.game.config.GameConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GameConfigurationTest {
    lateinit var config: GameConfiguration

    @BeforeEach
    fun init() {
        config = GameConfiguration()
    }

    @Test
    fun autoRepeatRateTest() {
        assertThrows<IllegalArgumentException> {
            config.autoRepeatRate = -1
        }
    }

    @Test
    fun delayedAutoShiftTest() {
        assertThrows<IllegalArgumentException> {
            config.delayedAutoShift = -1
        }
    }

    @Test
    fun previewPiecesTest() {
        assertThrows<IllegalArgumentException> {
            config.previewPieces = -1
        }

        assertThrows<IllegalArgumentException> {
            config.previewPieces = 7
        }
    }

    @Test
    fun lockDelayTest() {
        assertThrows<IllegalArgumentException> {
            config.lockDelay = -1
        }
    }
}