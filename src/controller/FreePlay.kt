package controller

import controller.config.GameConfiguration
import model.board.BOARD_WIDTH
import model.board.Board
import model.board.FIRST_VISIBLE_ROW
import model.board.synchronizedBoard
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
import view.synchronizedTetrisUI
import java.util.Collections
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.TimeUnit

class FreePlay(var gameConfiguration: GameConfiguration) : TetrisController {
    // Game settings
    lateinit var board: Board
    lateinit var view: TetrisUI
    private val config: GameConfiguration // Alias to shorten the name of the property
        get() = gameConfiguration

    // Auxiliary state
    /**
     * 1 thread each for auto dropping, moving left, moving right, soft drop, and locking.
     * 1 extra thread as a buffer for when a cancellation does not occur fast enough for any of those events.
     */
    private val executor: ScheduledExecutorService = Executors.newScheduledThreadPool(6) {
        Thread { it.run() }.apply { isDaemon = true }
    }
    private lateinit var mainLoop: ScheduledFuture<*>
    private lateinit var activePiece: StandardTetrimino
    private var isRunning = false
    private val pressedCmds = Collections.synchronizedSet(mutableSetOf<Command>())
    private val repeatableCmds = setOf(Command.LEFT, Command.RIGHT, Command.SOFT_DROP)
    private val cmdRepeatFutures = Collections.synchronizedMap(mutableMapOf<Command, Future<*>>())
    private var lockActivePieceFuture: Future<*> = finishedFuture()
    private var alreadyHolding = false
    private var heldPiece: StandardTetrimino? = null
    private val upcomingPiecesQueue: Queue<StandardTetrimino> = LinkedBlockingQueue()
    private val commandToAction = mapOf(
        Command.ROTATE_CCW to { forActivePiece { t -> config.rotationSystem.rotate90CCW(t, board) } },
        Command.ROTATE_CW to { forActivePiece { t -> config.rotationSystem.rotate90CW(t, board) } },
        Command.LEFT to { forActivePiece { t -> t.moveLeft() } },
        Command.RIGHT to { forActivePiece { t -> t.moveRight() } },
        Command.SOFT_DROP to { forActivePiece { t -> t.moveDown() } },
        Command.HARD_DROP to { forActivePiece { t -> t.hardDrop() } },
        Command.HOLD to { forActivePiece { t -> t.hold() } }
    )

    override fun run(board: Board, view: TetrisUI) {
        this.board = synchronizedBoard(board)
        this.view = synchronizedTetrisUI(view)
        this.isRunning = true
        this.pressedCmds.clear()
        this.config.generator.reset()
        this.activePiece = config.generator.generate()
        repeat(config.previewPieces) { upcomingPiecesQueue.add(config.generator.generate()) }

        view.drawCells(emptySet())
        view.drawHeldCells(emptySet())
        view.drawUpcomingCells(LinkedList(upcomingPiecesQueue.map { it.cells() }))

        mainLoop = executor.scheduleWithFixedDelay(Runnable {
            if (Command.SOFT_DROP !in pressedCmds) {
                val softDrop = commandToAction[Command.SOFT_DROP] ?: return@Runnable
                softDrop()
            }
        }, 0, config.autoDropDelay.toLong(), TimeUnit.MILLISECONDS)
    }

    override fun stop() {
        pressedCmds.clear()
        for (t in cmdRepeatFutures.values) t.cancel(true)
        cmdRepeatFutures.clear()
        lockActivePieceFuture.cancel(true)
        heldPiece = null
        alreadyHolding = false
        upcomingPiecesQueue.clear()
        mainLoop.cancel(true)
        isRunning = false
    }

    override fun handleKeyPress(keyCode: Int) {
        val cmd = config.keyToCommand[keyCode] ?: return

        if (cmd !in pressedCmds) {
            handleOppositeCommand(cmd)
            pressedCmds += cmd
            val action = commandToAction[cmd] ?: return
            action()
            if (cmd in repeatableCmds) handleRepeatableCmd(cmd)
        }
    }

    override fun handleKeyRelease(keyCode: Int) {
        val cmd = config.keyToCommand[keyCode]
        handleCommandRelease(cmd)
    }

    private fun handleCommandRelease(c: Command?) {
        pressedCmds -= c
        cmdRepeatFutures[c]?.cancel(true)
        cmdRepeatFutures -= c
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
        val candidateLines = this
            .cells()
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

        val ghostCells = t
            .cells()
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
            newPiece = heldPiece!!
            heldPiece = toHold
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
        if (config.showGhost) it.addAll(activePiece.ghostCells())
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

        if (pieceMoved) view.drawCells(allCells().standardize())

        if (canMoveDown || pieceMoved) {
            lockActivePieceFuture.cancel(true)
        }

        if (!canMoveDown) {
            val running = !lockActivePieceFuture.let { it.isCancelled || it.isDone }
            if (!running) {
                newLockActivePieceFuture()
            }
        }
    }

    // Changes the cells in such a way that they can be rendered properly
    private fun Set<Cell>.standardize(): Set<Cell> =
        this.map { it.move(-FIRST_VISIBLE_ROW, 0) }
            .filter { it.row >= 0 }
            .toSet()

    private fun newLockActivePieceFuture() {
        lockActivePieceFuture = executor.submit {
            if (delayCompletely(config.lockDelay.toLong())) {
                val hardDrop = commandToAction[Command.HARD_DROP] ?: return@submit
                hardDrop()
            }
        }
    }

    private fun nextPiece(): StandardTetrimino {
        upcomingPiecesQueue.add(config.generator.generate())
        val next = upcomingPiecesQueue.remove()
        val upcomingCells = upcomingPiecesQueue.map { it.cells() }
        view.drawUpcomingCells(LinkedList(upcomingCells))
        return next
    }

    private fun handleRepeatableCmd(cmd: Command) {
        val action = commandToAction[cmd] ?: return
        val delayedRepeatableCmds = setOf(Command.LEFT, Command.RIGHT)

        cmdRepeatFutures[cmd] = executor.submit {
            if (cmd !in delayedRepeatableCmds || delayCompletely(config.delayedAutoShift.toLong())) {
                action()

                while (cmd in pressedCmds && delayCompletely(config.autoRepeatRate.toLong())) {
                    action()
                }
            }
        }
    }

    private fun handleOppositeCommand(cmd: Command) {
        val opposites = mapOf(Command.RIGHT to Command.LEFT, Command.LEFT to Command.RIGHT)
        handleCommandRelease(opposites[cmd])
    }
}

/**
 * Possible player commands.
 */
enum class Command {
    ROTATE_CCW,
    ROTATE_CW,
    LEFT,
    RIGHT,
    SOFT_DROP,
    HARD_DROP,
    HOLD,
    DO_NOTHING
}

/**
 * No-op future to use as an initial value.
 */
internal fun finishedFuture() = object : Future<Unit> {
    override fun isDone(): Boolean = true

    override fun get() = Unit

    override fun get(timeout: Long, unit: TimeUnit) = Unit

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean = false

    override fun isCancelled(): Boolean = false
}