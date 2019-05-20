package model

interface Board {
    /**
     * @param mino The Tetrimino to test.
     * @return Whether or not the given Tetrimino is in the boundaries of this Board
     * and not overlapping with another piece.
     */
    fun isValidTetrimino(mino: Tetrimino): Boolean

    /**
     * @param mino The Tetrimino to place on this Board.
     * @throws IllegalArgumentException if it isn't possible to place the tetrimino on this Board.
     */
    fun placeTetrimino(mino: Tetrimino)

    /**
     * @param row The row to clear a line on this Board.
     */
    fun clearLine(row: Int)

    /**
     * @return All the cells that have been placed on the board.
     */
    fun placedCells(): Set<Cell>
}
