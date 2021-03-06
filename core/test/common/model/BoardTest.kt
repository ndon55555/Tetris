package model

import model.board.Board
import model.board.BoardImpl
import model.cell.CellColor
import model.cell.CellImpl
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

abstract class AbstractBoardTest {
    private lateinit var board: Board
    protected abstract fun boardImplementation(): Board

    @BeforeTest
    fun init() {
        board = boardImplementation()
    }

    @Test
    fun validCellsTest() {
        val valid = arrayOf(
            CellImpl(CellColor.GREEN, 1, 0),
            CellImpl(CellColor.GREEN, 0, 0),
            CellImpl(CellColor.GREEN, 1, 1),
            CellImpl(CellColor.GREEN, 2, 1)
        )
        assertTrue(board.areValidCells(*valid))

        val outOfBounds = arrayOf(
            CellImpl(CellColor.GREEN, 1, 0),
            CellImpl(CellColor.GREEN, 0, 0),
            CellImpl(CellColor.GREEN, 0, -1),
            CellImpl(CellColor.GREEN, -1, -1)
        )
        assertFalse(board.areValidCells(*outOfBounds))

        val overlapping = arrayOf(
            CellImpl(CellColor.RED, 2, 1)
        )
        board.placeCells(*valid)
        assertFalse(board.areValidCells(*overlapping))
    }

    @Test
    fun placeAndGetCellsTest() {
        assertEquals(0, board.getPlacedCells().size)

        val cells = arrayOf(
            CellImpl(CellColor.RED, 1, 2),
            CellImpl(CellColor.RED, 0, 0)
        )
        board.placeCells(*cells)
        val cells2 = arrayOf(
            CellImpl(CellColor.RED, 2, 5),
            CellImpl(CellColor.RED, 3, 4)
        )
        board.placeCells(*cells2)
        // Placed cells match cells of the original tetriminos
        assertTrue(board.getPlacedCells().containsAll(cells.toSet()))
        assertTrue(board.getPlacedCells().containsAll(cells2.toSet()))

        // Cannot place a piece if invalid
        assertFails { board.placeCells(*cells) }
    }

    @Test
    fun clearLineTest() {
        val cells = arrayOf(
            CellImpl(CellColor.RED, 1, 0),
            CellImpl(CellColor.RED, 1, 1),
            CellImpl(CellColor.RED, 1, 2),
            CellImpl(CellColor.RED, 0, 2)
        )
        board.placeCells(*cells)
        board.clearLine(1)
        // All cells matching given row are removed
        assertEquals(1, board.getPlacedCells().size)
        // All cells above given row are moved down
        assertTrue(
            board.getPlacedCells().contains(
                CellImpl(CellColor.RED, 0, 2).move(1, 0)
            )
        )
    }
}

class BoardImplTest() : AbstractBoardTest() {
    override fun boardImplementation(): Board = BoardImpl()
}
