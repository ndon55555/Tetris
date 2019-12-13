import controller.ControllerImpl
import controller.TetrisController
import model.game.config.GameConfiguration
import model.board.BoardImpl
import model.game.BaseGame
import view.TetrisUI
import view.TetrisWeb
import kotlin.browser.document
import kotlin.time.ExperimentalTime

@ExperimentalTime
fun main() {
    val controller: TetrisController = ControllerImpl()
    val view: TetrisUI = TetrisWeb()

    document.body?.onkeydown = {
        val key = it.key.toLowerCase()
        if (key == "r") {
            controller.stop()
            controller.run(BaseGame(BoardImpl(), GameConfiguration().apply {
                delayedAutoShift = 110
                autoRepeatRate = 5
            }), view)
        } else {
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

    controller.run(BaseGame(BoardImpl(), GameConfiguration().apply {
        delayedAutoShift = 110
        autoRepeatRate = 5
    }), view)
}