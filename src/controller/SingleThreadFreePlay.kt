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
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds
import kotlin.time.nanoseconds

fun runAtFixedRate(period: Long, shouldContinue: () -> Boolean, event: () -> Unit) {
    Executors.newSingleThreadScheduledExecutor { r ->
        object : Thread() {
            override fun run() {
                if (!shouldContinue()) {
                    this.interrupt()
                } else {
                    r.run()
                }
            }
        }.apply { isDaemon = true }
    }.scheduleAtFixedRate(event, 0, period, TimeUnit.MILLISECONDS)
}

@ExperimentalTime
fun timeStamp(): Duration = System.nanoTime().nanoseconds

@ExperimentalTime
class SingleThreadFreePlay(var gameConfiguration: GameConfiguration) : TetrisController {
    // Game settings
    lateinit var board: Board
    lateinit var view: TetrisUI
    private val config: GameConfiguration // Alias to shorten the name of the property
        get() = gameConfiguration

    private val FPS = 120
    private val MILLISEC_PER_SEC = 1_000

    private var lastAutoDrop by Delegates.notNull<Duration>()
    private val pressedCmds = mutableSetOf<Command>() // TODO synchronize
    private val initialPress = mutableSetOf<Command>() // TODO synchronize
    private val timeOfPrevAction = mutableMapOf<Command, Duration>() // TODO synchronize
    private val repeatableCmds = setOf(Command.LEFT, Command.RIGHT, Command.SOFT_DROP)
    private var isRunning = false
    private var heldPiece: StandardTetrimino? = null
    private var alreadyHolding = false
    private lateinit var activePiece: StandardTetrimino
    private val upcomingPiecesQueue = mutableListOf<StandardTetrimino>() // TODO synchronize
    private var shouldAutoLock = false
    private var autoLockStartTime: Duration? = null

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
        this.lastAutoDrop = timeStamp()
        this.isRunning = true
        this.heldPiece = null
        this.alreadyHolding = false
        this.shouldAutoLock = false
        this.autoLockStartTime = null
        this.pressedCmds.clear()
        this.initialPress.clear()
        this.upcomingPiecesQueue.clear()
        this.timeOfPrevAction.clear()
        this.config.generator.reset()
        this.activePiece = config.generator.generate()
        repeat(config.previewPieces) { upcomingPiecesQueue.add(config.generator.generate()) }

        view.drawCells(emptySet())
        view.drawHeldCells(emptySet())
        view.drawUpcomingCells(upcomingPiecesQueue.map { it.cells() })

        runAtFixedRate(MILLISEC_PER_SEC.toLong() / FPS, { isRunning }) {
            frame()
        }
    }

    private fun frame() {
        val curTime = timeStamp()
        val autoDropTimePassed = (curTime - lastAutoDrop) >= config.autoDropDelay.milliseconds
        if (autoDropTimePassed) {
            if (Command.SOFT_DROP !in pressedCmds) {
                (commandToAction[Command.SOFT_DROP]!!)()
            }

            lastAutoDrop = curTime
        }

        for (cmd in repeatableCmds) {
            if (cmd in pressedCmds) {
                val delay = if (cmd in initialPress) config.delayedAutoShift else config.autoRepeatRate
                val lastPressTime = timeOfPrevAction[cmd]!!
                val delayTimePassed = (curTime - lastPressTime) >= delay.milliseconds
                if (delayTimePassed) {
                    // perform cmd
                    (commandToAction[cmd]!!)()
                    initialPress.remove(cmd)
                    timeOfPrevAction[cmd] = curTime
                }
            }
        }
    }

    private fun StandardTetrimino.isValid(): Boolean = board.areValidCells(*this.cells().toTypedArray())

    private fun allCells(): Set<Cell> = board.getPlacedCells().toMutableSet().also {
        it.addAll(activePiece.cells())
        if (config.showGhost) it.addAll(activePiece.ghostCells())
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

    // Changes the cells in such a way that they can be rendered properly
    private fun Set<Cell>.standardize(): Set<Cell> =
        this.map { it.move(-FIRST_VISIBLE_ROW, 0) }
            .filter { it.row >= 0 }
            .toSet()

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

    private fun StandardTetrimino.placeOnBoard() {
        val cells = this.cells().toTypedArray()
        board.placeCells(*cells)
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

    private fun nextPiece(): StandardTetrimino {
        upcomingPiecesQueue.add(config.generator.generate())
        val next = upcomingPiecesQueue.removeAt(0)
        val upcomingCells = upcomingPiecesQueue.map { it.cells() }
        view.drawUpcomingCells(upcomingCells)
        return next
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

    private fun forActivePiece(op: (StandardTetrimino) -> StandardTetrimino) {
        synchronized(activePiece) {
            val candidate = op(activePiece)
            val next = if (candidate.isValid()) candidate else activePiece
            val canMoveDown = next.moveDown().isValid()
            val pieceMoved = next != activePiece
            activePiece = next

            if (pieceMoved) view.drawCells(allCells().standardize())

            if (canMoveDown || pieceMoved) {
                this.shouldAutoLock = false
                this.autoLockStartTime = null
            }

            if (!canMoveDown) {
                if (!this.shouldAutoLock) {
                    this.shouldAutoLock = true
                    this.autoLockStartTime = timeStamp()
                }
            }

            if (this.shouldAutoLock) {
                val lockDelayPassed =
                    (timeStamp() - this.autoLockStartTime!!) >= config.lockDelay.milliseconds
                if (lockDelayPassed) {
                    (commandToAction[Command.HARD_DROP]!!)()
                    this.shouldAutoLock = false
                    this.autoLockStartTime = null
                }
            }
        }
    }

    override fun stop() {
        this.isRunning = false
    }

    override fun handleKeyPress(keyCode: Int) {
        if (!isRunning) return

        val cmd = config.keyToCommand[keyCode] ?: return

        if (cmd !in pressedCmds) {
            (commandToAction[cmd]!!)()
            pressedCmds += cmd
            initialPress += cmd
            timeOfPrevAction[cmd] = timeStamp()
        }
    }

    override fun handleKeyRelease(keyCode: Int) {
        val cmd = config.keyToCommand[keyCode] ?: return

        pressedCmds -= cmd
        initialPress -= cmd
        timeOfPrevAction.remove(cmd)
    }
}