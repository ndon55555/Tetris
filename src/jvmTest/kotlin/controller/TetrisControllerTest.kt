package controller

import model.game.config.GameConfiguration
import model.board.Board
import model.board.VISIBLE_BOARD_HEIGHT
import model.cell.Cell
import model.game.BaseGame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import view.TetrisUI
import kotlin.test.assertTrue
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
            return cells.all { it.row < VISIBLE_BOARD_HEIGHT }
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
        autoDropDelay = Integer.MAX_VALUE
        lockDelay = 0
    }

    @BeforeEach
    fun init() {
        controller = ControllerImpl()
    }

    @Test
    override fun runTest() {
        assertDoesNotThrow { controller.run(BaseGame(SpyBoard(), testControllerConfig), SpyUI()) }
    }

    @Test
    override fun stopTest() {
        controller.run(BaseGame(SpyBoard(), testControllerConfig), SpyUI())
        assertDoesNotThrow { controller.stop() }
    }

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
}