package controller

import model.game.Command
import model.game.Game
import view.TetrisUI
import kotlin.time.ExperimentalTime

/**
 * Represents a controller for a Tetris game.
 */
@ExperimentalTime
interface TetrisController {
    fun run(game: Game, view: TetrisUI)

    fun stop()

    fun handleCmdPress(cmd: Command)

    fun handleCmdRelease(cmd: Command)
}