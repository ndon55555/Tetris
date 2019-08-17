package controller

import controller.config.GameConfiguration
import model.board.Board
import model.board.VISIBLE_BOARD_HEIGHT
import model.cell.Cell
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import view.TetrisUI
import java.awt.event.KeyEvent
import java.util.Queue
import kotlin.test.assertTrue

abstract class AbstractTetrisControllerTest {
    @Test
    abstract fun runTest()

    @Test
    abstract fun stopTest()

    @Test
    abstract fun handleKeyPressAndReleaseTest()
}

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

        override fun drawUpcomingCells(cellsQueue: Queue<Set<Cell>>) {
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
        controller = FreePlay(testControllerConfig)
    }

    @Test
    override fun runTest() {
        assertDoesNotThrow { controller.run(SpyBoard(), SpyUI()) }
    }

    @Test
    override fun stopTest() {
        controller.run(SpyBoard(), SpyUI())
        assertDoesNotThrow { controller.stop() }
    }

    @Test
    override fun handleKeyPressAndReleaseTest() {
        val sb = SpyBoard()
        val sUI = SpyUI()
        controller.run(sb, sUI)
        controller.handleKeyPress(KeyEvent.VK_LEFT)
        delay(testControllerConfig.delayedAutoShift + testControllerConfig.autoRepeatRate.toLong())
        controller.handleKeyRelease(KeyEvent.VK_LEFT)
        assertTrue(sb.cellsValidityChecks > 0)

        val initCellsPlacements = sb.cellsPlacements
        val initDrawUpcomingCellsCount = sUI.drawUpcomingCellsCount
        controller.handleKeyPress(KeyEvent.VK_SPACE)
        controller.handleKeyRelease(KeyEvent.VK_SPACE)
        assertTrue(initCellsPlacements < sb.cellsPlacements)
        assertTrue(initDrawUpcomingCellsCount < sUI.drawUpcomingCellsCount)

        val initDrawHeldCellsCount = sUI.drawHeldCellsCount
        controller.handleKeyPress(KeyEvent.VK_SHIFT)
        controller.handleKeyRelease(KeyEvent.VK_SHIFT)
        assertTrue(initDrawHeldCellsCount < sUI.drawHeldCellsCount)
    }

    private fun delay(ms: Long) {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start <= ms) {
        }
    }
}
