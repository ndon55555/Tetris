package model.board

import model.cell.Cell

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
 * Obtain a syncrhonized version of the given Board.
 */
internal fun synchronizedBoard(b: Board): Board = object : Board {
    @Synchronized
    override fun areValidCells(vararg cells: Cell): Boolean = b.areValidCells(*cells)

    @Synchronized
    override fun placeCells(vararg cells: Cell) = b.placeCells(*cells)

    @Synchronized
    override fun clearLine(row: Int) = b.clearLine(row)

    @Synchronized
    override fun getPlacedCells() = b.getPlacedCells()
}