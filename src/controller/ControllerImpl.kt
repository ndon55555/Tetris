package controller

import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import model.Board
import model.Cell
import model.Tetrimino
import model.TetriminoType
import model.initTetrimino
import tornadofx.Controller
import view.TetrisUI
import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate

class ControllerImpl : Controller(), TetrisController {
    private val timer = Timer()
    private lateinit var board: Board
    private lateinit var view: TetrisUI
    private val boardLock = Any()
    private val viewLock = Any()

    var activePiece: Tetrimino = generateTetrimino()
    var i = 0

    private fun generateTetrimino(): Tetrimino {
        i = (i + 1) % 6
        return when (i) {
            0 -> initTetrimino(TetriminoType.L)
            1 -> initTetrimino(TetriminoType.T)
            2 -> initTetrimino(TetriminoType.S)
            3 -> initTetrimino(TetriminoType.I)
            4 -> initTetrimino(TetriminoType.J)
            5 -> initTetrimino(TetriminoType.O)
            else -> initTetrimino(TetriminoType.Z)
        }
    }

    override fun handle(event: KeyEvent?) {
        if (event != null) {
            when (event.code) {
                KeyCode.Z -> activePiece = activePiece.onTheBoard { rotate90CCW() }
                KeyCode.UP -> activePiece = activePiece.onTheBoard { rotate90CW() }
                KeyCode.SPACE -> println("hard drop")
                KeyCode.DOWN -> activePiece = activePiece.onTheBoard { moveDown() }
                KeyCode.LEFT -> activePiece = activePiece.onTheBoard { moveLeft() }
                KeyCode.RIGHT -> activePiece = activePiece.onTheBoard { moveRight() }
                KeyCode.SHIFT -> println("hold")
                else -> {
                }
            }
        }

        synchronized(viewLock) {
            view.drawCells(allCells())
        }
    }

    override fun run(board: Board, view: TetrisUI) {
        this.board = board
        this.view = view
        timer.scheduleAtFixedRate(0, 750) {
            val next = activePiece.onTheBoard { moveDown() }
            if (activePiece == next) {
                val curCells = activePiece.cells().toTypedArray()

                synchronized(boardLock) {
                    board.placeCells(*curCells)
                }

                val newPiece = generateTetrimino()
                if (newPiece.isValid()) {
                    activePiece = newPiece
                } else {
                    stop()
                }
            } else {
                activePiece = next
            }

            synchronized(viewLock) {
                view.drawCells(allCells())
            }
        }
    }

    override fun stop() {
        timer.cancel()
    }

    private fun allCells(): Set<Cell> = board.getPlacedCells().toMutableSet().also { it.addAll(activePiece.cells()) }

    private fun Tetrimino.onTheBoard(op: Tetrimino.() -> Tetrimino): Tetrimino {
        val next = this.op()
        if (next.isValid()) {
            return next
        }

        return this
    }

    private fun Tetrimino.isValid(): Boolean = synchronized(boardLock) {
        board.areValidCells(*this.cells().toTypedArray())
    }
}