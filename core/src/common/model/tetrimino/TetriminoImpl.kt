package model.tetrimino

import model.cell.Cell
import model.cell.Posn
import kotlin.math.abs

/**
 * Represents a Tetris game piece.
 *
 * @property centerOfRotation The Posn with which the TetriminoImpl will rotate.
 * @property blocks The blocks that compose a TetriminoImpl.
 */
open class TetriminoImpl(
    private val centerOfRotation: Posn,
    private val blocks: Set<Cell>,
    private val orientation: Orientation
) : Tetrimino {
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

    override fun move(dRow: Int, dCol: Int): TetriminoImpl = TetriminoImpl(
        centerOfRotation.translate(-dRow.toDouble(), dCol.toDouble()),
        blocks.map { it.move(-dRow, dCol) }.toSet(),
        orientation
    )

    override fun moveUp(): TetriminoImpl = move(1, 0)

    override fun moveDown(): TetriminoImpl = move(-1, 0)

    override fun moveLeft(): TetriminoImpl = move(0, -1)

    override fun moveRight(): TetriminoImpl = move(0, 1)

    override fun rotate90CW(): TetriminoImpl = rotateHelper(Cell::rotate90CWAround, 1)

    override fun rotate90CCW(): TetriminoImpl = rotateHelper(Cell::rotate90CCWAround, -1)

    private fun rotateHelper(cellFunc: Cell.(Posn) -> Cell, dOrientationIdx: Int) =
        TetriminoImpl(
            centerOfRotation,
            blocks.map { it.cellFunc(centerOfRotation) }.toSet(),
            orientationWheel.indexOf(orientation).let { curIdx ->
                val r = curIdx + dOrientationIdx
                val n = orientationWheel.size
                val newIdx = when {
                    r < 0 -> n + r
                    r >= n -> n - r
                    else -> r
                }

                orientationWheel[newIdx]
            }
        )

    override fun orientation(): Orientation = orientation

    override fun equals(other: Any?): Boolean {
        if (other !is Tetrimino) return false
        return this.cells().size == other.cells().size && this.cells().containsAll(other.cells())
    }

    override fun hashCode(): Int {
        var hash = 3
        hash += 23 * this.centerOfRotation.hashCode()
        this.blocks.forEach {
            hash += 31 * it.hashCode()
        }
        hash += this.orientation.hashCode()

        return hash
    }
}

private val orientationWheel = listOf(Orientation.UP, Orientation.RIGHT, Orientation.DOWN, Orientation.LEFT)

