package controller

import model.game.BaseGame
import view.TetrisUI
import kotlin.time.ExperimentalTime

/**
 * Represents a controller for a Tetris game.
 */
@ExperimentalTime
interface TetrisController {
    fun run(game: BaseGame, view: TetrisUI)

    fun stop()

    fun handleKeyPress(key: String)

    fun handleKeyRelease(key: String)
}