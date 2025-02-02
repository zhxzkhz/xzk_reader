package com.zhhz.reader.ui.book

import android.graphics.Typeface
import android.os.Build
import android.text.StaticLayout
import android.text.TextPaint
import com.zhhz.reader.ui.book.entities.TextChapter
import com.zhhz.reader.ui.book.entities.TextColumn
import com.zhhz.reader.ui.book.entities.TextLine
import com.zhhz.reader.ui.book.entities.TextPage
import kotlin.math.pow
import kotlin.math.sqrt

object ReadProvider {

    private const val indentChar = "　"

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
    var indentCharWidth = 0f
        private set

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


    private fun textParse(
        x: Int,
        y: Float,
        text: String,
        textPages: ArrayList<TextPage>,
        textPaint: TextPaint,
        sb: StringBuilder,
        isTitle: Boolean = false,
    ): Pair<Int, Float> {

        val widthsArray = FloatArray(text.length)
        textPaint.getTextWidths(text, widthsArray)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            if (widthsArray.isNotEmpty()) {
                val letterSpacing = textPaint.letterSpacing * textPaint.textSize
                val letterSpacingHalf = letterSpacing * 0.5f
                widthsArray[0] += letterSpacingHalf
                widthsArray[widthsArray.lastIndex] += letterSpacingHalf
            }
        }

        val layout = if (ReadBookConfig.useZhLayout) {
            val (words, widths) = measureTextSplit(text, widthsArray)
            ZhLayout(text, textPaint, visibleWidth, words, widths)
        } else {
            StaticLayout. Builder.obtain(text, 0, text.length, textPaint, visibleWidth).build()
        }
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

            val lineStart = layout.getLineStart(lineIndex)
            val lineEnd = layout.getLineEnd(lineIndex)
            val lineText = text.substring(lineStart, lineEnd)
            val (words, widths) = measureTextSplit(lineText, widthsArray, lineStart)
            val desiredWidth = widths.sum()

            when {
                //第一行并且不是标题
                lineIndex == 0 && layout.lineCount > 1 && !isTitle -> {
                    textLine.text = lineText
                    addTextToLineFirst(textLine, curX, words, desiredWidth, widths)
                }

                lineIndex == layout.lineCount - 1 -> {

                    //最后一行
                    textLine.text = lineText
                    textLine.isParagraphEnd = true
                    addTextToLineNatural(
                        textLine,
                        curX,
                        words,
                        0f,
                        !isTitle && lineIndex == 0,
                        widths
                    )

                }

                else -> {
                    //中间行

                    textLine.text = lineText
                    addTextToLineMiddle(
                        textLine, curX, words,
                        desiredWidth, 0f, widths
                    )

                }
            }
            sb.append(lineText)
            textPages.last().addLine(textLine)
            textLine.updateTopBottom(curY, textPaint)
            curY += textPaint.textHeight * lineHeightRatio
            textPages.last().height = curY

        }

        curY += textPaint.textHeight * 0.3f + paragraphSpacing


        return Pair(curX, curY)
    }


    //首行缩进,两端对齐
    private fun addTextToLineFirst(
        textLine: TextLine,
        absStartX: Int,
        words: List<String>,
        desiredWidth: Float,
        textWidths: List<Float>,
    ) {
        var x = 0f
        val bodyIndent = "　　"
        for (i in bodyIndent.indices) {
            val x1 = x + indentCharWidth
            textLine.addColumn(
                TextColumn(
                    charData = indentChar,
                    start = absStartX + x,
                    end = absStartX + x1
                )
            )
            x = x1
            textLine.indentWidth = x
        }
        if (words.size > bodyIndent.length) {
            val text1 = words.subList(bodyIndent.length, words.size)
            val textWidths1 = textWidths.subList(bodyIndent.length, textWidths.size)
            addTextToLineMiddle(
                textLine, absStartX, text1,
                desiredWidth, x, textWidths1
            )
        }

    }

    //无缩进,两端对齐
    private fun addTextToLineMiddle(
        textLine: TextLine,
        absStartX: Int,
        words: List<String>,
        desiredWidth: Float,
        /**起始x坐标**/
        startX: Float,
        textWidths: List<Float>,
    ) {

        val residualWidth = visibleWidth - desiredWidth
        val spaceSize = words.count { it == " " }
        if (spaceSize > 1) {
            val d = residualWidth / spaceSize
            var x = startX
            for (index in words.indices) {
                val char = words[index]
                val cw = textWidths[index]
                val x1 = if (char == " ") {
                    if (index != words.lastIndex) (x + cw + d) else (x + cw)
                } else {
                    (x + cw)
                }
                addTextToLine(
                    textLine, absStartX, char,
                    x, x1, index + 1 == words.size
                )
                x = x1
            }
        } else {
            val gapCount: Int = words.lastIndex
            val d = residualWidth / gapCount
            var x = startX
            for (index in words.indices) {
                val char = words[index]
                val cw = textWidths[index]
                val x1 = if (index != words.lastIndex) (x + cw + d) else (x + cw)
                addTextToLine(
                    textLine, absStartX, char,
                    x, x1, index + 1 == words.size
                )
                x = x1
            }
        }
        exceed(absStartX, textLine, words)

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
    private fun addTextToLineNatural(
        textLine: TextLine,
        absStartX: Int,
        words: List<String>,
        startX: Float,
        hasIndent: Boolean,
        textWidths: List<Float>,
    ) {
        val indentLength = "　　".length
        var x = startX
        for (index in words.indices) {
            val char = words[index]
            val cw = textWidths[index]
            val x1 = x + cw
            addTextToLine(textLine, absStartX, char, x, x1, index + 1 == words.size)
            x = x1
            if (hasIndent && index == indentLength - 1) {
                textLine.indentWidth = x
            }
        }
        exceed(absStartX, textLine, words)

    }


    /**
     * 超出边界处理
     */
    private fun exceed(x: Int, textLine: TextLine, words: List<String>) {
        val visibleEnd = x + visibleWidth
        val endX = textLine.columns.lastOrNull()?.end ?: return
        if (endX > visibleEnd) {
            val cc = (endX - visibleEnd) / words.size
            for (i in 0..words.lastIndex) {
                textLine.getColumnReverseAt(i).let {
                    val py = cc * (words.size - i)
                    it.start -= py
                    it.end -= py
                }
            }
        }
    }

    private fun measureTextSplit(
        text: String,
        widthsArray: FloatArray,
        start: Int = 0
    ): Pair<ArrayList<String>, ArrayList<Float>> {
        val length = text.length
        var clusterCount = 0
        for (i in start..<start + length) {
            if (widthsArray[i] > 0) clusterCount++
        }
        val widths = ArrayList<Float>(clusterCount)
        val stringList = ArrayList<String>(clusterCount)
        var i = 0
        while (i < length) {
            val clusterBaseIndex = i++
            widths.add(widthsArray[start + clusterBaseIndex])
            while (i < length && widthsArray[start + i] == 0f && !isZeroWidthChar(text[i])) {
                i++
            }
            stringList.add(text.substring(clusterBaseIndex, i))
        }
        return stringList to widths
    }

    private fun measureTextSplit(
        text: String,
        paint: TextPaint
    ): Pair<ArrayList<String>, ArrayList<Float>> {
        val length = text.length
        val widthsArray = FloatArray(length)
        paint.getTextWidths(text, widthsArray)
        val clusterCount = widthsArray.count { it > 0f }
        val widths = ArrayList<Float>(clusterCount)
        val stringList = ArrayList<String>(clusterCount)
        var i = 0
        while (i < length) {
            val clusterBaseIndex = i++
            widths.add(widthsArray[clusterBaseIndex])
            while (i < length && widthsArray[i] == 0f) {
                i++
            }
            stringList.add(text.substring(clusterBaseIndex, i))
        }
        return stringList to widths
    }

    private fun isZeroWidthChar(char: Char): Boolean {
        val code = char.code
        return code == 8203 || code == 8204 || code == 8288
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
        val bodyIndent = "　　"
        var indentWidth = StaticLayout.getDesiredWidth(bodyIndent, textPaint)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            indentWidth += textPaint.letterSpacing * textPaint.textSize
        }
        indentCharWidth = indentWidth / bodyIndent.length
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