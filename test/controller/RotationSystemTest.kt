package controller

import controller.config.BasicRotation
import controller.config.SuperRotation
import model.board.Board
import model.cell.Cell
import model.tetrimino.I
import model.tetrimino.T
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

abstract class AbstractRotationSystem {
    @Test
    abstract fun testRotate90CW()

    @Test
    abstract fun testRotate90CCW()
}

class SuperRotationTest : AbstractRotationSystem() {
    private val rotationSystem = SuperRotation()
    private val stubInvalidCellsBoardFactory = { failures: Int ->
        var f = failures

        object : Board {
            override fun areValidCells(vararg cells: Cell): Boolean = f-- == 0

            override fun placeCells(vararg cells: Cell) = Unit

            override fun clearLine(row: Int) = Unit

            override fun getPlacedCells(): Set<Cell> = emptySet()
        }
    }
    val upT = T()
    val leftT = T().rotate90CCW()
    val rightT = T().rotate90CW()
    val downT = T().rotate90CW().rotate90CW()
    val upI = I()
    val leftI = I().rotate90CCW()
    val rightI = I().rotate90CW()
    val downI = I().rotate90CW().rotate90CW()

    @Test
    override fun testRotate90CW() {
        // Rotate T

        // Up -> Right
        assertEquals(rightT, rotationSystem.rotate90CW(upT, stubInvalidCellsBoardFactory(0)))
        assertEquals(rightT.moveLeft(), rotationSystem.rotate90CW(upT, stubInvalidCellsBoardFactory(1)))
        assertEquals(rightT.moveLeft().moveUp(), rotationSystem.rotate90CW(upT, stubInvalidCellsBoardFactory(2)))
        assertEquals(rightT.moveDown().moveDown(), rotationSystem.rotate90CW(upT, stubInvalidCellsBoardFactory(3)))
        assertEquals(
            rightT.moveLeft().moveDown().moveDown(),
            rotationSystem.rotate90CW(upT, stubInvalidCellsBoardFactory(4))
        )
        assertEquals(upT, rotationSystem.rotate90CW(upT, stubInvalidCellsBoardFactory(5)))

        // Right -> Down
        assertEquals(downT, rotationSystem.rotate90CW(rightT, stubInvalidCellsBoardFactory(0)))
        assertEquals(downT.moveRight(), rotationSystem.rotate90CW(rightT, stubInvalidCellsBoardFactory(1)))
        assertEquals(downT.moveRight().moveDown(), rotationSystem.rotate90CW(rightT, stubInvalidCellsBoardFactory(2)))
        assertEquals(downT.moveUp().moveUp(), rotationSystem.rotate90CW(rightT, stubInvalidCellsBoardFactory(3)))
        assertEquals(
            downT.moveRight().moveUp().moveUp(),
            rotationSystem.rotate90CW(rightT, stubInvalidCellsBoardFactory(4))
        )
        assertEquals(rightT, rotationSystem.rotate90CW(rightT, stubInvalidCellsBoardFactory(5)))

        // Down -> Left
        assertEquals(leftT, rotationSystem.rotate90CW(downT, stubInvalidCellsBoardFactory(0)))
        assertEquals(leftT.moveRight(), rotationSystem.rotate90CW(downT, stubInvalidCellsBoardFactory(1)))
        assertEquals(leftT.moveRight().moveUp(), rotationSystem.rotate90CW(downT, stubInvalidCellsBoardFactory(2)))
        assertEquals(leftT.moveDown().moveDown(), rotationSystem.rotate90CW(downT, stubInvalidCellsBoardFactory(3)))
        assertEquals(
            leftT.moveRight().moveDown().moveDown(),
            rotationSystem.rotate90CW(downT, stubInvalidCellsBoardFactory(4))
        )
        assertEquals(downT, rotationSystem.rotate90CW(downT, stubInvalidCellsBoardFactory(5)))

        // Left -> Up
        assertEquals(upT, rotationSystem.rotate90CW(leftT, stubInvalidCellsBoardFactory(0)))
        assertEquals(upT.moveLeft(), rotationSystem.rotate90CW(leftT, stubInvalidCellsBoardFactory(1)))
        assertEquals(upT.moveLeft().moveDown(), rotationSystem.rotate90CW(leftT, stubInvalidCellsBoardFactory(2)))
        assertEquals(upT.moveUp().moveUp(), rotationSystem.rotate90CW(leftT, stubInvalidCellsBoardFactory(3)))
        assertEquals(
            upT.moveLeft().moveUp().moveUp(),
            rotationSystem.rotate90CW(leftT, stubInvalidCellsBoardFactory(4))
        )
        assertEquals(leftT, rotationSystem.rotate90CW(leftT, stubInvalidCellsBoardFactory(5)))

        // Rotate I

        // Up -> Right
        assertEquals(rightI, rotationSystem.rotate90CW(upI, stubInvalidCellsBoardFactory(0)))
        assertEquals(rightI.moveLeft().moveLeft(), rotationSystem.rotate90CW(upI, stubInvalidCellsBoardFactory(1)))
        assertEquals(rightI.moveRight(), rotationSystem.rotate90CW(upI, stubInvalidCellsBoardFactory(2)))
        assertEquals(
            rightI.moveLeft().moveLeft().moveDown(),
            rotationSystem.rotate90CW(upI, stubInvalidCellsBoardFactory(3))
        )
        assertEquals(
            rightI.moveRight().moveUp().moveUp(),
            rotationSystem.rotate90CW(upI, stubInvalidCellsBoardFactory(4))
        )
        assertEquals(upI, rotationSystem.rotate90CW(upI, stubInvalidCellsBoardFactory(5)))

        // Right -> Down
        assertEquals(downI, rotationSystem.rotate90CW(rightI, stubInvalidCellsBoardFactory(0)))
        assertEquals(downI.moveLeft(), rotationSystem.rotate90CW(rightI, stubInvalidCellsBoardFactory(1)))
        assertEquals(downI.moveRight().moveRight(), rotationSystem.rotate90CW(rightI, stubInvalidCellsBoardFactory(2)))
        assertEquals(
            downI.moveLeft().moveUp().moveUp(),
            rotationSystem.rotate90CW(rightI, stubInvalidCellsBoardFactory(3))
        )
        assertEquals(
            downI.moveRight().moveRight().moveDown(),
            rotationSystem.rotate90CW(rightI, stubInvalidCellsBoardFactory(4))
        )
        assertEquals(rightI, rotationSystem.rotate90CW(rightI, stubInvalidCellsBoardFactory(5)))

        // Down -> Left
        assertEquals(leftI, rotationSystem.rotate90CW(downI, stubInvalidCellsBoardFactory(0)))
        assertEquals(leftI.moveRight().moveRight(), rotationSystem.rotate90CW(downI, stubInvalidCellsBoardFactory(1)))
        assertEquals(leftI.moveLeft(), rotationSystem.rotate90CW(downI, stubInvalidCellsBoardFactory(2)))
        assertEquals(
            leftI.moveRight().moveRight().moveUp(),
            rotationSystem.rotate90CW(downI, stubInvalidCellsBoardFactory(3))
        )
        assertEquals(
            leftI.moveLeft().moveDown().moveDown(),
            rotationSystem.rotate90CW(downI, stubInvalidCellsBoardFactory(4))
        )
        assertEquals(downI, rotationSystem.rotate90CW(downI, stubInvalidCellsBoardFactory(5)))

        // Left -> Up
        assertEquals(upI, rotationSystem.rotate90CW(leftI, stubInvalidCellsBoardFactory(0)))
        assertEquals(upI.moveRight(), rotationSystem.rotate90CW(leftI, stubInvalidCellsBoardFactory(1)))
        assertEquals(upI.moveLeft().moveLeft(), rotationSystem.rotate90CW(leftI, stubInvalidCellsBoardFactory(2)))
        assertEquals(
            upI.moveRight().moveDown().moveDown(),
            rotationSystem.rotate90CW(leftI, stubInvalidCellsBoardFactory(3))
        )
        assertEquals(
            upI.moveLeft().moveLeft().moveUp(),
            rotationSystem.rotate90CW(leftI, stubInvalidCellsBoardFactory(4))
        )
        assertEquals(leftI, rotationSystem.rotate90CW(leftI, stubInvalidCellsBoardFactory(5)))
    }

    @Test
    override fun testRotate90CCW() {
        // Rotate T

        // Right -> Up
        assertEquals(upT, rotationSystem.rotate90CCW(rightT, stubInvalidCellsBoardFactory(0)))
        assertEquals(upT.moveRight(), rotationSystem.rotate90CCW(rightT, stubInvalidCellsBoardFactory(1)))
        assertEquals(upT.moveRight().moveDown(), rotationSystem.rotate90CCW(rightT, stubInvalidCellsBoardFactory(2)))
        assertEquals(upT.moveUp().moveUp(), rotationSystem.rotate90CCW(rightT, stubInvalidCellsBoardFactory(3)))
        assertEquals(
            upT.moveRight().moveUp().moveUp(),
            rotationSystem.rotate90CCW(rightT, stubInvalidCellsBoardFactory(4))
        )
        assertEquals(rightT, rotationSystem.rotate90CCW(rightT, stubInvalidCellsBoardFactory(5)))

        // Down -> Right
        assertEquals(rightT, rotationSystem.rotate90CCW(downT, stubInvalidCellsBoardFactory(0)))
        assertEquals(rightT.moveLeft(), rotationSystem.rotate90CCW(downT, stubInvalidCellsBoardFactory(1)))
        assertEquals(rightT.moveLeft().moveUp(), rotationSystem.rotate90CCW(downT, stubInvalidCellsBoardFactory(2)))
        assertEquals(rightT.moveDown().moveDown(), rotationSystem.rotate90CCW(downT, stubInvalidCellsBoardFactory(3)))
        assertEquals(
            rightT.moveLeft().moveDown().moveDown(),
            rotationSystem.rotate90CCW(downT, stubInvalidCellsBoardFactory(4))
        )
        assertEquals(downT, rotationSystem.rotate90CCW(downT, stubInvalidCellsBoardFactory(5)))

        // Left -> Down
        assertEquals(downT, rotationSystem.rotate90CCW(leftT, stubInvalidCellsBoardFactory(0)))
        assertEquals(downT.moveLeft(), rotationSystem.rotate90CCW(leftT, stubInvalidCellsBoardFactory(1)))
        assertEquals(downT.moveLeft().moveDown(), rotationSystem.rotate90CCW(leftT, stubInvalidCellsBoardFactory(2)))
        assertEquals(downT.moveUp().moveUp(), rotationSystem.rotate90CCW(leftT, stubInvalidCellsBoardFactory(3)))
        assertEquals(
            downT.moveLeft().moveUp().moveUp(),
            rotationSystem.rotate90CCW(leftT, stubInvalidCellsBoardFactory(4))
        )
        assertEquals(leftT, rotationSystem.rotate90CCW(leftT, stubInvalidCellsBoardFactory(5)))

        // Up -> Left
        assertEquals(leftT, rotationSystem.rotate90CCW(upT, stubInvalidCellsBoardFactory(0)))
        assertEquals(leftT.moveRight(), rotationSystem.rotate90CCW(upT, stubInvalidCellsBoardFactory(1)))
        assertEquals(leftT.moveRight().moveUp(), rotationSystem.rotate90CCW(upT, stubInvalidCellsBoardFactory(2)))
        assertEquals(leftT.moveDown().moveDown(), rotationSystem.rotate90CCW(upT, stubInvalidCellsBoardFactory(3)))
        assertEquals(
            leftT.moveRight().moveDown().moveDown(),
            rotationSystem.rotate90CCW(upT, stubInvalidCellsBoardFactory(4))
        )
        assertEquals(upT, rotationSystem.rotate90CCW(upT, stubInvalidCellsBoardFactory(5)))

        // Rotate I

        // Right -> Up
        assertEquals(upI, rotationSystem.rotate90CCW(rightI, stubInvalidCellsBoardFactory(0)))
        assertEquals(upI.moveRight().moveRight(), rotationSystem.rotate90CCW(rightI, stubInvalidCellsBoardFactory(1)))
        assertEquals(upI.moveLeft(), rotationSystem.rotate90CCW(rightI, stubInvalidCellsBoardFactory(2)))
        assertEquals(
            upI.moveRight().moveRight().moveUp(),
            rotationSystem.rotate90CCW(rightI, stubInvalidCellsBoardFactory(3))
        )
        assertEquals(
            upI.moveLeft().moveDown().moveDown(),
            rotationSystem.rotate90CCW(rightI, stubInvalidCellsBoardFactory(4))
        )
        assertEquals(rightI, rotationSystem.rotate90CCW(rightI, stubInvalidCellsBoardFactory(5)))

        // Down -> Right
        assertEquals(rightI, rotationSystem.rotate90CCW(downI, stubInvalidCellsBoardFactory(0)))
        assertEquals(rightI.moveRight(), rotationSystem.rotate90CCW(downI, stubInvalidCellsBoardFactory(1)))
        assertEquals(rightI.moveLeft().moveLeft(), rotationSystem.rotate90CCW(downI, stubInvalidCellsBoardFactory(2)))
        assertEquals(
            rightI.moveRight().moveDown().moveDown(),
            rotationSystem.rotate90CCW(downI, stubInvalidCellsBoardFactory(3))
        )
        assertEquals(
            rightI.moveLeft().moveLeft().moveUp(),
            rotationSystem.rotate90CCW(downI, stubInvalidCellsBoardFactory(4))
        )
        assertEquals(downI, rotationSystem.rotate90CCW(downI, stubInvalidCellsBoardFactory(5)))

        // Left -> Down
        assertEquals(downI, rotationSystem.rotate90CCW(leftI, stubInvalidCellsBoardFactory(0)))
        assertEquals(downI.moveLeft().moveLeft(), rotationSystem.rotate90CCW(leftI, stubInvalidCellsBoardFactory(1)))
        assertEquals(downI.moveRight(), rotationSystem.rotate90CCW(leftI, stubInvalidCellsBoardFactory(2)))
        assertEquals(
            downI.moveLeft().moveLeft().moveDown(),
            rotationSystem.rotate90CCW(leftI, stubInvalidCellsBoardFactory(3))
        )
        assertEquals(
            downI.moveRight().moveUp().moveUp(),
            rotationSystem.rotate90CCW(leftI, stubInvalidCellsBoardFactory(4))
        )
        assertEquals(leftI, rotationSystem.rotate90CCW(leftI, stubInvalidCellsBoardFactory(5)))

        // Up -> Left
        assertEquals(leftI, rotationSystem.rotate90CCW(upI, stubInvalidCellsBoardFactory(0)))
        assertEquals(leftI.moveLeft(), rotationSystem.rotate90CCW(upI, stubInvalidCellsBoardFactory(1)))
        assertEquals(leftI.moveRight().moveRight(), rotationSystem.rotate90CCW(upI, stubInvalidCellsBoardFactory(2)))
        assertEquals(
            leftI.moveLeft().moveUp().moveUp(),
            rotationSystem.rotate90CCW(upI, stubInvalidCellsBoardFactory(3))
        )
        assertEquals(
            leftI.moveRight().moveRight().moveDown(),
            rotationSystem.rotate90CCW(upI, stubInvalidCellsBoardFactory(4))
        )
        assertEquals(upI, rotationSystem.rotate90CCW(upI, stubInvalidCellsBoardFactory(5)))
    }
}

class BasicRotationTest : AbstractRotationSystem() {
    private val rotationSystem = BasicRotation()
    private val dummyBoard = object : Board {
        override fun areValidCells(vararg cells: Cell): Boolean = false

        override fun placeCells(vararg cells: Cell) = Unit

        override fun clearLine(row: Int) = Unit

        override fun getPlacedCells(): Set<Cell> = emptySet()
    }

    @Test
    override fun testRotate90CW() {
        assertEquals(T().rotate90CW(), rotationSystem.rotate90CW(T(), dummyBoard))
        assertEquals(I().rotate90CW(), rotationSystem.rotate90CW(I(), dummyBoard))
    }

    @Test
    override fun testRotate90CCW() {
        assertEquals(T().rotate90CCW(), rotationSystem.rotate90CCW(T(), dummyBoard))
        assertEquals(I().rotate90CCW(), rotationSystem.rotate90CCW(I(), dummyBoard))
    }
}