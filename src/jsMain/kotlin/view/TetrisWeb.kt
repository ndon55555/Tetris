package view

import model.board.BOARD_WIDTH
import model.cell.Cell
import model.cell.CellColor
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import kotlin.browser.document

const val BOARD_ID = "board"

class TetrisWeb : TetrisUI {
    override fun drawCells(cells: Set<Cell>) {
        val canvas = document.getElementById("board") as HTMLCanvasElement
        clearBoard(canvas)
        paintCells(canvas, cells)
        paintGrid(canvas)
    }

    private fun clearBoard(canvas: HTMLCanvasElement) {
        val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
        ctx.clearRect(0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble())
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

    private fun paintGrid(canvas: HTMLCanvasElement) {
        val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
        val squareSize = canvas.width / BOARD_WIDTH

        // TODO
    }

    override fun drawHeldCells(cells: Set<Cell>) {
        // TODO
    }

    override fun drawUpcomingCells(cellsQueue: List<Set<Cell>>) {
        // TODO
    }
}