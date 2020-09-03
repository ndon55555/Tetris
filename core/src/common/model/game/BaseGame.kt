package model.game

import model.board.BOARD_WIDTH
import model.board.Board
import model.board.FIRST_VISIBLE_ROW
import model.cell.Cell
import model.cell.CellColor
import model.cell.CellImpl
import model.game.config.GameConfiguration
import model.tetrimino.I
import model.tetrimino.J
import model.tetrimino.L
import model.tetrimino.O
import model.tetrimino.S
import model.tetrimino.StandardTetrimino
import model.tetrimino.T
import model.tetrimino.Z
import kotlin.properties.Delegates
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds

@ExperimentalTime
expect fun timeStamp(): Duration

@ExperimentalTime
open class BaseGame(board: Board, val config: GameConfiguration) : Game {
    var gameOver = false
    private var board: Board = object : Board {
        override fun areValidCells(vararg cells: Cell): Boolean = board.areValidCells(*cells)

        override fun placeCells(vararg cells: Cell) {
            board.placeCells(*cells)
            for (handler in boardChangeHandlers) handler()
        }

        override fun clearLine(row: Int) {
            board.clearLine(row)
            for (handler in boardChangeHandlers) handler()
        }

        override fun getPlacedCells(): Set<Cell> = board.getPlacedCells()
    }

    private val repeatableCmds = setOf(Command.LEFT, Command.RIGHT, Command.SOFT_DROP)

    private var alreadyStarted = false
    private var lastAutoDrop by Delegates.notNull<Duration>()
    private lateinit var pressedCmds: MutableSet<Command>
    private lateinit var initialPress: MutableSet<Command>
    private lateinit var timeOfPrevAction: MutableMap<Command, Duration>
    private var heldPiece: StandardTetrimino? = null
    private var alreadyHolding = false
    private lateinit var upcomingPiecesQueue: MutableList<StandardTetrimino>
    private var autoLockStartTime: Duration? = null
    private lateinit var activePiece: StandardTetrimino

    private val boardChangeHandlers = mutableSetOf<() -> Unit>()
    private val heldPieceHandlers = mutableSetOf<() -> Unit>()
    private val upcomingPiecesHandlers = mutableSetOf<() -> Unit>()

    override fun start() {
        if (alreadyStarted) throw Exception("game had already been started")
        alreadyStarted = true

        lastAutoDrop = timeStamp()
        pressedCmds = mutableSetOf()
        initialPress = mutableSetOf()
        timeOfPrevAction = mutableMapOf()
        heldPiece = null
        alreadyHolding = false
        upcomingPiecesQueue = mutableListOf()
        autoLockStartTime = null
        activePiece = config.generator.generate().moveDown()
        repeat(config.previewPieces) { upcomingPiecesQueue.add(config.generator.generate()) }
    }

    override fun stop() {
        gameOver = true
    }

    override fun frame() {
        val curTime = timeStamp()
        val autoDropTimePassed = (curTime - lastAutoDrop) >= config.autoDropDelay.milliseconds
        if (autoDropTimePassed) {
            if (Command.SOFT_DROP !in pressedCmds) {
                perform(Command.SOFT_DROP)
            }

            lastAutoDrop = curTime
        }

        for (cmd in repeatableCmds) {
            if (cmd in pressedCmds) {
                val delay = if (cmd in initialPress) config.delayedAutoShift else config.autoRepeatRate
                if (delay == 0) {
                    var pieceMoved = perform(cmd)
                    while (pieceMoved) {
                        pieceMoved = perform(cmd)
                    }
                }
                val lastPressTime = timeOfPrevAction[cmd]!!
                val delayTimePassed = (curTime - lastPressTime) >= delay.milliseconds
                if (delayTimePassed) {
                    perform(cmd)
                    initialPress.remove(cmd)
                    timeOfPrevAction[cmd] = curTime
                }
            }
        }
    }

    override fun finished(): Boolean {
        return gameOver
    }

    override fun allCells(): Set<Cell> = board.getPlacedCells().toMutableSet().also {
        it.addAll(activePiece.cells())
        if (config.showGhost) it.addAll(activePiece.ghostCells())
    }

    override fun heldCells(): Set<Cell> = heldPiece?.cells() ?: emptySet()

    override fun upcomingCells(): List<Set<Cell>> = upcomingPiecesQueue.map { it.cells() }

    override fun commandInitiated(cmd: Command) {
        if (cmd !in pressedCmds) {
            perform(cmd)
            pressedCmds.add(cmd)
            initialPress.add(cmd)
            timeOfPrevAction[cmd] = timeStamp()
        }
    }

    override fun commandStopped(cmd: Command) {
        pressedCmds.remove(cmd)
        initialPress.remove(cmd)
        timeOfPrevAction.remove(cmd)
    }

    override fun onBoardChange(action: () -> Unit) {
        boardChangeHandlers += action
    }

    override fun onHeldPieceChange(action: () -> Unit) {
        heldPieceHandlers += action
    }

    override fun onUpcomingPiecesChange(action: () -> Unit) {
        upcomingPiecesHandlers += action
    }

    private fun forActivePiece(op: (StandardTetrimino) -> StandardTetrimino): Boolean {
        val prevActivePiece = activePiece
        val candidate = op(activePiece)
        val next = if (candidate.isValid()) candidate else activePiece
        val canMoveDown = next.moveDown().isValid()
        val pieceMoved = next != activePiece
        activePiece = next

        if (canMoveDown || pieceMoved) {
            this.autoLockStartTime = null
        }

        if (!canMoveDown) {
            if (autoLockStartTime == null) {
                this.autoLockStartTime = timeStamp()
            }
        }

        if (autoLockStartTime != null) {
            val lockDelayPassed =
                (timeStamp() - this.autoLockStartTime!!) >= config.lockDelay.milliseconds
            if (lockDelayPassed) {
                activePiece = activePiece.hardDrop()
                this.autoLockStartTime = null
            }
        }

        for (handler in boardChangeHandlers) handler()

        return activePiece != prevActivePiece
    }

    private fun StandardTetrimino.isValid(): Boolean = board.areValidCells(*this.cells().toTypedArray())

    private fun StandardTetrimino.hardDrop(): StandardTetrimino {
        var t = this
        while (t.moveDown().isValid()) t = t.moveDown()
        t.placeOnBoard()

        // check for "top out"
        if (t.cells().all { it.row < FIRST_VISIBLE_ROW }) {
            stop()
        }

        t.clearCompletedLines()
        var newPiece = nextPiece()

        // check for "block out"
        if (!newPiece.isValid()) {
            stop()
        }

        if (newPiece.moveDown().isValid()) {
            newPiece = newPiece.moveDown()
        }

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
        for (handler in upcomingPiecesHandlers) handler()
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

        alreadyHolding = true
        for (handler in heldPieceHandlers) handler()

        return newPiece
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

    private fun perform(cmd: Command): Boolean = when (cmd) {
        Command.ROTATE_CCW -> forActivePiece { config.rotationSystem.rotate90CCW(it, board) }
        Command.ROTATE_CW  -> forActivePiece { config.rotationSystem.rotate90CW(it, board) }
        Command.LEFT       -> forActivePiece { it.moveLeft() }
        Command.RIGHT      -> forActivePiece { it.moveRight() }
        Command.SOFT_DROP  -> forActivePiece { it.moveDown() }
        Command.HARD_DROP  -> forActivePiece { it.hardDrop() }
        Command.HOLD       -> forActivePiece { it.hold() }
        Command.DO_NOTHING -> {
            false
        }
    }
}
