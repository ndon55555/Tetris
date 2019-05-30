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
import java.util.Random
import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate

class ControllerImpl : Controller(), TetrisController {
    private lateinit var timer: Timer
    private lateinit var board: Board
    private lateinit var view: TetrisUI
    private val boardLock = Any()
    private val viewLock = Any()
    private var isRunning = false
    private lateinit var activePiece: Tetrimino

    override fun handle(event: KeyEvent?) {
        if (event != null && isRunning) {
            when (event.code) {
                KeyCode.Z -> activePiece = activePiece.onTheBoard { rotate90CCW() }
                KeyCode.UP -> activePiece = activePiece.onTheBoard { rotate90CW() }
                KeyCode.SPACE -> activePiece = activePiece.hardDrop()
                KeyCode.DOWN -> activePiece = activePiece.onTheBoard { moveDown() }
                KeyCode.LEFT -> activePiece = activePiece.onTheBoard { moveLeft() }
                KeyCode.RIGHT -> activePiece = activePiece.onTheBoard { moveRight() }
                KeyCode.SHIFT -> println("hold")
                else -> return
            }

            synchronized(viewLock) {
                view.drawCells(allCells())
            }
        }

    }

    override fun run(board: Board, view: TetrisUI) {
        this.isRunning = true
        this.board = board
        this.view = view
        this.timer = Timer()
        this.activePiece = generateTetrimino()
        timer.scheduleAtFixedRate(0, 750) {
            val next = activePiece.onTheBoard { moveDown() }
            if (activePiece == next) {
                activePiece.placeOnBoard()
                activePiece.clearCompletedLines()
                activePiece = newActivePiece()
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
        this.isRunning = false
    }

    private fun allCells(): Set<Cell> = board.getPlacedCells().toMutableSet().also { it.addAll(activePiece.cells()) }

    private fun Tetrimino.onTheBoard(op: Tetrimino.() -> Tetrimino): Tetrimino {
        val next = this.op()
        if (next.isValid()) return next

        return this
    }

    private fun Tetrimino.isValid(): Boolean = synchronized(boardLock) {
        board.areValidCells(*this.cells().toTypedArray())
    }

    private fun Tetrimino.hardDrop(): Tetrimino {
        var t = this
        while (t.moveDown().isValid()) t = t.moveDown()
        t.placeOnBoard()
        t.clearCompletedLines()
        return newActivePiece()
    }

    private fun Tetrimino.clearCompletedLines() {
        val candidateLines = this.cells().map { it.row }.distinct().sorted()
        for (line in candidateLines) {
            synchronized(boardLock) {
                if (board.getPlacedCells().filter { it.row == line }.size == 10) {
                    board.clearLine(line)
                }
            }
        }
    }

    private fun Tetrimino.placeOnBoard() {
        val cells = this.cells().toTypedArray()

        synchronized(boardLock) {
            board.placeCells(*cells)
        }
    }

    private fun newActivePiece(): Tetrimino {
        val newPiece = generateTetrimino()
        // check for topping out
        if (!newPiece.isValid()) stop()

        return newPiece
    }
}

private fun generateTetrimino(): Tetrimino {
    return when (Random().nextInt(6)) {
        0 -> initTetrimino(TetriminoType.L)
        1 -> initTetrimino(TetriminoType.T)
        2 -> initTetrimino(TetriminoType.S)
        3 -> initTetrimino(TetriminoType.I)
        4 -> initTetrimino(TetriminoType.J)
        5 -> initTetrimino(TetriminoType.O)
        else -> initTetrimino(TetriminoType.Z)
    }
}
