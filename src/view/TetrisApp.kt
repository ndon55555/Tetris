package view

import controller.ControllerImpl
import javafx.application.Platform
import javafx.scene.input.KeyEvent
import javafx.scene.layout.GridPane
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeType
import javafx.stage.Stage
import model.board.BOARD_HEIGHT
import model.board.BOARD_WIDTH
import model.board.BoardImpl
import model.cell.Cell
import model.cell.CellColor
import tornadofx.App
import tornadofx.View
import tornadofx.action
import tornadofx.button
import tornadofx.clear
import tornadofx.gridpane
import tornadofx.hbox
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
    private lateinit var grid: GridPane

    override val root = with(this) {
        val view = this
        hbox {
            gridpane {
                repeat(boardHeight) {
                    row {
                        repeat(boardWidth) {
                            add(backgroundCell())
                        }
                    }
                }

                grid = this
                requestFocus()
            }

            button("Restart") {
                action {
                    controller.stop()
                    controller.run(BoardImpl(), view)
                }

                isFocusTraversable = false
            }
        }
    }

    override fun onDock() {
        // to understand why this handler is set here instead of on the root,
        // see https://stackoverflow.com/questions/52356548/tornadofx-key-press-listener-issues
        currentStage?.addEventHandler(KeyEvent.ANY, controller)
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
            with(grid) {
                clear()

                for (row in 0 until boardHeight) {
                    for (col in 0 until boardWidth) add(backgroundCell(), col, row)
                }

                for (c in cells) add(foregroundCell(c), c.col, c.row)
            }
        }
    }

}


const val CELL_SIZE = 30.0

internal fun backgroundCell(): Rectangle =
        Rectangle(CELL_SIZE, CELL_SIZE, Color.BLACK)

internal fun foregroundCell(c: Cell): Rectangle =
        Rectangle(CELL_SIZE, CELL_SIZE, getPaint(c.color)).also {
            it.stroke = Color.BLACK
            it.strokeWidth = 1.5
            it.strokeType = StrokeType.INSIDE
            it.arcWidth = 10.0
            it.arcHeight = 10.0
        }

internal fun getPaint(c: CellColor): Paint = when (c) {
    CellColor.GREEN -> Paint.valueOf("green")
    CellColor.RED -> Paint.valueOf("red")
    CellColor.DARK_BLUE -> Paint.valueOf("blue")
    CellColor.ORANGE -> Paint.valueOf("orange")
    CellColor.LIGHT_BLUE -> Paint.valueOf("teal")
    CellColor.YELLOW -> Paint.valueOf("yellow")
    CellColor.PURPLE -> Paint.valueOf("purple")
    CellColor.NULL -> Paint.valueOf("grey")
}
