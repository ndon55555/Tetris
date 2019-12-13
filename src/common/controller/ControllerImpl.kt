package controller

import model.board.FIRST_VISIBLE_ROW
import model.cell.Cell
import model.game.BaseGame
import model.game.Command
import view.TetrisUI
import kotlin.time.ExperimentalTime

expect fun runAtFixedRate(period: Long, shouldContinue: () -> Boolean, event: () -> Unit)

@ExperimentalTime
class ControllerImpl : TetrisController {
    val fps = 60
    lateinit var game: BaseGame
    lateinit var view: TetrisUI
    private var keyToCommand = mutableMapOf( // TODO need a better system for this
        "z" to Command.ROTATE_CCW,
        "up" to Command.ROTATE_CW,
        "left" to Command.LEFT,
        "right" to Command.RIGHT,
        "down" to Command.SOFT_DROP,
        "space" to Command.HARD_DROP,
        "shift" to Command.HOLD
    )

    override fun run(game: BaseGame, view: TetrisUI) {
        this.game = game
        this.view = view
        game.onBoardChange { view.drawCells(game.allCells().standardize()) }
        game.onHeldPieceChange { view.drawHeldCells(game.heldCells()) }
        game.onUpcomingPiecesChange { view.drawUpcomingCells(game.upcomingCells()) }
        game.start()
        view.drawCells(emptySet())
        view.drawHeldCells(emptySet())
        view.drawUpcomingCells(game.upcomingCells())

        runAtFixedRate(1000L / fps, { !game.finished }) {
            game.frame()
        }
    }

    override fun stop() {
        game.finished = true
    }

    override fun handleKeyPress(key: String) {
        if (game.finished) return

        game.commandInitiated(keyToCommand.getOrElse(key, { Command.DO_NOTHING }))
    }

    override fun handleKeyRelease(key: String) {
        if (game.finished) return

        game.commandStopped(keyToCommand.getOrElse(key, { Command.DO_NOTHING }))
    }

    // Changes the cells in such a way that they can be rendered properly
    private fun Set<Cell>.standardize(): Set<Cell> =
        this.map { it.move(-FIRST_VISIBLE_ROW, 0) }
            .filter { it.row >= 0 }
            .toSet()
}