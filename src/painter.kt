import java.awt.BasicStroke
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Rectangle
import kotlin.math.PI

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

class SmallCrossPainter : Painter {
    override fun paintCell(p0: Graphics, bounds: Rectangle) {
        with(bounds) {
            p0.drawLine(x + (width + 1) / 3, y + (height + 1) / 3, x + (width + 1) * 2 / 3, y + (height + 1) * 2 / 3)
            p0.drawLine(x + (width + 1) / 3, y + (height + 1) * 2 / 3, x + (width + 1) * 2 / 3, y + (height + 1) / 3)
        }
    }
}

class CirclePainter : Painter {
    override fun paintCell(p0: Graphics, bounds: Rectangle) {
        with(bounds) {
            p0.drawOval(x + 2, y + 2, width - 4, height - 4)
        }
    }
}

class SmallCirclePainter : Painter {
    override fun paintCell(p0: Graphics, bounds: Rectangle) {
        with(bounds) {
            p0.drawOval(x + width / 3, y + height / 3, width / 3 + 1, height / 3 + 1)
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
