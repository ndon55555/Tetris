package controller

import javafx.event.EventHandler
import javafx.scene.input.KeyEvent
import model.Board
import view.TetrisUI

/**
 * Represents a controller for a Tetris game.
 */
interface TetrisController : EventHandler<KeyEvent> {
    fun run(board: Board, view: TetrisUI)

    fun stop()
}