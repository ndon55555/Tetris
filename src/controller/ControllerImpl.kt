package controller

import controller.config.RandomBagOf7
import controller.config.RotationSystem
import controller.config.StandardTetriminoGenerator
import controller.config.SuperRotation
import model.board.BOARD_WIDTH
import model.board.Board
import model.board.FIRST_VISIBLE_ROW
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
import view.TetrisUI
import java.awt.event.KeyEvent.VK_DOWN
import java.awt.event.KeyEvent.VK_LEFT
import java.awt.event.KeyEvent.VK_RIGHT
import java.awt.event.KeyEvent.VK_SHIFT
import java.awt.event.KeyEvent.VK_SPACE
import java.awt.event.KeyEvent.VK_UP
import java.awt.event.KeyEvent.VK_Z
import java.util.Collections
import java.util.LinkedList
import java.util.Queue
import java.util.Timer
import kotlin.concurrent.schedule

class ControllerImpl : TetrisController {
    // Game settings
    lateinit var board: Board
    lateinit var view: TetrisUI
    lateinit var rotationSystem: RotationSystem
    lateinit var generator: StandardTetriminoGenerator
    var showGhost = true
    var autoRepeatRate = 30L // Milliseconds between each auto repeat
    var delayedAutoShift = 140L // Milliseconds before activating auto repeat
    var previewPieces = 5
    var lockDelay = 500L // Milliseconds before locking a piece on the board
    var keyToCommand = mutableMapOf(
            VK_Z to Command.ROTATE_CCW,
            VK_UP to Command.ROTATE_CW,
            VK_LEFT to Command.LEFT,
            VK_RIGHT to Command.RIGHT,
            VK_DOWN to Command.SOFT_DROP,
            VK_SPACE to Command.HARD_DROP,
            VK_SHIFT to Command.HOLD
    ).withDefault { Command.DO_NOTHING }

    // Auxiliary state
    private lateinit var mainLoop: Timer
    private lateinit var activePiece: StandardTetrimino
    private var isRunning = false
    private val pressedCmds = Collections.synchronizedSet(mutableSetOf<Command>())
    private val delayedRepeatableCmds = setOf(Command.LEFT, Command.RIGHT)
    private val repeatableCmds = delayedRepeatableCmds + Command.SOFT_DROP
    private val cmdRepeatThreads = Collections.synchronizedMap(mutableMapOf<Command, Thread>())
    private var lockActivePieceThread = newLockActivePieceThread()
    private var alreadyHolding = false
    private var heldPiece: StandardTetrimino? = null
    private val upcomingPiecesQueue: Queue<StandardTetrimino> = LinkedList()
    private val commandToAction = mapOf(
            Command.ROTATE_CCW to { forActivePiece { t -> rotationSystem.rotate90CCW(t, board) } },
            Command.ROTATE_CW to { forActivePiece { t -> rotationSystem.rotate90CW(t, board) } },
            Command.LEFT to { forActivePiece { t -> t.moveLeft() } },
            Command.RIGHT to { forActivePiece { t -> t.moveRight() } },
            Command.SOFT_DROP to { forActivePiece { t -> t.moveDown() } },
            Command.HARD_DROP to { forActivePiece { t -> t.hardDrop() } },
            Command.HOLD to { forActivePiece { t -> t.hold() } }
    )

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
        this.mainLoop = Timer()
        this.pressedCmds.clear()
        this.activePiece = generator.generate()
        this.heldPiece = null
        repeat(previewPieces) { upcomingPiecesQueue.add(generator.generate()) }

        view.drawCells(allCells())
        view.drawHeldCells(emptySet())
        view.drawUpcomingCells(LinkedList(upcomingPiecesQueue.map { it.cells() }))

        mainLoop.schedule(delay = 0, period = 1000) {
            if (Command.SOFT_DROP !in pressedCmds) {
                val softDrop = commandToAction[Command.SOFT_DROP] ?: return@schedule
                softDrop()
            }
        }
    }

    override fun stop() {
        pressedCmds.clear()
        for (t in cmdRepeatThreads.values) t.interrupt()
        cmdRepeatThreads.clear()
        lockActivePieceThread.interrupt()
        upcomingPiecesQueue.clear()
        mainLoop.cancel()
        this.isRunning = false
    }

    override fun handleKeyPress(keyCode: Int) {
        val cmd = keyToCommand[keyCode] ?: return

        if (cmd !in pressedCmds) {
            handleOppositeCommand(cmd)
            pressedCmds += cmd
            val action = commandToAction[cmd] ?: return
            action()
            if (cmd in repeatableCmds) handleRepeatableCmd(cmd)
        }
    }

    override fun handleKeyRelease(keyCode: Int) {
        val cmd = keyToCommand[keyCode]
        pressedCmds -= cmd
        cmdRepeatThreads[cmd]?.interrupt()
        cmdRepeatThreads -= cmd
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
        val candidateLines = this.cells()
                .map { it.row }
                .distinct()
                .sorted()

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

        val ghostCells = t.cells()
                .map { CellImpl(CellColor.NULL, it.row, it.col) }
                .toMutableSet()

        ghostCells.removeAll { ghostCell ->
            this.cells().any { activeCell -> ghostCell.sharesPositionWith(activeCell) }
        }
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

        var canMoveDown: Boolean
        var pieceMoved: Boolean

        synchronized(activePiece) {
            val candidate = op(activePiece)
            val next = if (candidate.isValid()) candidate else activePiece
            canMoveDown = next.moveDown().isValid()
            pieceMoved = next != activePiece
            activePiece = next
        }

        if (pieceMoved) view.drawCells(allCells())

        if (pieceMoved || canMoveDown) {
            lockActivePieceThread.interrupt()
        } else {
            beginLockingActivePiece()
        }
    }

    private fun beginLockingActivePiece() {
        if (lockActivePieceThread.isAlive) return

        lockActivePieceThread = newLockActivePieceThread()
        lockActivePieceThread.start()
    }

    private fun newLockActivePieceThread(delay: Long = lockDelay): Thread = Thread {
        if (delayCompletely(delay)) {
            val hardDrop = commandToAction[Command.HARD_DROP] ?: return@Thread
            hardDrop()
        }
    }.apply { isDaemon = true }

    private fun nextPiece(): StandardTetrimino {
        upcomingPiecesQueue.add(generator.generate())
        val next = upcomingPiecesQueue.remove()
        val upcomingCells = upcomingPiecesQueue.map { it.cells() }
        view.drawUpcomingCells(LinkedList(upcomingCells))
        return next
    }

    private fun handleRepeatableCmd(cmd: Command) {
        val action = commandToAction[cmd] ?: return

        Thread {
            if (cmd !in delayedRepeatableCmds || delayCompletely(delayedAutoShift)) {
                action()

                while (cmd in pressedCmds && delayCompletely(autoRepeatRate)) {
                    action()
                }
            }
        }.also {
            it.isDaemon = true
            cmdRepeatThreads[cmd] = it
            it.start()
        }
    }

    private fun handleOppositeCommand(cmd: Command) {
        when (cmd) {
            Command.RIGHT -> {
                pressedCmds -= Command.LEFT
                cmdRepeatThreads[Command.LEFT]?.interrupt()
            }
            Command.LEFT -> {
                pressedCmds -= Command.RIGHT
                cmdRepeatThreads[Command.RIGHT]?.interrupt()
            }
            else -> {
            }
        }
    }
}

enum class Command {
    ROTATE_CCW, ROTATE_CW, LEFT, RIGHT, SOFT_DROP, HARD_DROP, HOLD, DO_NOTHING
}