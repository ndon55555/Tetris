package view

import model.cell.Cell

interface TetrisUI {
    fun drawCells(cells: Set<Cell>)
}