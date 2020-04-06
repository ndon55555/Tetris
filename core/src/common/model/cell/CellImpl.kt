package model.cell

/**
 * Represents a 1x1 block on a Tetris board. (x, y) represents (row, col) of the Cell.
 *
 * @property color The color of the Cell.
 * @property position Where the Cell is (measured from the top left of the board).
 */
data class CellImpl(override val color: CellColor, private val position: Posn) : Cell {
    override val row: Int
        get() = position.x.toInt()
    override val col: Int
        get() = position.y.toInt()

    /**
     * @param color The color of the Cell.
     * @param row Number of rows from the top of the game board.
     * @param col Number of columns from the left of the game board.
     */
    constructor(color: CellColor, row: Int, col: Int) : this(color, Posn(row.toDouble(), col.toDouble()))

    override fun move(dRow: Int, dCol: Int): Cell = CellImpl(color, position.translate(dRow.toDouble(), dCol.toDouble()))

    override fun rotate90CWAround(centerOfRotation: Posn): Cell = CellImpl(color, position.rotate90CWAround(centerOfRotation))

    override fun rotate90CCWAround(centerOfRotation: Posn): Cell = CellImpl(color, position.rotate90CCWAround(centerOfRotation))

    override fun sharesPositionWith(other: Cell): Boolean = (this.row == other.row && this.col == other.col)
}
