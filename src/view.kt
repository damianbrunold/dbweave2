import java.awt.Color
import java.awt.Graphics
import javax.swing.JComponent

class ViewSettings {
    var dx = 12
    var dy = 12
}

class ThreadingView(val data: Grid, val settings: ViewSettings): JComponent() {
    override fun paintComponent(p0: Graphics) {
        val dx = settings.dx
        val dy = settings.dy
        p0.color = Color.BLACK
        for (i in 0..data.size) {
            p0.drawLine(i * dx, height - 0, i * dx, height - 64 * dy)
        }
        for (i in 0..64) {
            p0.drawLine(0, height - i * dy, data.size * dx, height - i * dy)
        }
        for (i in 0 until data.size) {
            for (j in 0 until 64) {
                if (data[i, j]) {
                    p0.fillRect(i * dx, height - (j + 1) * dy, dx, dy)
                }
            }
        }
    }
}