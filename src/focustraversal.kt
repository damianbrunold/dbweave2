import java.awt.Component
import java.awt.Container
import java.awt.FocusTraversalPolicy

class DbweaveFocusTraversalPolicy(val threading: ThreadingView, val tieup: TieupView, val pattern: PatternView, val treadling: TreadlingView)
    : FocusTraversalPolicy() {

    override fun getComponentAfter(aContainer: Container?, aComponent: Component?): Component {
        if (aComponent == pattern) return threading
        if (aComponent == threading) return treadling
        if (aComponent == treadling) return tieup
        if (aComponent == tieup) return pattern
        // TODO add color and other components
        return pattern
    }

    override fun getComponentBefore(aContainer: Container?, aComponent: Component?): Component {
        if (aComponent == pattern) return tieup
        if (aComponent == tieup) return treadling
        if (aComponent == treadling) return threading
        if (aComponent == threading) return pattern
        // TODO add color and other components
        return pattern
    }

    override fun getFirstComponent(aContainer: Container?): Component {
        return pattern
    }

    override fun getLastComponent(aContainer: Container?): Component {
        return pattern
    }

    override fun getDefaultComponent(aContainer: Container?): Component {
        return pattern
    }
}
