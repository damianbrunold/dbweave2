import java.awt.Color
import java.awt.Graphics
import java.awt.Rectangle
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent

class ViewSettings {
    var dx = 12
    var dy = 12
    var threadingVisible = 12
    var treadlingVisible = 12
}

interface Painter {
    fun paintCell(p0: Graphics, bounds: Rectangle)
}

class FillPainter : Painter {
    override fun paintCell(p0: Graphics, bounds: Rectangle) {
        with (bounds) {
            p0.fillRect(x + 2, y + 2, width - 3, height - 3)
        }
    }
}

class CrossPainter : Painter {
    override fun paintCell(p0: Graphics, bounds: Rectangle) {
        with(bounds) {
            p0.drawLine(x + 2, y + 2, x + width - 2, y + height - 2)
            p0.drawLine(x + 2, y + height - 2, x + width - 2, y + 2)
        }
    }
}

class VerticalPainter : Painter {
    override fun paintCell(p0: Graphics, bounds: Rectangle) {
        with(bounds) {
            p0.drawLine(x + width / 2 - 1, y + 2, x + width / 2 - 1, y + height - 2)
            p0.drawLine(x + width / 2, y + 2, x + width / 2, y + height - 2)
            p0.drawLine(x + width / 2 + 1, y + 2, x + width / 2 + 1, y + height - 2)
        }
    }
}

class DotPainter : Painter {
    override fun paintCell(p0: Graphics, bounds: Rectangle) {
        with (bounds) {
            p0.drawLine(x + width / 2 - 1, y + height / 2 - 1, x + width / 2 + 1, y + height / 2 - 1)
            p0.drawLine(x + width / 2 - 1, y + height / 2 + 0, x + width / 2 + 1, y + height / 2 + 0)
            p0.drawLine(x + width / 2 - 1, y + height / 2 + 1, x + width / 2 + 1, y + height / 2 + 1)
        }
    }
}

abstract class BaseView(val settings: ViewSettings) : JComponent() {
    var maxi: Int = 10
    var maxj: Int = 10

    fun updateMax(i: Int, j: Int) {
        maxi = i
        maxj = j
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
}

class ThreadingView(val threading: Threading, val callback: UICallback, settings: ViewSettings, val painter: Painter): BaseView(settings) {
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
    }

    override fun paintComponent(p0: Graphics) {
        paintGrid(p0)
        p0.color = Color.DARK_GRAY
        for (i in 0 until maxi) {
            val j = threading[i]
            if (j == -1) continue
            painter.paintCell(p0, cellBounds(i, j))
        }
    }
}

class TreadlingView(val treadling: Treadling, val callback: UICallback, settings: ViewSettings, val painter: Painter): BaseView(settings) {
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
    }

    override fun paintComponent(p0: Graphics) {
        paintGrid(p0)
        p0.color = Color.DARK_GRAY
        for (i in 0 until maxi) {
            for (j in 0 until maxj) {
                if (treadling[i, j]) {
                    painter.paintCell(p0, cellBounds(i, j))
                }
            }
        }
    }
}

class TieupView(val tieup: Tieup, val callback: UICallback, settings: ViewSettings, val painter: Painter): BaseView(settings) {
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
    }

    override fun paintComponent(p0: Graphics) {
        paintGrid(p0)
        p0.color = Color.BLACK
        for (i in 0 until maxi) {
            for (j in 0 until maxj) {
                if (tieup[i, j]) {
                    painter.paintCell(p0, cellBounds(i, j))
                }
            }
        }
    }
}

class PatternView(val pattern: Pattern, val callback: UICallback, settings: ViewSettings, val painter: Painter): BaseView(settings) {
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
    }

    override fun paintComponent(p0: Graphics) {
        paintGrid(p0)
        p0.color = Color.DARK_GRAY
        for (i in 0 until maxi) {
            for (j in 0 until maxj) {
                if (pattern[i, j]) {
                    painter.paintCell(p0, cellBounds(i, j))
                }
            }
        }
    }
}