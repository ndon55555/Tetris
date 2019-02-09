import org.junit.jupiter.api.Test
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TetriminoTest {
    private val tetriS = initialTetrimino(TetriminoType.S)
    private val tetriJ = initialTetrimino(TetriminoType.J)
    private val tetriI = initialTetrimino(TetriminoType.I)
    private val tetriO = initialTetrimino(TetriminoType.O)
    private val tetriT = initialTetrimino(TetriminoType.T)
    
    @Test
    fun identityMovementTest() {
        assertEquals(tetriS, tetriS.moveLeft().moveRight())
        assertEquals(tetriS, tetriS.moveRight().moveLeft())
        assertEquals(tetriS, tetriS.moveUp().moveDown())
        assertEquals(tetriS, tetriS.moveDown().moveUp())
        assertEquals(tetriS, tetriS.rotate90CCW().rotate90CW())
        assertEquals(tetriS, tetriS.rotate90CW().rotate90CCW())
        
        assertEquals(tetriJ, tetriJ.moveLeft().moveRight())
        assertEquals(tetriJ, tetriJ.moveRight().moveLeft())
        assertEquals(tetriJ, tetriJ.moveUp().moveDown())
        assertEquals(tetriJ, tetriJ.moveDown().moveUp())
        assertEquals(tetriJ, tetriJ.rotate90CCW().rotate90CW())
        assertEquals(tetriJ, tetriJ.rotate90CW().rotate90CCW())
        
        assertEquals(tetriI, tetriI.moveLeft().moveRight())
        assertEquals(tetriI, tetriI.moveRight().moveLeft())
        assertEquals(tetriI, tetriI.moveUp().moveDown())
        assertEquals(tetriI, tetriI.moveDown().moveUp())
        assertEquals(tetriI, tetriI.rotate90CCW().rotate90CW())
        assertEquals(tetriI, tetriI.rotate90CW().rotate90CCW())
        
        assertEquals(tetriO, tetriO.moveLeft().moveRight())
        assertEquals(tetriO, tetriO.moveRight().moveLeft())
        assertEquals(tetriO, tetriO.moveUp().moveDown())
        assertEquals(tetriO, tetriO.moveDown().moveUp())
        assertEquals(tetriO, tetriO.rotate90CCW().rotate90CW())
        assertEquals(tetriO, tetriO.rotate90CW().rotate90CCW())
        
        assertEquals(tetriT, tetriT.moveLeft().moveRight())
        assertEquals(tetriT, tetriT.moveRight().moveLeft())
        assertEquals(tetriT, tetriT.moveUp().moveDown())
        assertEquals(tetriT, tetriT.moveDown().moveUp())
        assertEquals(tetriT, tetriT.rotate90CCW().rotate90CW())
        assertEquals(tetriT, tetriT.rotate90CW().rotate90CCW())
    }
    
    @Test
    fun equivalentMovementTest() {
        assertEquals(tetriT.rotate90CCW(), tetriT.rotate90CW().rotate90CW().rotate90CW())
        assertEquals(tetriT.moveUp().moveLeft().moveLeft(), tetriT.moveLeft().moveUp().moveLeft())
    }
    
    @Test
    fun notFourCellsTest() {
        expectException(IllegalArgumentException::class, "should have exactly 4 cells") {
            TetrisTetrimino(Posn(1, 1), arrayOf())
        }
    }
    
    @Test
    fun nonAdjacentCellsTest() {
        expectException(IllegalArgumentException::class, "should be adjacent with at least 1 other cell") {
            TetrisTetrimino(
                Posn(5, 5),
                arrayOf(
                    Cell(CellColor.DARK_BLUE, 1, 1),
                    Cell(CellColor.DARK_BLUE, 1, 2),
                    Cell(CellColor.DARK_BLUE, 2, 2),
                    Cell(CellColor.DARK_BLUE, 3, 3)
                )
            )
        }
    }
    
    private fun <T : Throwable> expectException(exceptionClass: KClass<T>, substring: String, block: () -> Unit) {
        val ex = assertFailsWith(exceptionClass) { block() }
        
        assertTrue(ex.message?.contains(substring) ?: false)
    }
}