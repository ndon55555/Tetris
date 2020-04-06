package model.game.config

import model.tetrimino.I
import model.tetrimino.J
import model.tetrimino.L
import model.tetrimino.O
import model.tetrimino.S
import model.tetrimino.StandardTetrimino
import model.tetrimino.T
import model.tetrimino.Z

interface StandardTetriminoGenerator {
    fun generate(): StandardTetrimino

    fun reset()
}

private val allStandardPieces = setOf(Z(), S(), L(), J(), T(), I(), O())

class RandomBagOf7 : StandardTetriminoGenerator {
    private var currentBag = newBag()
    override fun generate(): StandardTetrimino {
        if (currentBag.isEmpty()) currentBag = newBag()
        return currentBag.removeAt(0)
    }

    private fun newBag(): MutableList<StandardTetrimino> = allStandardPieces.shuffled().toMutableList()

    override fun reset() {
        currentBag = newBag()
    }
}

class PurelyRandom : StandardTetriminoGenerator {
    override fun generate(): StandardTetrimino = allStandardPieces.random()

    override fun reset() = Unit
}