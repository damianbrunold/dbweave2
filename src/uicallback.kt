interface UICallback {
    fun gainedFocus(part: Part)
    fun lostFocus(part: Part)

    fun startCoord(part: Part, i: Int, j: Int)
    fun addCoord(part: Part, i: Int, j: Int)
    fun endCoord(part: Part, i: Int, j: Int)

    fun toggle(moveCursor: Boolean = false)
}
