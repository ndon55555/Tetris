package view

import model.cell.Cell

interface TetrisUI {
    fun drawCells(cells: Set<Cell>)

    fun drawHeldCells(cells: Set<Cell>)

    fun drawUpcomingCells(cellsQueue: List<Set<Cell>>)
}

/**
 * Obtain a synchronized version of the given TetrisUI.
 */
fun synchronizedTetrisUI(ui: TetrisUI): TetrisUI = object :
    TetrisUI {
    val cellsLock = Any()
    val heldCellsLock = Any()
    val upcomingCellsLock = Any()

    override fun drawCells(cells: Set<Cell>) = synchronized(cellsLock) { ui.drawCells(cells) }

    override fun drawHeldCells(cells: Set<Cell>) = synchronized(heldCellsLock) { ui.drawHeldCells(cells) }

    override fun drawUpcomingCells(cellsQueue: List<Set<Cell>>) = synchronized(upcomingCellsLock) {
        ui.drawUpcomingCells(cellsQueue)
    }
}