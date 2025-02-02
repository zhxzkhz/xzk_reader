package com.zhhz.reader.ui.book.entities

/**
 * 文字列
 */
data class TextColumn(
    //文字绘制X轴坐标
    override var start: Float,
    override var end: Float,
    val charData: String,
    var selected: Boolean = false
) : BaseColumn
