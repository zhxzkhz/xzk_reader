package com.zhhz.reader.ui.book.entities

import kotlin.math.min

data class TextChapter(
    val title: String,
    val position: Int,
    val pages: List<TextPage>
){

    private val pageSize: Int get() = pages.size


    private fun getPage(index: Int): TextPage? {
        return pages.getOrNull(index)
    }

    fun getPageByReadPos(readPos: Int): TextPage? {
        return getPage(getPageIndexByCharIndex(readPos))
    }

    /**
     * @param index 页数
     * @return 是否是最后一页
     */
    fun isLastPage(index: Int): Boolean {
        return index + 1 >= pages.size
    }

    /**
     * @param index 页数
     * @return 是否是第一页
     */
    fun isFirstPage(index: Int): Boolean {
        return index == 0
    }

    /**
     * 获取上一页
     * @param textPage 当前页
     * @return 上一页
     */
    fun previousPage(textPage: TextPage): TextPage{
        return pages[textPage.index-1]
    }

    /**
     * 获取下一页
     * @param textPage 当前页
     * @return 下一页
     */
    fun nextPage(textPage: TextPage): TextPage{
        return pages[textPage.index+1]
    }


    /**
     * @param pageIndex 页数
     * @return 已读长度
     */
    fun getReadLength(pageIndex: Int): Int {
        var length = 0
        val maxIndex = min(pageIndex, pages.size)
        for (index in 0 until maxIndex) {
            length += pages[index].charSize
        }
        return length
    }

    /**
     * 用于校验当前文本位置，比如字体大小发生变化的情况
     * @param textIndex 当前页面文字在章节中的位置
     * @return 校验后的当前页面文字在章节中的位置
     */
    fun getTextIndex(textIndex: Int): Int {
        var length = 0
        val maxIndex = min(getPageIndexByCharIndex(textIndex), pages.size - 1)
        for (index in 0 until maxIndex) {
            length += pages[index].charSize
        }
        return length
    }

    /**
    * @param length 当前页面文字在章节中的位置
    * @return 下一页位置,如果没有下一页返回-1
    */
    fun getNextPageLength(length: Int): Int {
        val pageIndex = getPageIndexByCharIndex(length)
        if (pageIndex + 1 >= pageSize) {
            return -1
        }
        return getReadLength(pageIndex + 1)
    }

    /**
     * @return 根据索引位置获取所在页
     */
    private fun getPageIndexByCharIndex(charIndex: Int): Int {
        var length = 0
        pages.forEach {
            length += it.charSize
            if (length > charIndex) {
                return it.index
            }
        }
        return pages.lastIndex
    }

    /**
     * 获取内容
     */
    fun getContent(): String {
        val stringBuilder = StringBuilder()
        pages.forEach {
            stringBuilder.append(it.text)
        }
        return stringBuilder.toString()
    }

}
