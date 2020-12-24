package model.tetrimino

import model.board.BOARD_WIDTH
import model.board.FIRST_VISIBLE_ROW
import model.cell.Cell
import model.cell.CellColor
import model.cell.CellImpl
import model.cell.Posn

interface Tetrimino {
    /**
     * @param dRow Number of rows to move up. Negative means down.
     * @param dCol Number of columns to move right. Negative means left.
     * @return This Tetrimino translated dCol columns and dRow rows.
     */
    fun move(dRow: Int, dCol: Int): Tetrimino

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

    /**
     * @return The orientation of this Tetrimino.
     */
    fun orientation(): Orientation
}

/**
 * Orientation of a Tetrimino. Pieces spawn in the UP orientation.
 */
enum class Orientation {
    UP, DOWN, LEFT, RIGHT
}

/**
 * Represents the common different game pieces of a Tetris game.
 */
sealed class StandardTetrimino(private val t: Tetrimino) : Tetrimino {
    protected abstract fun newPiece(t: Tetrimino): StandardTetrimino

    override fun move(dRow: Int, dCol: Int): StandardTetrimino = newPiece(t.move(dRow, dCol))

    override fun moveUp(): StandardTetrimino = newPiece(t.moveUp())

    override fun moveDown(): StandardTetrimino = newPiece(t.moveDown())

    override fun moveLeft(): StandardTetrimino = newPiece(t.moveLeft())

    override fun moveRight(): StandardTetrimino = newPiece(t.moveRight())

    override fun rotate90CW(): StandardTetrimino = newPiece(t.rotate90CW())

    override fun rotate90CCW(): StandardTetrimino = newPiece(t.rotate90CCW())

    override fun cells(): Set<Cell> = t.cells()

    override fun orientation(): Orientation = t.orientation()

    override fun equals(other: Any?): Boolean {
        if (other !is StandardTetrimino) return false
        return this.t == other.t
    }

    override fun hashCode(): Int {
        return 37 * this.t.hashCode()
    }
}

class S private constructor(t: Tetrimino) : StandardTetrimino(t) {
    constructor() : this(
        TetriminoImpl(
            Posn(FIRST_VISIBLE_ROW - 1.0, (BOARD_WIDTH / 2 - 1).toDouble()),
            setOf(
                CellImpl(CellColor.GREEN, FIRST_VISIBLE_ROW - 2, BOARD_WIDTH / 2),
                CellImpl(CellColor.GREEN, FIRST_VISIBLE_ROW - 2, BOARD_WIDTH / 2 - 1),
                CellImpl(CellColor.GREEN, FIRST_VISIBLE_ROW - 1, BOARD_WIDTH / 2 - 1),
                CellImpl(CellColor.GREEN, FIRST_VISIBLE_ROW - 1, BOARD_WIDTH / 2 - 2)
            ),
            Orientation.UP
        )
    )

    override fun newPiece(t: Tetrimino): StandardTetrimino = S(t)
}

class Z private constructor(t: Tetrimino) : StandardTetrimino(t) {
    constructor() : this(
        TetriminoImpl(
            Posn(FIRST_VISIBLE_ROW - 1.0, (BOARD_WIDTH / 2 - 1).toDouble()),
            setOf(
                CellImpl(CellColor.RED, FIRST_VISIBLE_ROW - 2, BOARD_WIDTH / 2 - 2),
                CellImpl(CellColor.RED, FIRST_VISIBLE_ROW - 2, BOARD_WIDTH / 2 - 1),
                CellImpl(CellColor.RED, FIRST_VISIBLE_ROW - 1, BOARD_WIDTH / 2 - 1),
                CellImpl(CellColor.RED, FIRST_VISIBLE_ROW - 1, BOARD_WIDTH / 2)
            ),
            Orientation.UP
        )
    )

    override fun newPiece(t: Tetrimino): StandardTetrimino = Z(t)
}

class J private constructor(t: Tetrimino) : StandardTetrimino(t) {
    constructor() : this(
        TetriminoImpl(
            Posn(FIRST_VISIBLE_ROW - 1.0, (BOARD_WIDTH / 2 - 1).toDouble()),
            setOf(
                CellImpl(CellColor.DARK_BLUE, FIRST_VISIBLE_ROW - 2, BOARD_WIDTH / 2 - 2),
                CellImpl(CellColor.DARK_BLUE, FIRST_VISIBLE_ROW - 1, BOARD_WIDTH / 2 - 2),
                CellImpl(CellColor.DARK_BLUE, FIRST_VISIBLE_ROW - 1, BOARD_WIDTH / 2 - 1),
                CellImpl(CellColor.DARK_BLUE, FIRST_VISIBLE_ROW - 1, BOARD_WIDTH / 2)
            ),
            Orientation.UP
        )
    )

    override fun newPiece(t: Tetrimino): StandardTetrimino = J(t)
}

class L private constructor(t: Tetrimino) : StandardTetrimino(t) {
    constructor() : this(
        TetriminoImpl(
            Posn(FIRST_VISIBLE_ROW - 1.0, (BOARD_WIDTH / 2 - 1).toDouble()),
            setOf(
                CellImpl(CellColor.ORANGE, FIRST_VISIBLE_ROW - 2, BOARD_WIDTH / 2),
                CellImpl(CellColor.ORANGE, FIRST_VISIBLE_ROW - 1, BOARD_WIDTH / 2),
                CellImpl(CellColor.ORANGE, FIRST_VISIBLE_ROW - 1, BOARD_WIDTH / 2 - 1),
                CellImpl(CellColor.ORANGE, FIRST_VISIBLE_ROW - 1, BOARD_WIDTH / 2 - 2)
            ),
            Orientation.UP
        )
    )

    override fun newPiece(t: Tetrimino): StandardTetrimino = L(t)
}

class O private constructor(t: Tetrimino) : StandardTetrimino(t) {
    constructor() : this(
        TetriminoImpl(
            Posn(FIRST_VISIBLE_ROW - 1.5, (BOARD_WIDTH - 1) / 2.0),
            setOf(
                CellImpl(CellColor.YELLOW, FIRST_VISIBLE_ROW - 2, BOARD_WIDTH / 2 - 1),
                CellImpl(CellColor.YELLOW, FIRST_VISIBLE_ROW - 2, BOARD_WIDTH / 2),
                CellImpl(CellColor.YELLOW, FIRST_VISIBLE_ROW - 1, BOARD_WIDTH / 2 - 1),
                CellImpl(CellColor.YELLOW, FIRST_VISIBLE_ROW - 1, BOARD_WIDTH / 2)
            ),
            Orientation.UP
        )
    )

    override fun newPiece(t: Tetrimino): StandardTetrimino = O(t)
}

class I private constructor(t: Tetrimino) : StandardTetrimino(t) {
    constructor() : this(
        TetriminoImpl(
            Posn(FIRST_VISIBLE_ROW - 0.5, (BOARD_WIDTH - 1) / 2.0),
            setOf(
                CellImpl(CellColor.LIGHT_BLUE, FIRST_VISIBLE_ROW - 1, BOARD_WIDTH / 2 - 2),
                CellImpl(CellColor.LIGHT_BLUE, FIRST_VISIBLE_ROW - 1, BOARD_WIDTH / 2 - 1),
                CellImpl(CellColor.LIGHT_BLUE, FIRST_VISIBLE_ROW - 1, BOARD_WIDTH / 2),
                CellImpl(CellColor.LIGHT_BLUE, FIRST_VISIBLE_ROW - 1, BOARD_WIDTH / 2 + 1)
            ),
            Orientation.UP
        )
    )

    override fun newPiece(t: Tetrimino): StandardTetrimino = I(t)
}

class T private constructor(t: Tetrimino) : StandardTetrimino(t) {
    constructor() : this(
        TetriminoImpl(
            Posn(FIRST_VISIBLE_ROW - 1.0, (BOARD_WIDTH / 2 - 1).toDouble()),
            setOf(
                CellImpl(CellColor.PURPLE, FIRST_VISIBLE_ROW - 2, BOARD_WIDTH / 2 - 1),
                CellImpl(CellColor.PURPLE, FIRST_VISIBLE_ROW - 1, BOARD_WIDTH / 2 - 2),
                CellImpl(CellColor.PURPLE, FIRST_VISIBLE_ROW - 1, BOARD_WIDTH / 2 - 1),
                CellImpl(CellColor.PURPLE, FIRST_VISIBLE_ROW - 1, BOARD_WIDTH / 2)
            ),
            Orientation.UP
        )
    )

    override fun newPiece(t: Tetrimino): StandardTetrimino = T(t)
}
