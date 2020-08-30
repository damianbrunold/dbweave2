import java.awt.*
import java.awt.event.*
import javax.swing.JComponent

class ViewSettings {
    var dx = 14
    var dy = 14

    var threadingVisible = 12
    var treadlingVisible = 12

    var groupx = 4
    var groupy = 4
}

val rangeColors = listOf<Color>(Color.WHITE, Color.BLACK, Color.BLUE.darker(), Color.RED.darker(), Color.GREEN.darker()) // TODO

class GridView(val model: Model,
               val part: Part,
               private val settings: ViewSettings,
               private val callback: UICallback,
               var painter: Painter) : JComponent() {
    var maxi: Int = 10
    var maxj: Int = 10

    init {
        maxi = when (part) {
            Part.THREADING -> 50
            Part.TIEUP -> settings.treadlingVisible
            Part.TREADLING -> settings.treadlingVisible
            Part.PATTERN -> 50
        }
        maxj = when (part) {
            Part.THREADING -> settings.threadingVisible
            Part.TIEUP -> settings.threadingVisible
            Part.TREADLING -> 50
            Part.PATTERN -> 50
        }
    }

    var w = 100
    var h = 100

    var cursorState = true

    var mouseDrag = false

    fun getI(x: Int) = x / settings.dx
    fun getJ(y: Int) = (maxj * settings.dy - y) / settings.dy

    fun getGrid(): Grid = when(part) {
        Part.THREADING -> model.threading
        Part.TIEUP -> model.tieup
        Part.TREADLING -> model.treadling
        Part.PATTERN -> model.pattern
    }

    init {
        isFocusable = true
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                super.mouseClicked(e)
                requestFocusInWindow()
            }
            override fun mousePressed(e: MouseEvent) {
                super.mousePressed(e)
                if (mouseDrag) {
                    mouseDrag = false
                    return
                }
                mouseDrag = true
                callback.startCoord(part, getI(e.x), getJ(e.y))
            }

            override fun mouseReleased(e: MouseEvent) {
                super.mouseReleased(e)
                if (!mouseDrag) return
                mouseDrag = false
                if (!e.isControlDown) {
                    callback.endCoord(part, getI(e.x), getJ(e.y))
                }
            }

        })
        addMouseMotionListener(object : MouseAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                super.mouseDragged(e)
                if (!mouseDrag) return
                callback.addCoord(part, getI(e.x), getJ(e.y))
            }

            override fun mouseMoved(e: MouseEvent) {
                super.mouseMoved(e)
                if (!mouseDrag) return
                callback.addCoord(part, getI(e.x), getJ(e.y))
            }

        })
        addFocusListener(object: FocusAdapter() {
            override fun focusGained(e: FocusEvent) {
                super.focusGained(e)
                callback.gainedFocus(part)
            }

            override fun focusLost(e: FocusEvent) {
                super.focusLost(e)
                callback.lostFocus(part)
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

    override fun paintComponent(p0: Graphics) {
        super.paintComponent(p0)
        paintGrid(p0)
        p0.color = Color.BLACK
        val grid = getGrid()
        for (i in 0 until maxi) {
            for (j in 0 until maxj) {
                val range = grid[i, j]
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

    fun paintGrid(p0: Graphics) {
        if (p0 !is Graphics2D) return
        val dx = settings.dx
        val dy = settings.dy
        p0.color = Color.GRAY
        for (i in 0..maxi) {
            p0.drawLine(i * dx,  0, i * dx, maxj * dy)
        }
        for (j in 0..maxj) {
            p0.drawLine(0, (maxj - j) * dy, maxi * dx, (maxj - j) * dy)
        }
        p0.color = Color.BLACK
        for (i in 0..maxi) {
            if (i % settings.groupx != 0) continue
            p0.drawLine(i * dx,  0, i * dx, maxj * dy)
        }
        for (j in 0..maxj) {
            if (j % settings.groupy != 0) continue
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

        if (!model.selection.empty) {
            p.color = Color.ORANGE
            p.stroke = BasicStroke(3.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 0.0f, floatArrayOf(9.0f), if (cursorState) 0.0f else 4.5f);
            val bl = model.selection.bottomLeft()
            val tr = model.selection.topRight()
            val w = tr.i - bl.i + 1
            val h = tr.j - bl.j + 1
            p.drawRect(bl.i * settings.dx, (maxj - tr.j - 1) * settings.dy, w * settings.dx, h * settings.dy)
        }
        p.color = if (cursorState) Color.RED else Color.ORANGE
        p.stroke = BasicStroke(3.0f)
        p.drawRect(model.selection.pos.i * settings.dx, (maxj - model.selection.pos.j - 1) * settings.dy, settings.dx, settings.dy)
        p.dispose()
    }

    fun toggleCursorState() {
        cursorState = !cursorState
        repaint()
    }
}
