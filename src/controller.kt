import javax.swing.JFrame
import java.awt.EventQueue
import java.awt.Rectangle
import java.awt.event.*
import javax.swing.Timer
import javax.swing.UIManager

class Dbweave(title: String) : JFrame() {

    val settings = ViewSettings()

    val model = Model(500, 500)

    val callback: UICallback = object : UICallback {
        override fun gainedFocus(part: Part) {
            updateCursor(part)
            repaint()
        }

        override fun lostFocus(part: Part) {
            repaint()
        }

        override fun startCoord(part: Part, i: Int, j: Int) {
            model.selection.setLocation(part, i, j)
            syncCursor(part, i, j)
            views[part]?.repaint()
        }

        override fun addCoord(part: Part, i: Int, j: Int) {
            model.selection.addLocation(part, i, j)
            syncCursor(part, i, j)
            views[part]?.repaint()
        }

        override fun endCoord(part: Part, i: Int, j: Int) {
            model.selection.addLocation(part, i, j)
            syncCursor(part, i, j)
            if (model.selection.empty) {
                toggle()
            }
        }

        override fun toggle(moveCursor: Boolean) {
            val i = model.selection.pos.i
            val j = model.selection.pos.j
            if (moveCursor) {
                // TODO add checks, make configurable
                model.selection.pos.j++
                model.selection.orig.j++
            }
            when (model.selection.part) {
                Part.THREADING -> {
                    model.threading.setSingleInColumn(i, j, 1)
                    model.updateRange()
                    model.recalcPattern()
                    views[Part.THREADING]?.repaint()
                    views[Part.PATTERN]?.repaint()
                }
                Part.TIEUP -> {
                    model.tieup[i, j] = if (model.tieup[i, j] <= 0.toByte()) activeRange else 0
                    model.updateRange()
                    model.recalcPattern()
                    views[Part.TIEUP]?.repaint()
                    views[Part.PATTERN]?.repaint()
                }
                Part.TREADLING -> {
                    model.treadling[i, j] = if (model.treadling[i, j] <= 0.toByte()) 1 else 0
                    model.updateRange()
                    model.recalcPattern()
                    views[Part.TREADLING]?.repaint()
                    views[Part.PATTERN]?.repaint()
                }
                Part.PATTERN -> {
                    model.pattern[i, j] = if (model.pattern[i, j] <= 0.toByte()) activeRange else 0
                    model.recalcFromPattern()
                    model.updateRange()
                    repaint()
                }
            }
        }
    }

    val views = mapOf(
            Part.THREADING to GridView(model, Part.THREADING, settings, callback, VerticalPainter()),
            Part.TIEUP     to GridView(model, Part.TIEUP,     settings, callback, CrossPainter()),
            Part.TREADLING to GridView(model, Part.TREADLING, settings, callback, DotPainter()),
            Part.PATTERN   to GridView(model, Part.PATTERN,   settings, callback, FillPainter())
    )

    var activeRange = 1.toByte()

    init {
//        for (i in 0 until 70) {
//            model.threading[i, i % 8] = 1
//        }
//        for (j in 0 until 40) {
//            model.treadling[j % 8, j] = 1
//        }
//        for (i in 0 until 8) {
//            model.tieup[i, i] = 1
//            model.tieup[i, (i + 1) % 8] = 1
//            model.tieup[i, (i + 2) % 8] = 1
//        }
//        model.updateRange()
//        model.recalcPattern()
    }

    init {
        createUI(title)
    }

    private fun createUI(title: String) {
        setTitle(title)

        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(1024, 768)
        setLocationRelativeTo(null)

        layout = null

        views[Part.THREADING]?.updateSize(model.threading.width, model.threading.height)
        views[Part.TIEUP]?.updateSize(model.tieup.width, model.tieup.height)
        views[Part.TREADLING]?.updateSize(model.treadling.width, model.treadling.height)
        views[Part.PATTERN]?.updateSize(model.pattern.width, model.pattern.height)

        add(views[Part.THREADING])
        add(views[Part.TIEUP])
        add(views[Part.TREADLING])
        add(views[Part.PATTERN])

        arrangeComponents()

        addWindowListener(object : WindowAdapter() {
            // TODO
        })

        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                super.componentResized(e)
                arrangeComponents()
            }
        })

        Timer(500) {
            if (focusOwner is GridView) {
                (focusOwner as GridView).toggleCursorState()
            }
        }.start()

        // TODO do this in general, not only for pattern
        views[model.selection.part]?.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent?) {
                super.keyReleased(e)
                if (e == null) return
                // TODO use shift+1, ..., shift+0, ctrl+0, ctrl+shift+0
                if (e.keyCode == KeyEvent.VK_1 || e.keyCode == KeyEvent.VK_NUMPAD1) {
                    activeRange = 1
                    println("activeRange = $activeRange")
                } else if (e.keyCode == KeyEvent.VK_2 || e.keyCode == KeyEvent.VK_NUMPAD2) {
                    activeRange = 2
                    println("activeRange = $activeRange")
                } else if (e.keyCode == KeyEvent.VK_3 || e.keyCode == KeyEvent.VK_NUMPAD3) {
                    activeRange = 3
                    println("activeRange = $activeRange")
                } else if (e.keyCode == KeyEvent.VK_4 || e.keyCode == KeyEvent.VK_NUMPAD4) {
                    activeRange = 4
                    println("activeRange = $activeRange")
                }
                // TODO do all 9 ranges, plus the special ranges
            }
        })

        isFocusCycleRoot = true
        focusTraversalPolicy = DbweaveFocusTraversalPolicy(
                views[Part.THREADING]!!,
                views[Part.TIEUP]!!,
                views[Part.PATTERN]!!,
                views[Part.TREADLING]!!)
    }

    private fun arrangeComponents() {
        val border = 2
        val cx = (contentPane.width - 2 * border) / settings.dx
        val cy = (contentPane.height - 2 * border) / settings.dy
        val px = cx - settings.treadlingVisible - 1
        val py = cy - settings.threadingVisible - 1

        val x1 = border
        val w1 = px * settings.dx + 1
        val x2 = x1 + w1 + settings.dx
        val w2 = settings.treadlingVisible * settings.dx + 1

        val y1 = border
        val h1 = settings.threadingVisible * settings.dy + 1
        val y2 = y1 + h1 + settings.dx
        val h2 = py * settings.dy + 1

        views[Part.THREADING]?.bounds = Rectangle(x1, y1, w1, h1)
        views[Part.TIEUP]?.bounds = Rectangle(x2, y1, w2, h1)
        views[Part.TREADLING]?.bounds = Rectangle(x2, y2, w2, h2)
        views[Part.PATTERN]?.bounds = Rectangle(x1, y2, w1, h2)

        views[Part.THREADING]?.updateMax(px, settings.threadingVisible)
        views[Part.TIEUP]?.updateMax(settings.treadlingVisible, settings.threadingVisible)
        views[Part.TREADLING]?.updateMax(settings.treadlingVisible, py)
        views[Part.PATTERN]?.updateMax(px, py)

        model.updateRange()
    }

    fun syncCursor(part: Part, i: Int, j: Int) {
        when (part) {
            Part.THREADING -> {
                model.cursorPos.cursorLeft = i
                model.cursorPos.cursorTop = j
            }
            Part.TIEUP -> {
                model.cursorPos.cursorRight = i
                model.cursorPos.cursorTop = j
            }
            Part.TREADLING -> {
                model.cursorPos.cursorRight = i
                model.cursorPos.cursorBottom = j
            }
            Part.PATTERN -> {
                model.cursorPos.cursorLeft = i
                model.cursorPos.cursorBottom = j
            }
        }
    }

    fun updateCursor(part: Part) {
        when (part) {
            Part.THREADING -> {
                model.selection.pos.i = model.cursorPos.cursorLeft
                model.selection.pos.j = model.cursorPos.cursorTop
            }
            Part.TIEUP -> {
                model.selection.pos.i = model.cursorPos.cursorRight
                model.selection.pos.j = model.cursorPos.cursorTop
            }
            Part.TREADLING -> {
                model.selection.pos.i = model.cursorPos.cursorRight
                model.selection.pos.j = model.cursorPos.cursorBottom
            }
            Part.PATTERN -> {
                model.selection.pos.i = model.cursorPos.cursorLeft
                model.selection.pos.j = model.cursorPos.cursorBottom
            }
        }
        model.selection.orig.i = model.selection.pos.i
        model.selection.orig.j = model.selection.pos.j
    }

}

private fun createAndShowGUI() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    val frame = Dbweave("DB-WEAVE")
    frame.isVisible = true
    frame.views[Part.PATTERN]?.requestFocus()
}

fun main() {
    EventQueue.invokeLater(::createAndShowGUI)
}
