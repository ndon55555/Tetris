package model.game.config

import model.board.Board
import model.tetrimino.I
import model.tetrimino.Orientation
import model.tetrimino.StandardTetrimino

interface RotationSystem {
    fun rotate90CW(t: StandardTetrimino, board: Board): StandardTetrimino

    fun rotate90CCW(t: StandardTetrimino, board: Board): StandardTetrimino
}

class SuperRotation : RotationSystem {
    private val jlstzoData = mapOf(
        Orientation.UP to mapOf(
            Orientation.LEFT to listOf(Pair(1, 0), Pair(1, 1), Pair(0, -2), Pair(1, -2)),
            Orientation.RIGHT to listOf(Pair(-1, 0), Pair(-1, 1), Pair(0, -2), Pair(-1, -2))
        ),
        Orientation.RIGHT to mapOf(
            Orientation.UP to listOf(Pair(1, 0), Pair(1, -1), Pair(0, 2), Pair(1, 2)),
            Orientation.DOWN to listOf(Pair(1, 0), Pair(1, -1), Pair(0, 2), Pair(1, 2))
        ),
        Orientation.DOWN to mapOf(
            Orientation.RIGHT to listOf(Pair(-1, 0), Pair(-1, 1), Pair(0, -2), Pair(-1, -2)),
            Orientation.LEFT to listOf(Pair(1, 0), Pair(1, 1), Pair(0, -2), Pair(1, -2))
        ),
        Orientation.LEFT to mapOf(
            Orientation.DOWN to listOf(Pair(-1, 0), Pair(-1, -1), Pair(0, 2), Pair(-1, 2)),
            Orientation.UP to listOf(Pair(-1, 0), Pair(-1, -1), Pair(0, 2), Pair(-1, 2))
        )
    )

    private val iData = mapOf(
        Orientation.UP to mapOf(
            Orientation.LEFT to listOf(Pair(-1, 0), Pair(2, 0), Pair(-1, 2), Pair(2, -1)),
            Orientation.RIGHT to listOf(Pair(-2, 0), Pair(1, 0), Pair(-2, -1), Pair(1, 2))
        ),
        Orientation.RIGHT to mapOf(
            Orientation.UP to listOf(Pair(2, 0), Pair(-1, 0), Pair(2, 1), Pair(-1, -2)),
            Orientation.DOWN to listOf(Pair(-1, 0), Pair(2, 0), Pair(-1, 2), Pair(2, -1))
        ),
        Orientation.DOWN to mapOf(
            Orientation.RIGHT to listOf(Pair(1, 0), Pair(-2, 0), Pair(1, -2), Pair(-2, 1)),
            Orientation.LEFT to listOf(Pair(2, 0), Pair(-1, 0), Pair(2, 1), Pair(-1, -2))
        ),
        Orientation.LEFT to mapOf(
            Orientation.DOWN to listOf(Pair(-2, 0), Pair(1, 0), Pair(-2, -1), Pair(1, 2)),
            Orientation.UP to listOf(Pair(1, 0), Pair(-2, 0), Pair(1, -2), Pair(-2, 1))
        )
    )

    override fun rotate90CW(t: StandardTetrimino, board: Board): StandardTetrimino =
        superRotate(t, board) { rotate90CW() }

    override fun rotate90CCW(t: StandardTetrimino, board: Board): StandardTetrimino =
        superRotate(t, board) { rotate90CCW() }

    private fun superRotate(
        t: StandardTetrimino,
        board: Board,
        op: StandardTetrimino.() -> StandardTetrimino
    ): StandardTetrimino {
        val rotated = t.op()
        if (board.areValidCells(*rotated.cells().toTypedArray())) return rotated

        val targetOrientation = rotated.orientation()
        val data = if (t is I) iData else jlstzoData
        val testDeltas: List<Pair<Int, Int>> = data.getValue(t.orientation()).getValue(targetOrientation)
        for ((dCol, dRow) in testDeltas) {
            var candidate = rotated
            candidate = candidate.move(dRow, dCol)

            if (board.areValidCells(*candidate.cells().toTypedArray())) return candidate
        }

        return t
    }
}

class BasicRotation : RotationSystem {
    override fun rotate90CW(t: StandardTetrimino, board: Board): StandardTetrimino = t.rotate90CW()

    override fun rotate90CCW(t: StandardTetrimino, board: Board): StandardTetrimino = t.rotate90CCW()
}
