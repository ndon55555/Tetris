package controller

import model.board.Board
import view.TetrisUI

/**
 * Represents a controller for a Tetris game.
 */
interface TetrisController {
    fun run(board: Board, view: TetrisUI)

    fun stop()

    fun handleKeyPress(key: String)

    fun handleKeyRelease(key: String)
}