package model

const val BOARD_WIDTH = 10
const val BOARD_HEIGHT = 20

/**
 * Represents the Tetris game board. Top-left position of the board considered (0, 0).
 * This means that a position at row 0 is "above" a position at row 1.
 *
 * @property lockedCells The Cells that have been dropped on the board by the player.
 */
class BoardImpl : Board {
    val lockedCells = mutableSetOf<Cell>()

    /**
     * @param mino The Tetrimino to test.
     * @return Whether the given Tetrimino is in a valid place in this BoardImpl.
     */
    override fun validTetrimino(mino: Tetrimino): Boolean = TODO()

    /**
     * @param mino The Tetrimino to place on this BoardImpl.
     */
    override fun placeTetrimino(mino: Tetrimino): Unit = TODO()

    /**
     * @param row The row to clear a line on this BoardImpl.
     */
    override fun clearLine(row: Int): Unit = TODO()

    /**
     * @param row The row where a line was cleared on this BoardImpl.
     *
     * Shifts all Cells above the given row down by 1 row.
     */
    override fun shiftDownTo(row: Int): Unit = TODO()
}