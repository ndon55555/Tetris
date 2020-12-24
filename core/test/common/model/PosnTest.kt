package model

import model.cell.Posn
import kotlin.test.Test
import kotlin.test.assertEquals

class PosnTest {
    private val origin = Posn(0, 0)
    private val p1 = Posn(1, 2) // Quadrant 1
    private val p2 = Posn(-1, 3) // Quadrant 2
    private val p3 = Posn(-2, -4) // Quadrant 3
    private val p4 = Posn(3, -1) // Quadrant 4
    private val p5 = Posn(5, 0) // Positive x-axis
    private val p6 = Posn(0.0, 3.5) // Positive y-axis
    private val p7 = Posn(-2.5, 0.0) // Negative x-axis
    private val p8 = Posn(0, -7) // Negative y-axis

    @Test
    fun translate() {
        assertEquals(Posn(0.1, 0.2), origin.translate(0.1, 0.2))
        assertEquals(Posn(1.1, 2.2), p1.translate(0.1, 0.2))
        assertEquals(Posn(-1.5, 2.5), p2.translate(-0.5, -0.5))
        assertEquals(Posn(-2.5, -4.5), p3.translate(-0.5, -0.5))
        assertEquals(Posn(2, 1), p4.translate(-1.0, 2.0))
        assertEquals(Posn(4, 2), p5.translate(-1.0, 2.0))
        assertEquals(Posn(3.0, -1.5), p6.translate(3.0, -5.0))
        assertEquals(Posn(0.5, -5.0), p7.translate(3.0, -5.0))
        assertEquals(Posn(3.1415, -6.0), p8.translate(3.1415, 1.0))
    }

    @Test
    fun rotate90CWAround() {
        assertEquals(Posn(0, 0), origin.rotate90CWAround(origin))
        assertEquals(Posn(2, -1), p1.rotate90CWAround(origin))
        assertEquals(Posn(3, 1), p2.rotate90CWAround(origin))
        assertEquals(Posn(-4, 2), p3.rotate90CWAround(origin))
        assertEquals(Posn(-1, -3), p4.rotate90CWAround(origin))
        assertEquals(Posn(0, -5), p5.rotate90CWAround(origin))
        assertEquals(Posn(3.5, 0.0), p6.rotate90CWAround(origin))
        assertEquals(Posn(0.0, 2.5), p7.rotate90CWAround(origin))
        assertEquals(Posn(-7, 0), p8.rotate90CWAround(origin))
    }

    @Test
    fun rotate90CCWAround() {
        assertEquals(Posn(0, 0), origin.rotate90CCWAround(origin))
        assertEquals(Posn(-2, 1), p1.rotate90CCWAround(origin))
        assertEquals(Posn(-3, -1), p2.rotate90CCWAround(origin))
        assertEquals(Posn(4, -2), p3.rotate90CCWAround(origin))
        assertEquals(Posn(1, 3), p4.rotate90CCWAround(origin))
        assertEquals(Posn(0, 5), p5.rotate90CCWAround(origin))
        assertEquals(Posn(-3.5, 0.0), p6.rotate90CCWAround(origin))
        assertEquals(Posn(0.0, -2.5), p7.rotate90CCWAround(origin))
        assertEquals(Posn(7, 0), p8.rotate90CCWAround(origin))
    }
}
