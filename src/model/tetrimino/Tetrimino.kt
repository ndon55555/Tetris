package model.tetrimino

import model.board.BOARD_WIDTH
import model.cell.Cell
import model.cell.CellColor
import model.cell.CellImpl
import model.cell.Posn
import java.util.Objects

interface Tetrimino {
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
 * Represents the different game pieces of a Tetris game.
 */
sealed class StandardTetrimino(private val t: Tetrimino) : Tetrimino {
    abstract fun newPiece(t: Tetrimino): StandardTetrimino

    override fun moveDown(): Tetrimino = newPiece(t.moveDown())

    override fun moveLeft(): Tetrimino = newPiece(t.moveLeft())

    override fun moveRight(): Tetrimino = newPiece(t.moveRight())

    override fun rotate90CW(): Tetrimino = newPiece(t.rotate90CW())

    override fun rotate90CCW(): Tetrimino = newPiece(t.rotate90CCW())

    override fun cells(): Set<Cell> = t.cells()

    override fun equals(other: Any?): Boolean {
        if (other !is StandardTetrimino) return false
        return this.t == other.t
    }

    override fun hashCode(): Int = Objects.hashCode(this.t)
}

class S private constructor(t: Tetrimino) : StandardTetrimino(t) {
    constructor() : this(
            TetriminoImpl(
                    Posn(1.0, (BOARD_WIDTH / 2 - 1).toDouble()),
                    setOf(CellImpl(CellColor.GREEN, 0, BOARD_WIDTH / 2),
                            CellImpl(CellColor.GREEN, 0, BOARD_WIDTH / 2 - 1),
                            CellImpl(CellColor.GREEN, 1, BOARD_WIDTH / 2 - 1),
                            CellImpl(CellColor.GREEN, 1, BOARD_WIDTH / 2 - 2))))

    override fun newPiece(t: Tetrimino): StandardTetrimino = S(t)
}

class Z private constructor(t: Tetrimino) : StandardTetrimino(t) {
    constructor() : this(
            TetriminoImpl(
                    Posn(1.0, (BOARD_WIDTH / 2 - 1).toDouble()),
                    setOf(CellImpl(CellColor.RED, 0, BOARD_WIDTH / 2 - 2),
                            CellImpl(CellColor.RED, 0, BOARD_WIDTH / 2 - 1),
                            CellImpl(CellColor.RED, 1, BOARD_WIDTH / 2 - 1),
                            CellImpl(CellColor.RED, 1, BOARD_WIDTH / 2))))

    override fun newPiece(t: Tetrimino): StandardTetrimino = Z(t)
}

class J private constructor(t: Tetrimino) : StandardTetrimino(t) {
    constructor() : this(
            TetriminoImpl(
                    Posn(1.0, (BOARD_WIDTH / 2 - 1).toDouble()),
                    setOf(CellImpl(CellColor.DARK_BLUE, 0, BOARD_WIDTH / 2 - 2),
                            CellImpl(CellColor.DARK_BLUE, 1, BOARD_WIDTH / 2 - 2),
                            CellImpl(CellColor.DARK_BLUE, 1, BOARD_WIDTH / 2 - 1),
                            CellImpl(CellColor.DARK_BLUE, 1, BOARD_WIDTH / 2))))

    override fun newPiece(t: Tetrimino): StandardTetrimino = J(t)
}

class L private constructor(t: Tetrimino) : StandardTetrimino(t) {
    constructor() : this(
            TetriminoImpl(
                    Posn(1.0, (BOARD_WIDTH / 2 - 1).toDouble()),
                    setOf(CellImpl(CellColor.ORANGE, 0, BOARD_WIDTH / 2),
                            CellImpl(CellColor.ORANGE, 1, BOARD_WIDTH / 2),
                            CellImpl(CellColor.ORANGE, 1, BOARD_WIDTH / 2 - 1),
                            CellImpl(CellColor.ORANGE, 1, BOARD_WIDTH / 2 - 2))))

    override fun newPiece(t: Tetrimino): StandardTetrimino = L(t)
}

class O private constructor(t: Tetrimino) : StandardTetrimino(t) {
    constructor() : this(
            TetriminoImpl(Posn(0.5, (BOARD_WIDTH - 1) / 2.0),
                    setOf(CellImpl(CellColor.YELLOW, 0, BOARD_WIDTH / 2 - 1),
                            CellImpl(CellColor.YELLOW, 0, BOARD_WIDTH / 2),
                            CellImpl(CellColor.YELLOW, 1, BOARD_WIDTH / 2 - 1),
                            CellImpl(CellColor.YELLOW, 1, BOARD_WIDTH / 2))))

    override fun newPiece(t: Tetrimino): StandardTetrimino = O(t)
}

class I private constructor(t: Tetrimino) : StandardTetrimino(t) {
    constructor() : this(
            TetriminoImpl(
                    Posn(0.5, (BOARD_WIDTH - 1) / 2.0),
                    setOf(CellImpl(CellColor.LIGHT_BLUE, 0, BOARD_WIDTH / 2 - 2),
                            CellImpl(CellColor.LIGHT_BLUE, 0, BOARD_WIDTH / 2 - 1),
                            CellImpl(CellColor.LIGHT_BLUE, 0, BOARD_WIDTH / 2),
                            CellImpl(CellColor.LIGHT_BLUE, 0, BOARD_WIDTH / 2 + 1))))

    override fun newPiece(t: Tetrimino): StandardTetrimino = I(t)
}

class T private constructor(t: Tetrimino) : StandardTetrimino(t) {
    constructor() : this(
            TetriminoImpl(
                    Posn(1.0, (BOARD_WIDTH / 2 - 1).toDouble()),
                    setOf(CellImpl(CellColor.PURPLE, 0, BOARD_WIDTH / 2 - 1),
                            CellImpl(CellColor.PURPLE, 1, BOARD_WIDTH / 2 - 2),
                            CellImpl(CellColor.PURPLE, 1, BOARD_WIDTH / 2 - 1),
                            CellImpl(CellColor.PURPLE, 1, BOARD_WIDTH / 2))))

    override fun newPiece(t: Tetrimino): StandardTetrimino = T(t)
}