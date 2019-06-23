package controller

import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import model.board.BOARD_WIDTH
import model.board.Board
import model.cell.Cell
import model.cell.CellColor
import model.cell.CellImpl
import model.tetrimino.I
import model.tetrimino.J
import model.tetrimino.L
import model.tetrimino.O
import model.tetrimino.S
import model.tetrimino.StandardTetrimino
import model.tetrimino.T
import model.tetrimino.Z
import tornadofx.Controller
import view.TetrisUI
import java.util.Collections
import java.util.Timer
import kotlin.concurrent.schedule

class ControllerImpl : Controller(), TetrisController {
    private lateinit var clockTimer: Timer
    private lateinit var board: Board
    private lateinit var view: TetrisUI
    private lateinit var activePiece: StandardTetrimino
    private var isRunning = false
    private val generator: StandardTetriminoGenerator = RandomBagOf7()
    private val showGhost = true
    private val autoRepeatRate = 30L // Milliseconds between each auto repeat
    private val delayAutoShift = 140L // Milliseconds before activating auto repeat
    private val pressedRepeatableKeys = Collections.synchronizedSet(mutableSetOf<KeyCode>())
    private val pressedNonRepeatableKeys = Collections.synchronizedSet(mutableSetOf<KeyCode>())
    private val repeatableKeys = setOf(KeyCode.DOWN, KeyCode.LEFT, KeyCode.RIGHT)
    private val keyToAction = mapOf(
            KeyCode.Z to { forActivePiece { rotate90CCW() } },
            KeyCode.UP to { forActivePiece { rotate90CW() } },
            KeyCode.LEFT to { forActivePiece { moveLeft() } },
            KeyCode.RIGHT to { forActivePiece { moveRight() } },
            KeyCode.DOWN to { forActivePiece { moveDown() } },
            KeyCode.SPACE to { forActivePiece { hardDrop() } },
            KeyCode.SHIFT to { println("hold") }
    ).withDefault { {} }

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
        if (code in repeatableKeys) {
            if (code !in pressedRepeatableKeys) {
                pressedRepeatableKeys += code
                keyToAction.getValue(code).invoke()

                Thread {
                    if (code != KeyCode.DOWN) Thread.sleep(delayAutoShift)

                    while (pressedRepeatableKeys.contains(code)) {
                        keyToAction.getValue(code).invoke()
                        Thread.sleep(autoRepeatRate)
                    }
                }.start()
            }
        } else {
            if (code !in pressedNonRepeatableKeys) {
                keyToAction.getValue(code).invoke()
                pressedNonRepeatableKeys += code
            }
        }
    }

    private fun handleKeyRelease(code: KeyCode) {
        pressedRepeatableKeys -= code
        pressedNonRepeatableKeys -= code
    }

    override fun run(board: Board, view: TetrisUI) {
        this.board = object : Board {
            @Synchronized
            override fun areValidCells(vararg cells: Cell): Boolean = board.areValidCells(*cells)

            @Synchronized
            override fun placeCells(vararg cells: Cell) = board.placeCells(*cells)

            @Synchronized
            override fun clearLine(row: Int) = board.clearLine(row)

            @Synchronized
            override fun getPlacedCells() = board.getPlacedCells()
        }
        this.view = object : TetrisUI {
            @Synchronized
            override fun drawCells(cells: Set<Cell>) = view.drawCells(cells)
        }
        this.isRunning = true
        this.clockTimer = Timer()
        this.generator.reset()
        this.pressedRepeatableKeys.clear()
        this.pressedNonRepeatableKeys.clear()
        this.activePiece = generator.generate()

        clockTimer.schedule(0, 1000) {
            val prev = activePiece
            keyToAction.getValue(KeyCode.DOWN).invoke()

            if (activePiece == prev) keyToAction.getValue(KeyCode.SPACE).invoke()
        }
    }

    override fun stop() {
        clockTimer.cancel()
        this.isRunning = false
    }

    private fun allCells(): Set<Cell> = board.getPlacedCells().toMutableSet().also {
        it.addAll(activePiece.cells())
        if (showGhost) it.addAll(activePiece.ghostCells())
    }

    private fun forActivePiece(op: StandardTetrimino.() -> StandardTetrimino) {
        synchronized(activePiece) {
            val next = activePiece.op()
            if (next.isValid()) activePiece = next
        }

        view.drawCells(allCells())
    }

    private fun StandardTetrimino.isValid(): Boolean = board.areValidCells(*this.cells().toTypedArray())

    private fun StandardTetrimino.hardDrop(): StandardTetrimino {
        var t = this
        while (t.moveDown().isValid()) t = t.moveDown()
        t.placeOnBoard()
        t.clearCompletedLines()
        return newActivePiece()
    }

    private fun StandardTetrimino.clearCompletedLines() {
        val candidateLines = this.cells().map { it.row }.distinct().sorted()
        for (line in candidateLines) {
            val cellsInRow = board.getPlacedCells().filter { it.row == line }.size
            if (cellsInRow == BOARD_WIDTH) board.clearLine(line)
        }
    }

    private fun StandardTetrimino.placeOnBoard() {
        val cells = this.cells().toTypedArray()
        board.placeCells(*cells)
    }

    private fun newActivePiece(): StandardTetrimino {
        val newPiece = generator.generate()
        // check for topping out
        if (!newPiece.isValid()) stop()
        return newPiece
    }

    private fun StandardTetrimino.ghostCells(): Set<Cell> {
        var t = this
        while (t.moveDown().isValid()) t = t.moveDown()
        val ghostCells = t.cells().map { CellImpl(CellColor.NULL, it.row, it.col) }.toMutableSet()
        ghostCells.removeAll { this.cells().any { c -> it.sharesPositionWith(c) } }
        return ghostCells
    }
}

interface StandardTetriminoGenerator {
    fun generate(): StandardTetrimino

    fun reset()
}

class RandomBagOf7 : StandardTetriminoGenerator {
    private val allPieces = setOf(Z(), S(), L(), J(), T(), I(), O())
    private var currentBag = newBag()

    override fun generate(): StandardTetrimino {
        if (currentBag.isEmpty()) reset()
        return currentBag.removeAt(0)
    }

    override fun reset() {
        currentBag = newBag()
    }

    private fun newBag(): MutableList<StandardTetrimino> = allPieces.shuffled().toMutableList()
}