import java.awt.*
import java.awt.event.*
import javax.swing.JComponent
import kotlin.math.max
import kotlin.math.min

class ViewSettings {
    var dx = 12
    var dy = 12
    var threadingVisible = 12
    var treadlingVisible = 12
}

val rangeColors = listOf<Color>(Color.WHITE, Color.BLACK, Color.BLUE.darker(), Color.RED.darker(), Color.GREEN.darker()) // TODO

abstract class BaseView(val settings: ViewSettings, val selection: Selection) : JComponent() {
    var maxi: Int = 10
    var maxj: Int = 10

    var w = 100
    var h = 100

    var cursorState = true

    init {
        isFocusable = true
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                super.mouseClicked(e)
                requestFocusInWindow()
            }
        })
        addFocusListener(object: FocusAdapter() {
            override fun focusGained(e: FocusEvent?) {
                super.focusGained(e)
                repaint()
            }

            override fun focusLost(e: FocusEvent?) {
                super.focusLost(e)
                repaint()
            }
        })
        addKeyListener(object: KeyAdapter() {
            override fun keyPressed(e: KeyEvent?) {
                super.keyPressed(e)
                if (e == null) return
                if (e.keyCode == KeyEvent.VK_LEFT) {
                    if (e.isShiftDown) {
                        selection.addLocation(max(selection.pos.i - 1, 0), selection.pos.j)
                    } else {
                        selection.setLocation(max(selection.pos.i - 1, 0), selection.pos.j)
                    }
                    repaint()
                } else if (e.keyCode == KeyEvent.VK_RIGHT) {
                    if (e.isShiftDown) {
                        selection.addLocation(min(selection.pos.i + 1, w - 1), selection.pos.j)
                    } else {
                        selection.setLocation(min(selection.pos.i + 1, w - 1), selection.pos.j)
                    }
                    repaint()
                } else if (e.keyCode == KeyEvent.VK_UP) {
                    if (e.isShiftDown) {
                        selection.addLocation(selection.pos.i, min(selection.pos.j + 1, h - 1))
                    } else {
                        selection.setLocation(selection.pos.i, min(selection.pos.j + 1, h - 1))
                    }
                    repaint()
                } else if (e.keyCode == KeyEvent.VK_DOWN) {
                    if (e.isShiftDown) {
                        selection.addLocation(selection.pos.i, max(selection.pos.j - 1, 0))
                    } else {
                        selection.setLocation(selection.pos.i, max(selection.pos.j - 1, 0))
                    }
                    repaint()
                } else if (e.keyCode == KeyEvent.VK_ENTER) {
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent()
                }
            }
        })
       
    }
    
    fun updateMax(i: Int, j: Int) {
        maxi = i
        maxj = j
    }

    fun updateSize(width: Int, height: Int) {
        w = width
        h = height
    }

    fun paintGrid(p0: Graphics) {
        val dx = settings.dx
        val dy = settings.dy
        p0.color = Color.BLACK
        for (i in 0..maxi) {
            p0.drawLine(i * dx,  0, i * dx, maxj * dy)
        }
        for (j in 0..maxj) {
            p0.drawLine(0, (maxj - j) * dy, maxi * dx, (maxj - j) * dy)
        }
    }

    fun cellBounds(i: Int, j: Int): Rectangle {
        return Rectangle(i * settings.dx, (maxj - j - 1) * settings.dy, settings.dx, settings.dy)
    }

    fun paintSelection(p0: Graphics) {
        if (!hasFocus()) return;
        val p = p0.create()
        if (p !is Graphics2D) return
        p.color = Color.RED
        p.drawRect(0, 0, maxi * settings.dx, maxj * settings.dy)

        if (!selection.empty) {
            p.color = Color.ORANGE
            p.stroke = BasicStroke(3.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 0.0f, floatArrayOf(9.0f), if (cursorState) 0.0f else 4.5f);
            val bl = selection.bottomLeft()
            val tr = selection.topRight()
            val w = tr.i - bl.i + 1
            val h = tr.j - bl.j + 1
            p.drawRect(bl.i * settings.dx, (maxj - tr.j - 1) * settings.dy, w * settings.dx, h * settings.dy)
        }
        p.color = if (cursorState) Color.RED else Color.ORANGE
        p.stroke = BasicStroke(3.0f)
        p.drawRect(selection.pos.i * settings.dx, (maxj - selection.pos.j - 1) * settings.dy, settings.dx, settings.dy)
        p.dispose()
    }

    fun toggleCursorState() {
        cursorState = !cursorState
        repaint()
    }
}

class ThreadingView(val threading: Threading, val callback: UICallback, settings: ViewSettings, val painter: Painter): BaseView(settings, threading.selection) {
    init {
        maxi = 50
        maxj = settings.threadingVisible
        addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent) {
                val i = e.x / settings.dx
                val j = (maxj * settings.dy - e.y) / settings.dy
                callback.toggleThreading(i, j)
            }
        })
        addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent?) {
                super.keyReleased(e)
                if (e == null) return
                if (e.keyCode == KeyEvent.VK_SPACE) {
                    callback.toggleThreading(selection.pos.i, selection.pos.j)
                    selection.setLocation(selection.pos.i, selection.pos.j + 1)
                }
            }
        })
    }

    override fun paintComponent(p0: Graphics) {
        super.paintComponent(p0)
        paintGrid(p0)
        p0.color = Color.DARK_GRAY
        for (i in 0 until maxi) {
            val j = threading[i]
            if (j == -1) continue
            painter.paintCell(p0, cellBounds(i, j))
        }
        if (hasFocus()) {
            p0.color = Color.RED
            p0.drawRect(0, 0, maxi * settings.dx, maxj * settings.dy)
        }
        paintSelection(p0)
    }
}

class TreadlingView(val treadling: Treadling, val callback: UICallback, settings: ViewSettings, val painter: Painter): BaseView(settings, treadling.selection) {
    init {
        maxi = settings.treadlingVisible
        maxj = 50
        addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent) {
                val i = e.x / settings.dx
                val j = (maxj * settings.dy - e.y) / settings.dy
                callback.toggleTreadling(i, j)
            }
        })
        addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent?) {
                super.keyReleased(e)
                if (e == null) return
                if (e.keyCode == KeyEvent.VK_SPACE) {
                    callback.toggleTreadling(selection.pos.i, selection.pos.j)
                    selection.setLocation(selection.pos.i, selection.pos.j + 1)
                }
            }
        })
    }

    override fun paintComponent(p0: Graphics) {
        super.paintComponent(p0)
        paintGrid(p0)
        p0.color = Color.DARK_GRAY
        for (i in 0 until maxi) {
            for (j in 0 until maxj) {
                if (treadling[i, j]) {
                    painter.paintCell(p0, cellBounds(i, j))
                }
            }
        }
        if (hasFocus()) {
            p0.color = Color.RED
            p0.drawRect(0, 0, maxi * settings.dx, maxj * settings.dy)
        }
        paintSelection(p0)
    }
}

class TieupView(val tieup: Tieup, val callback: UICallback, settings: ViewSettings, val painter: Painter): BaseView(settings, tieup.selection) {
    init {
        maxi = settings.treadlingVisible
        maxj = settings.threadingVisible
        addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent) {
                val i = e.x / settings.dx
                val j = (maxj * settings.dy - e.y) / settings.dy
                callback.toggleTieup(i, j)
            }
        })
        addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent?) {
                super.keyReleased(e)
                if (e == null) return
                if (e.keyCode == KeyEvent.VK_SPACE) {
                    callback.toggleTieup(selection.pos.i, selection.pos.j)
                    selection.setLocation(selection.pos.i, selection.pos.j + 1)
                }
            }
        })
    }

    override fun paintComponent(p0: Graphics) {
        super.paintComponent(p0)
        paintGrid(p0)
        p0.color = Color.BLACK
        for (i in 0 until maxi) {
            for (j in 0 until maxj) {
                val range = tieup[i, j]
                if (range != 0.toByte()) {
                    if (range <= 9) {
                        p0.color = rangeColors[range.toInt()]
                        painter.paintCell(p0, cellBounds(i, j))
                    } else {
                        // TODO use custom painter for special ranges (lift out, binding, unbinding)
                    }
                }
            }
        }
        if (hasFocus()) {
            p0.color = Color.RED
            p0.drawRect(0, 0, maxi * settings.dx, maxj * settings.dy)
        }
        paintSelection(p0)
    }
}

class PatternView(val pattern: Pattern, val callback: UICallback, settings: ViewSettings, val painter: Painter): BaseView(settings, pattern.selection) {
    init {
        maxi = 50
        maxj = 50
        addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent) {
                val i = e.x / settings.dx
                val j = (maxj * settings.dy - e.y) / settings.dy
                callback.togglePattern(i, j)
            }
        })
        addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent?) {
                super.keyReleased(e)
                if (e == null) return
                if (e.keyCode == KeyEvent.VK_SPACE) {
                    callback.togglePattern(selection.pos.i, selection.pos.j)
                    selection.setLocation(selection.pos.i, selection.pos.j + 1)
                }
            }
        })
    }

    override fun paintComponent(p0: Graphics) {
        super.paintComponent(p0)
        paintGrid(p0)
        p0.color = Color.DARK_GRAY
        for (i in 0 until maxi) {
            for (j in 0 until maxj) {
                val range = pattern[i, j]
                if (range != 0.toByte()) {
                    if (range <= 9) {
                        p0.color = rangeColors[range.toInt()]
                        painter.paintCell(p0, cellBounds(i, j))
                    } else {
                        // TODO use custom painter for special ranges (lift out, binding, unbinding)
                    }
                }
            }
        }
        paintSelection(p0)
    }
}
