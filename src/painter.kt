import java.awt.Graphics
import java.awt.Rectangle

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
