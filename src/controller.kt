import javax.swing.JFrame
import java.awt.EventQueue
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

class Dbweave(title: String) : JFrame() {

    init {
        createUI(title)
    }

    private fun createUI(title: String) {

        setTitle(title)

        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(1024, 768)
        setLocationRelativeTo(null)

        val settings = ViewSettings()
        val threading = Grid(100)
        val threadingView = ThreadingView(threading, settings)
        add(threadingView)

        threadingView.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                val i = e.x / settings.dx
                val j = (threadingView.height - e.y) / settings.dy
                threading[i, j] = !threading[i, j]
                threadingView.invalidate()
                threadingView.repaint()
            }

            override fun mouseEntered(e: MouseEvent) {

            }
        })
    }
}

private fun createAndShowGUI() {
    val frame = Dbweave("DB-WEAVE")
    frame.isVisible = true
}

fun main() {
    EventQueue.invokeLater(::createAndShowGUI)
}