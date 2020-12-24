package controller

import model.board.FIRST_VISIBLE_ROW
import model.cell.Cell
import model.game.Command
import model.game.Game
import view.TetrisUI
import kotlin.time.ExperimentalTime

expect fun runAtFixedRate(period: Long, shouldContinue: () -> Boolean, event: () -> Unit)

@ExperimentalTime
class ControllerImpl : TetrisController {
    val fps = 60
    lateinit var game: Game
    lateinit var view: TetrisUI

    override fun run(game: Game, view: TetrisUI) {
        this.game = game
        this.view = view
        game.onBoardChange { view.drawCells(game.allCells().standardize()) }
        game.onHeldPieceChange { view.drawHeldCells(game.heldCells()) }
        game.onUpcomingPiecesChange { view.drawUpcomingCells(game.upcomingCells()) }
        game.start()
        view.drawCells(game.allCells().standardize())
        view.drawHeldCells(game.heldCells())
        view.drawUpcomingCells(game.upcomingCells())

        runAtFixedRate(1000L / fps, { !game.finished() }) {
            game.frame()
        }
    }

    override fun stop() {
        game.stop()
    }

    override fun handleCmdPress(cmd: Command) {
        if (game.finished()) return

        game.commandInitiated(cmd)
    }

    override fun handleCmdRelease(cmd: Command) {
        if (game.finished()) return

        game.commandStopped(cmd)
    }

    // Changes the cells in such a way that they can be rendered properly
    private fun Set<Cell>.standardize(): Set<Cell> =
        this.map { it.move(-FIRST_VISIBLE_ROW, 0) }
            .filter { it.row >= 0 }
            .toSet()
}