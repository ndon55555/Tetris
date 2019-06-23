package model.tetrimino

import model.cell.Cell
import model.cell.Posn
import java.util.Objects
import kotlin.math.abs

/**
 * Represents a Tetris game piece.
 *
 * @property centerOfRotation The Posn with which the TetriminoImpl will rotate.
 * @property blocks The blocks that compose a TetriminoImpl.
 */
open class TetriminoImpl(private val centerOfRotation: Posn, private val blocks: Set<Cell>, private val orientation: Orientation) : Tetrimino {
    init {
        if (blocks.size != 4) {
            throw IllegalArgumentException("Tetrimino should have exactly 4 cells")
        }

        if (!blocks.allAdjacent()) {
            throw IllegalArgumentException("Each cell should be adjacent with at least 1 other cell")
        }
    }

    private fun Set<Cell>.allAdjacent(): Boolean {
        return this.all { c ->
            this.any {
                val dx = abs(it.col - c.col)
                val dy = abs(it.row - c.row)
                val manhattanDist = dx + dy
                manhattanDist == 1
            }
        }
    }

    override fun cells(): Set<Cell> = blocks.toSet()

    /**
     * @param dRow Number of rows to move from the top of the board.
     * @param dCol Number of columns to move from the left of the board.
     * @return This TetriminoImpl translated over dRow and dCol.
     */
    private fun move(dRow: Int, dCol: Int): TetriminoImpl = TetriminoImpl(
            centerOfRotation.translate(dRow.toDouble(), dCol.toDouble()),
            blocks.map { it.move(dRow, dCol) }.toSet(),
            orientation
    )

    override fun moveUp(): Tetrimino = move(-1, 0)

    override fun moveDown(): TetriminoImpl = move(1, 0)

    override fun moveLeft(): TetriminoImpl = move(0, -1)

    override fun moveRight(): TetriminoImpl = move(0, 1)

    override fun rotate90CW(): TetriminoImpl =
            TetriminoImpl(
                    centerOfRotation,
                    blocks.map { it.rotate90CWAround(centerOfRotation) }.toSet(),
                    when (orientation) {
                        Orientation.UP -> Orientation.RIGHT
                        Orientation.DOWN -> Orientation.LEFT
                        Orientation.LEFT -> Orientation.UP
                        Orientation.RIGHT -> Orientation.DOWN
                    }
            )

    override fun rotate90CCW(): TetriminoImpl =
            TetriminoImpl(
                    centerOfRotation,
                    blocks.map { it.rotate90CCWAround(centerOfRotation) }.toSet(),
                    when (orientation) {
                        Orientation.UP -> Orientation.LEFT
                        Orientation.DOWN -> Orientation.RIGHT
                        Orientation.LEFT -> Orientation.DOWN
                        Orientation.RIGHT -> Orientation.UP
                    }
            )

    override fun orientation(): Orientation = orientation

    override fun equals(other: Any?): Boolean {
        if (other !is Tetrimino) return false
        return this.cells().size == other.cells().size && this.cells().containsAll(other.cells())
    }

    override fun hashCode(): Int = Objects.hash(this.cells())
}

