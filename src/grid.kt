import kotlin.math.min

class Grid(val width: Int, val height: Int){
    private val data = ByteArray(width * height)

    private fun index(i: Int, j: Int) = i + j * width

    operator fun get(i: Int, j: Int): Byte {
        return data[index(i, j)]
    }

    operator fun set(i: Int, j: Int, value: Byte) {
        data[index(i, j)] = value
    }

    fun clear() {
        for (idx in 0 until width*height) data[idx] = 0
    }

    fun copy(): Grid {
        val result = Grid(width, height)
        for (idx in 0 until width*height) result.data[idx] = data[idx]
        return result
    }

    fun copyFrom(source: Grid) {
        clear()
        for (i in 0 until min(width, source.width)) {
            for (j in 0 until min(height, source.height)) {
                data[index(i, j)] = source[i, j]
            }
        }
    }

    fun isSameColumn(i1: Int, i2: Int): Boolean {
        for (j in 0 until height) {
            val value1 = data[index(i1, j)]
            val value2 = data[index(i2, j)]
            if ((value1 <= 0) and (value2 <= 0)) continue
            if (value1 != value2) return false
        }
        return true
    }

    fun isSameRow(j1: Int, j2: Int): Boolean {
        for (i in 0 until width) {
            val value1 = data[index(i, j1)]
            val value2 = data[index(i, j2)]
            if ((value1 <= 0) and (value2 <= 0)) continue
            if (value1 != value2) return false
        }
        return true
    }

    fun isEmptyColumn(i: Int): Boolean {
        for (j in 0 until height) if (data[index(i, j)] > 0) return false
        return true
    }

    fun isEmptyRow(j: Int): Boolean {
        for (i in 0 until width) if (data[index(i, j)] > 0) return false
        return true
    }

    fun copyColumn(from: Int, to: Int) {
        for (j in 0 until height) data[index(to, j)] = data[index(from, j)]
    }

    fun copyRow(from: Int, to: Int) {
        for (i in 0 until width) data[index(i, to)] = data[index(i, from)]
    }

    fun clearColumn(i: Int) {
        for (j in 0 until height) data[index(i, j)] = 0
    }

    fun clearRow(j: Int) {
        for (i in 0 until width) data[index(i, j)] = 0
    }
}
