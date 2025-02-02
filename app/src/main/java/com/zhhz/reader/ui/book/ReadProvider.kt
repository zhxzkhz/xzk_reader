package com.zhhz.reader.ui.book

import android.graphics.Typeface
import android.text.StaticLayout
import android.text.TextPaint
import com.zhhz.reader.ui.book.entities.TextChapter
import com.zhhz.reader.ui.book.entities.TextColumn
import com.zhhz.reader.ui.book.entities.TextLine
import com.zhhz.reader.ui.book.entities.TextPage
import kotlin.math.pow
import kotlin.math.sqrt

object ReadProvider {
    @JvmStatic
    var viewWidth = 0
        private set

    @JvmStatic
    var viewHeight = 0
        private set

    @JvmStatic
    var marginLeft = 0
        private set

    @JvmStatic
    var marginTop = 0
        private set

    @JvmStatic
    var marginRight = 0
        private set

    @JvmStatic
    var marginBottom = 0
        private set

    @JvmStatic
    var visibleWidth = 0
        private set

    @JvmStatic
    var visibleHeight = 0
        private set

    @JvmStatic
    var visibleBottom = 0
        private set

    @JvmStatic
    private var titleTopSpacing = 0

    @JvmStatic
    private var titleBottomSpacing = 0

    //行高，倍率
    @JvmStatic
    var lineHeightRatio = 1.2f
        private set

    @JvmStatic
    private var paragraphSpacing = 5f

    @JvmStatic
    var typeface: Typeface? = Typeface.DEFAULT
        private set

    @JvmStatic
    var titlePaint: TextPaint = TextPaint()

    @JvmStatic
    var textPaint: TextPaint = TextPaint()

    @JvmStatic
    var density = 2.625f

    init {
        updateStyle()
    }

    suspend fun getTextChapter(
        title: String,
        text: String,
        pos: Int
    ): TextChapter {
        val contents = arrayListOf<String>()
        text.split("\n").forEach { str ->
            val paragraph = str.trim {
                it.code <= 0x20 || it == '　'
            }
            if (paragraph.isNotEmpty()) {
                //添加首行缩进
                contents.add("　　$paragraph")
            }
        }
        val textPages = arrayListOf<TextPage>()
        textPages.add(TextPage())
        var x = marginLeft
        var y = 0f
        val stringBuilder = StringBuilder()
        contents.forEach { content ->
            textParse(x, y, content, textPages, textPaint, stringBuilder).let {
                x = it.first
                y = it.second
            }
        }
        textPages.last().text = stringBuilder.toString()
        textPages.forEachIndexed { index, item ->
            item.index = index
            item.pageSize = textPages.size
            item.title = title
            item.updateLinesPosition()
        }
        return TextChapter(title, pos, textPages)
    }

    private suspend fun textParse(
        x: Int,
        y: Float,
        text: String,
        textPages: ArrayList<TextPage>,
        textPaint: TextPaint,
        sb: StringBuilder,
        isTitle: Boolean = false,
    ): Pair<Int, Float> {
        val layout = ZhLayout(text, textPaint, visibleWidth)
        var curX = x
        var curY = y
        for (lineIndex in 0 until layout.lineCount) {
            val textLine = TextLine(isTitle = isTitle)
            if (curY + textPaint.textHeight > visibleHeight) {
                val textPage = textPages.last()
                textPage.text = sb.toString()
                //增加下一页
                textPages.add(TextPage())
                sb.clear()
                curX = marginLeft
                curY = 0f
            }
            val words =
                text.substring(layout.getLineStart(lineIndex), layout.getLineEnd(lineIndex))
            val desiredWidth = layout.getLineWidth(lineIndex)

            when {
                //第一行并且不是标题
                lineIndex == 0 && layout.lineCount > 1 && !isTitle -> {
                    textLine.text = words
                    addTextToLineFirst(textLine, curX, words.toStringArray(), textPaint, desiredWidth)
                }

                lineIndex == layout.lineCount - 1 -> {
                    //最后一行
                    textLine.text = words
                    textLine.isParagraphEnd = true
                    addCharsToLineNatural(
                        textLine,
                        curX,
                        words.toStringArray(),
                        textPaint,
                        0f,
                        !isTitle && lineIndex == 0
                    )
                }

                else -> {
                    //中间行
                    textLine.text = words
                    addTextToLineMiddle(
                        textLine, curX, words.toStringArray(),
                        textPaint, desiredWidth, 0f
                    )
                }
            }
            sb.append(words)
            if (textLine.isParagraphEnd) {
                sb.append("\n")
            }
            textPages.last().addLine(textLine)
            textLine.updateTopBottom(curY, textPaint)
            curY += textPaint.textHeight * lineHeightRatio
            textPages.last().height = curY
        }
        curY += textPaint.textHeight * 0.3f + paragraphSpacing
        return Pair(curX, curY)
    }

    //首行缩进,两端对齐
    private suspend fun addTextToLineFirst(
        textLine: TextLine,
        x: Int,
        words: Array<String>,
        textPaint: TextPaint,
        desiredWidth: Float,
    ) {
        var x1 = 0f
        val bodyIndent = "　　"
        val icw = StaticLayout.getDesiredWidth(bodyIndent, textPaint) / bodyIndent.length
        for (char in bodyIndent.toStringArray()) {
            val x2 = x1 + icw
            textLine.addColumn(
                TextColumn(
                    charData = char,
                    start = x + x1,
                    end = x + x2
                )
            )
            x1 = x2
            textLine.indentWidth = x1
        }
        if (words.size > bodyIndent.length) {
            val words1 = words.copyOfRange(bodyIndent.length, words.size)
            addTextToLineMiddle(textLine, x, words1, textPaint, desiredWidth, x1)
        }
    }

    //无缩进,两端对齐
    private suspend fun addTextToLineMiddle(
        textLine: TextLine,
        x: Int,
        words: Array<String>,
        textPaint: TextPaint,
        desiredWidth: Float,
        /**起始x坐标**/
        startX: Float,
    ) {
        //获取剩余宽度
        val residualWidth = visibleWidth - desiredWidth
        val gapCount: Int = words.lastIndex
        val spacingWidth = residualWidth / gapCount
        var x1 = startX
        words.forEachIndexed { index, char ->
            val cw = StaticLayout.getDesiredWidth(char, textPaint)
            val x2 = if (index != words.lastIndex) (x1 + cw + spacingWidth) else (x1 + cw)
            addTextToLine(textLine, x, char, x1, x2, index + 1 == words.size)
            x1 = x2
        }
        exceed(x, textLine, words)
    }

    /**
     * 添加字符
     */
    private fun addTextToLine(
        textLine: TextLine,
        x: Int,
        char: String,
        startX: Float,
        endX: Float,
        isLineEnd: Boolean
    ) {
        textLine.addColumn(
            TextColumn(
                start = x + startX,
                end = x + endX,
                charData = char
            )
        )
    }

    /**
     * 自然排列
     */
    private suspend fun addCharsToLineNatural(
        textLine: TextLine,
        x: Int,
        words: Array<String>,
        textPaint: TextPaint,
        startX: Float,
        hasIndent: Boolean
    ) {
        val indentLength = "　　".length
        var x1 = startX
        words.forEachIndexed { index, char ->
            val cw = StaticLayout.getDesiredWidth(char, textPaint)
            val x2 = x1 + cw
            addTextToLine(textLine, x, char, x1, x2, index + 1 == words.size)
            x1 = x2
            if (hasIndent && index == indentLength - 1) {
                textLine.indentWidth = x1
            }
        }
        exceed(x, textLine, words)
    }


    /**
     * 超出边界处理
     */
    private fun exceed(x: Int, textLine: TextLine, words: Array<String>) {
        val visibleEnd = x + visibleWidth
        val endX = textLine.columns.lastOrNull()?.end ?: return
        if (endX > visibleEnd) {
            val cc = (endX - visibleEnd) / words.size
            for (i in 0..words.lastIndex) {
                textLine.getColumnReverseAt(i).let {
                    val py = cc * (words.size - i)
                    it.start = it.start - py
                    it.end = it.end - py
                }
            }
        }
    }

    /**
     * 更新画笔样式
     */
    fun updateStyle() {
        //内容画笔
        textPaint.textSize = ReadBookConfig.textSize * density
        textPaint.color = ReadBookConfig.textColor
        textPaint.isSubpixelText = true
        textPaint.isAntiAlias = true
        //标题画笔
        titlePaint.textSize = (sqrt(
            (ReadBookConfig.textSize * density).toDouble().pow(2.0) / 2f
        ) - ReadBookConfig.textSize * density / 12f).toFloat()
        titlePaint.color = ReadBookConfig.textColor
        titlePaint.isSubpixelText = true
        titlePaint.isAntiAlias = true

        lineHeightRatio = ReadBookConfig.lineHeightRatio
        paragraphSpacing = ReadBookConfig.paragraphSpacing
        marginLeft = ReadBookConfig.marginSpacing.toInt()
        marginRight = ReadBookConfig.marginSpacing.toInt()
        marginTop = (titlePaint.textHeight * 1.5f).toInt()
        marginBottom = (titlePaint.textHeight * 1.5f).toInt()
        updateLayout()
    }

    /**
     * 更新View尺寸
     */
    fun updateViewSize(width: Int, height: Int) {
        if (width > 0 && height > 0 && (width != viewWidth || height != viewHeight)) {
            viewWidth = width
            viewHeight = height
            updateLayout()
            postEvent(EventBus.UPDATE_CONFIG, true)
        }
    }

    /**
     * 更新绘制尺寸
     */
    private fun updateLayout() {
        if (viewWidth > 0 && viewHeight > 0) {
            visibleHeight = viewHeight - marginTop - marginBottom
            visibleWidth = viewWidth - marginLeft - marginRight
            visibleBottom = marginTop + visibleHeight
        }
    }


}

val TextPaint.textHeight: Float
    get() = fontMetrics.descent - fontMetrics.ascent + fontMetrics.leading