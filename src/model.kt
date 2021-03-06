import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

enum class Part {
    THREADING,
    TIEUP,
    TREADLING,
    PATTERN,
    WARP_COLORS,
    WEFT_COLORS
}

data class Coord(var i: Int, var j: Int)

data class CursorPos(var cursorLeft: Int = 0, var cursorRight: Int = 0, var cursorTop: Int = 0, var cursorBottom: Int = 0)

class Selection {
    var part = Part.PATTERN
    var orig = Coord(0, 0)
    var pos = Coord(0, 0)

    val empty: Boolean
        get() = orig == pos

    val width: Int
        get() = abs(pos.i - orig.i + 1)

    val height: Int
        get() = abs(pos.j - orig.j + 1)


    fun bottomLeft() = Coord(min(orig.i, pos.i), min(orig.j, pos.j))
    fun topRight() = Coord(max(orig.i, pos.i), max(orig.j, pos.j))

    fun setLocation(part_: Part, i: Int, j: Int) {
        part = part_
        orig = Coord(i, j)
        pos = Coord(i, j)
    }

    fun addLocation(part_: Part, i: Int, j: Int) {
        if (part != part_) {
            part = part_
            orig = Coord(i, j)
        }
        pos.i = i
        pos.j = j
    }

    override fun toString(): String {
        return "Selection(part=$part, orig=$orig, pos=$pos)"
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

    val pattern = Grid(width, height)

    val warpColors = Grid(width, 1, 2)
    val weftColors = Grid(1, height, 15)

    var warpRange = ThreadRange(0, 0)
    var weftRange = ThreadRange(0, 0)

    val selection = Selection()
    val cursorPos = CursorPos()

    fun updateRange() {
        warpRange = calcWarpRange()
        weftRange = calcWeftRange()
    }

    fun calcWarpRange(): ThreadRange {
        var start = Int.MAX_VALUE
        var end = 0
        for (i in 0 until threading.width) {
            if (threading.isEmptyColumn(i)) continue
            start = min(start, i)
            end = max(end, i + 1)
        }
        return ThreadRange(start, end)
    }

    fun calcWeftRange(): ThreadRange {
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
