package view

import model.Cell

interface TetrisUI {
    fun drawCells(cells: Set<Cell>)
}