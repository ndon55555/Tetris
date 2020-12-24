package view

import model.cell.Cell

interface TetrisUI {
    fun drawCells(cells: Set<Cell>)

    fun drawHeldCells(cells: Set<Cell>)

    fun drawUpcomingCells(cellsQueue: List<Set<Cell>>)
}
