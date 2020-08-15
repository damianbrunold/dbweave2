import javax.swing.JFrame
import java.awt.EventQueue
import java.awt.Rectangle
import java.awt.event.*

interface UICallback {
    fun toggleThreading(i: Int, j: Int)
    fun toggleTieup(i: Int, j: Int)
    fun toggleTreadling(i: Int, j: Int)
    fun togglePattern(i: Int, j: Int)
}

class Dbweave(title: String) : JFrame() {

    val settings = ViewSettings()

    val model = Model(500, 500)

    init {
        for (i in 0 until model.threading.size) {
            model.threading[i, i % 8] = true
        }
        for (j in 0 until model.treadling.size) {
            model.treadling[j % 8, j] = true
        }
        for (i in 0 until 8) {
            model.tieup[i, i] = true
            model.tieup[i, (i + 1) % 8] = true
            model.tieup[i, (i + 2) % 8] = true
        }
    }

    val callback: UICallback = object : UICallback {
        override fun toggleThreading(i: Int, j: Int) {
            model.threading[i, j] = model.threading[i] != j
            threadingView.invalidate()
            threadingView.repaint()
            patternView.invalidate()
            patternView.repaint()
        }

        override fun toggleTieup(i: Int, j: Int) {
            model.tieup[i, j] = !model.tieup[i, j]
            tieupView.invalidate()
            tieupView.repaint()
            patternView.invalidate()
            patternView.repaint()
        }

        override fun toggleTreadling(i: Int, j: Int) {
            model.treadling[i, j] = !model.treadling[i, j]
            treadlingView.invalidate()
            treadlingView.repaint()
            patternView.invalidate()
            patternView.repaint()
        }

        override fun togglePattern(i: Int, j: Int) {
            // TODO
        }
    }

    val threadingView = ThreadingView(model.threading, callback, settings, VerticalPainter())
    val tieupView = TieupView(model.tieup, callback, settings, CrossPainter())
    val treadlingView = TreadlingView(model.treadling, callback, settings, DotPainter())
    val patternView = PatternView(model, callback, settings, FillPainter())

    init {
        createUI(title)
    }

    private fun createUI(title: String) {
        setTitle(title)

        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(1024, 768)
        setLocationRelativeTo(null)

        layout = null

        add(threadingView)
        add(tieupView)
        add(patternView)
        add(treadlingView)

        arrangeComponents()

        addWindowListener(object : WindowAdapter() {
            // TODO
        })

        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                super.componentResized(e)
                arrangeComponents()
            }
        })
    }

    private fun arrangeComponents() {
        val xx = width - (settings.treadling_visible + 1) * settings.dx
        val yy = (settings.threading_visible + 1) * settings.dy
        threadingView.bounds = Rectangle(0, 0, xx - settings.dx + 1, settings.threading_visible * settings.dy + 1)
        tieupView.bounds = Rectangle(xx, 0, settings.treadling_visible * settings.dx + 1, settings.threading_visible * settings.dy + 1)
        treadlingView.bounds = Rectangle(xx, yy, settings.treadling_visible * settings.dx + 1, height - yy - settings.dy + 1)
        patternView.bounds = Rectangle(0, yy, xx - settings.dx + 1, height - yy - settings.dy + 1)
    }
}

private fun createAndShowGUI() {
    val frame = Dbweave("DB-WEAVE")
    frame.isVisible = true
}

fun main() {
    EventQueue.invokeLater(::createAndShowGUI)
}