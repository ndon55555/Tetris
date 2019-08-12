package view

import model.cell.Cell
import java.util.Queue

interface TetrisUI {
    fun drawCells(cells: Set<Cell>)

    fun drawHeldCells(cells: Set<Cell>)

    fun drawUpcomingCells(cellsQueue: Queue<Set<Cell>>)
}

/**
 * Obtain a synchronized version of the given TetrisUI.
 */
fun synchronizedTetrisUI(ui: TetrisUI): TetrisUI = object : TetrisUI {
    val cellsLock = Object()
    val heldCellsLock = Object()
    val upcomingCellsLock = Object()

    override fun drawCells(cells: Set<Cell>) = synchronized(cellsLock) { ui.drawCells(cells) }

    override fun drawHeldCells(cells: Set<Cell>) = synchronized(heldCellsLock) { ui.drawHeldCells(cells) }

    override fun drawUpcomingCells(cellsQueue: Queue<Set<Cell>>) = synchronized(upcomingCellsLock) {
        ui.drawUpcomingCells(cellsQueue)
    }
}