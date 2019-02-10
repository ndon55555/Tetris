import kotlin.math.abs

/**
 * Represents the different game pieces of a Tetris Game.
 */
enum class TetriminoType {
    S, Z, J, L, O, I, T
}

/**
 * Represents a Tetris game piece.
 *
 * @property centerOfRotation The Posn with which the Tetrimino will rotate.
 * @property blocks The blocks that compose a Tetrimino.
 */
data class Tetrimino(val centerOfRotation: Posn, val blocks: Array<Cell>) {
    init {
        if (blocks.size != 4) {
            throw IllegalArgumentException("Tetrimino should have exactly 4 cells")
        }
        
        if (!blocks.allAdjacent()) {
            throw IllegalArgumentException("Each cell should be adjacent with at least 1 other cell")
        }
    }
    
    private fun Array<Cell>.allAdjacent(): Boolean {
        return this.all { c ->
            this.any {
                val dx = abs(it.position.x - c.position.x).toInt()
                val dy = abs(it.position.y - c.position.y).toInt()
                val manhattanDist = dx + dy
                manhattanDist == 1
            }
        }
    }
    
    /**
     * @param dRow Number of rows to move from the top of the board.
     * @param dCol Number of columns to move from the left of the board.
     * @return This Tetrimino translated over dRow and dCol.
     */
    private fun move(dRow: Int, dCol: Int): Tetrimino = Tetrimino(
        centerOfRotation.translate(dRow.toDouble(), dCol.toDouble()),
        blocks.map { it.move(dRow, dCol) }.toTypedArray())
    
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
    fun rotate90CW(): Tetrimino = Tetrimino(centerOfRotation, blocks.map {
        it.rotate90CWAround(centerOfRotation)
    }.toTypedArray())
    
    /**
     * @return This Tetrimino rotated 90 degress counter-clockwise around its center of rotation.
     */
    fun rotate90CCW(): Tetrimino = Tetrimino(centerOfRotation, blocks.map {
        it.rotate90CCWAround(centerOfRotation)
    }.toTypedArray())
    
    /**
     * @param other The object to compare to.
     * @return Decides whether or not this Tetrimino is equal to the given object.
     */
    override fun equals(other: Any?): Boolean {
        if (other !is Tetrimino) return false
        
        return (this.centerOfRotation == other.centerOfRotation) && (this.blocks.all { it in other.blocks })
    }
    
    /**
     * @return The hashcode of this Tetrimino.
     */
    override fun hashCode(): Int {
        var hash = 1
        hash *= 31 + this.centerOfRotation.hashCode()
        hash *= 31 + this.blocks.contentDeepHashCode()
        
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
                  arrayOf(Cell(CellColor.GREEN, 0, BOARD_WIDTH / 2),
                          Cell(CellColor.GREEN, 0, BOARD_WIDTH / 2 - 1),
                          Cell(CellColor.GREEN, 1, BOARD_WIDTH / 2 - 1),
                          Cell(CellColor.GREEN, 1, BOARD_WIDTH / 2 - 2)))
    TetriminoType.Z ->
        Tetrimino(Posn(1.0, (BOARD_WIDTH / 2 - 1).toDouble()),
                  arrayOf(Cell(CellColor.RED, 0, BOARD_WIDTH / 2 - 2),
                          Cell(CellColor.RED, 0, BOARD_WIDTH / 2 - 1),
                          Cell(CellColor.RED, 1, BOARD_WIDTH / 2 - 1),
                          Cell(CellColor.RED, 1, BOARD_WIDTH / 2)))
    TetriminoType.J ->
        Tetrimino(Posn(1.0, (BOARD_WIDTH / 2 - 1).toDouble()),
                  arrayOf(Cell(CellColor.DARK_BLUE, 0, BOARD_WIDTH / 2 - 2),
                          Cell(CellColor.DARK_BLUE, 1, BOARD_WIDTH / 2 - 2),
                          Cell(CellColor.DARK_BLUE, 1, BOARD_WIDTH / 2 - 1),
                          Cell(CellColor.DARK_BLUE, 1, BOARD_WIDTH / 2)))
    TetriminoType.L ->
        Tetrimino(Posn(1.0, (BOARD_WIDTH / 2 - 1).toDouble()),
                  arrayOf(Cell(CellColor.ORANGE, 0, BOARD_WIDTH / 2),
                          Cell(CellColor.ORANGE, 1, BOARD_WIDTH / 2),
                          Cell(CellColor.ORANGE, 1, BOARD_WIDTH / 2 - 1),
                          Cell(CellColor.ORANGE, 1, BOARD_WIDTH / 2)))
    TetriminoType.O ->
        Tetrimino(Posn(0.5, (BOARD_WIDTH - 1) / 2.0),
                  arrayOf(Cell(CellColor.YELLOW, 0, BOARD_WIDTH / 2 - 1),
                          Cell(CellColor.YELLOW, 0, BOARD_WIDTH / 2),
                          Cell(CellColor.YELLOW, 1, BOARD_WIDTH / 2 - 1),
                          Cell(CellColor.YELLOW, 1, BOARD_WIDTH / 2)))
    TetriminoType.I ->
        Tetrimino(Posn(0.5, (BOARD_WIDTH - 1) / 2.0),
                  arrayOf(Cell(CellColor.LIGHT_BLUE, 0, BOARD_WIDTH / 2 - 2),
                          Cell(CellColor.LIGHT_BLUE, 0, BOARD_WIDTH / 2 - 1),
                          Cell(CellColor.LIGHT_BLUE, 0, BOARD_WIDTH / 2),
                          Cell(CellColor.LIGHT_BLUE, 0, BOARD_WIDTH / 2 + 1)))
    TetriminoType.T ->
        Tetrimino(Posn(1.0, (BOARD_WIDTH / 2 - 1).toDouble()),
                  arrayOf(Cell(CellColor.PURPLE, 0, BOARD_WIDTH / 2 - 1),
                          Cell(CellColor.PURPLE, 1, BOARD_WIDTH / 2 - 2),
                          Cell(CellColor.PURPLE, 1, BOARD_WIDTH / 2 - 1),
                          Cell(CellColor.PURPLE, 1, BOARD_WIDTH / 2)))
}