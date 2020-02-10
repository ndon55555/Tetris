import controller.ControllerImpl
import controller.TetrisController
import kotlinx.html.button
import kotlinx.html.dom.append
import kotlinx.html.id
import kotlinx.html.js.canvas
import kotlinx.html.js.div
import kotlinx.html.js.span
import kotlinx.html.style
import model.board.BoardImpl
import model.game.BaseGame
import model.game.config.GameConfiguration
import view.TetrisUI
import view.TetrisWeb
import kotlin.browser.document
import kotlin.time.ExperimentalTime

@ExperimentalTime
fun main() {
    loadHTML()

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

fun loadHTML() {
    document.body!!.append {
        span {
            div {
                style = "display: inline-block; vertical-align: top"
                canvas {
                    id = "hold"
                    width = "120"
                    height = "120"
                }
                button {
                    id = "restart"
                    style = "display: block"
                    +"Restart"
                }
            }
            +" "
            div {
                style = "display: inline-block; vertical-align: top"
                canvas {
                    id = "board"
                    width = "300"
                    height = "600"
                }
            }
            +" "
            div {
                id = "upcomingPieces"
                style = "display: inline-block; vertical-align: top"
            }
        }
    }
}