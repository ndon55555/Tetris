package model

enum class CellColor {
    GREEN, RED, DARK_BLUE, ORANGE, LIGHT_BLUE, YELLOW, PURPLE
}

/**
 * Represents a 1x1 block that is part of a model.TetriminoImpl. (x, y) represents (row, col) of the Cell.
 *
 * @property color The color of the Cell.
 * @property position Where the Cell is (measured from the top left of the board).
 */
// TODO break this out into an interface
data class Cell(val position: Posn) {
    lateinit var color: CellColor

    /**
     * @param color The color of the Cell.
     * @param position Where the Cell is.
     */
    constructor(color: CellColor, position: Posn) : this(position) {
        this.color = color
    }

    /**
     * @param color The color of the Cell.
     * @param row Number of rows from the top of the game board.
     * @param col Number of columns from the left of the game board.
     */
    constructor(color: CellColor, row: Int, col: Int) : this(color, Posn(row.toDouble(), col.toDouble()))

    /**
     * @param dRow Change in the number of rows from the top.
     * @param dCol Change in the number of columns from the left.
     * @return This Cell translated over dRow and dCol.
     */
    fun move(dRow: Int, dCol: Int): Cell = Cell(color, position.translate(dRow.toDouble(), dCol.toDouble()))

    /**
     * @param centerOfRotation The model.Posn to rotate around.
     * @return This Cell rotated 90 degrees clockwise around the given model.Posn.
     */
    fun rotate90CWAround(centerOfRotation: Posn): Cell = Cell(color, position.rotate90CWAround(centerOfRotation))

    /**
     * @param centerOfRotation The model.Posn to rotate around.
     * @return This Cell rotated 90 degrees counter-clockwise around the given model.Posn.
     */
    fun rotate90CCWAround(centerOfRotation: Posn): Cell = Cell(color, position.rotate90CCWAround(centerOfRotation))

    /**
     * @param other The Cell to check against.
     * @return Determines whether or not the given Cell occupies the same position as this one.
     */
    fun sharesPositionWith(other: Cell): Boolean = (this.position == other.position)
}