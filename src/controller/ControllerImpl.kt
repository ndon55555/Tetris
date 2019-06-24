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
import model.tetrimino.Orientation
import model.tetrimino.S
import model.tetrimino.StandardTetrimino
import model.tetrimino.T
import model.tetrimino.Z
import tornadofx.Controller
import view.TetrisUI
import java.util.Collections
import java.util.Timer
import kotlin.concurrent.schedule
import kotlin.math.abs

class ControllerImpl : Controller(), TetrisController {
    private lateinit var clockTimer: Timer
    private lateinit var board: Board
    private lateinit var view: TetrisUI
    private lateinit var rotationSystem: RotationSystem
    private lateinit var activePiece: StandardTetrimino
    private var isRunning = false
    private val generator: StandardTetriminoGenerator = RandomBagOf7()
    private val showGhost = true
    private val autoRepeatRate = 30L // Milliseconds between each auto repeat
    private val delayAutoShift = 140L // Milliseconds before activating auto repeat
    private val pressedKeys = Collections.synchronizedSet(mutableSetOf<KeyCode>())
    private val repeatableKeys = setOf(KeyCode.DOWN, KeyCode.LEFT, KeyCode.RIGHT)
    private val keyToAction = mapOf(
            KeyCode.Z to { forActivePiece { t -> rotationSystem.rotate90CCW(t, board) } },
            KeyCode.UP to { forActivePiece { t -> rotationSystem.rotate90CW(t, board) } },
            KeyCode.LEFT to { forActivePiece { t -> t.moveLeft() } },
            KeyCode.RIGHT to { forActivePiece { t -> t.moveRight() } },
            KeyCode.DOWN to { forActivePiece { t -> t.moveDown() } },
            KeyCode.SPACE to { forActivePiece { t -> t.hardDrop() } },
            KeyCode.SHIFT to { println("hold") }
    ).withDefault { {} }
    private val keyRepeatThreads = Collections.synchronizedMap(mutableMapOf<KeyCode, Thread>())

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
        if (code !in pressedKeys) {
            when (code) {
                KeyCode.RIGHT -> pressedKeys -= KeyCode.LEFT
                KeyCode.LEFT -> pressedKeys -= KeyCode.RIGHT
                else -> {
                }
            }

            pressedKeys += code
            val action = keyToAction.getValue(code)
            action.invoke()

            if (code in repeatableKeys) {
                val keyRepeat = Thread(object : Runnable {
                    override fun run() {
                        if (code == KeyCode.LEFT || code == KeyCode.RIGHT) {
                            if (!delayCompletely(delayAutoShift)) return
                        }

                        while (pressedKeys.contains(code)) {
                            action.invoke()
                            if (!delayCompletely(autoRepeatRate)) return
                        }
                    }
                })

                keyRepeat.isDaemon = true
                keyRepeatThreads[code] = keyRepeat
                keyRepeat.start()
            }
        }
    }

    /**
     * @param duration How long to sleep the current thread.
     * @return Whether or not the thread slept for the full specified duration.
     */
    private fun delayCompletely(duration: Long): Boolean {
        try {
            Thread.sleep(duration)
        } catch (e: InterruptedException) {
            return false
        }

        return true
    }

    private fun handleKeyRelease(code: KeyCode) {
        pressedKeys -= code
        keyRepeatThreads[code]?.interrupt()
        keyRepeatThreads -= code
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
        this.rotationSystem = SuperRotation()
        this.isRunning = true
        this.clockTimer = Timer()
        this.generator.reset()
        this.pressedKeys.clear()
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

    private fun forActivePiece(op: (StandardTetrimino) -> StandardTetrimino) {
        synchronized(activePiece) {
            val next = op(activePiece)
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

interface RotationSystem {
    fun rotate90CW(t: StandardTetrimino, board: Board): StandardTetrimino

    fun rotate90CCW(t: StandardTetrimino, board: Board): StandardTetrimino
}

class SuperRotation : RotationSystem {
    val jlstzoData = mapOf(
            Orientation.UP to mapOf(
                    Orientation.LEFT to listOf(Pair(1, 0), Pair(1, 1), Pair(0, -2), Pair(1, -2)),
                    Orientation.RIGHT to listOf(Pair(-1, 0), Pair(-1, 1), Pair(0, -2), Pair(-1, -2))
            ),
            Orientation.RIGHT to mapOf(
                    Orientation.UP to listOf(Pair(1, 0), Pair(1, -1), Pair(0, 2), Pair(1, 2)),
                    Orientation.DOWN to listOf(Pair(1, 0), Pair(1, -1), Pair(0, 2), Pair(1, 2))
            ),
            Orientation.DOWN to mapOf(
                    Orientation.RIGHT to listOf(Pair(-1, 0), Pair(-1, 1), Pair(0, -2), Pair(-1, -2)),
                    Orientation.LEFT to listOf(Pair(1, 0), Pair(1, 1), Pair(0, -2), Pair(1, -2))
            ),
            Orientation.LEFT to mapOf(
                    Orientation.DOWN to listOf(Pair(-1, 0), Pair(-1, -1), Pair(0, 2), Pair(-1, 2)),
                    Orientation.UP to listOf(Pair(-1, 0), Pair(-1, -1), Pair(0, 2), Pair(-1, 2))
            )
    )

    val iData = mapOf(
            Orientation.UP to mapOf(
                    Orientation.LEFT to listOf(Pair(-1, 0), Pair(2, 0), Pair(-1, 2), Pair(2, -1)),
                    Orientation.RIGHT to listOf(Pair(-2, 0), Pair(1, 0), Pair(-2, -1), Pair(1, 2))
            ),
            Orientation.RIGHT to mapOf(
                    Orientation.UP to listOf(Pair(2, 0), Pair(-1, 0), Pair(2, 1), Pair(-1, -2)),
                    Orientation.DOWN to listOf(Pair(-1, 0), Pair(2, 0), Pair(-1, 2), Pair(2, -1))
            ),
            Orientation.DOWN to mapOf(
                    Orientation.RIGHT to listOf(Pair(1, 0), Pair(-2, 0), Pair(1, -2), Pair(-2, 1)),
                    Orientation.LEFT to listOf(Pair(2, 0), Pair(-1, 0), Pair(2, 1), Pair(-1, -2))
            ),
            Orientation.LEFT to mapOf(
                    Orientation.DOWN to listOf(Pair(-2, 0), Pair(1, 0), Pair(-2, -1), Pair(1, 2)),
                    Orientation.UP to listOf(Pair(1, 0), Pair(-2, 0), Pair(1, -2), Pair(-2, 1))
            )
    )

    override fun rotate90CW(t: StandardTetrimino, board: Board): StandardTetrimino = superRotate(t, board) { rotate90CW() }

    override fun rotate90CCW(t: StandardTetrimino, board: Board): StandardTetrimino = superRotate(t, board) { rotate90CCW() }

    private fun superRotate(t: StandardTetrimino, board: Board, op: StandardTetrimino.() -> StandardTetrimino): StandardTetrimino {
        val rotated = t.op()
        if (board.areValidCells(*rotated.cells().toTypedArray())) return rotated

        val targetOrientation = rotated.orientation()
        val data = if (t is I) iData else jlstzoData
        val testDeltas: List<Pair<Int, Int>> = data[t.orientation()]!![targetOrientation]!!
        for ((dx, dy) in testDeltas) {
            var candidate = rotated

            repeat(abs(dx)) {
                candidate = if (dx < 0) {
                    candidate.moveLeft()
                } else {
                    candidate.moveRight()
                }
            }

            repeat(abs(dy)) {
                candidate = if (dy < 0) {
                    candidate.moveDown()
                } else {
                    candidate.moveUp()
                }
            }

            if (board.areValidCells(*candidate.cells().toTypedArray())) return candidate
        }

        return t
    }
}