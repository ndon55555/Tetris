package model.board

import model.cell.Cell
import model.sync

interface Board {
    /**
     * @param cells The Cells to test.
     * @return Whether or not the given Cells are in the boundaries of this Board
     * and not overlapping with other Cells.
     */
    fun areValidCells(vararg cells: Cell): Boolean

    /**
     * @param cells The Cells to place on this Board.
     * @throws IllegalArgumentException if it isn't possible to place the Cells on this Board.
     */
    fun placeCells(vararg cells: Cell)

    /**
     * @param row The row to clear on this Board.
     */
    fun clearLine(row: Int)

    /**
     * @return All the cells that have been placed on the board.
     */
    fun getPlacedCells(): Set<Cell>
}

const val BOARD_WIDTH = 10
const val BOARD_HEIGHT = 40
const val VISIBLE_BOARD_HEIGHT = 20 // Must be less than or equal to BOARD_HEIGHT
const val FIRST_VISIBLE_ROW = BOARD_HEIGHT - VISIBLE_BOARD_HEIGHT

/**
 * Obtain a synchronized version of the given Board.
 */
internal fun synchronizedBoard(b: Board): Board = object : Board {
    private val lock = Any()

    override fun areValidCells(vararg cells: Cell): Boolean = sync(lock) { b.areValidCells(*cells) }

    override fun placeCells(vararg cells: Cell) = sync(lock) { b.placeCells(*cells) }

    override fun clearLine(row: Int) = sync(lock) { b.clearLine(row) }

    override fun getPlacedCells() = sync(lock) { b.getPlacedCells() }
}