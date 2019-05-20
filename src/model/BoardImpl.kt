package model

const val BOARD_WIDTH = 10
const val BOARD_HEIGHT = 18

/**
 * Represents the Tetris game board. Top-left position of the board considered (0, 0).
 * This means that a position at row 0 is "above" a position at row 1.
 *
 * @property placedCells The Cells that have been dropped on the board by the player.
 */
class BoardImpl : Board {
    private val placedCells = mutableSetOf<Cell>()

    override fun placedCells(): Set<Cell> = setOf(*placedCells.toTypedArray())

    override fun areValidCells(vararg cells: Cell): Boolean = cells.all {
        it.position.x.toInt() in 0..BOARD_WIDTH
                && it.position.y.toInt() in 0..BOARD_HEIGHT
                && placedCells.none { cell -> cell.sharesPositionWith(it) }
    }

    override fun placeCells(vararg cells: Cell) {
        if (!areValidCells(*cells)) {
            throw IllegalArgumentException("invalid tetrimino for this board")
        }

        placedCells += cells
    }

    override fun clearLine(row: Int) {
        placedCells.removeIf { it.position.x.toInt() == row }
        placedCells.forEach {
            if (it.position.x < row) {
                placedCells -= it
                placedCells += it.move(1, 0)
            }
        }
    }
}