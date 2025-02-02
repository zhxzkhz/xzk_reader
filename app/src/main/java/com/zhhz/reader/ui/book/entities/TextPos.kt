package com.zhhz.reader.ui.book.entities

data class TextPos(
    var lineIndex: Int,
    var columnIndex: Int
){
    fun upData(lineIndex: Int, charIndex: Int) {
        this.lineIndex = lineIndex
        this.columnIndex = charIndex
    }

    fun upData(pos: TextPos) {
        lineIndex = pos.lineIndex
        columnIndex = pos.columnIndex
    }

    fun compare(pos: TextPos): Int {
        return when {
            lineIndex < pos.lineIndex -> -2
            lineIndex > pos.lineIndex -> 2
            columnIndex < pos.columnIndex -> -1
            columnIndex > pos.columnIndex -> 1
            else -> 0
        }
    }

}
