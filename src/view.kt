import java.awt.Color
import java.awt.Graphics
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.JComponent

class ViewSettings {
    var dx = 12
    var dy = 12
    var threading_visible = 12
    var treadling_visible = 12
}

interface Painter {
    fun paintCell(p0: Graphics, x: Int, y: Int, width: Int, height: Int)
}

class FillPainter : Painter {
    override fun paintCell(p0: Graphics, x: Int, y: Int, width: Int, height: Int) {
        p0.fillRect(x + 2, y + 2, width - 3, height - 3)
    }
}

class CrossPainter : Painter {
    override fun paintCell(p0: Graphics, x: Int, y: Int, width: Int, height: Int) {
        p0.drawLine(x + 2, y + 2, x + width - 2, y + height - 2)
        p0.drawLine(x + 2, y + height - 2, x + width - 2, y + 2)
    }
}

class VerticalPainter : Painter {
    override fun paintCell(p0: Graphics, x: Int, y: Int, width: Int, height: Int) {
        p0.drawLine(x + width / 2 - 1, y + 2, x + width / 2 - 1, y + height - 2)
        p0.drawLine(x + width / 2, y + 2, x + width / 2, y + height - 2)
        p0.drawLine(x + width / 2 + 1, y + 2, x + width / 2 + 1, y + height - 2)
    }
}

class DotPainter : Painter {
    override fun paintCell(p0: Graphics, x: Int, y: Int, width: Int, height: Int) {
        p0.drawLine(x + width / 2 - 1, y + height / 2 - 1, x + width / 2 + 1, y + height / 2 - 1)
        p0.drawLine(x + width / 2 - 1, y + height / 2 + 0, x + width / 2 + 1, y + height / 2 + 0)
        p0.drawLine(x + width / 2 - 1, y + height / 2 + 1, x + width / 2 + 1, y + height / 2 + 1)
    }
}

class ThreadingView(val data: SingleGrid, val callback: UICallback, val settings: ViewSettings, val painter: Painter): JComponent() {
    init {
        border = BorderFactory.createLineBorder(Color.BLACK)
        addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent) {
                val i = e.x / settings.dx
                val j = (height - e.y) / settings.dy
                callback.toggleThreading(i, j)
            }
        })
    }

    override fun paintComponent(p0: Graphics) {
        val dx = settings.dx
        val dy = settings.dy
        val threading_visible = settings.threading_visible
        p0.color = Color.BLACK
        for (i in 0..data.size) {
            p0.drawLine(i * dx,  height - 1 - 0, i * dx, height - 1 - threading_visible * dy)
        }
        for (j in 0..threading_visible) {
            p0.drawLine(0, height - 1 - j * dy, data.size * dx, height - 1 - j * dy)
        }
        p0.color = Color.BLACK
        for (i in 0 until data.size) {
            val j = data[i]
            if (j == -1) continue
            painter.paintCell(p0, i * dx, height - 1 - (j + 1) * dy, dx, dy)
        }
    }
}

class TreadlingView(val data: Grid, val callback: UICallback, val settings: ViewSettings, val painter: Painter): JComponent() {
    init {
        border = BorderFactory.createLineBorder(Color.BLACK)
        addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent) {
                val i = e.x / settings.dx
                val j = (height - e.y) / settings.dy
                callback.toggleTreadling(i, j)
            }
        })
    }

    override fun paintComponent(p0: Graphics) {
        val dx = settings.dx
        val dy = settings.dy
        val treadling_visible = settings.treadling_visible
        p0.color = Color.BLACK
        for (i in 0..treadling_visible) {
            p0.drawLine(i * dx, height - 1 - 0, i * dx, height - 1 - data.size * dy)
        }
        for (j in 0..data.size) {
            p0.drawLine(0, height - 1 - j * dy, treadling_visible * dx, height - 1 - j * dy)
        }
        p0.color = Color.BLACK
        for (i in 0 until treadling_visible) {
            for (j in 0 until data.size) {
                if (data[i, j]) {
                    painter.paintCell(p0, i * dx, height - 1 - (j + 1) * dy, dx, dy)
                }
            }
        }
    }
}

class TieupView(val data: Grid, val callback: UICallback, val settings: ViewSettings, val painter: Painter): JComponent() {
    init {
        border = BorderFactory.createLineBorder(Color.BLACK)
        addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent) {
                val i = e.x / settings.dx
                val j = (height - e.y) / settings.dy
                callback.toggleTieup(i, j)
            }
        })
    }

    override fun paintComponent(p0: Graphics) {
        val dx = settings.dx
        val dy = settings.dy
        val threading_visible = settings.threading_visible
        val treadling_visible = settings.treadling_visible
        p0.color = Color.BLACK
        for (i in 0..treadling_visible) {
            p0.drawLine(i * dx, height - 1 - 0, i * dx, height - 1 - threading_visible * dy)
        }
        for (i in 0..threading_visible) {
            p0.drawLine(0, height - 1 - i * dy, treadling_visible * dx, height - 1 - i * dy)
        }
        p0.color = Color.BLACK
        for (i in 0 until treadling_visible) {
            for (j in 0 until threading_visible) {
                if (data[i, j]) {
                    painter.paintCell(p0, i * dx, height - 1 - (j + 1) * dy, dx, dy)
                }
            }
        }
    }
}

class PatternView(val model: Model, val callback: UICallback, val settings: ViewSettings, val painter: Painter): JComponent() {
    init {
        border = BorderFactory.createLineBorder(Color.BLACK)
        addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent) {
                val i = e.x / settings.dx
                val j = (height - e.y) / settings.dy
                callback.togglePattern(i, j)
            }
        })
    }

    override fun paintComponent(p0: Graphics) {
        val dx = settings.dx
        val dy = settings.dy
        p0.color = Color.BLACK
        for (i in 0..model.threading.size) {
            p0.drawLine(i * dx, height - 1 - 0, i * dx, height - 1 -  model.treadling.size * dy)
        }
        for (j in 0..model.treadling.size) {
            p0.drawLine(0, height - 1 - j * dy, model.threading.size * dx, height - 1 - j * dy)
        }
        for (i in 0 until model.threading.size) {
            for (j in 0 until model.treadling.size) {
                val threading = model.threading[i]
                if (threading == -1) continue
                for (m in 0 until model.treadling.size) {
                    if (!model.treadling[m, j]) continue
                    if (model.tieup[m, threading]) {
                        painter.paintCell(p0, i * dx, height - 1 - (j + 1) * dy, dx, dy)
                        break
                    }
                }
            }
        }
    }
}