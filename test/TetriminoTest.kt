import model.cell.CellColor
import model.cell.CellImpl
import model.tetrimino.I
import model.tetrimino.J
import model.tetrimino.O
import model.cell.Posn
import model.tetrimino.Orientation
import model.tetrimino.S
import model.tetrimino.T
import model.tetrimino.TetriminoImpl
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TetriminoTest {
    private val tetriS = S()
    private val tetriJ = J()
    private val tetriI = I()
    private val tetriO = O()
    private val tetriT = T()

    @Test
    fun identityMovementTest() {
        assertEquals(tetriS, tetriS.moveLeft().moveRight())
        assertEquals(tetriS, tetriS.moveRight().moveLeft())
        assertEquals(tetriS, tetriS.rotate90CCW().rotate90CW())
        assertEquals(tetriS, tetriS.rotate90CW().rotate90CCW())

        assertEquals(tetriJ, tetriJ.moveLeft().moveRight())
        assertEquals(tetriJ, tetriJ.moveRight().moveLeft())
        assertEquals(tetriJ, tetriJ.rotate90CCW().rotate90CW())
        assertEquals(tetriJ, tetriJ.rotate90CW().rotate90CCW())

        assertEquals(tetriI, tetriI.moveLeft().moveRight())
        assertEquals(tetriI, tetriI.moveRight().moveLeft())
        assertEquals(tetriI, tetriI.rotate90CCW().rotate90CW())
        assertEquals(tetriI, tetriI.rotate90CW().rotate90CCW())

        assertEquals(tetriO, tetriO.moveLeft().moveRight())
        assertEquals(tetriO, tetriO.moveRight().moveLeft())
        assertEquals(tetriO, tetriO.rotate90CCW().rotate90CW())
        assertEquals(tetriO, tetriO.rotate90CW().rotate90CCW())

        assertEquals(tetriT, tetriT.moveLeft().moveRight())
        assertEquals(tetriT, tetriT.moveRight().moveLeft())
        assertEquals(tetriT, tetriT.rotate90CCW().rotate90CW())
        assertEquals(tetriT, tetriT.rotate90CW().rotate90CCW())
    }

    @Test
    fun equivalentMovementTest() {
        assertEquals(tetriT.rotate90CCW(), tetriT.rotate90CW().rotate90CW().rotate90CW())
        assertEquals(tetriT.moveDown().moveLeft().moveLeft(), tetriT.moveLeft().moveDown().moveLeft())
    }

    @Test
    fun notFourCellsTest() {
        expectException(IllegalArgumentException::class, "should have exactly 4 cells") {
            TetriminoImpl(Posn(1, 1), setOf(), Orientation.UP)
        }
    }

    @Test
    fun nonAdjacentCellsTest() {
        expectException(IllegalArgumentException::class, "should be adjacent with at least 1 other cell") {
            TetriminoImpl(
                    Posn(5, 5),
                    setOf(
                            CellImpl(CellColor.DARK_BLUE, 1, 1),
                            CellImpl(CellColor.DARK_BLUE, 1, 2),
                            CellImpl(CellColor.DARK_BLUE, 2, 2),
                            CellImpl(CellColor.DARK_BLUE, 3, 3)
                    ),
                    Orientation.UP
            )
        }
    }

    @Test
    fun orientationTest() {
        assertEquals(Orientation.UP, tetriT.orientation())
        assertEquals(Orientation.RIGHT, tetriT.rotate90CW().orientation())
        assertEquals(Orientation.LEFT, tetriT.rotate90CCW().orientation())
        assertEquals(Orientation.DOWN, tetriT.rotate90CW().rotate90CW().orientation())
        assertEquals(Orientation.DOWN, tetriT.rotate90CCW().rotate90CCW().orientation())
    }

    private fun <T : Throwable> expectException(exceptionClass: KClass<T>, substring: String, block: () -> Unit) {
        val ex = assertFailsWith(exceptionClass) { block() }

        assertTrue(ex.message?.contains(substring) ?: false,
                String.format("Exception message does not contain: %s", substring))
    }
}