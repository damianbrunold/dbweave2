import kotlin.math.max
import kotlin.math.min

data class Coord(var i: Int, var j: Int)

data class CursorPos(var cursorLeft: Int = 0, var cursorRight: Int = 0, var cursorTop: Int = 0, var cursorBottom: Int = 0)

class Selection {
    var orig = Coord(0, 0)
    var pos = Coord(0, 0)

    val empty: Boolean
        get() = orig == pos

    fun bottomLeft() = Coord(min(orig.i, pos.i), min(orig.j, pos.j))
    fun topRight() = Coord(max(orig.i, pos.i), max(orig.j, pos.j))

    fun setLocation(i: Int, j: Int) {
        orig = Coord(i, j)
        pos = Coord(i, j)
    }

    fun addLocation(i: Int, j: Int) {
        pos.i = i
        pos.j = j
    }

    override fun toString(): String {
        return "Selection(orig=$orig, pos=$pos)"
    }

}

data class ThreadRange(val start: Int, val end: Int) {
    operator fun contains(thread: Int): Boolean {
        return (start <= thread) and (thread < end)
    }

    fun extended(thread: Int): ThreadRange {
        if (thread in this) return this
        return ThreadRange(min(start, thread), max(thread + 1, end))
    }
}

class Model(width: Int, height: Int) {
    val threading = Grid(width, Int.SIZE_BITS)
    val tieup = Grid(Int.SIZE_BITS, Int.SIZE_BITS)
    val treadling = Grid(Int.SIZE_BITS, height)
    val pegplan = Grid(Int.SIZE_BITS, height)

    var pattern = Grid(width, height)

    var warp_range = ThreadRange(0, 0)
    var weft_range = ThreadRange(0, 0)

    val selection = Selection()
    val cursorPos = CursorPos()

    fun updateRange() {
        warp_range = get_warp_range()
        weft_range = get_weft_range()
    }

    fun get_warp_range(): ThreadRange {
        var start = Int.MAX_VALUE
        var end = 0
        for (i in 0 until threading.width) {
            if (threading.isEmptyColumn(i)) continue
            start = min(start, i)
            end = max(end, i + 1)
        }
        return ThreadRange(start, end)
    }

    fun get_weft_range(): ThreadRange {
        var start = Int.MAX_VALUE
        var end = 0
        for (j in 0 until treadling.height) {
            if (treadling.isEmptyRow(j)) continue
            start = min(start, j)
            end = max(end, j + 1)
        }
        return ThreadRange(start, end)
    }

    fun recalcPattern() {
        updateRange()
        pattern.clear()
        for (i in 0 until pattern.width) {
            for (j in 0 until pattern.height) {
                val jj = threading.getFirstInColumn(i)
                if (jj == -1) continue
                for (m in 0 until treadling.width) {
                    if (treadling[m, j] <= 0) continue
                    val value = tieup[m, jj]
                    if (value != 0.toByte()) {
                        pattern[i, j] = value
                        break
                    }
                }
            }
        }
    }

    fun recalcFromPattern() {
        recalcThreading()
        recalcTreadling()
        recalcTieup()
        updateRange()
    }

    fun recalcThreading() {
        threading.clear()
        var next_heddle = 0
        for (i in 0 until pattern.width) {
            if (pattern.isEmptyColumn(i)) continue
            var found = false
            for (ii in 0 until i) {
                if (pattern.isSameColumn(i, ii)) {
                    threading.copyColumn(ii, i)
                    found = true
                    break
                }
            }
            if (!found) {
                threading[i, next_heddle++] = 1
                // todo handle heddle overflow?!
            }
        }
    }

    fun recalcTreadling() {
        treadling.clear()
        var next_tread = 0
        for (j in 0 until pattern.height) {
            if (pattern.isEmptyRow(j)) continue
            var found = false
            for (jj in 0 until j) {
                if (pattern.isSameRow(j, jj)) {
                    treadling.copyRow(jj, j)
                    found = true
                    break
                }
            }
            if (!found) {
                treadling[next_tread++, j] = 1
                // todo handle tread overflow?!
            }
        }

    }

    fun recalcTieup() {
        tieup.clear()
        for (i in 0 until tieup.width) {
            for (j in 0 until tieup.height) {
                for (ii in 0 until threading.width) {
                    if (threading[ii, j] <= 0) continue
                    for (jj in 0 until treadling.height) {
                        if (treadling[i, jj] <= 0) continue
                        tieup[i, j] = pattern[ii, jj]
                        break
                    }
                    break
                }
            }
        }
    }

}
