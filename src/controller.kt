import javax.swing.JFrame
import java.awt.EventQueue
import java.awt.KeyboardFocusManager
import java.awt.Rectangle
import java.awt.event.*
import javax.swing.Timer
import javax.swing.UIManager
import kotlin.math.max
import kotlin.math.min

class Dbweave(title: String) : JFrame() {

    val settings = ViewSettings()

    val model = Model(500, 500)

    val callback: UICallback = object : UICallback {
        override fun gainedFocus(part: Part) {
            focusedPart = part
            focusedView = getView(part)
            focusedGrid = getGrid(part)
            updateCursor(part)
            repaint()
        }

        override fun lostFocus(part: Part) {
            repaint()
        }

        override fun startCoord(part: Part, i: Int, j: Int) {
            model.selection.setLocation(part, i, j)
            syncCursor(part, i, j)
            repaint()
        }

        override fun addCoord(part: Part, i: Int, j: Int) {
            model.selection.addLocation(part, i, j)
            syncCursor(part, i, j)
            repaint()
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
                if (model.selection.part == Part.WARP_COLORS) {
                    // TODO add checks, make configurable
                    model.selection.pos.i++
                    model.selection.orig.i++
                } else {
                    // TODO add checks, make configurable
                    model.selection.pos.j++
                    model.selection.orig.j++
                }
            }
            when (model.selection.part) {
                Part.THREADING -> {
                    model.threading.setSingleInColumn(i, j, 1)
                    model.updateRange()
                    model.recalcPattern()
                    repaint()
                }
                Part.TIEUP -> {
                    model.tieup[i, j] = if (model.tieup[i, j] <= 0.toByte()) activeRange else 0
                    model.updateRange()
                    model.recalcPattern()
                    repaint()
                }
                Part.TREADLING -> {
                    model.treadling[i, j] = if (model.treadling[i, j] <= 0.toByte()) 1 else 0
                    model.updateRange()
                    model.recalcPattern()
                    repaint()
                }
                Part.PATTERN -> {
                    model.pattern[i, j] = if (model.pattern[i, j] <= 0.toByte()) activeRange else 0
                    model.recalcFromPattern()
                    model.updateRange()
                    repaint()
                }
                Part.WARP_COLORS -> {
                    model.warpColors[i, j] = activeColor.toByte()
                    repaint()
                }
                Part.WEFT_COLORS -> {
                    model.weftColors[i, j] = activeColor.toByte()
                    repaint()
                }
            }
        }
    }

    val views = mapOf(
            Part.THREADING   to GridView(model, Part.THREADING, settings,   callback, VerticalPainter()),
            Part.TIEUP       to GridView(model, Part.TIEUP,     settings,   callback, SmallCirclePainter()),
            Part.TREADLING   to GridView(model, Part.TREADLING, settings,   callback, DotPainter()),
            Part.PATTERN     to GridView(model, Part.PATTERN,   settings,   callback, FillPainter()),
            Part.WARP_COLORS to GridView(model, Part.WARP_COLORS, settings, callback, FillPainter()),
            Part.WEFT_COLORS to GridView(model, Part.WEFT_COLORS, settings, callback, FillPainter())
    )

    var focusedPart = Part.PATTERN
    var focusedView: GridView = views[Part.PATTERN] ?: error("Pattern not defined")
    var focusedGrid: Grid = model.pattern

    var activeRange = 1.toByte()
    var activeColor = 200

    fun getView(part: Part): GridView {
        return views[part] ?: error("$part not defined")
    }

    fun getGrid(part: Part): Grid {
        return when (part) {
            Part.THREADING -> model.threading
            Part.TIEUP -> model.tieup
            Part.TREADLING -> model.treadling
            Part.PATTERN -> model.pattern
            Part.WARP_COLORS -> model.warpColors
            Part.WEFT_COLORS -> model.weftColors
        }
    }

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

        getView(Part.THREADING).updateSize(model.threading.width, model.threading.height)
        getView(Part.TIEUP).updateSize(model.tieup.width, model.tieup.height)
        getView(Part.TREADLING).updateSize(model.treadling.width, model.treadling.height)
        getView(Part.PATTERN).updateSize(model.pattern.width, model.pattern.height)
        getView(Part.WARP_COLORS).updateSize(model.warpColors.width, model.warpColors.height)
        getView(Part.WEFT_COLORS).updateSize(model.weftColors.width, model.weftColors.height)

        add(getView(Part.THREADING))
        add(getView(Part.TIEUP))
        add(getView(Part.TREADLING))
        add(getView(Part.PATTERN))
        add(getView(Part.WARP_COLORS))
        add(getView(Part.WEFT_COLORS))

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

        val keylistener = object: KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                super.keyPressed(e)
                val selection = model.selection
                val i = if (e.isControlDown) settings.groupx else 1
                val j = if (e.isControlDown) settings.groupy else 1
                if (e.keyCode == KeyEvent.VK_LEFT) {
                    if (e.isShiftDown) {
                        if (selection.width == settings.groupx) {
                            if (i > 1) callback.addCoord(focusedPart, max(selection.pos.i - i + 1, 0), selection.pos.j)
                            else callback.addCoord(focusedPart, max(selection.pos.i - i, 0), selection.pos.j)
                        } else callback.addCoord(focusedPart, max(selection.pos.i - i, 0), selection.pos.j)
                    } else {
                        callback.startCoord(focusedPart, max(selection.pos.i - i, 0), selection.pos.j)
                    }
                } else if (e.keyCode == KeyEvent.VK_RIGHT) {
                    if (e.isShiftDown) {
                        if (selection.width == 1) {
                            if (i > 1) callback.addCoord(focusedPart, min(selection.pos.i + i - 1, focusedGrid.width - 1), selection.pos.j)
                            else callback.addCoord(focusedPart, min(selection.pos.i + i, focusedGrid.width - 1), selection.pos.j)
                        } else callback.addCoord(focusedPart, min(selection.pos.i + i, focusedGrid.width - 1), selection.pos.j)
                    } else {
                        callback.startCoord(focusedPart, min(selection.pos.i + i, focusedGrid.width - 1), selection.pos.j)
                    }
                } else if (e.keyCode == KeyEvent.VK_UP) {
                    if (e.isShiftDown) {
                        if (selection.height == 1) {
                            if (j > 1) callback.addCoord(focusedPart, selection.pos.i, min(selection.pos.j + j - 1, focusedGrid.height - 1))
                            else callback.addCoord(focusedPart, selection.pos.i, min(selection.pos.j + j, focusedGrid.height - 1))
                        } else callback.addCoord(focusedPart, selection.pos.i, min(selection.pos.j + j, focusedGrid.height - 1))
                    } else {
                        callback.startCoord(focusedPart, selection.pos.i, min(selection.pos.j + j, focusedGrid.height - 1))
                    }
                } else if (e.keyCode == KeyEvent.VK_DOWN) {
                    if (e.isShiftDown) {
                        if (selection.height == settings.groupy) {
                            if (j > 1) callback.addCoord(focusedPart, selection.pos.i, max(selection.pos.j - j + 1, 0))
                            else callback.addCoord(focusedPart, selection.pos.i, max(selection.pos.j - j, 0))
                        } else callback.addCoord(focusedPart, selection.pos.i, max(selection.pos.j - j, 0))
                    } else {
                        callback.startCoord(focusedPart, selection.pos.i, max(selection.pos.j - j, 0))
                    }
                } else if (e.keyCode == KeyEvent.VK_SPACE) {
                    callback.toggle(true)
                } else if (e.keyCode == KeyEvent.VK_ENTER) {
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent()
                }
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
                if (e.keyCode == KeyEvent.VK_I) {
                    settings.dx += 2
                    settings.dy += 2
                    arrangeComponents()
                    repaint()
                } else if (e.keyCode == KeyEvent.VK_U) {
                    settings.dx = max(settings.dx - 2, 10)
                    settings.dy = max(settings.dy - 2, 10)
                    arrangeComponents()
                    repaint()
                } else if (e.keyCode == KeyEvent.VK_O) {
                    settings.dx = 14
                    settings.dy = 14
                    arrangeComponents()
                    repaint()
                }
                if (e.keyCode == KeyEvent.VK_Q) {
                    settings.style = ViewStyle.DRAFT
                } else if (e.keyCode == KeyEvent.VK_W) {
                    settings.style = ViewStyle.COLOR
                } else if (e.keyCode == KeyEvent.VK_E) {
                    settings.style = ViewStyle.SIMULATION
                } else if (e.keyCode == KeyEvent.VK_R) {
                    settings.style = ViewStyle.HIDDEN
                }
            }
        }
        getView(Part.PATTERN).addKeyListener(keylistener)
        getView(Part.THREADING).addKeyListener(keylistener)
        getView(Part.TIEUP).addKeyListener(keylistener)
        getView(Part.TREADLING).addKeyListener(keylistener)
        getView(Part.WARP_COLORS).addKeyListener(keylistener)
        getView(Part.WEFT_COLORS).addKeyListener(keylistener)

        isFocusCycleRoot = true
        focusTraversalPolicy = DbweaveFocusTraversalPolicy(
                getView(Part.THREADING),
                getView(Part.TIEUP),
                getView(Part.PATTERN),
                getView(Part.TREADLING),
                getView(Part.WARP_COLORS),
                getView(Part.WEFT_COLORS))
    }

    private fun arrangeComponents() {
        val border = 2
        val cx = (contentPane.width - 2 * border) / settings.dx
        val cy = (contentPane.height - 2 * border) / settings.dy
        val px = cx - settings.treadlingVisible - 1 - 1 - 1
        val py = cy - settings.threadingVisible - 1 - 1 - 1

        val x1 = border
        val w1 = px * settings.dx + 1
        val x2 = x1 + px * settings.dx + settings.dx
        val w2 = settings.treadlingVisible * settings.dx + 1
        val x3 = x2 + settings.treadlingVisible * settings.dx + settings.dx
        val w3 = settings.dx + 1

        val y1 = border
        val h1 = settings.dy + 1
        val y2 = y1 + settings.dy + settings.dy
        val h2 = settings.threadingVisible * settings.dy + 1
        val y3 = y2 + settings.threadingVisible * settings.dy + settings.dy
        val h3 = py * settings.dy + 1

        getView(Part.WARP_COLORS).bounds = Rectangle(x1, y1, w1, h1)
        getView(Part.THREADING).bounds = Rectangle(x1, y2, w1, h2)
        getView(Part.TIEUP).bounds = Rectangle(x2, y2, w2, h2)
        getView(Part.TREADLING).bounds = Rectangle(x2, y3, w2, h3)
        getView(Part.PATTERN).bounds = Rectangle(x1, y3, w1, h3)
        getView(Part.WEFT_COLORS).bounds = Rectangle(x3, y3, w3, h3)

        getView(Part.WARP_COLORS).updateMax(px, 1)
        getView(Part.THREADING).updateMax(px, settings.threadingVisible)
        getView(Part.TIEUP).updateMax(settings.treadlingVisible, settings.threadingVisible)
        getView(Part.TREADLING).updateMax(settings.treadlingVisible, py)
        getView(Part.PATTERN).updateMax(px, py)
        getView(Part.WEFT_COLORS).updateMax(1, py)

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
            Part.WARP_COLORS -> {
                model.cursorPos.cursorLeft = i
            }
            Part.WEFT_COLORS -> {
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
            Part.WARP_COLORS -> {
                model.selection.pos.i = model.cursorPos.cursorLeft
                model.selection.pos.j = 0
            }
            Part.WEFT_COLORS -> {
                model.selection.pos.i = 0
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
