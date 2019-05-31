package controller

import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import model.Board
import model.Cell
import model.CellColor
import model.CellImpl
import model.Tetrimino
import model.TetriminoType
import model.initTetrimino
import tornadofx.Controller
import view.TetrisUI
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
    private val generator: TetriminoGenerator = RandomBagOf7()
    private val showGhost = true

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
        this.activePiece = generator.generate()
        timer.scheduleAtFixedRate(0, 500) {
            val next = activePiece.onTheBoard { moveDown() }
            activePiece = if (activePiece == next) {
                activePiece.placeOnBoard()
                activePiece.clearCompletedLines()
                newActivePiece()
            } else {
                next
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

    private fun allCells(): Set<Cell> = board.getPlacedCells().toMutableSet().also {
        it.addAll(activePiece.cells())
        if (showGhost) it.addAll(activePiece.ghostCells())
    }

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
        val newPiece = generator.generate()
        // check for topping out
        if (!newPiece.isValid()) stop()
        return newPiece
    }

    private fun Tetrimino.ghostCells(): Set<Cell> {
        var t = this
        while (t.moveDown().isValid()) t = t.moveDown()
        val ghostCells = t.cells().map { CellImpl(CellColor.NULL, it.row, it.col) }.toMutableSet()
        ghostCells.removeAll { activePiece.cells().any { c -> it.sharesPositionWith(c)} }
        return ghostCells
    }
}

interface TetriminoGenerator {
    fun generate(): Tetrimino
}

class RandomBagOf7 : TetriminoGenerator {
    private val allPieces = setOf(
            initTetrimino(TetriminoType.Z),
            initTetrimino(TetriminoType.S),
            initTetrimino(TetriminoType.L),
            initTetrimino(TetriminoType.J),
            initTetrimino(TetriminoType.T),
            initTetrimino(TetriminoType.I),
            initTetrimino(TetriminoType.O)
    )
    private var currentBag = allPieces.shuffled().toMutableList()

    override fun generate(): Tetrimino {
        if (currentBag.isEmpty()) {
            currentBag = allPieces.shuffled().toMutableList()
        }

        return currentBag.removeAt(0)
    }
}