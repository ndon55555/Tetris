package model

import kotlin.math.abs

/**
 * Represents a Tetris game piece.
 *
 * @property centerOfRotation The model.Posn with which the model.TetriminoImpl will rotate.
 * @property blocks The blocks that compose a model.TetriminoImpl.
 */
data class TetriminoImpl(val centerOfRotation: Posn, val blocks: Array<Cell>) : Tetrimino {
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

    override fun cells(): Set<Cell> = setOf(*blocks)

    /**
     * @param dRow Number of rows to move from the top of the board.
     * @param dCol Number of columns to move from the left of the board.
     * @return This TetriminoImpl translated over dRow and dCol.
     */
    private fun move(dRow: Int, dCol: Int): TetriminoImpl = TetriminoImpl(
            centerOfRotation.translate(dRow.toDouble(), dCol.toDouble()),
            blocks.map { it.move(dRow, dCol) }.toTypedArray())

    override fun moveUp(): TetriminoImpl = move(-1, 0)

    override fun moveDown(): TetriminoImpl = move(1, 0)

    override fun moveLeft(): TetriminoImpl = move(0, -1)

    override fun moveRight(): TetriminoImpl = move(0, 1)

    override fun rotate90CW(): TetriminoImpl = TetriminoImpl(centerOfRotation, blocks.map {
        it.rotate90CWAround(centerOfRotation)
    }.toTypedArray())

    override fun rotate90CCW(): TetriminoImpl = TetriminoImpl(centerOfRotation, blocks.map {
        it.rotate90CCWAround(centerOfRotation)
    }.toTypedArray())

    override fun equals(other: Any?): Boolean {
        if (other !is TetriminoImpl) return false

        return (this.centerOfRotation == other.centerOfRotation) && (this.blocks.all { it in other.blocks })
    }

    override fun hashCode(): Int {
        var hash = 1
        hash *= 31 + this.centerOfRotation.hashCode()
        hash *= 31 + this.blocks.contentDeepHashCode()

        return hash
    }
}

