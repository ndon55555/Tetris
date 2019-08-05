package controller.config

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

class RandomBagOf7 : StandardTetriminoGenerator {
    private val allPieces = setOf(Z(), S(), L(), J(), T(), I(), O())

    private var currentBag = newBag()
    override fun generate(): StandardTetrimino {
        if (currentBag.isEmpty()) currentBag = newBag()
        return currentBag.removeAt(0)
    }

    private fun newBag(): MutableList<StandardTetrimino> = allPieces.shuffled().toMutableList()

    override fun reset() {
        currentBag = newBag()
    }
}

class PurelyRandom : StandardTetriminoGenerator {
    override fun generate(): StandardTetrimino = setOf(Z(), S(), L(), J(), T(), I(), O()).shuffled().first()

    override fun reset() = Unit
}