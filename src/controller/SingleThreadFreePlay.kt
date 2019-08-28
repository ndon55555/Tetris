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
import kotlin.concurrent.timer

class SingleThreadFreePlay(val config: GameConfiguration) : TetrisController {
    lateinit var board: Board
    lateinit var view: TetrisUI

    private lateinit var pressedCmds: MutableSet<Command>
    private lateinit var cmdToFirstActivated: MutableMap<Command, Long> // time is System.nanoTime()
    private lateinit var cmdToLastReactivated: MutableMap<Command, Long> // last reactivated is in System.nanoTime()
    private var shouldLock = false
    private var shouldLockTimeStamp: Long = 0
    private var isGameOver = false

    private lateinit var activePiece: StandardTetrimino
    private var heldPiece: StandardTetrimino? = null
    private var alreadyHolding = false
    private lateinit var upcomingPieces: MutableList<StandardTetrimino>
    private var lastAutoDrop = System.nanoTime()

    private val FPS = 120

    override fun run(board: Board, view: TetrisUI) {
        this.board = synchronizedBoard(board)
        this.view = synchronizedTetrisUI(view)
        this.pressedCmds = mutableSetOf()
        this.cmdToFirstActivated = mutableMapOf()
        this.cmdToLastReactivated = mutableMapOf()
        this.shouldLock = false
        this.shouldLockTimeStamp = 0
        this.isGameOver = false
        this.heldPiece = null
        this.alreadyHolding = false
        this.upcomingPieces = mutableListOf()
        config.generator.reset()
        this.activePiece = config.generator.generate()
        repeat(config.previewPieces) { upcomingPieces.add(config.generator.generate()) }

        view.drawCells(emptySet())
        view.drawHeldCells(emptySet())
        view.drawUpcomingCells(upcomingPieces.map { it.cells() })

        timer(daemon = true, period = 1000L / FPS) {
            if (!isGameOver) {
                tick()
            }
        }
    }

    override fun stop() {
        isGameOver = true
    }

    override fun handleKeyPress(keyCode: Int) {
        val cmd = config.keyToCommand[keyCode]!!
        synchronized(pressedCmds) {
            pressedCmds.add(cmd)
        }
    }

    override fun handleKeyRelease(keyCode: Int) {
        val cmd = config.keyToCommand[keyCode]!!
        synchronized(pressedCmds) {
            pressedCmds.remove(cmd)
            cmdToFirstActivated.remove(cmd)
            cmdToLastReactivated.remove(cmd)
        }
    }

    private fun tick() {
        val repeatableCmds = setOf(Command.LEFT, Command.RIGHT, Command.SOFT_DROP)
        val dasCmds = setOf(Command.LEFT, Command.RIGHT)
        val currentTime = System.nanoTime()
        val prevPiece = activePiece

        synchronized(pressedCmds) {
            for (cmd in pressedCmds) {
                if (cmd !in cmdToFirstActivated.keys) {
                    doCommand(cmd)
                    cmdToFirstActivated[cmd] = currentTime
                }

                if (cmd !in repeatableCmds) {
                    continue
                }

                if (cmd in dasCmds && (currentTime - cmdToFirstActivated[cmd]!! < config.delayedAutoShift * 1000000L)) {
                    continue
                }

                if (cmd !in cmdToLastReactivated || (currentTime - cmdToLastReactivated[cmd]!! >= config.autoRepeatRate * 1000000L)) {
                    doCommand(cmd)
                    cmdToLastReactivated[cmd] = currentTime
                }
            }
        }

        if (currentTime - lastAutoDrop >= 1_000_000_000L) {
            doCommand(Command.SOFT_DROP)
            lastAutoDrop = currentTime
        }

        val curPiece = activePiece
        val pieceMoved = prevPiece != curPiece
        val canMoveDown = curPiece.moveDown().isValid()

        if (pieceMoved) view.drawCells(allCells().standardize())

        if (canMoveDown || pieceMoved) {
            shouldLock = false
        } else {
            if (!shouldLock) {
                shouldLock = true
                shouldLockTimeStamp = currentTime
            }
        }

        if (shouldLock && (currentTime - shouldLockTimeStamp >= config.lockDelay * 1_000_000L)) {
            doCommand(Command.HARD_DROP)
            shouldLock = false
        }
    }

    private fun doCommand(cmd: Command) {
        when (cmd) {
            Command.ROTATE_CCW -> forActivePiece { config.rotationSystem.rotate90CCW(this, board) }
            Command.ROTATE_CW  -> forActivePiece { config.rotationSystem.rotate90CW(this, board) }
            Command.LEFT       -> forActivePiece { moveLeft() }
            Command.RIGHT      -> forActivePiece { moveRight() }
            Command.SOFT_DROP  -> forActivePiece { moveDown() }
            Command.HARD_DROP  -> forActivePiece { hardDrop() }
            Command.HOLD       -> forActivePiece { hold() }
            Command.DO_NOTHING -> {
            }
        }
    }

    private fun forActivePiece(action: StandardTetrimino.() -> StandardTetrimino) {
        val candidate = activePiece.action()
        if (candidate.isValid()) {
            activePiece = candidate
        }
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

    private fun nextPiece(): StandardTetrimino {
        upcomingPieces.add(config.generator.generate())
        val next = upcomingPieces.removeAt(0)
        val upcomingCells = upcomingPieces.map { it.cells() }
        view.drawUpcomingCells(upcomingCells)
        return next
    }

    private fun allCells(): Set<Cell> = board.getPlacedCells().toMutableSet().also {
        it.addAll(activePiece.cells())
        if (config.showGhost) it.addAll(activePiece.ghostCells())
    }

    // Changes the cells in such a way that they can be rendered properly
    private fun Set<Cell>.standardize(): Set<Cell> =
        this.map { it.move(-FIRST_VISIBLE_ROW, 0) }
            .filter { it.row >= 0 }
            .toSet()
}

/*
DESIGN

main loop:
- ticks a certain number of times per second
- during a tick:
    - check for each key pressed
        - if not a DAS key, and action not performed yet, do action
        - if it's a DAS key and it's not already pressed, perform the action
          if it's already pressed and it's been DAS ms since initial press and (it's been ARR ms since the previous ARR press or no ARR press yet), do the action
    - if down key not pressed, auto drop if a second has passed since the last softdrop
    - if can move down || the piece moved, shouldLock = false
      else shouldLock = true
    - if it has been lockDelay ms since shouldLock turned true, hard drop the piece

STATE
- keys pressed
- time stamp of initial press
- whether or not action associated with pressed key has already been performed
- time stamp of last action associated with pressed key
- shouldLock (the active piece)
- time stamp of when shouldLock turned true

- active piece
- held piece
- upcoming pieces
 */