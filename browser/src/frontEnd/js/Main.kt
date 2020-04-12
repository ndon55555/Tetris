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
import model.game.Command
import model.game.config.GameConfiguration
import org.w3c.dom.HTMLButtonElement
import view.TetrisUI
import view.TetrisWeb
import kotlin.browser.document
import kotlin.time.ExperimentalTime

@ExperimentalTime
fun main() {
    loadHTML()
    loadGame()
}

const val BOARD_ID = "board"
const val HOLD_ID = "hold"
const val UPCOMING_PIECES_ID = "upcomingPieces"
const val RESTART_ID = "restart"

fun loadHTML() {
    document.body!!.style.apply {
        backgroundImage = "url('https://media.giphy.com/media/5PjafLZFxMWc/giphy.gif')"
        backgroundRepeat = "no-repeat"
        backgroundSize = "100% 100%"
    }
    document.body!!.append {
        span {
            style = "position: absolute; top: 50%; left: 50%; margin-right: -50%; transform: translate(-50%, -50%)"
            div {
                style = "display: inline-block; vertical-align: top"
                canvas {
                    id = HOLD_ID
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
                    id = BOARD_ID
                    width = "300"
                    height = "600"
                }
            }
            +" "
            div {
                id = UPCOMING_PIECES_ID
                style = "display: inline-block; vertical-align: top"
            }
        }
    }
}

@ExperimentalTime
fun loadGame() {
    val controller: TetrisController = ControllerImpl()
    val view: TetrisUI = TetrisWeb()

    val restartGame = {
        controller.stop()
        controller.run(BaseGame(BoardImpl(), GameConfiguration()), view)
    }

    val keysToCommand = { key: String ->
        when (key) {
            "arrowleft"  -> Command.LEFT
            "arrowright" -> Command.RIGHT
            "arrowup"    -> Command.ROTATE_CW
            "arrowdown"  -> Command.SOFT_DROP
            "z"          -> Command.ROTATE_CCW
            " "          -> Command.HARD_DROP
            "shift"      -> Command.HOLD
            else         -> Command.DO_NOTHING
        }
    }

    document.body?.onkeydown = {
        val key = it.key.toLowerCase()
        if (key == "r") {
            restartGame()
        } else {
            controller.handleCmdPress(keysToCommand(key))
        }
    }

    document.body?.onkeyup = {
        val key = it.key.toLowerCase()
        controller.handleCmdRelease(keysToCommand(key))
    }

    (document.getElementById(RESTART_ID) as HTMLButtonElement).onclick = {
        restartGame()
    }

    controller.run(BaseGame(BoardImpl(), GameConfiguration()), view)
}