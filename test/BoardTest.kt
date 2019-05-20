import model.Board
import model.BoardImpl
import model.Cell
import model.CellColor
import model.Posn
import model.Tetrimino
import model.TetriminoImpl
import model.TetriminoType
import model.initTetrimino
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BoardTest {
    private lateinit var board: Board

    @BeforeEach
    fun init() {
        board = BoardImpl()
    }

    @Test
    fun validTetriminoTest() {
        val valid: Tetrimino = initTetrimino(TetriminoType.T)
        assertTrue(board.isValidTetrimino(valid))

        val outOfBounds: Tetrimino = TetriminoImpl(Posn(-1, -1), arrayOf(
                Cell(CellColor.GREEN, 1, 0),
                Cell(CellColor.GREEN, 0, 0),
                Cell(CellColor.GREEN, 0, -1),
                Cell(CellColor.GREEN, -1, -1)
        ))
        assertFalse(board.isValidTetrimino(outOfBounds))

        val overlapping: Tetrimino = valid.rotate90CW()
        board.placeTetrimino(valid)
        assertFalse(board.isValidTetrimino(overlapping))
    }

    @Test
    fun placeTetriminoAndGetCellsTest() {
        assertEquals(0, board.placedCells().size)

        val t: Tetrimino = initTetrimino(TetriminoType.S)
        board.placeTetrimino(t)
        val t2: Tetrimino = t.moveDown().moveDown()
        board.placeTetrimino(t2)
        // Placed cells match cells of the original tetriminos
        assertTrue(board.placedCells().containsAll(t.cells()))
        assertTrue(board.placedCells().containsAll(t2.cells()))

        // Cannot place a piece if invalid
        assertFails { board.placeTetrimino(t) }
    }

    @Test
    fun clearLineTest() {
        val t: Tetrimino = TetriminoImpl(Posn(1, 1), arrayOf(
                Cell(CellColor.RED, Posn(1, 0)),
                Cell(CellColor.RED, Posn(1, 1)),
                Cell(CellColor.RED, Posn(1, 2)),
                Cell(CellColor.RED, Posn(0, 2))
        ))
        board.placeTetrimino(t)
        board.clearLine(1)
        // All cells matching given row are removed
        assertEquals(1, board.placedCells().size)
        // All cells above given row are moved down
        assertTrue(board.placedCells().contains(
                Cell(CellColor.RED, Posn(0, 2)).move(1, 0)
        ))
    }
}