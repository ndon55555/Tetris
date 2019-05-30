package model

const val BOARD_WIDTH = 10
const val BOARD_HEIGHT = 20

/**
 * Represents the Tetris game board. Top-left position of the board considered (0, 0).
 * This means that a position at row 0 is "above" a position at row 1.
 *
 * @property getPlacedCells The Cells that have been dropped on the board by the player.
 */
class BoardImpl : Board {
    private val placedCells = mutableSetOf<Cell>()

    override fun getPlacedCells(): Set<Cell> = setOf(*placedCells.toTypedArray())

    override fun areValidCells(vararg cells: Cell): Boolean = cells.all {
        it.row in 0 until BOARD_HEIGHT && it.col in 0 until BOARD_WIDTH
                && placedCells.all { cell -> it !== cell && !cell.sharesPositionWith(it) }
    }

    override fun placeCells(vararg cells: Cell) {
        if (!areValidCells(*cells)) {
            throw IllegalArgumentException("invalid cells for this board")
        }

        placedCells += cells
    }

    override fun clearLine(row: Int) {
        placedCells.removeIf { it.row == row }
        placedCells.forEach {
            if (it.row < row) {
                placedCells -= it
                placedCells += it.move(1, 0)
            }
        }
    }
}