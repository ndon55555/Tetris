package controller

import model.board.BOARD_HEIGHT
import model.board.Board
import model.cell.Cell
import model.game.BaseGame
import model.game.config.GameConfiguration
import view.TetrisUI
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

abstract class AbstractTetrisControllerTest {
    @Test
    abstract fun runTest()

    @Test
    abstract fun stopTest()

    @Test
    abstract fun handleKeyPressAndReleaseTest()
}

@ExperimentalTime
class FreePlayTest : AbstractTetrisControllerTest() {
    private class SpyBoard : Board {
        var cellsValidityChecks = 0
        var cellsPlacements = 0
        override fun areValidCells(vararg cells: Cell): Boolean {
            cellsValidityChecks++
            val boardBottomRow = BOARD_HEIGHT - 1
            return cells.all { it.row <= boardBottomRow }
        }

        override fun placeCells(vararg cells: Cell) {
            cellsPlacements++
        }

        override fun clearLine(row: Int) = Unit

        override fun getPlacedCells(): Set<Cell> = emptySet()
    }

    private class SpyUI : TetrisUI {
        var drawCellsCount = 0
        var drawHeldCellsCount = 0
        var drawUpcomingCellsCount = 0
        override fun drawCells(cells: Set<Cell>) {
            drawCellsCount++
        }

        override fun drawHeldCells(cells: Set<Cell>) {
            drawHeldCellsCount++
        }

        override fun drawUpcomingCells(cellsQueue: List<Set<Cell>>) {
            drawUpcomingCellsCount++
        }
    }

    private lateinit var controller: TetrisController
    private val testControllerConfig = GameConfiguration().apply {
        autoDropDelay = 999999999
        lockDelay = 0
    }

    @BeforeTest
    fun init() {
        controller = ControllerImpl()
    }

    @Test
    override fun runTest() {
        // Expect no exceptions to be thrown
        controller.run(BaseGame(SpyBoard(), testControllerConfig), SpyUI())
    }

    @Test
    override fun stopTest() {
        controller.run(BaseGame(SpyBoard(), testControllerConfig), SpyUI())
        // Expect no exceptions to be thrown
        controller.stop()
    }

    @Test
    override fun handleKeyPressAndReleaseTest() {
        return
    }
    /*  TODO: figure out a better way to test this
    @Test
    override fun handleKeyPressAndReleaseTest() {
        val sb = SpyBoard()
        val sUI = SpyUI()
        controller.run(BaseGame(sb, testControllerConfig), sUI)
        controller.handleKeyPress("left")
        delay(testControllerConfig.delayedAutoShift + testControllerConfig.autoRepeatRate.toLong())
        controller.handleKeyRelease("left")
        assertTrue(sb.cellsValidityChecks > 0)

        val initCellsPlacements = sb.cellsPlacements
        val initDrawUpcomingCellsCount = sUI.drawUpcomingCellsCount
        controller.handleKeyPress("space")
        controller.handleKeyRelease("space")
        assertTrue(initCellsPlacements < sb.cellsPlacements)
        assertTrue(initDrawUpcomingCellsCount < sUI.drawUpcomingCellsCount)

        val initDrawHeldCellsCount = sUI.drawHeldCellsCount
        controller.handleKeyPress("shift")
        controller.handleKeyRelease("shift")
        assertTrue(initDrawHeldCellsCount < sUI.drawHeldCellsCount)
    }

    private fun delay(ms: Long) {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start <= ms) {
        }
    }
     */
}