package model

interface Board {
    /**
     * @param mino The Tetrimino to test.
     * @return Whether the given Tetrimino is in a valid place in this model.BoardImpl.
     */
    fun validTetrimino(mino: Tetrimino): Boolean

    /**
     * @param mino The Tetrimino to place on this model.BoardImpl.
     */
    fun placeTetrimino(mino: Tetrimino)

    /**
     * @param row The row to clear a line on this model.BoardImpl.
     */
    fun clearLine(row: Int)

    /**
     * @param row The row where a line was cleared on this model.BoardImpl.
     *
     * Shifts all Cells above the given row down by 1 row.
     */
    fun shiftDownTo(row: Int)
}
