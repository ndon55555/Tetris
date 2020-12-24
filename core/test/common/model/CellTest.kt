package model

import model.cell.Cell
import model.cell.CellColor.GREEN
import model.cell.CellColor.PURPLE
import model.cell.CellColor.RED
import model.cell.CellColor.YELLOW
import model.cell.CellImpl
import model.cell.Posn
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CellTest {
    private val c1: Cell = CellImpl(RED, 0, 0)
    private val c2: Cell = CellImpl(PURPLE, 1, 1)
    private val c3: Cell = CellImpl(GREEN, 3, 4)

    @Test
    fun moveTest() {
        assertEquals(CellImpl(RED, 2, 2), c1.move(2, 2))
        assertEquals(CellImpl(PURPLE, 0, 0), c2.move(-1, -1))
        assertEquals(CellImpl(GREEN, 5, 2), c3.move(2, -2))
    }

    @Test
    fun rotate90CWAroundTest() {
        assertEquals(CellImpl(RED, -3, 9), c1.rotate90CWAround(Posn(3.0, 6.0)))
        assertEquals(CellImpl(PURPLE, 1, 3), c2.rotate90CWAround(Posn(2.0, 2.0)))
        assertEquals(CellImpl(GREEN, 1, 6), c3.rotate90CWAround(Posn(3.0, 6.0)))
    }

    @Test
    fun rotate90CCWAroundTest() {
        assertEquals(CellImpl(RED, 9, 3), c1.rotate90CCWAround(Posn(3.0, 6.0)))
        assertEquals(CellImpl(PURPLE, 3, 1), c2.rotate90CCWAround(Posn(2.0, 2.0)))
        assertEquals(CellImpl(GREEN, 5, 6), c3.rotate90CCWAround(Posn(3.0, 6.0)))
    }

    @Test
    fun sharesPositionWithTest() {
        assertFalse(c1.sharesPositionWith(c2))
        assertFalse(c2.sharesPositionWith(c3))
        assertFalse(c3.sharesPositionWith(c1))
        assertTrue(c1.sharesPositionWith(c1))
        assertTrue(c2.sharesPositionWith(c2))
        assertTrue(c3.sharesPositionWith(c3))
        assertTrue(c1.sharesPositionWith(CellImpl(YELLOW, 0, 0)))
    }

    @Test
    fun differentColorSharesPositionTest() {
        val p = Posn(12, 3)
        assertTrue(CellImpl(RED, p).sharesPositionWith(CellImpl(PURPLE, p)))
    }
}