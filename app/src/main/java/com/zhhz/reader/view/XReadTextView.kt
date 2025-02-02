package com.zhhz.reader.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.zhhz.reader.ui.book.ReadBookConfig
import com.zhhz.reader.ui.book.ReadProvider
import com.zhhz.reader.ui.book.entities.BaseColumn
import com.zhhz.reader.ui.book.entities.TextChapter
import com.zhhz.reader.ui.book.entities.TextColumn
import com.zhhz.reader.ui.book.entities.TextLine
import com.zhhz.reader.ui.book.entities.TextPage
import com.zhhz.reader.ui.book.entities.TextPos
import com.zhhz.reader.ui.book.textHeight
import java.text.BreakIterator
import java.util.Locale


class XReadTextView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    //是否选中文本
    var isTextSelected = false
    private var pressOnTextSelected = false
    private val initialTextPos = TextPos(0, 0)

    private var selectAble = true

    private val selectedPaint by lazy {
        Paint().apply {
            color = 0X55555555
            style = Paint.Style.FILL
        }
    }

    //长按
    private var longPressed = false
    private val longPressTimeout = 600L
    private val longPressRunnable = Runnable {
        longPressed = true
        onLongPress()
    }

    //起始点
    private var startX: Float = 0f
    private var startY: Float = 0f

    private val boundary by lazy { BreakIterator.getWordInstance(Locale.getDefault()) }

    val selectStart = TextPos(0, 0)
    private val selectEnd = TextPos(0, 0)
    private var textPage: TextPage = TextPage()

    private var textChapter: TextChapter? = null

    private var callBack: CallBack

    //文本位置索引
    private var textIndex: Int = 0

    private val leftRect = RectF()
    private val centerRect = RectF()
    private val rightRect = RectF()

    init {
        callBack = context as CallBack
    }


    /**
     * index 文本位置索引
     */
    fun setContent(textChapter: TextChapter?, index: Int = 0) {
        this.textChapter = textChapter
        if (textChapter != null) {
            this.textIndex = textChapter.getTextIndex(index)
            this.textPage = textChapter.getPageByReadPos(this.textIndex) ?: textPage
            invalidate()
            callBack.saveProgress()
        }
    }

    fun getTextChapter(): TextChapter? {
        return textChapter
    }

    fun getReadProgress(): Int {
        return this.textIndex
    }

    /**
     * 长按选择
     */
    private fun onLongPress() {
        kotlin.runCatching {
            touch(startX,startY) { textPos, _, _, column ->
                when (column) {
                    is TextColumn -> {
                        //是否启用选中
                        if (!selectAble) return@touch
                        column.selected = true
                        invalidate()
                        selectText(textPos)
                    }
                }
            }
        }
    }

    private fun selectText(x1: Float, y1: Float){
        kotlin.runCatching {
            touch(x1,y1) { textPos, _, _, column ->
                when (column) {
                    is TextColumn -> {
                        //是否启用选中
                        if (!selectAble) return@touch
                        column.selected = true
                        invalidate()
                        val compare = initialTextPos.compare(textPos)
                        when {
                            compare >= 0 -> {
                                selectStartMoveIndex(
                                    textPos.lineIndex,
                                    textPos.columnIndex
                                )
                                selectEndMoveIndex(
                                    initialTextPos.lineIndex,
                                    initialTextPos.columnIndex
                                )
                            }
                            else -> {
                                selectStartMoveIndex(
                                    initialTextPos.lineIndex,
                                    initialTextPos.columnIndex
                                )
                                selectEndMoveIndex(
                                    textPos.lineIndex,
                                    textPos.columnIndex
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun selectText(textPos: TextPos) {
        isTextSelected = true
        initialTextPos.upData(textPos)
        val startPos = textPos.copy()
        val endPos = textPos.copy()
        val stringBuilder = StringBuilder()
        var cIndex = textPos.columnIndex
        var lineStart = textPos.lineIndex
        var lineEnd = textPos.lineIndex

        for (index in textPos.lineIndex - 1 downTo 0) {
            val textLine = textPage.getLine(index)
            if (textLine.isParagraphEnd) {
                break
            } else {
                stringBuilder.insert(0, textLine.text)
                lineStart -= 1
                cIndex += textLine.charSize
            }
        }
        for (index in textPos.lineIndex until textPage.lineSize) {
            val textLine = textPage.getLine(index)
            stringBuilder.append(textLine.text)
            lineEnd += 1
            if (textLine.isParagraphEnd) {
                break
            }
        }
        var start: Int
        var end: Int
        boundary.setText(stringBuilder.toString())
        start = boundary.first()
        end = boundary.next()
        while (end != BreakIterator.DONE) {
            if (cIndex in start until end) {
                break
            }
            start = end
            end = boundary.next()
        }
        kotlin.run {
            var ci = 0
            for (index in lineStart..lineEnd) {
                val textLine = textPage.getLine(index)
                for (j in 0 until textLine.charSize) {
                    if (ci == start) {
                        startPos.lineIndex = index
                        startPos.columnIndex = j
                    } else if (ci == end - 1) {
                        endPos.lineIndex = index
                        endPos.columnIndex = j
                        return@run
                    }
                    ci++
                }
            }
        }
        selectStartMoveIndex(
            startPos.lineIndex,
            startPos.columnIndex
        )
        selectEndMoveIndex(
            endPos.lineIndex,
            endPos.columnIndex
        )
    }

    /**
     * 选择开始文字
     */
    fun selectStartMoveIndex(lineIndex: Int, charIndex: Int) {
        selectStart.lineIndex = lineIndex
        selectStart.columnIndex = charIndex
        val textLine = textPage.getLine(lineIndex)
        val textColumn = textLine.getColumn(charIndex)
        upSelectedStart(
            textColumn.start,
            textLine.lineBottom,
            textLine.lineTop
        )
    }

    /**
     * 选择结束文字
     */
    fun selectEndMoveIndex(lineIndex: Int, charIndex: Int) {
        selectEnd.lineIndex = lineIndex
        selectEnd.columnIndex = charIndex
        val textLine = textPage.getLine(lineIndex)
        val textColumn = textLine.getColumn(charIndex)
        updateSelectedEnd(textColumn.end, textLine.lineBottom)
    }

    private fun upSelectedStart(x: Float, y: Float, top: Float) {
        callBack.run {
            updateSelectedStart(x, y + headerHeight, top + headerHeight)
        }
    }

    private fun updateSelectedEnd(x: Float, y: Float) {
        callBack.run {
            upSelectedEnd(x, y + headerHeight)
        }
    }

    /**
     * 取消选择文本
     */
    private fun cancelSelect() {
        textPage.lines.forEach { textLine ->
            textLine.columns.forEach {
                if (it is TextColumn) {
                    it.selected = false
                }
            }
        }
        invalidate()
        callBack.onCancelSelect()
    }

    /**
     * 触碰位置信息
     * @param touched 回调
     */
    private fun touch(
        x: Float,
        y: Float,
        touched: (
            textPos: TextPos,
            textPage: TextPage,
            textLine: TextLine,
            column: BaseColumn
        ) -> Unit
    ) {
        for ((lineIndex, textLine) in textPage.lines.withIndex()) {
            if (textLine.isTouch(x, y)) {
                for ((charIndex, textColumn) in textLine.columns.withIndex()) {
                    if (textColumn.isTouch(x)) {
                        touched.invoke(
                            TextPos(lineIndex, charIndex),
                            textPage, textLine, textColumn
                        )
                        return
                    }
                }
                val (charIndex, textColumn) = textLine.columns.withIndex().last()
                touched.invoke(
                    TextPos(lineIndex, charIndex),
                    textPage, textLine, textColumn
                )
                return
            }
        }
    }

    fun onSingleTapUp(x: Float, y: Float) {

        when {
            isTextSelected -> Unit
            leftRect.contains(x, y) -> if (textChapter != null) {
                //上一页
                if (!textChapter!!.isFirstPage(textPage.index)) {
                    this.textIndex = maxOf(0, this.textIndex - textPage.charSize)
                    textPage = textChapter!!.previousPage(textPage)
                    callBack.saveProgress()
                    invalidate()
                } else {
                    callBack.switchChapter(0)
                }
            }

            centerRect.contains(x, y) -> {
                //弹窗菜单
                callBack.showBookMenu()
            }

            rightRect.contains(x, y) -> if (textChapter != null) {
                //下一页
                if (!textChapter!!.isLastPage(textPage.index)) {
                    this.textIndex += textPage.charSize
                    textPage = textChapter!!.nextPage(textPage)
                    callBack.saveProgress()
                    invalidate()
                } else {
                    callBack.switchChapter(1)
                }
            }
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isTextSelected) {
                    cancelSelect()
                    isTextSelected = false
                    pressOnTextSelected = true
                } else {
                    pressOnTextSelected = false
                }
                longPressed = false
                postDelayed(longPressRunnable, longPressTimeout)
                startX = event.x
                startY = event.y
            }

            MotionEvent.ACTION_MOVE -> {
                longPressed = false
                removeCallbacks(longPressRunnable)
                if (isTextSelected) {
                    selectText(event.x, event.y)
                }
            }

            MotionEvent.ACTION_UP -> {
                removeCallbacks(longPressRunnable)
                if (!longPressed && !pressOnTextSelected) {
                    onSingleTapUp(event.x, event.y)
                }

                if (isTextSelected) {
                    callBack.showTextActionMenu()
                }
            }
        }
        return true
    }

    private fun setAllRect() {
        leftRect.set(0f, 0f, width * 0.333f, height.toFloat())
        //防止解锁亮屏触发事件，屏蔽指纹位置
        centerRect.set(width * 0.333f , 0f,width * 0.666f , height.toFloat() * 0.75f)
        rightRect.set(width * 0.666f, 0f, width.toFloat(), height.toFloat())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setAllRect()
        ReadProvider.updateViewSize(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (textChapter != null) {
            canvas.drawText(textChapter!!.title, ReadProvider.marginLeft * 1.8f ,
                ReadProvider.marginTop * 0.6f, ReadProvider.titlePaint)
            canvas.drawText(
                "${textPage.index+1}/${textChapter!!.pages.last().index+1}",
                width * 0.85f,
                height - ReadProvider.titlePaint.textHeight * 0.4f,
                ReadProvider.titlePaint
            )
        }

        textPage.lines.forEach { textLine ->
            val textPaint = if (textLine.isTitle) {
                ReadProvider.titlePaint
            } else {
                ReadProvider.textPaint
            }
            val lineTop = textLine.lineTop
            val lineBase = textLine.lineBase
            val lineBottom = textLine.lineBottom
            textLine.columns.forEach {
                when (it) {
                    is TextColumn -> {
                        textPaint.color = ReadBookConfig.textColor
                        canvas.drawText(it.charData, it.start, lineBase, textPaint)
                        if (it.selected) {
                            println("selected -> ${it.start}, ${lineTop}, ${it.end}, $lineBottom")
                            canvas.drawRect(it.start, lineTop, it.end, lineBottom, selectedPaint)
                        }
                    }
                }
            }

        }

    }

    interface CallBack {
        val headerHeight: Int
        fun updateSelectedStart(x: Float, y: Float, top: Float)
        fun upSelectedEnd(x: Float, y: Float)
        fun showTextActionMenu()
        fun onCancelSelect()
        fun saveProgress()

        // 0 代表上一章，1代表下一章
        fun switchChapter(i: Int)
        fun showBookMenu()

    }

}
