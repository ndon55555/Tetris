package view

import javafx.beans.property.SimpleIntegerProperty
import javafx.scene.control.Label
import javafx.scene.input.KeyCode
import tornadofx.App
import tornadofx.View
import tornadofx.action
import tornadofx.button
import tornadofx.label
import tornadofx.minusAssign
import tornadofx.plusAssign
import tornadofx.singleAssign
import tornadofx.vbox

class TetrisApp : App(BoardView::class)

class BoardView : View() {
    var lblCount: Label by singleAssign()
    var count = SimpleIntegerProperty(0)
    
    override val root = vbox {
        lblCount = label(count)
        button("Press Me!") {
            action {
                count += 1
            }
        }
        
        setOnKeyPressed {
            when(it.code) {
                KeyCode.Z -> count += 1
                KeyCode.X -> count += 2
                else -> count -= 1
            }
        }
    }
}