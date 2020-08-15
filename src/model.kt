class Grid(size: Int) {
    val data = LongArray(size)

    val size: Int
        get() = this.data.size

    operator fun get(primary: Int, secondary: Int): Boolean {
        val value = data[primary]
        return (value and (1L shl secondary)) != 0L
    }

    operator fun set(primary: Int, secondary: Int, set: Boolean) {
        if (set) {
            data[primary] = data[primary] or (1L shl secondary)
        } else {
            data[primary] = data[primary] and (1L shl secondary).inv()
        }
    }
}

class Model(width: Int, height: Int) {
    val threading = Grid(width)
    val tieup = Grid(width)
    val treadling = Grid(height)
    val pegplan = Grid(height)
}
