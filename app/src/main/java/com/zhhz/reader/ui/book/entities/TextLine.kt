package com.zhhz.reader.ui.book.entities

import android.text.TextPaint
import com.zhhz.reader.ui.book.ReadProvider
import com.zhhz.reader.ui.book.textHeight

data class TextLine(
    var text: String = "",
    private val textColumns: ArrayList<BaseColumn> = arrayListOf(),
    var lineTop: Float = 0f,
    var lineBase: Float = 0f,
    var lineBottom: Float = 0f,
    var indentWidth: Float = 0f,
    val isTitle: Boolean = false,
    var isParagraphEnd: Boolean = false,
){

    val columns: List<BaseColumn> get() = textColumns
    val charSize: Int get() = textColumns.size
    val lineStart: Float get() = textColumns.firstOrNull()?.start ?: 0f
    val lineEnd: Float get() = textColumns.lastOrNull()?.end ?: 0f

    fun addColumn(column: BaseColumn) {
        textColumns.add(column)
    }

    fun getColumnReverseAt(index: Int): BaseColumn {
        return textColumns[textColumns.lastIndex - index]
    }

    fun updateTopBottom(curY: Float, textPaint: TextPaint) {
        lineTop = ReadProvider.marginTop + curY
        lineBottom = lineTop + textPaint.textHeight
        lineBase = lineBottom - textPaint.fontMetrics.descent
    }

    fun isTouch(x: Float, y: Float): Boolean {
        return y > lineTop
                && y < lineBottom
                && x >= lineStart
                && x <= lineEnd
    }

    fun getColumn(index: Int): BaseColumn {
        return textColumns.getOrElse(index) {
            textColumns.last()
        }
    }

}
