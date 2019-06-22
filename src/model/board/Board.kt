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
