package view

import controller.ControllerImpl
import javafx.application.Platform
import javafx.scene.input.KeyEvent
import javafx.scene.layout.GridPane
import javafx.scene.paint.Paint
import javafx.scene.shape.Rectangle
import javafx.stage.Stage
import model.BOARD_HEIGHT
import model.BOARD_WIDTH
import model.BoardImpl
import model.Cell
import model.CellColor
import tornadofx.App
import tornadofx.View
import tornadofx.gridpane
import tornadofx.pane
import tornadofx.rectangle
import tornadofx.replaceChildren
import tornadofx.row

class TetrisApp : App(BoardView::class) {
    override fun start(stage: Stage) {
        stage.isResizable = false
        super.start(stage)
    }
}

class BoardView : View("Tetris"), TetrisUI {
    private val controller: ControllerImpl by inject()
    private val boardWidth = BOARD_WIDTH
    private val boardHeight = BOARD_HEIGHT

    override val root = pane {
        gridpane {
            repeat(boardHeight) {
                row {
                    repeat(boardWidth) {
                        add(backgroundCell())
                    }
                }
            }
        }
    }

    override fun onDock() {
        // to understand why this handler is set here instead of on the root,
        // see https://stackoverflow.com/questions/52356548/tornadofx-key-press-listener-issues
        currentStage?.addEventHandler(KeyEvent.KEY_PRESSED, controller)
        val view = this
        controller.run(BoardImpl(), view)
        super.onDock()
    }

    override fun onUndock() {
        controller.stop()
        super.onUndock()
    }

    override fun drawCells(cells: Set<Cell>) {
        Platform.runLater {
            val grid = GridPane()

            with(grid) {
                for (row in 0 until boardHeight) {
                    for (col in 0 until boardWidth) add(backgroundCell(), col, row)
                }

                for (c in cells) add(foregroundCell(c), c.col, c.row)
            }

            root.replaceChildren(grid)
        }
    }

}


const val CELL_SIZE = 30.0

internal fun backgroundCell(): Rectangle =
        Rectangle(CELL_SIZE, CELL_SIZE, Paint.valueOf("black")).also { it.stroke = Paint.valueOf("grey") }

internal fun foregroundCell(c: Cell): Rectangle =
        Rectangle(CELL_SIZE, CELL_SIZE, getPaint(c.color)).also { it.stroke = Paint.valueOf("grey") }

internal fun getPaint(c: CellColor): Paint = when(c) {
    CellColor.GREEN -> Paint.valueOf("green")
    CellColor.RED -> Paint.valueOf("red")
    CellColor.DARK_BLUE -> Paint.valueOf("blue")
    CellColor.ORANGE -> Paint.valueOf("orange")
    CellColor.LIGHT_BLUE -> Paint.valueOf("teal")
    CellColor.YELLOW -> Paint.valueOf("yellow")
    CellColor.PURPLE -> Paint.valueOf("purple")
    CellColor.NULL -> Paint.valueOf("grey")
}