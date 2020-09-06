import java.awt.Component
import java.awt.Container
import java.awt.FocusTraversalPolicy

class DbweaveFocusTraversalPolicy(val threading: GridView, val tieup: GridView,
                                  val pattern: GridView, val treadling: GridView,
                                  val warpColors: GridView, val weftColors: GridView)
    : FocusTraversalPolicy() {

    override fun getComponentAfter(aContainer: Container?, aComponent: Component?): Component {
        if (aComponent == pattern) return threading
        if (aComponent == threading) return treadling
        if (aComponent == treadling) return tieup
        if (aComponent == tieup) return warpColors
        if (aComponent == warpColors) return weftColors
        if (aComponent == weftColors) return pattern
        return pattern
    }

    override fun getComponentBefore(aContainer: Container?, aComponent: Component?): Component {
        if (aComponent == pattern) return weftColors
        if (aComponent == weftColors) return warpColors
        if (aComponent == warpColors) return tieup
        if (aComponent == tieup) return treadling
        if (aComponent == treadling) return threading
        if (aComponent == threading) return pattern
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
