package model.game

import model.board.Board
import model.game.config.GameConfiguration
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
class FortyLineSprint(val board: Board, config: GameConfiguration) : BaseGame(board, config) {
    var linesCleared = 0
    var startTime: Duration? = null
    var endTime: Duration? = null
    var prevNumRowsWithAnyCells = 0

    init {
        super.onBoardChange { updateLinesCleared() }
    }

    private fun numberOfRowsWithAnyCells(board: Board): Int {
        return board.getPlacedCells().groupBy { it.row }.size
    }

    private fun updateLinesCleared() {
        val numRowsWithAnyCells = numberOfRowsWithAnyCells(board)
        if (numRowsWithAnyCells < prevNumRowsWithAnyCells) {
            linesCleared += prevNumRowsWithAnyCells - numRowsWithAnyCells
        }

        prevNumRowsWithAnyCells = numRowsWithAnyCells
    }

    override fun finished(): Boolean {
        if (super.finished() || linesCleared >= 40) {
            if (endTime != null) {
                endTime = timeStamp()
            }

            return true
        }

        return false
    }

    override fun start() {
        super.start()
        startTime = timeStamp()
    }
}
