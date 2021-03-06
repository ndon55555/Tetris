package model

import model.game.config.PurelyRandom
import model.game.config.RandomBagOf7
import model.tetrimino.I
import model.tetrimino.J
import model.tetrimino.L
import model.tetrimino.O
import model.tetrimino.S
import model.tetrimino.StandardTetrimino
import model.tetrimino.T
import model.tetrimino.Z
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

val allStandardTetriminos = setOf(S(), Z(), J(), L(), I(), O(), T())

abstract class AbstractStandardTetriminoGeneratorTest {
    @Test
    abstract fun generateTest()

    @Test
    abstract fun resetTest()
}

class RandomBagOf7Test : AbstractStandardTetriminoGeneratorTest() {
    @Test
    override fun generateTest() {
        val generator = RandomBagOf7()
        val pieces = mutableSetOf<StandardTetrimino>()
        repeat(7) {
            pieces += generator.generate()
        }

        assertTrue(allStandardTetriminos.containsAll(pieces) && pieces.containsAll(allStandardTetriminos))
        // Expect no exception to be thrown; should still be able to generate more pieces beyond the 7th one.
        generator.generate()
    }

    @Test
    override fun resetTest() {
        val generator = RandomBagOf7()
        repeat(3) {
            generator.generate()
        }

        generator.reset()
        val pieces = mutableSetOf<StandardTetrimino>()
        repeat(7) {
            pieces += generator.generate()
        }

        assertTrue(
            allStandardTetriminos.containsAll(pieces) && pieces.containsAll(
                allStandardTetriminos
            )
        )
    }
}

class PurelyRandomTest : AbstractStandardTetriminoGeneratorTest() {
    @Test
    override fun generateTest() {
        val tolerance = 0.05
        val tally = hashMapOf<StandardTetrimino, Int>().withDefault { 0 }
        val generator = PurelyRandom()
        val n = 7 * 1000

        repeat(n) {
            val piece = generator.generate()
            val count = tally.getValue(piece)
            tally[piece] = count + 1
        }

        for (count in tally.values) {
            val ratio = count / n.toDouble()
            val expectedRatio = 1 / 7.0
            val error = ratio - expectedRatio
            assertTrue(abs(error) <= tolerance)
        }
    }

    @Test
    override fun resetTest() {
        // Expect no exception to be thrown
        PurelyRandom().reset()
    }
}
