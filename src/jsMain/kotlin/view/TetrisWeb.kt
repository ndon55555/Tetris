package view

import model.board.BOARD_WIDTH
import model.cell.Cell
import model.cell.CellColor
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import kotlin.browser.document

const val BOARD_ID = "board"
const val HOLD_ID = "hold"

class TetrisWeb : TetrisUI {
    override fun drawCells(cells: Set<Cell>) {
        val canvas = document.getElementById(BOARD_ID) as HTMLCanvasElement
        clearBoard(canvas)
        paintCells(canvas, cells)
    }

    private fun clearBoard(canvas: HTMLCanvasElement) {
        val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
        ctx.fillStyle = "black"
        ctx.fillRect(0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble())
    }

    private fun paintCells(canvas: HTMLCanvasElement, cells: Set<Cell>) {
        for (cell in cells) {
            paintCell(canvas, cell)
        }
    }

    private fun paintCell(canvas: HTMLCanvasElement, cell: Cell) {
        val r = cell.row
        val c = cell.col
        val color = when (cell.color) {
            CellColor.GREEN      -> "green"
            CellColor.RED        -> "red"
            CellColor.DARK_BLUE  -> "blue"
            CellColor.ORANGE     -> "orange"
            CellColor.LIGHT_BLUE -> "cyan"
            CellColor.YELLOW     -> "yellow"
            CellColor.PURPLE     -> "purple"
            CellColor.NULL       -> "grey"
        }

        val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
        val squareSize = (canvas.width / BOARD_WIDTH).toDouble()

        // Draw bigger rectangle for outline of cell
        ctx.fillStyle = "grey"
        ctx.fillRect(c * squareSize, r * squareSize, squareSize, squareSize)

        // Draw smaller rectangle for fill of cell
        ctx.fillStyle = color
        ctx.fillRect(c * squareSize + 1, r * squareSize + 1, squareSize - 2, squareSize - 2)
    }

    override fun drawHeldCells(cells: Set<Cell>) {
        val canvas = document.getElementById(HOLD_ID) as HTMLCanvasElement
        clearHold(canvas)
        paintHeldCells(canvas, cells)
    }

    private fun clearHold(canvas: HTMLCanvasElement) {
        val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
        ctx.fillStyle = "black"
        ctx.fillRect(0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble())
    }

    private fun paintHeldCells(canvas: HTMLCanvasElement, cells: Set<Cell>) {
        val PREVIEW_BOX_SIZE = 4

        // TODO can this logic be moved into the controller or model?
        val rows = cells.map { it.row }
        val cols = cells.map { it.col }
        val minRow = rows.min() ?: 0
        val maxRow = rows.max() ?: PREVIEW_BOX_SIZE
        val minCol = cols.min() ?: 0
        val maxCol = cols.max() ?: PREVIEW_BOX_SIZE
        val dRow = minRow - (PREVIEW_BOX_SIZE - (maxRow - minRow + 1)) / 2
        val dCol = minCol - (PREVIEW_BOX_SIZE - (maxCol - minCol + 1)) / 2

        val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
        val squareSize = (canvas.width / PREVIEW_BOX_SIZE).toDouble()
        for (cell in cells) {
            val r = cell.row
            val c = cell.col
            val color = when (cell.color) {
                CellColor.GREEN      -> "green"
                CellColor.RED        -> "red"
                CellColor.DARK_BLUE  -> "blue"
                CellColor.ORANGE     -> "orange"
                CellColor.LIGHT_BLUE -> "cyan"
                CellColor.YELLOW     -> "yellow"
                CellColor.PURPLE     -> "purple"
                CellColor.NULL       -> "grey"
            }

            // Draw bigger rectangle for outline of cell
            ctx.fillStyle = "grey"
            ctx.fillRect((c - dCol) * squareSize, (r - dRow) * squareSize, squareSize, squareSize)

            // Draw smaller rectangle for fill of cell
            ctx.fillStyle = color
            ctx.fillRect(
                (c - dCol) * squareSize + 1,
                (r - dRow) * squareSize + 1,
                squareSize - 2,
                squareSize - 2
            )
        }
    }

    override fun drawUpcomingCells(cellsQueue: List<Set<Cell>>) {
        // TODO
    }
}