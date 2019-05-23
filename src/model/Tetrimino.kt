package model

interface Tetrimino {
    /**
     * @return This Tetrimino translated one row towards the top of the board.
     */
    fun moveUp(): Tetrimino

    /**
     * @return This Tetrimino translated one row away from the top of the board.
     */
    fun moveDown(): Tetrimino

    /**
     * @return This Tetrimino translated one column towards the left of the board.
     */
    fun moveLeft(): Tetrimino

    /**
     * @return This Tetrimino translated one column away from the left of the board.
     */
    fun moveRight(): Tetrimino

    /**
     * @return This Tetrimino rotated 90 degrees clockwise around its center of rotation.
     */
    fun rotate90CW(): Tetrimino

    /**
     * @return This Tetrimino rotated 90 degress counter-clockwise around its center of rotation.
     */
    fun rotate90CCW(): Tetrimino

    /**
     * @return A copy of this Tetrimino's cells.
     */
    fun cells(): Set<Cell>
}

/**
 * Represents the different game pieces of a Tetris Game.
 */
enum class TetriminoType {
    S, Z, J, L, O, I, T
}

/**
 * Factory method for creating a Tetrimino.
 *
 * @param type The type of Tetrimino to produce.
 * @return The corresponding Tetrimino.
 */
fun initTetrimino(type: TetriminoType): Tetrimino = when (type) {
    TetriminoType.S ->
        TetriminoImpl(Posn(1.0, (BOARD_WIDTH / 2 - 1).toDouble()),
                setOf(Cell(CellColor.GREEN, 0, BOARD_WIDTH / 2),
                        Cell(CellColor.GREEN, 0, BOARD_WIDTH / 2 - 1),
                        Cell(CellColor.GREEN, 1, BOARD_WIDTH / 2 - 1),
                        Cell(CellColor.GREEN, 1, BOARD_WIDTH / 2 - 2)))
    TetriminoType.Z ->
        TetriminoImpl(Posn(1.0, (BOARD_WIDTH / 2 - 1).toDouble()),
                setOf(Cell(CellColor.RED, 0, BOARD_WIDTH / 2 - 2),
                        Cell(CellColor.RED, 0, BOARD_WIDTH / 2 - 1),
                        Cell(CellColor.RED, 1, BOARD_WIDTH / 2 - 1),
                        Cell(CellColor.RED, 1, BOARD_WIDTH / 2)))
    TetriminoType.J ->
        TetriminoImpl(Posn(1.0, (BOARD_WIDTH / 2 - 1).toDouble()),
                setOf(Cell(CellColor.DARK_BLUE, 0, BOARD_WIDTH / 2 - 2),
                        Cell(CellColor.DARK_BLUE, 1, BOARD_WIDTH / 2 - 2),
                        Cell(CellColor.DARK_BLUE, 1, BOARD_WIDTH / 2 - 1),
                        Cell(CellColor.DARK_BLUE, 1, BOARD_WIDTH / 2)))
    TetriminoType.L ->
        TetriminoImpl(Posn(1.0, (BOARD_WIDTH / 2 - 1).toDouble()),
                setOf(Cell(CellColor.ORANGE, 0, BOARD_WIDTH / 2),
                        Cell(CellColor.ORANGE, 1, BOARD_WIDTH / 2),
                        Cell(CellColor.ORANGE, 1, BOARD_WIDTH / 2 - 1),
                        Cell(CellColor.ORANGE, 1, BOARD_WIDTH / 2)))
    TetriminoType.O ->
        TetriminoImpl(Posn(0.5, (BOARD_WIDTH - 1) / 2.0),
                setOf(Cell(CellColor.YELLOW, 0, BOARD_WIDTH / 2 - 1),
                        Cell(CellColor.YELLOW, 0, BOARD_WIDTH / 2),
                        Cell(CellColor.YELLOW, 1, BOARD_WIDTH / 2 - 1),
                        Cell(CellColor.YELLOW, 1, BOARD_WIDTH / 2)))
    TetriminoType.I ->
        TetriminoImpl(Posn(0.5, (BOARD_WIDTH - 1) / 2.0),
                setOf(Cell(CellColor.LIGHT_BLUE, 0, BOARD_WIDTH / 2 - 2),
                        Cell(CellColor.LIGHT_BLUE, 0, BOARD_WIDTH / 2 - 1),
                        Cell(CellColor.LIGHT_BLUE, 0, BOARD_WIDTH / 2),
                        Cell(CellColor.LIGHT_BLUE, 0, BOARD_WIDTH / 2 + 1)))
    TetriminoType.T ->
        TetriminoImpl(Posn(1.0, (BOARD_WIDTH / 2 - 1).toDouble()),
                setOf(Cell(CellColor.PURPLE, 0, BOARD_WIDTH / 2 - 1),
                        Cell(CellColor.PURPLE, 1, BOARD_WIDTH / 2 - 2),
                        Cell(CellColor.PURPLE, 1, BOARD_WIDTH / 2 - 1),
                        Cell(CellColor.PURPLE, 1, BOARD_WIDTH / 2)))
}