import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class TetriminoTest {
    private val tetriS = initialTetrimino(TetriminoType.S)
    private val tetriJ = initialTetrimino(TetriminoType.J)
    private val tetriI = initialTetrimino(TetriminoType.I)
    private val tetriO = initialTetrimino(TetriminoType.O)
    private val tetriT = initialTetrimino(TetriminoType.T)

    @Test
    fun movement() {
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
}