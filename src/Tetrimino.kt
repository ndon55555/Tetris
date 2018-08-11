/**
 * Represents possible colors for Cells to have.
 */
enum class Color {
    RED, GREEN, DARK_BLUE, ORANGE, YELLOW, LIGHT_BLUE, PURPLE
}

/**
 * Represents the different game pieces of a Tetris Game.
 */
enum class TetriminoType {
    S, Z, J, L, O, I, T
}

/**
 * Represents a 1x1 block that is part of a Tetrimino.
 *
 * @property color The color of the Cell.
 * @property position Where the Cell is (measured from the top left of the board).
 */
data class Cell(val color: Color, val position: Posn) {
    /**
     * @param color The color of the Cell.
     * @param row Number of rows from the top of the game board.
     * @param col Number of columns from the left of the game board.
     */
    constructor(color: Color, row: Int, col: Int) : this(color, Posn(row.toDouble(), col.toDouble()))

    /**
     * @param dRow Change in the number of rows from the top.
     * @param dCol Change in the number of columns from the left.
     * @return This Cell translated over dRow and dCol.
     */
    fun move(dRow: Int, dCol: Int): Cell = Cell(color, position.translate(dCol.toDouble(), dRow.toDouble()))

    /**
     * @param centerOfRotation The Posn to rotate around.
     * @return This Cell rotated 90 degrees clockwise around the given Posn.
     */
    fun rotate90CWAround(centerOfRotation: Posn): Cell = Cell(color, position.rotate90CWAround(centerOfRotation))

    /**
     * @param centerOfRotation The Posn to rotate around.
     * @return This Cell rotated 90 degrees counter-clockwise around the given Posn.
     */
    fun rotate90CCWAround(centerOfRotation: Posn): Cell = Cell(color, position.rotate90CCWAround(centerOfRotation))
    
    /**
     * @param other The Cell to check against.
     * @return Determines whether or not the given Cell occupies the same position as this one.
     */
    fun sharesPositionWith(other: Cell): Boolean = (this.position == other.position)
}

/**
 * Represents a Tetris game piece.
 *
 * @property centerOfRotation The Posn with which the Tetrimino will rotate.
 * @property cells The cells that compose a Tetrimino.
 */
data class Tetrimino(val centerOfRotation: Posn, val cells: Array<Cell>) {
    /**
     * @param dRow Number of rows to move from the top of the board.
     * @param dCol Number of columns to move from the left of the board.
     * @return This Tetrimino translated over dRow and dCol.
     */
    private fun move(dRow: Int, dCol: Int): Tetrimino = Tetrimino(centerOfRotation.translate(dRow.toDouble(), dCol.toDouble()),
                                                          cells.map { it.move(dRow, dCol) }.toTypedArray())

    /**
     * @return This Tetrimino translated one row towards the top of the board.
     */
    fun moveUp(): Tetrimino = move(-1, 0)

    /**
     * @return This Tetrimino translated one row away from the top of the board.
     */
    fun moveDown(): Tetrimino = move(1, 0)

    /**
     * @return This Tetrimino translated one column towards the left of the board.
     */
    fun moveLeft(): Tetrimino = move(0, -1)

    /**
     * @return This Tetrimino translated one column away from the left of the board.
     */
    fun moveRight(): Tetrimino = move(0, 1)

    /**
     * @return This Tetrimino rotated 90 degrees clockwise around its center of rotation.
     */
    fun rotate90CW(): Tetrimino = Tetrimino(centerOfRotation, cells.map {
        it.rotate90CWAround(centerOfRotation)
    }.toTypedArray())

    /**
     * @return This Tetrimino rotated 90 degress counter-clockwise around its center of rotation.
     */
    fun rotate90CCW(): Tetrimino = Tetrimino(centerOfRotation, cells.map {
        it.rotate90CCWAround(centerOfRotation)
    }.toTypedArray())
    
    /**
     * @param other The object to compare to.
     * @return Decides whether or not this Tetrimino is equal to the given object.
     */
    override fun equals(other: Any?): Boolean {
        if (!(other is Tetrimino)) return false

        return (this.centerOfRotation == other.centerOfRotation) && (this.cells.all { it in other.cells })
    }
    
    /**
     * @return The hashcode of this Tetrimino.
     */
    override fun hashCode(): Int {
        var hash = 1
        hash *= 31 + this.centerOfRotation.hashCode()
        hash *= 31 + this.cells.contentDeepHashCode()
        
        return hash
    }
}

/**
 * @param type The type of Tetrimino to produce.
 * @return The corresponding Tetrimino.
 */
fun initialTetrimino(type: TetriminoType): Tetrimino = when (type) {
    TetriminoType.S ->
        Tetrimino(Posn(1.0, (BOARD_WIDTH / 2 - 1).toDouble()),
                  arrayOf(Cell(Color.GREEN, 0, BOARD_WIDTH / 2),
                          Cell(Color.GREEN, 0, BOARD_WIDTH / 2 - 1),
                          Cell(Color.GREEN, 1, BOARD_WIDTH / 2 - 1),
                          Cell(Color.GREEN, 1, BOARD_WIDTH / 2 - 2)))
    TetriminoType.Z ->
        Tetrimino(Posn(1.0, (BOARD_WIDTH / 2 - 1).toDouble()),
                  arrayOf(Cell(Color.RED, 0, BOARD_WIDTH / 2 - 2),
                          Cell(Color.RED, 0, BOARD_WIDTH / 2 - 1),
                          Cell(Color.RED, 1, BOARD_WIDTH / 2 - 1),
                          Cell(Color.RED, 1, BOARD_WIDTH / 2)))
    TetriminoType.J ->
        Tetrimino(Posn(1.0, (BOARD_WIDTH / 2 - 1).toDouble()),
                  arrayOf(Cell(Color.DARK_BLUE, 0, BOARD_WIDTH / 2 - 2),
                          Cell(Color.DARK_BLUE, 1, BOARD_WIDTH / 2 - 2),
                          Cell(Color.DARK_BLUE, 1, BOARD_WIDTH / 2 - 1),
                          Cell(Color.DARK_BLUE, 1, BOARD_WIDTH / 2)))
    TetriminoType.L ->
        Tetrimino(Posn(1.0, (BOARD_WIDTH / 2 - 1).toDouble()),
                  arrayOf(Cell(Color.ORANGE, 0, BOARD_WIDTH / 2),
                          Cell(Color.ORANGE, 1, BOARD_WIDTH / 2),
                          Cell(Color.ORANGE, 1, BOARD_WIDTH / 2 - 1),
                          Cell(Color.ORANGE, 1, BOARD_WIDTH / 2)))
    TetriminoType.O ->
        Tetrimino(Posn(0.5, (BOARD_WIDTH - 1) / 2.0),
                  arrayOf(Cell(Color.YELLOW, 0, BOARD_WIDTH / 2 - 1),
                          Cell(Color.YELLOW, 0, BOARD_WIDTH / 2),
                          Cell(Color.YELLOW, 1, BOARD_WIDTH / 2 - 1),
                          Cell(Color.YELLOW, 1, BOARD_WIDTH / 2)))
    TetriminoType.I ->
        Tetrimino(Posn(0.5, (BOARD_WIDTH - 1) / 2.0),
                  arrayOf(Cell(Color.LIGHT_BLUE, 0, BOARD_WIDTH / 2 - 2),
                          Cell(Color.LIGHT_BLUE, 0, BOARD_WIDTH / 2 - 1),
                          Cell(Color.LIGHT_BLUE, 0, BOARD_WIDTH / 2),
                          Cell(Color.LIGHT_BLUE, 0, BOARD_WIDTH / 2 + 1)))
    TetriminoType.T ->
        Tetrimino(Posn(1.0, (BOARD_WIDTH / 2 - 1).toDouble()),
                  arrayOf(Cell(Color.PURPLE, 0, BOARD_WIDTH / 2 - 1),
                          Cell(Color.PURPLE, 1, BOARD_WIDTH / 2 - 2),
                          Cell(Color.PURPLE, 1, BOARD_WIDTH / 2 - 1),
                          Cell(Color.PURPLE, 1, BOARD_WIDTH / 2)))
}