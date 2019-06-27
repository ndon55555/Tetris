package view

import model.cell.Cell
import java.util.Queue

interface TetrisUI {
    fun drawCells(cells: Set<Cell>)

    fun drawHeldCells(cells: Set<Cell>)

    fun drawUpcomingCells(cellsQueue: Queue<Set<Cell>>)
}