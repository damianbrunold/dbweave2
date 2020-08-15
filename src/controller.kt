import javax.swing.JFrame
import java.awt.EventQueue
import java.awt.GridLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.JLabel

class Dbweave(title: String) : JFrame() {

    init {
        createUI(title)
    }

    private fun createUI(title: String) {

        setTitle(title)

        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(1024, 768)
        setLocationRelativeTo(null)

        layout = GridLayout(2, 2)

        val settings = ViewSettings()

        val model = Model(50, 50)

        val threadingView = ThreadingView(model.threading, settings)
        add(threadingView)

        val tieupView = TieupView(model.tieup, settings)
        add(tieupView)

        val patternView = JLabel("Pattern")
        add(patternView)

        val treadlingView = TreadlingView(model.treadling, settings)
        add(treadlingView)

        threadingView.addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent) {
                val i = e.x / settings.dx
                val j = (threadingView.height - e.y) / settings.dy
                if (model.threading[i] == j) model.threading[i, j] = false
                else model.threading[i, j] = true
                threadingView.invalidate()
                threadingView.repaint()
            }
        })

        treadlingView.addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent) {
                val i = e.x / settings.dx
                val j = (treadlingView.height - e.y) / settings.dy
                model.treadling[i, j] = !model.treadling[i, j]
                treadlingView.invalidate()
                treadlingView.repaint()
            }
        })

        tieupView.addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent) {
                val i = e.x / settings.dx
                val j = (tieupView.height - e.y) / settings.dy
                model.tieup[i, j] = !model.tieup[i, j]
                tieupView.invalidate()
                tieupView.repaint()
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