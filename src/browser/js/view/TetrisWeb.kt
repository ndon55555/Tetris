package view

import BOARD_ID
import HOLD_ID
import UPCOMING_PIECES_ID
import model.board.BOARD_WIDTH
import model.cell.Cell
import model.cell.CellColor
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLDivElement
import kotlin.browser.document
import kotlin.dom.clear

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
            val color = htmlColor(cell.color)
            val squareSize = (canvas.width / BOARD_WIDTH)

            paintCell(canvas, cell.row, cell.col, squareSize, color)
        }
    }

    override fun drawHeldCells(cells: Set<Cell>) {
        val canvas = document.getElementById(HOLD_ID) as HTMLCanvasElement
        preview(canvas, cells)
    }

    override fun drawUpcomingCells(cellsQueue: List<Set<Cell>>) {
        val pieceQueueDiv = document.getElementById(UPCOMING_PIECES_ID) as HTMLDivElement
        pieceQueueDiv.clear()

        for (cells in cellsQueue) {
            val previewCanvas = document.createElement("canvas") as HTMLCanvasElement
            // TODO move styling to a CSS file?
            previewCanvas.width = 120
            previewCanvas.height = 120
            previewCanvas.style.display = "block"
            preview(previewCanvas, cells)
            pieceQueueDiv.appendChild(previewCanvas)
        }
    }

    private fun preview(canvas: HTMLCanvasElement, cells: Set<Cell>) {
        clearPreviewCanvas(canvas)
        paintPreviewCells(canvas, cells)
    }

    private fun clearPreviewCanvas(canvas: HTMLCanvasElement) {
        val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
        ctx.fillStyle = "black"
        ctx.fillRect(0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble())
    }

    private fun paintPreviewCells(canvas: HTMLCanvasElement, cells: Set<Cell>) {
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
        val squareSize = canvas.width / PREVIEW_BOX_SIZE

        for (cell in cells) {
            val color = htmlColor(cell.color)
            paintCell(canvas, cell.row - dRow, cell.col - dCol, squareSize, color)
        }
    }

    private fun paintCell(canvas: HTMLCanvasElement, row: Int, col: Int, squareSize: Int, fillStyle: String) {
        val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
        val size = squareSize.toDouble()
        // Draw bigger rectangle for outline of cell
        ctx.fillStyle = "grey"
        ctx.fillRect(col * size, row * size, size, size)

        // Draw smaller rectangle for fill of cell
        ctx.fillStyle = fillStyle
        ctx.fillRect(col * size + 1, row * size + 1, size - 2, size - 2)
    }

    private fun htmlColor(color: CellColor): String = when (color) {
        CellColor.GREEN      -> "green"
        CellColor.RED        -> "red"
        CellColor.DARK_BLUE  -> "blue"
        CellColor.ORANGE     -> "orange"
        CellColor.LIGHT_BLUE -> "cyan"
        CellColor.YELLOW     -> "yellow"
        CellColor.PURPLE     -> "purple"
        CellColor.NULL       -> "grey"
    }
}