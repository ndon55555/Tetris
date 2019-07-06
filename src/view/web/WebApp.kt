package view.web

import controller.ControllerImpl
import io.kweb.Kweb
import io.kweb.dom.element.creation.tags.canvas
import io.kweb.dom.element.events.on
import io.kweb.dom.element.new
import io.kweb.state.KVar
import io.kweb.state.render.render
import model.board.BOARD_WIDTH
import model.board.BoardImpl
import model.board.VISIBLE_BOARD_HEIGHT
import model.cell.Cell
import model.cell.CellColor
import view.TetrisUI
import java.awt.event.KeyEvent.VK_DOWN
import java.awt.event.KeyEvent.VK_LEFT
import java.awt.event.KeyEvent.VK_RIGHT
import java.awt.event.KeyEvent.VK_SHIFT
import java.awt.event.KeyEvent.VK_SPACE
import java.awt.event.KeyEvent.VK_UP
import java.util.Queue
import kotlin.math.ceil

class TetrisApp() : TetrisUI {
    private val controller = ControllerImpl()
    private val cells = KVar(setOf<Cell>())

    init {
        Kweb(port = 80) {
            doc.body.new {
                val squareSize = 30
                val offset = 0.5
                val canvasPad = ceil(offset).toInt()
                val canvasWidth = canvasPad + BOARD_WIDTH * squareSize
                val canvasHeight = canvasPad + VISIBLE_BOARD_HEIGHT * squareSize
                val board = canvas(canvasWidth, canvasHeight)

                board.execute(
                        """
                        var c = document.getElementById("${board.id}");
                        var ctx = c.getContext("2d");
                        function drawBoard() {
                            // Generate vertical lines
                            for (var r = 0; r <= $BOARD_WIDTH; r++) {
                                ctx.moveTo($offset + $squareSize * r, 0);
                                ctx.lineTo($offset + $squareSize * r, $VISIBLE_BOARD_HEIGHT * $squareSize);
                            }
        
                            // Generate horizontal lines
                            for (var c = 0; c <= $VISIBLE_BOARD_HEIGHT; c++) {
                                ctx.moveTo(0, $offset + $squareSize * c);
                                ctx.lineTo($BOARD_WIDTH * $squareSize, $offset + $squareSize * c);
                            }
                            
                            ctx.strokeStyle = "black";
                            ctx.stroke();
                        }
                        
                        drawBoard();
                        """.trimIndent()
                )

                render(cells) {
                    execute(
                            """
                            // Clear the board
                            var c = document.getElementById("${board.id}");
                            var ctx = c.getContext("2d");
                            ctx.clearRect(0, 0, $canvasWidth, $canvasHeight);
                            """.trimIndent()
                    )

                    for (cell in it) {
                        val r = cell.row
                        val c = cell.col
                        val color = when (cell.color) {
                            CellColor.GREEN -> "green"
                            CellColor.RED -> "red"
                            CellColor.DARK_BLUE -> "blue"
                            CellColor.ORANGE -> "orange"
                            CellColor.LIGHT_BLUE -> "cyan"
                            CellColor.YELLOW -> "yellow"
                            CellColor.PURPLE -> "purple"
                            CellColor.NULL -> "grey"
                        }
                        execute(
                                """
                                var c = document.getElementById("${board.id}");
                                var ctx = c.getContext("2d");
                                // Draw bigger rectangle for outline of cell
                                ctx.fillStyle = 'grey';
                                ctx.fillRect($c * $squareSize + $offset, $r * $squareSize + $offset, $squareSize, $squareSize);
                                // Draw smaller rectangle for fill of cell
                                ctx.fillStyle = '$color';
                                ctx.fillRect($c * $squareSize + $offset + 1, $r * $squareSize + $offset + 1, $squareSize - 2, $squareSize - 2);
                                
                                
                                function drawBoard() {
                                    // Generate vertical lines
                                    for (var r = 0; r <= $BOARD_WIDTH; r++) {
                                        ctx.moveTo($offset + $squareSize * r, 0);
                                        ctx.lineTo($offset + $squareSize * r, $VISIBLE_BOARD_HEIGHT * $squareSize);
                                    }
                
                                    // Generate horizontal lines
                                    for (var c = 0; c <= $VISIBLE_BOARD_HEIGHT; c++) {
                                        ctx.moveTo(0, $offset + $squareSize * c);
                                        ctx.lineTo($BOARD_WIDTH * $squareSize, $offset + $squareSize * c);
                                    }
                                    
                                    ctx.strokeStyle = "black";
                                    ctx.stroke();
                                }
                                
                                drawBoard();
                                """.trimIndent()
                        )
                    }
                }
            }

            doc.body.on.keydown {
                controller.handleKeyPress(when {
                    it.key == "Shift" -> VK_SHIFT
                    it.key == " " -> VK_SPACE
                    it.key == "ArrowUp" -> VK_UP
                    it.key == "ArrowLeft" -> VK_LEFT
                    it.key == "ArrowRight" -> VK_RIGHT
                    it.key == "ArrowDown" -> VK_DOWN
                    it.key.length == 1 -> it.key.toUpperCase().single().toInt()
                    else -> {
                        System.err.println("unhandled key: -->${it.key}<--")
                        0
                    }
                })
            }

            doc.body.on.keyup {
                controller.handleKeyRelease(when {
                    it.key == "Shift" -> VK_SHIFT
                    it.key == " " -> VK_SPACE
                    it.key == "ArrowUp" -> VK_UP
                    it.key == "ArrowLeft" -> VK_LEFT
                    it.key == "ArrowRight" -> VK_RIGHT
                    it.key == "ArrowDown" -> VK_DOWN
                    it.key.length == 1 -> it.key.toUpperCase().single().toInt()
                    else -> {
                        System.err.println("unhandled key: -->${it.key}<--")
                        0
                    }
                })
            }
        }
        controller.run(BoardImpl(), this)
    }

    override fun drawCells(cells: Set<Cell>) {
        this.cells.value = cells
    }

    override fun drawHeldCells(cells: Set<Cell>) {
        // TODO
    }

    override fun drawUpcomingCells(cellsQueue: Queue<Set<Cell>>) {
        // TODO
    }
}