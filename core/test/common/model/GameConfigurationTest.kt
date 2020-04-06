package model

import model.game.config.GameConfiguration
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class GameConfigurationTest {
    lateinit var config: GameConfiguration

    @BeforeTest
    fun init() {
        config = GameConfiguration()
    }

    @Test
    fun autoRepeatRateTest() {
        assertFailsWith<IllegalArgumentException> {
            config.autoRepeatRate = -1
        }
    }

    @Test
    fun delayedAutoShiftTest() {
        assertFailsWith<IllegalArgumentException> {
            config.delayedAutoShift = -1
        }
    }

    @Test
    fun previewPiecesTest() {
        assertFailsWith<IllegalArgumentException> {
            config.previewPieces = -1
        }

        assertFailsWith<IllegalArgumentException> {
            config.previewPieces = 7
        }
    }

    @Test
    fun lockDelayTest() {
        assertFailsWith<IllegalArgumentException> {
            config.lockDelay = -1
        }
    }
}