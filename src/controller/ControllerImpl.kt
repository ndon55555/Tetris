package controller

import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import model.BOARD_WIDTH
import model.Board
import model.Cell
import model.CellColor
import model.CellImpl
import model.Tetrimino
import model.TetriminoType
import model.initTetrimino
import tornadofx.Controller
import view.TetrisUI
import java.util.Collections
import java.util.Timer
import kotlin.concurrent.schedule

class ControllerImpl : Controller(), TetrisController {
    private lateinit var clockTimer: Timer
    private lateinit var frameTimer: Timer
    private lateinit var board: Board
    private lateinit var view: TetrisUI
    private lateinit var activePiece: Tetrimino
    private var isRunning = false
    private val generator: TetriminoGenerator = RandomBagOf7()
    private val showGhost = true
    private val pressedRepeatableKeys = Collections.synchronizedSet(mutableSetOf<KeyCode>())
    private val pressedNonRepeatableKeys = Collections.synchronizedSet(mutableSetOf<KeyCode>())
    private var initialMoveLeft = true
    private var initialMoveRight = true
    private var initialMoveDown = true

    override fun handle(event: KeyEvent?) {
        if (event != null && isRunning) {
            when (event.eventType) {
                KeyEvent.KEY_PRESSED -> handleKeyPress(event.code)
                KeyEvent.KEY_RELEASED -> handleKeyRelease(event.code)
                else -> return
            }
        }
    }

    private fun handleKeyPress(code: KeyCode) {
        val repeatableKeys = setOf(KeyCode.DOWN, KeyCode.LEFT, KeyCode.RIGHT)
        if (code in repeatableKeys) {
            pressedRepeatableKeys += code
        } else {
            if (code !in pressedNonRepeatableKeys) {
                when (code) {
                    KeyCode.Z -> forActivePiece { rotate90CCW() }
                    KeyCode.UP -> forActivePiece { rotate90CW() }
                    KeyCode.SPACE -> forActivePiece { hardDrop() }
                    KeyCode.SHIFT -> println("hold")
                    else -> return
                }

                pressedNonRepeatableKeys += code
            }
        }
    }

    private fun handleKeyRelease(code: KeyCode) {
        when (code) {
            KeyCode.DOWN -> initialMoveDown = true
            KeyCode.LEFT -> initialMoveLeft = true
            KeyCode.RIGHT -> initialMoveRight = true
            else -> {
            }
        }
        pressedRepeatableKeys -= code
        pressedNonRepeatableKeys -= code
    }

    override fun run(board: Board, view: TetrisUI) {
        this.isRunning = true
        this.board = board
        this.view = view
        this.clockTimer = Timer()
        this.frameTimer = Timer()
        this.generator.reset()
        this.pressedRepeatableKeys.clear()
        this.pressedNonRepeatableKeys.clear()
        this.activePiece = generator.generate()

        clockTimer.schedule(0, 500) {
            val prev = activePiece
            forActivePiece { moveDown() }

            if (activePiece == prev) forActivePiece { hardDrop() }
        }

        val autoRepeatRate = 30L
        val delayAutoShift = 133L
        frameTimer.schedule(0, 1000 / autoRepeatRate) {
            pressedRepeatableKeys
                    .toSet() // create a copy to avoid concurrent modifications to pressedRepeatableKeys
                    .parallelStream()
                    .forEach { key ->
                        // FIXME needs synchronization
                        when (key) {
                            KeyCode.DOWN -> {
                                forActivePiece { moveDown() }

                                if (initialMoveDown) {
                                    initialMoveDown = false
                                    Thread.sleep(delayAutoShift)
                                }
                            }
                            KeyCode.LEFT -> {
                                forActivePiece { moveLeft() }

                                if (initialMoveLeft) {
                                    initialMoveLeft = false
                                    Thread.sleep(delayAutoShift)
                                }
                            }
                            KeyCode.RIGHT -> {
                                forActivePiece { moveRight() }

                                if (initialMoveRight) {
                                    initialMoveRight = false
                                    Thread.sleep(delayAutoShift)
                                }
                            }
                            else -> {
                            }
                        }
                    }
        }
    }

    override fun stop() {
        clockTimer.cancel()
        frameTimer.cancel()
        this.isRunning = false
    }

    private fun allCells(): Set<Cell> = board.getPlacedCells().toMutableSet().also {
        it.addAll(activePiece.cells())
        if (showGhost) it.addAll(activePiece.ghostCells())
    }

    private fun forActivePiece(op: Tetrimino.() -> Tetrimino) {
        synchronized(activePiece) {
            val next = activePiece.op()
            if (next.isValid()) activePiece = next
        }

        synchronized(view) {
            view.drawCells(allCells())
        }
    }

    private fun Tetrimino.isValid(): Boolean = synchronized(board) {
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
            synchronized(board) {
                val cellsInRow = board.getPlacedCells().filter { it.row == line }.size
                if (cellsInRow == BOARD_WIDTH) board.clearLine(line)
            }
        }
    }

    private fun Tetrimino.placeOnBoard() {
        val cells = this.cells().toTypedArray()

        synchronized(board) {
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
        ghostCells.removeAll { this.cells().any { c -> it.sharesPositionWith(c) } }
        return ghostCells
    }
}

interface TetriminoGenerator {
    fun generate(): Tetrimino

    fun reset()
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
    private var currentBag = newBag()

    override fun generate(): Tetrimino {
        if (currentBag.isEmpty()) reset()
        return currentBag.removeAt(0)
    }

    override fun reset() {
        currentBag = newBag()
    }

    private fun newBag(): MutableList<Tetrimino> = allPieces.shuffled().toMutableList()
}