import model.Cell
import model.CellColor
import model.Posn
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CellTest {
    private val c1 = Cell(CellColor.RED, 0, 0)
    private val c2 = Cell(CellColor.PURPLE, 1, 1)
    private val c3 = Cell(CellColor.GREEN, 3, 4)

    @Test
    fun moveTest() {
        assertEquals(Cell(CellColor.RED, 2, 2), c1.move(2, 2))
        assertEquals(Cell(CellColor.PURPLE, 0, 0), c2.move(-1, -1))
        assertEquals(Cell(CellColor.GREEN, 5, 2), c3.move(2, -2))
    }

    @Test
    fun rotate90CWAroundTest() {
        assertEquals(Cell(CellColor.RED, -3, 9), c1.rotate90CWAround(Posn(3.0, 6.0)))
        assertEquals(Cell(CellColor.PURPLE, 1, 3), c2.rotate90CWAround(Posn(2.0, 2.0)))
        assertEquals(Cell(CellColor.GREEN, 1, 6), c3.rotate90CWAround(Posn(3.0, 6.0)))
    }

    @Test
    fun rotate90CCWAroundTest() {
        assertEquals(Cell(CellColor.RED, 9, 3), c1.rotate90CCWAround(Posn(3.0, 6.0)))
        assertEquals(Cell(CellColor.PURPLE, 3, 1), c2.rotate90CCWAround(Posn(2.0, 2.0)))
        assertEquals(Cell(CellColor.GREEN, 5, 6), c3.rotate90CCWAround(Posn(3.0, 6.0)))
    }

    @Test
    fun sharesPositionWithTest() {
        assertFalse(c1.sharesPositionWith(c2))
        assertFalse(c2.sharesPositionWith(c3))
        assertFalse(c3.sharesPositionWith(c1))
        assertTrue(c1.sharesPositionWith(c1))
        assertTrue(c2.sharesPositionWith(c2))
        assertTrue(c3.sharesPositionWith(c3))
        assertTrue(c1.sharesPositionWith(Cell(CellColor.YELLOW, 0, 0)))
    }
}