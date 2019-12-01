import controller.SingleThreadFreePlay
import controller.TetrisController
import controller.config.GameConfiguration
import model.board.Board
import model.board.BoardImpl
import view.TetrisUI
import view.TetrisWeb
import kotlin.browser.document
import kotlin.time.ExperimentalTime

@ExperimentalTime
fun main() {
    val model: Board = BoardImpl()
    val controller: TetrisController = SingleThreadFreePlay(GameConfiguration().apply {
        delayedAutoShift = 110
        autoRepeatRate = 10
    })
    val view: TetrisUI = TetrisWeb()

    document.body?.onkeydown = {
        val key = it.key.toLowerCase()
        controller.handleKeyPress(
            when (key) {
                "arrowleft"  -> "left"
                "arrowright" -> "right"
                "arrowup"    -> "up"
                "arrowdown"  -> "down"
                " "          -> "space"
                "shift"      -> "shift"
                else         -> key
            }
        )
    }

    document.body?.onkeyup = {
        val key = it.key.toLowerCase()
        controller.handleKeyRelease(
            when (key) {
                "arrowleft"  -> "left"
                "arrowright" -> "right"
                "arrowup"    -> "up"
                "arrowdown"  -> "down"
                " "          -> "space"
                "shift"      -> "shift"
                else         -> key
            }
        )
    }

    controller.run(model, view)
}