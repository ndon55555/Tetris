package model.game

import model.cell.Cell

interface Game {
    fun allCells(): Set<Cell>
    fun heldCells(): Set<Cell>
    fun upcomingCells(): List<Set<Cell>>
    fun commandInitiated(cmd: Command)
    fun commandStopped(cmd: Command)
    fun frame()
    fun finished(): Boolean
    fun onBoardChange(action: () -> Unit)
    fun onHeldPieceChange(action: () -> Unit)
    fun onUpcomingPiecesChange(action: () -> Unit)
    fun start()
    fun stop()
}