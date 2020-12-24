package model.cell

enum class CellColor {
    GREEN, RED, DARK_BLUE, ORANGE, LIGHT_BLUE, YELLOW, PURPLE, NULL
}

/**
 * Represents a 1x1 block on a Tetris board. (x, y) represents (row, col) of the Cell.
 * */
interface Cell {
    val row: Int
    val col: Int
    val color: CellColor

    /**
     * @param dRow Change in the number of rows from the top.
     * @param dCol Change in the number of columns from the left.
     * @return This Cell translated over dRow and dCol.
     */
    fun move(dRow: Int, dCol: Int): Cell

    /**
     * @param centerOfRotation The Posn to rotate around.
     * @return This Cell rotated 90 degrees clockwise around the given Posn.
     */
    fun rotate90CWAround(centerOfRotation: Posn): Cell

    /**
     * @param centerOfRotation The Posn to rotate around.
     * @return This Cell rotated 90 degrees counter-clockwise around the given Posn.
     */
    fun rotate90CCWAround(centerOfRotation: Posn): Cell

    /**
     * @param other The Cell to check against.
     * @return Whether or not the given Cell occupies the same position as this one.
     */
    fun sharesPositionWith(other: Cell): Boolean
}
