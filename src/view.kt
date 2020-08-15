import java.awt.Color
import java.awt.Graphics
import javax.swing.JComponent

class ViewSettings {
    var dx = 12
    var dy = 12
    var threading_visible = 12
    var treadling_visible = 12
}

class ThreadingView(val data: SingleGrid, val settings: ViewSettings): JComponent() {
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
        for (i in 0 until data.size) {
            val j = data[i]
            if (j == -1) continue
            p0.fillRect(i * dx, height - 1 - (j + 1) * dy, dx, dy)
        }
    }
}

class TreadlingView(val data: Grid, val settings: ViewSettings): JComponent() {
    override fun paintComponent(p0: Graphics) {
        val dx = settings.dx
        val dy = settings.dy
        val treadling_visible = settings.treadling_visible
        p0.color = Color.GREEN
        for (i in 0..treadling_visible) {
            p0.drawLine(i * dx, height - 1 - 0, i * dx, height - 1 - data.size * dy)
        }
        for (j in 0..data.size) {
            p0.drawLine(0, height - 1 - j * dy, treadling_visible * dx, height - 1 - j * dy)
        }
        for (i in 0 until treadling_visible) {
            for (j in 0 until data.size) {
                if (data[i, j]) {
                    p0.fillRect(i * dx, height - 1 - (j + 1) * dy, dx, dy)
                }
            }
        }
    }
}

class TieupView(val data: Grid, val settings: ViewSettings): JComponent() {
    override fun paintComponent(p0: Graphics) {
        val dx = settings.dx
        val dy = settings.dy
        val threading_visible = settings.threading_visible
        val treadling_visible = settings.treadling_visible
        p0.color = Color.RED
        for (i in 0..treadling_visible) {
            p0.drawLine(i * dx, height - 1 - 0, i * dx, height - 1 - threading_visible * dy)
        }
        for (i in 0..threading_visible) {
            p0.drawLine(0, height - 1 - i * dy, treadling_visible * dx, height - 1 - i * dy)
        }
        for (i in 0 until treadling_visible) {
            for (j in 0 until threading_visible) {
                if (data[i, j]) {
                    p0.fillRect(i * dx, height - 1 - (j + 1) * dy, dx, dy)
                }
            }
        }
    }
}