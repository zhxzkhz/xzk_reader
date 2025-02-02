package com.zhhz.reader.ui.book.entities

import com.zhhz.reader.ui.book.ReadProvider
import com.zhhz.reader.ui.book.textHeight

data class TextPage(
    var index: Int = 0,
    var text: String = "加载中…",
    var title: String = "",
    private val textLines: ArrayList<TextLine> = arrayListOf(),
    var pageSize: Int = 0,
    var height: Float = 0f,
) {
    val lines: List<TextLine> get() = textLines
    val lineSize: Int get() = textLines.size
    val charSize: Int get() = text.length

    fun addLine(line: TextLine) {
        textLines.add(line)
    }

    /**
     * 更新每行位置，保持底部对齐
     */
    fun updateLinesPosition() {
        if (textLines.size <= 1) return
        ReadProvider.run {
            val lastLine = textLines[lineSize - 1]
            val lastLineHeight = with(lastLine) { lineBottom - lineTop }
            val pageHeight = lastLine.lineBottom + textPaint.textHeight * lineHeightRatio
            if (visibleHeight - pageHeight >= lastLineHeight) return@run
            //获取剩余可见空间
            val surplus = (visibleBottom - lastLine.lineBottom)
            if (surplus == 0f) return@run
            height += surplus
            val tj = surplus / (lineSize - 1)
            for (i in 1 until lineSize) {
                val line = textLines[i]
                line.lineTop += tj * i
                line.lineBase += tj * i
                line.lineBottom += tj * i
            }
        }
    }

    fun getLine(index: Int): TextLine {
        return textLines.getOrElse(index) {
            textLines.last()
        }
    }

    override fun toString(): String {
        return "TextPage(index=$index, text='$text', pageSize=$pageSize, height=$height, lineSize=$lineSize, charSize=$charSize)"
    }


}
