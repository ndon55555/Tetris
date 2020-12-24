package model

import model.cell.CellColor
import model.cell.CellImpl
import model.cell.Posn
import model.tetrimino.I
import model.tetrimino.J
import model.tetrimino.L
import model.tetrimino.O
import model.tetrimino.Orientation
import model.tetrimino.S
import model.tetrimino.T
import model.tetrimino.Tetrimino
import model.tetrimino.TetriminoImpl
import model.tetrimino.Z
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

open class TetriminoTest {
    private val allStandardPieces = setOf(S(), Z(), J(), L(), I(), O(), T())
    private val prettyName = mapOf<Any, String>(
        S::class to "S",
        Z::class to "Z",
        J::class to "J",
        L::class to "L",
        I::class to "I",
        O::class to "O",
        T::class to "T",
        Tetrimino::moveDown to "Down",
        Tetrimino::moveUp to "Up",
        Tetrimino::moveLeft to "Left",
        Tetrimino::moveRight to "Right",
        Tetrimino::rotate90CW to "90 degrees CW",
        Tetrimino::rotate90CCW to "90 degrees CCW"
    )

    @TestFactory
    fun oppositeMovementTest(): List<DynamicTest> {
        val opposites = setOf(
            Pair(Tetrimino::moveDown, Tetrimino::moveUp),
            Pair(Tetrimino::moveLeft, Tetrimino::moveRight),
            Pair(Tetrimino::rotate90CW, Tetrimino::rotate90CCW)
        )

        val testTemplate =
            fun(p: Tetrimino, f1: Tetrimino.() -> Tetrimino, f2: Tetrimino.() -> Tetrimino): DynamicTest =
                dynamicTest(String.format("%s: %s then %s", prettyName[p::class], prettyName[f1], prettyName[f2])) {
                    assertEquals(p, f2(f1(p)))
                }

        return allStandardPieces.fold(mutableListOf()) { tests, piece ->
            opposites.forEach { (f1, f2) ->
                tests += testTemplate(piece, f1, f2)
                tests += testTemplate(piece, f2, f1)
            }

            tests
        }
    }

    @TestFactory
    fun equivalentMovementTest(): List<DynamicTest> {
        val testTemplate = fun(p: Tetrimino): Pair<DynamicTest, DynamicTest> = Pair(
            dynamicTest(String.format("Rotate %s: 90 CCW = 90CW * 3", prettyName[p::class])) {
                assertEquals(p.rotate90CCW(), p.rotate90CW().rotate90CW().rotate90CW())
            },
            dynamicTest(String.format("Rotate %s: Down, Left, Left = Left, Down Left", prettyName[p::class])) {
                assertEquals(p.moveDown().moveLeft().moveLeft(), p.moveLeft().moveDown().moveLeft())
            }
        )

        return allStandardPieces.fold(mutableListOf()) { tests, piece ->
            tests.addAll(testTemplate(piece).toList())
            tests
        }
    }

    @TestFactory
    fun orientationTest(): List<DynamicTest> {
        val descriptionExpectedActual = setOf(
            Triple("Default orientation", Orientation.UP, { p: Tetrimino -> p.orientation() }),
            Triple("Rotate 90 CW orientation", Orientation.RIGHT, { p: Tetrimino -> p.rotate90CW().orientation() }),
            Triple("Rotate 90 CCW orientation", Orientation.LEFT, { p: Tetrimino -> p.rotate90CCW().orientation() }),
            Triple(
                "Rotate 90 CW * 2 orientation",
                Orientation.DOWN,
                { p: Tetrimino -> p.rotate90CW().rotate90CW().orientation() }),
            Triple(
                "Rotate 90 CCW * 2 orientation",
                Orientation.DOWN,
                { p: Tetrimino -> p.rotate90CCW().rotate90CCW().orientation() })
        )

        return allStandardPieces.fold(mutableListOf()) { tests, piece ->
            for ((description, expected, actual) in descriptionExpectedActual) {
                tests += dynamicTest(String.format("%s: %s", prettyName[piece::class], description)) {
                    assertEquals(
                        expected,
                        actual(piece)
                    )
                }
            }

            tests
        }
    }

    @TestFactory
    fun identityMoveTest(): List<DynamicTest> = allStandardPieces.map { piece ->
        val x = Random.Default.nextInt()
        val y = Random.Default.nextInt()

        dynamicTest(String.format("%s move (%d, %d) then (%d, %d)", prettyName[piece::class], x, y, -x, -y)) {
            assertEquals(piece, piece.move(x, y).move(-x, -y))
        }
    }

    @TestFactory
    fun cellsTest(): List<DynamicTest> = allStandardPieces.map { piece ->
        dynamicTest(String.format("%s cells", prettyName[piece::class])) {
            assertEquals(4, piece.cells().size)
        }
    }

    @TestFactory
    fun equalsAndHashCodeTest(): List<DynamicTest> {
        val n = Random.Default.nextInt(10, 20)
        val seed = Random.Default.nextInt()
        println(String.format("Randomly generating `equals` and `hashCode` tests using seed %d", seed))
        val rand = Random(seed)
        val tests = mutableListOf<DynamicTest>()

        repeat(n) {
            val a = allStandardPieces.random(rand)
            val b = allStandardPieces.random(rand)
            val c = allStandardPieces.random(rand)

            tests += dynamicTest(String.format("Reflexivity of %s", prettyName[a::class])) {
                assertEquals(a, a)
                assertEquals(a.hashCode(), a.hashCode())
            }

            if (a == b) {
                tests += dynamicTest(String.format("Symmetry of %s", prettyName[a::class])) {
                    assertEquals(b, a)
                }
            }

            if (a == b || b == c) {
                tests += dynamicTest(
                    String.format(
                        "Transitivity of %s, %s, %s",
                        prettyName[a::class],
                        prettyName[b::class],
                        prettyName[c::class]
                    )
                ) {
                    when {
                        a == b && b == c -> assertEquals(a, c)
                        a != b && b == c -> assertNotEquals(a, c)
                        a == b && b != c -> assertNotEquals(a, c)
                    }
                }
            }
        }

        return tests
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

    private fun <T : Throwable> expectException(exceptionClass: KClass<T>, substring: String, block: () -> Unit) {
        val ex = assertFailsWith(exceptionClass) { block() }

        assertTrue(
            ex.message?.contains(substring) ?: false,
            String.format("Exception message does not contain: %s", substring)
        )
    }
}