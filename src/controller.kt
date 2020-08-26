import javax.swing.JFrame
import java.awt.EventQueue
import java.awt.Rectangle
import java.awt.event.*
import javax.swing.Timer
import javax.swing.UIManager

interface UICallback {
    fun toggleThreading(i: Int, j: Int)
    fun toggleTieup(i: Int, j: Int)
    fun toggleTreadling(i: Int, j: Int)
    fun togglePattern(i: Int, j: Int)
}

class Dbweave(title: String) : JFrame() {

    val settings = ViewSettings()

    val model = Model(500, 500)

    var activeRange = 1.toByte()

    init {
//        for (i in 0 until 70) {
//            model.threading[i, i % 8] = 1
//        }
//        for (j in 0 until 40) {
//            model.treadling[j % 8, j] = 1
//        }
//        for (i in 0 until 8) {
//            model.tieup[i, i] = 1
//            model.tieup[i, (i + 1) % 8] = 1
//            model.tieup[i, (i + 2) % 8] = 1
//        }
//        model.updateRange()
//        model.recalcPattern()
    }

    val callback: UICallback = object : UICallback {
        override fun toggleThreading(i: Int, j: Int) {
            model.threading.setSingleInColumn(i, j, 1)
            model.selection.setLocation(i, j)
            // TODO update cursorpos?
            threadingView.repaint()
            model.updateRange()
            model.recalcPattern()
            patternView.repaint()
            println("warp " + model.warp_range)
            println("weft " + model.weft_range)
        }

        override fun toggleTieup(i: Int, j: Int) {
            model.tieup[i, j] = if (model.tieup[i, j] <= 0.toByte()) activeRange else 0
            model.selection.setLocation(i, j)
            // TODO update cursorpos?
            tieupView.repaint()
            model.updateRange()
            model.recalcPattern()
            patternView.repaint()
            println("warp " + model.warp_range)
            println("weft " + model.weft_range)
        }

        override fun toggleTreadling(i: Int, j: Int) {
            model.treadling[i, j] = if (model.treadling[i, j] <= 0.toByte()) 1 else 0
            model.selection.setLocation(i, j)
            // TODO update cursorpos?
            treadlingView.repaint()
            model.recalcPattern()
            patternView.repaint()
            println("warp " + model.warp_range)
            println("weft " + model.weft_range)
        }

        override fun togglePattern(i: Int, j: Int) {
            model.pattern[i, j] = if (model.pattern[i, j] <= 0.toByte()) activeRange else 0
            model.selection.setLocation(i, j)
            // TODO update cursorpos?
            patternView.repaint()
            model.recalcFromPattern()
            threadingView.repaint()
            tieupView.repaint()
            treadlingView.repaint()
            println("warp " + model.warp_range)
            println("weft " + model.weft_range)
        }
    }

    val threadingView = ThreadingView(model, callback, settings, VerticalPainter())
    val tieupView = TieupView(model, callback, settings, CrossPainter())
    val treadlingView = TreadlingView(model, callback, settings, DotPainter())
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

        threadingView.updateSize(model.threading.width, model.threading.height)
        tieupView.updateSize(model.tieup.width, model.tieup.height)
        treadlingView.updateSize(model.treadling.width, model.treadling.height)
        patternView.updateSize(model.pattern.width, model.pattern.height)

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

        Timer(500, {
            if (focusOwner is BaseView) {
                (focusOwner as BaseView).toggleCursorState()
            }
        }).start()

        // TODO do this in general, not only for pattern
        patternView.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent?) {
                super.keyReleased(e)
                if (e == null) return
                // TODO use shift+1, ..., shift+0, ctrl+0, ctrl+shift+0
                if (e.keyCode == KeyEvent.VK_1 || e.keyCode == KeyEvent.VK_NUMPAD1) {
                    activeRange = 1
                    println("activeRange = $activeRange")
                } else if (e.keyCode == KeyEvent.VK_2 || e.keyCode == KeyEvent.VK_NUMPAD2) {
                    activeRange = 2
                    println("activeRange = $activeRange")
                } else if (e.keyCode == KeyEvent.VK_3 || e.keyCode == KeyEvent.VK_NUMPAD3) {
                    activeRange = 3
                    println("activeRange = $activeRange")
                } else if (e.keyCode == KeyEvent.VK_4 || e.keyCode == KeyEvent.VK_NUMPAD4) {
                    activeRange = 4
                    println("activeRange = $activeRange")
                }
                // TODO do all 9 ranges, plus the special ranges
            }
        })

        isFocusCycleRoot = true
        focusTraversalPolicy = DbweaveFocusTraversalPolicy(threadingView, tieupView, patternView, treadlingView)
    }

    private fun arrangeComponents() {
        val border = 2
        val cx = (contentPane.width - 2 * border) / settings.dx
        val cy = (contentPane.height - 2 * border) / settings.dy
        val px = cx - settings.treadlingVisible - 1
        val py = cy - settings.threadingVisible - 1

        val x1 = border
        val w1 = px * settings.dx + 1
        val x2 = x1 + w1 + settings.dx
        val w2 = settings.treadlingVisible * settings.dx + 1

        val y1 = border
        val h1 = settings.threadingVisible * settings.dy + 1
        val y2 = y1 + h1 + settings.dx
        val h2 = py * settings.dy + 1

        threadingView.bounds = Rectangle(x1, y1, w1, h1)
        tieupView.bounds = Rectangle(x2, y1, w2, h1)
        treadlingView.bounds = Rectangle(x2, y2, w2, h2)
        patternView.bounds = Rectangle(x1, y2, w1, h2)

        threadingView.updateMax(px, settings.threadingVisible)
        tieupView.updateMax(settings.treadlingVisible, settings.threadingVisible)
        treadlingView.updateMax(settings.treadlingVisible, py)
        patternView.updateMax(px, py)

        model.updateRange()
    }
}

private fun createAndShowGUI() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    val frame = Dbweave("DB-WEAVE")
    frame.isVisible = true
    frame.patternView.requestFocus()
}

fun main() {
    EventQueue.invokeLater(::createAndShowGUI)
}
