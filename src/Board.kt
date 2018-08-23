const val BOARD_WIDTH = 10
const val BOARD_HEIGHT = 20

/**
 * Represents the Tetris game board. Top-left position of the board considered (0, 0).
 * This means that a position at row 0 is "above" a position at row 1.
 *
 * @property lockedCells The Cells that have been dropped on the board by the player.
 */
class Board {
    val lockedCells = mutableSetOf<Cell>()
    
    /**
     * @param mino The Tetrimino to test.
     * @return Whether the given Tetrimino is in a valid place in this Board.
     */
    fun validTetrimino(mino: Tetrimino): Boolean = TODO()
    
    /**
     * @param mino The Tetrimino to place on this Board.
     */
    fun placeTetrimino(mino: Tetrimino): Unit = TODO()
    
    /**
     * @param row The row to clear a line on this Board.
     */
    fun clearLine(row: Int): Unit = TODO()
    
    /**
     * @param row The row where a line was cleared on this Board.
     *
     * Shifts all Cells above the given row down by 1 row.
     */
    fun shiftDownTo(row: Int): Unit = TODO()
}