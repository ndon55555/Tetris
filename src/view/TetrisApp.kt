package view

import controller.FreePlay
import controller.config.GameConfiguration
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.input.KeyEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeType
import javafx.stage.Stage
import model.board.BOARD_WIDTH
import model.board.BoardImpl
import model.board.VISIBLE_BOARD_HEIGHT
import model.cell.Cell
import model.cell.CellColor
import tornadofx.App
import tornadofx.View
import tornadofx.action
import tornadofx.borderpane
import tornadofx.bottom
import tornadofx.button
import tornadofx.center
import tornadofx.clear
import tornadofx.gridpane
import tornadofx.hbox
import tornadofx.row
import tornadofx.top
import tornadofx.vbox
import java.util.Queue

class TetrisApp : App(BoardView::class) {
    override fun start(stage: Stage) {
        stage.isResizable = false
        super.start(stage)
    }
}

class BoardView : View("Tetris"), TetrisUI {
    private val controller = FreePlay(GameConfiguration())

    private val boardWidth = BOARD_WIDTH // # cells
    private val boardHeight = VISIBLE_BOARD_HEIGHT // # cells
    private lateinit var grid: GridPane
    private lateinit var heldPiecePane: BorderPane
    private lateinit var rightOfBoard: BorderPane
    override val root = with(this) {
        val view = this
        hbox {
            spacing = 5.0

            borderpane {
                top {
                    heldPiecePane = this
                    add(previewBackground())
                }
            }

            gridpane {
                grid = this
                repeat(boardHeight) {
                    row { repeat(boardWidth) { add(backgroundCell()) } }
                }

                requestFocus()
            }

            borderpane {
                padding = Insets(0.0, 0.0, 10.0, 0.0)
                rightOfBoard = this
                spacing = 5.0

                center {
                    vbox {
                        repeat(controller.gameConfiguration.previewPieces) { add(previewBackground()) }
                    }
                }

                bottom {
                    button("Restart") {
                        action {
                            controller.stop()
                            controller.run(BoardImpl(), view)
                        }

                        isFocusTraversable = false
                    }
                }
            }
        }
    }

    override fun onDock() {
        // to understand why this handler is set here instead of on the root,
        // see https://stackoverflow.com/questions/52356548/tornadofx-key-press-listener-issues
        currentStage?.addEventHandler(KeyEvent.ANY, object : EventHandler<KeyEvent> {
            override fun handle(event: KeyEvent?) {
                if (event != null) {
                    when (event.eventType) {
                        KeyEvent.KEY_PRESSED  -> controller.handleKeyPress(event.code.code)
                        KeyEvent.KEY_RELEASED -> controller.handleKeyRelease(event.code.code)
                        else                  -> return
                    }
                }
            }
        })
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

                repeat(boardHeight) { row ->
                    repeat(boardWidth) { col -> add(backgroundCell(), col, row) }
                }

                for (c in cells) add(foregroundCell(c), c.col, c.row)
            }
        }
    }

    override fun drawHeldCells(cells: Set<Cell>) {
        val g = preview(cells)
        Platform.runLater { heldPiecePane.top = g }
    }

    override fun drawUpcomingCells(cellsQueue: Queue<Set<Cell>>) {
        val v = VBox()
        for (cells in cellsQueue) v.add(preview(cells))
        Platform.runLater { rightOfBoard.center = v }
    }
}

const val CELL_SIZE = 30.0 // pixels
const val PREVIEW_BOX_SIZE = 4 // # of Cells
const val PREVIEW_SCALE = 0.80 // times original size

internal fun backgroundCell(): Rectangle =
    Rectangle(CELL_SIZE, CELL_SIZE, Color.BLACK).apply {
        strokeType = StrokeType.INSIDE
        stroke = Color(0.8, 0.8, 0.8, 0.2)
    }

internal fun foregroundCell(c: Cell): Rectangle =
    Rectangle(CELL_SIZE, CELL_SIZE, getPaint(c.color)).apply {
        stroke = Color.BLACK
        strokeType = StrokeType.INSIDE
        arcWidth = 10.0
        arcHeight = 10.0
    }

internal fun getPaint(c: CellColor): Paint = when (c) {
    CellColor.GREEN      -> Paint.valueOf("green")
    CellColor.RED        -> Paint.valueOf("red")
    CellColor.DARK_BLUE  -> Color(0.12, 0.29, 0.58, 1.0)
    CellColor.ORANGE     -> Paint.valueOf("orange")
    CellColor.LIGHT_BLUE -> Paint.valueOf("teal")
    CellColor.YELLOW     -> Paint.valueOf("yellow")
    CellColor.PURPLE     -> Paint.valueOf("purple")
    CellColor.NULL       -> Paint.valueOf("grey")
}

internal fun previewBackground(): GridPane {
    val g = GridPane()

    repeat(PREVIEW_BOX_SIZE) { row ->
        repeat(PREVIEW_BOX_SIZE) { col ->
            g.add(backgroundCell().apply {
                width *= PREVIEW_SCALE
                height *= PREVIEW_SCALE
            }, col, row)
        }
    }

    return g
}

internal fun preview(cells: Set<Cell>): GridPane = previewBackground().apply {
    val rows = cells.map { it.row }
    val cols = cells.map { it.col }
    val minRow = rows.min() ?: 0
    val maxRow = rows.max() ?: PREVIEW_BOX_SIZE
    val minCol = cols.min() ?: 0
    val maxCol = cols.max() ?: PREVIEW_BOX_SIZE
    val dRow = minRow - (PREVIEW_BOX_SIZE - (maxRow - minRow + 1)) / 2
    val dCol = minCol - (PREVIEW_BOX_SIZE - (maxCol - minCol + 1)) / 2

    for (c in cells) add(foregroundCell(c).apply {
        height *= PREVIEW_SCALE
        width *= PREVIEW_SCALE
    }, c.col - dCol, c.row - dRow)
}
