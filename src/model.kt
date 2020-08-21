import kotlin.math.max
import kotlin.math.min

data class Coord(var i: Int, var j: Int)

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

class Threading(width: Int) {
    private val data = IntArray(width) { -1 }

    val selection = Selection()

    val width: Int
        get() = this.data.size

    operator fun get(i: Int): Int {
        return data[i]
    }

    operator fun set(i: Int, value: Int) {
        if (data[i] == value) {
            data[i] = -1
        } else {
            data[i] = value
        }
    }

    fun clear() {
        for (i in 0 until width) data[i] = -1
    }
}

class Tieup() {
    private val data = LongArray(Long.SIZE_BITS)

    val selection = Selection()

    val width: Int
        get() = Long.SIZE_BITS

    val height: Int
        get() = Long.SIZE_BITS

    fun column(i: Int): Long {
        return data[i]
    }

    fun row(j: Int): Long {
        var result = 0L;
        for (i in 0 until width) {
            if (this[i, j]) {
                result = result or (1L shl i)
            }
        }
        return result
    }

    operator fun get(i: Int, j: Int): Boolean {
        val value = data[i]
        return (value and (1L shl j)) != 0L
    }

    operator fun set(i: Int, j: Int, set: Boolean) {
        if (set) {
            data[i] = data[i] or (1L shl j)
        } else {
            data[i] = data[i] and (1L shl j).inv()
        }
    }

    fun clear() {
        for (i in 0 until width) data[i] = 0L
    }
}

class Treadling(height: Int) {
    private val data = LongArray(height)

    val selection = Selection()

    val width: Int
        get() = Long.SIZE_BITS

    val height: Int
        get() = data.size

    fun row(j: Int): Long {
        return data[j]
    }

    fun setRow(j: Int, value: Long) {
        data[j] = value
    }

    operator fun get(i: Int, j: Int): Boolean {
        val value = data[j]
        return (value and (1L shl i)) != 0L
    }

    operator fun set(i: Int, j: Int, set: Boolean) {
        if (set) {
            data[j] = data[j] or (1L shl i)
        } else {
            data[j] = data[j] and (1L shl i).inv()
        }
    }

    fun clear() {
        for (j in 0 until height) data[j] = 0L
    }
}

class Pegplan(height: Int) {
    private val data = LongArray(height)

    val selection = Selection()

    val width: Int
        get() = Long.SIZE_BITS

    val height: Int
        get() = data.size

    fun row(j: Int): Long {
        return data[j]
    }

    fun setRow(j: Int, value: Long) {
        data[j] = value
    }

    operator fun get(i: Int, j: Int): Boolean {
        val value = data[j]
        return (value and (1L shl i)) != 0L
    }

    operator fun set(i: Int, j: Int, set: Boolean) {
        if (set) {
            data[j] = data[j] or (1L shl i)
        } else {
            data[j] = data[j] and (1L shl i).inv()
        }
    }

    fun clear() {
        for (j in 0 until height) data[j] = 0L
    }
}

class Pattern(var width: Int, var height: Int) {
    private val data = ByteArray(width * height)

    val selection = Selection()

    private fun index(i: Int, j: Int): Int {
        return i +  j * width
    }

    operator fun get(i: Int, j: Int): Boolean {
        return data[index(i, j)] != 0.toByte()
    }

    operator fun set(i: Int, j: Int, set: Boolean) {
        val idx = index(i, j)
        if (set) {
            data[idx] = 1.toByte()
        } else {
            data[idx] = 0.toByte()
        }
    }

    fun isSameRow(j1: Int, j2: Int): Boolean {
        for (i in 0 until width) {
            if (this[i, j1] != this[i, j2]) return false
        }
        return true
    }

    fun isSameColumn(i1: Int, i2: Int): Boolean {
        for (j in 0 until height) {
            if (this[i1, j] != this[i2, j]) return false
        }
        return true
    }

    fun isEmptyRow(j: Int): Boolean {
        for (i in 0 until width) {
            if (this[i, j]) return false
        }
        return true
    }

    fun isEmptyColumn(i: Int): Boolean {
        for (j in 0 until height) {
            if (this[i, j]) return false
        }
        return true
    }

    fun clear() {
        for (idx in 0 until width * height) data[idx] = 0
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
    val threading = Threading(width)
    val tieup = Tieup()
    val treadling = Treadling(height)
    val pegplan = Pegplan(height)

    var pattern = Pattern(width, height)

    var warp_range = ThreadRange(0, 0)
    var weft_range = ThreadRange(0, 0)

    fun updateRange() {
        warp_range = get_warp_range()
        weft_range = get_weft_range()
    }

    fun get_warp_range(): ThreadRange {
        var start = Int.MAX_VALUE
        var end = 0
        for (i in 0 until threading.width) {
            if (threading[i] == -1) continue
            start = min(start, i)
            end = max(end, i + 1)
        }
        return ThreadRange(start, end)
    }

    fun get_weft_range(): ThreadRange {
        var start = Int.MAX_VALUE
        var end = 0
        for (j in 0 until treadling.height) {
            if (treadling.row(j) == 0L) continue
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
                val threading = threading[i]
                if (threading == -1) continue
                for (m in 0 until treadling.width) {
                    if (!treadling[m, j]) continue
                    if (tieup[m, threading]) {
                        pattern[i, j] = true
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
                    threading[i] = threading[ii]
                    found = true
                    break
                }
            }
            if (!found) {
                threading[i] = next_heddle++
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
                    treadling.setRow(j, treadling.row(jj))
                    found = true
                    break
                }
            }
            if (!found) {
                treadling[next_tread++, j] = true
                // todo handle tread overflow?!
            }
        }

    }

    fun recalcTieup() {
        tieup.clear()
        for (i in 0 until tieup.width) {
            for (j in 0 until tieup.height) {
                for (ii in 0 until threading.width) {
                    if (threading[ii] != j) continue
                    for (jj in 0 until treadling.height) {
                        if (!treadling[i, jj]) continue
                        tieup[i, j] = pattern[ii, jj]
                        break
                    }
                    break
                }
            }
        }
    }

}
