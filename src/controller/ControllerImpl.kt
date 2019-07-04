package controller

import controller.config.RandomBagOf7
import controller.config.RotationSystem
import controller.config.StandardTetriminoGenerator
import controller.config.SuperRotation
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import model.board.BOARD_WIDTH
import model.board.Board
import model.cell.Cell
import model.cell.CellColor
import model.cell.CellImpl
import model.board.FIRST_VISIBLE_ROW
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
import java.util.LinkedList
import java.util.Queue
import java.util.Timer
import kotlin.concurrent.schedule

class ControllerImpl : Controller(), TetrisController {
    // Game settings
    lateinit var board: Board
    lateinit var view: TetrisUI
    lateinit var rotationSystem: RotationSystem
    lateinit var generator: StandardTetriminoGenerator
    var showGhost = true
    var autoRepeatRate = 30L // Milliseconds between each auto repeat
    var delayAutoShift = 140L // Milliseconds before activating auto repeat
    var previewPieces = 5
    var lockDelay = 500L // Milliseconds before locking a piece on the board

    // Auxiliary state
    private lateinit var clockTimer: Timer
    private lateinit var activePiece: StandardTetrimino
    private var isRunning = false
    private val pressedKeys = Collections.synchronizedSet(mutableSetOf<KeyCode>())
    private val repeatableKeys = setOf(KeyCode.DOWN, KeyCode.LEFT, KeyCode.RIGHT)
    private val keyRepeatThreads = Collections.synchronizedMap(mutableMapOf<KeyCode, Thread>())
    private var lockActivePieceThread = newLockActivePieceThread()
    private var alreadyHolding = false
    private var heldPiece: StandardTetrimino? = null
    private val upcomingPiecesQueue: Queue<StandardTetrimino> = LinkedList()
    private val keyToAction = mapOf(
            KeyCode.Z to { forActivePiece { t -> rotationSystem.rotate90CCW(t, board) } },
            KeyCode.UP to { forActivePiece { t -> rotationSystem.rotate90CW(t, board) } },
            KeyCode.LEFT to { forActivePiece { t -> t.moveLeft() } },
            KeyCode.RIGHT to { forActivePiece { t -> t.moveRight() } },
            KeyCode.DOWN to { forActivePiece { t -> t.moveDown() } },
            KeyCode.SPACE to { forActivePiece { t -> t.hardDrop() } },
            KeyCode.SHIFT to { forActivePiece { t -> t.hold() } }
    ).withDefault { {} }

    override fun handle(event: KeyEvent?) {
        if (event != null) {
            when (event.eventType) {
                KeyEvent.KEY_PRESSED -> handleKeyPress(event.code)
                KeyEvent.KEY_RELEASED -> handleKeyRelease(event.code)
                else -> return
            }
        }
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
            val cellsLock = Object()
            val heldCellsLock = Object()
            val upcomingCellsLock = Object()

            override fun drawCells(cells: Set<Cell>) = synchronized(cellsLock) {
                view.drawCells(cells
                        .map { it.move(-FIRST_VISIBLE_ROW, 0) }
                        .filter { it.row >= 0 }
                        .toSet())
            }

            override fun drawHeldCells(cells: Set<Cell>) = synchronized(heldCellsLock) { view.drawHeldCells(cells) }

            override fun drawUpcomingCells(cellsQueue: Queue<Set<Cell>>) = synchronized(upcomingCellsLock) {
                view.drawUpcomingCells(cellsQueue)
            }
        }
        this.rotationSystem = SuperRotation()
        this.generator = RandomBagOf7()
        this.isRunning = true
        this.clockTimer = Timer()
        this.pressedKeys.clear()
        this.activePiece = generator.generate()
        this.heldPiece = null
        repeat(previewPieces) { upcomingPiecesQueue.add(generator.generate()) }

        view.drawCells(allCells())
        view.drawHeldCells(emptySet())
        view.drawUpcomingCells(LinkedList(upcomingPiecesQueue.map { it.cells() }))

        clockTimer.schedule(0, 1000) {
            if (KeyCode.DOWN !in pressedKeys) keyToAction.getValue(KeyCode.DOWN).invoke()
        }
    }

    override fun stop() {
        pressedKeys.clear()
        for (t in keyRepeatThreads.values) t.interrupt()
        keyRepeatThreads.clear()
        lockActivePieceThread.interrupt()
        upcomingPiecesQueue.clear()
        clockTimer.cancel()
        this.isRunning = false
    }

    private fun StandardTetrimino.isValid(): Boolean = board.areValidCells(*this.cells().toTypedArray())

    private fun StandardTetrimino.hardDrop(): StandardTetrimino {
        var t = this
        while (t.moveDown().isValid()) t = t.moveDown()
        t.placeOnBoard()
        t.clearCompletedLines()
        val newPiece = nextPiece()
        // check for topping out
        if (!newPiece.isValid()) stop()
        alreadyHolding = false
        return newPiece
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

    private fun StandardTetrimino.ghostCells(): Set<Cell> {
        var t = this
        while (t.moveDown().isValid()) t = t.moveDown()
        val ghostCells = t.cells().map { CellImpl(CellColor.NULL, it.row, it.col) }.toMutableSet()
        ghostCells.removeAll { this.cells().any { c -> it.sharesPositionWith(c) } }
        return ghostCells
    }

    private fun StandardTetrimino.hold(): StandardTetrimino {
        if (alreadyHolding) return this

        val newPiece: StandardTetrimino
        val toHold = when (this) {
            is S -> S()
            is Z -> Z()
            is J -> J()
            is L -> L()
            is O -> O()
            is I -> I()
            is T -> T()
        }
        if (heldPiece == null) {
            heldPiece = toHold
            newPiece = nextPiece()
        } else {
            val temp = heldPiece!!
            heldPiece = toHold
            newPiece = temp
        }

        view.drawHeldCells(heldPiece!!.cells())
        alreadyHolding = true
        return newPiece
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

    private fun handleKeyRelease(code: KeyCode) {
        pressedKeys -= code
        keyRepeatThreads[code]?.interrupt()
        keyRepeatThreads -= code
    }

    /**
     * @param duration How many milliseconds to sleep the current thread.
     * @return Whether or not the thread slept for the full specified duration.
     */
    private fun delayCompletely(duration: Long): Boolean {
        val start = System.nanoTime()
        // NOTE: (end - start) shr 20 = (end - start) / 2^20 ~ (end - start) / 10^6
        while ((System.nanoTime() - start) shr 20 < duration) {
            if (Thread.interrupted()) return false
        }

        return true
    }

    private fun allCells(): Set<Cell> = board.getPlacedCells().toMutableSet().also {
        it.addAll(activePiece.cells())
        if (showGhost) it.addAll(activePiece.ghostCells())
    }

    /**
     * Synchronized transformation of the active piece. Tells the view to draw the board and handles the locking mechanism of the active piece.
     * @op The action to perform on the active piece.
     */
    private fun forActivePiece(op: (StandardTetrimino) -> StandardTetrimino) {
        if (!isRunning) return

        synchronized(activePiece) {
            val candidate = op(activePiece)
            val next = if (candidate.isValid()) candidate else activePiece
            val cannotMoveDown = !next.moveDown().isValid()
            val couldNotMovePiece = next == activePiece

            if (cannotMoveDown && couldNotMovePiece) {
                if (!lockActivePieceThread.isAlive) {
                    lockActivePieceThread = newLockActivePieceThread()
                    lockActivePieceThread.start()
                }
            } else {
                lockActivePieceThread.interrupt()
            }

            activePiece = next
        }

        view.drawCells(allCells())
    }

    private fun newLockActivePieceThread(delay: Long = lockDelay): Thread = Thread {
        if (delayCompletely(delay)) keyToAction.getValue(KeyCode.SPACE).invoke()
    }.apply { isDaemon = true }

    private fun nextPiece(): StandardTetrimino {
        upcomingPiecesQueue.add(generator.generate())
        val next = upcomingPiecesQueue.remove()
        val upcomingCells = upcomingPiecesQueue.map { it.cells() }
        view.drawUpcomingCells(LinkedList(upcomingCells))
        return next
    }
}