import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class CellTest {
    private val c1 = Cell(Color.RED, 0, 0)
    private val c2 = Cell(Color.PURPLE, 1, 1)
    private val c3 = Cell(Color.GREEN, 3, 4)

    @Test
    fun move() {
        assertEquals(Cell(Color.RED, 2, 2), c1.move(2, 2))
        assertEquals(Cell(Color.PURPLE, 0, 0), c2.move(-1, -1))
        assertEquals(Cell(Color.GREEN, 5, 2), c3.move(2, -2))
    }

    @Test
    fun rotate90CWAround() {
        assertEquals(Cell(Color.RED, -3, 9), c1.rotate90CWAround(Posn(3.0, 6.0)))
        assertEquals(Cell(Color.PURPLE, 1, 3), c2.rotate90CWAround(Posn(2.0, 2.0)))
        assertEquals(Cell(Color.GREEN, 1, 6), c3.rotate90CWAround(Posn(3.0, 6.0)))
    }

    @Test
    fun rotate90CCWAround() {
        assertEquals(Cell(Color.RED, 9, 3), c1.rotate90CCWAround(Posn(3.0, 6.0)))
        assertEquals(Cell(Color.PURPLE, 3, 1), c2.rotate90CCWAround(Posn(2.0, 2.0)))
        assertEquals(Cell(Color.GREEN, 5, 6), c3.rotate90CCWAround(Posn(3.0, 6.0)))
    }
    
    @Test
    fun sharesPositionWith() {
        assertEquals(false, c1.sharesPositionWith(c2))
        assertEquals(false, c2.sharesPositionWith(c3))
        assertEquals(false, c3.sharesPositionWith(c1))
        assertEquals(true, c1.sharesPositionWith(c1))
        assertEquals(true, c2.sharesPositionWith(c2))
        assertEquals(true, c3.sharesPositionWith(c3))
        assertEquals(true, c1.sharesPositionWith(Cell(Color.YELLOW, 0, 0)))
    }
}