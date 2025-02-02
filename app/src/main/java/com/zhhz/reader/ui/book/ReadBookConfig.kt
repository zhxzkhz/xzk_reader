package com.zhhz.reader.ui.book

import android.graphics.Color
import com.alibaba.fastjson2.JSON
import com.zhhz.reader.sql.SQLiteUtil


object ReadBookConfig{

    var curConfig: Config = JSON.parseObject(SQLiteUtil.readSetting("read_text_x_setting"), Config::class.java)
        private set

    var textSize: Int
        get() = curConfig.textSize
        set(value) {
            curConfig.textSize = value
        }

    var marginSpacing: Float
        get() = curConfig.marginSpacing
        set(value) {
            curConfig.marginSpacing = value
        }

    var lineHeight: Float
        get() = curConfig.lineHeight
        set(value) {
            curConfig.lineHeight = value
        }

    var fontSpacing: Float
        get() = curConfig.fontSpacing
        set(value) {
            curConfig.fontSpacing = value
        }

    var lineHeightRatio: Float
        get() = curConfig.lineHeightRatio
        set(value) {
            curConfig.lineHeightRatio = value
        }

    var paragraphSpacing: Float
        get() = curConfig.paragraphSpacing
        set(value) {
            curConfig.paragraphSpacing = value
        }

    var textColor: Int
        get() = Color.parseColor(curConfig.textColor)
        set(value) {
            curConfig.textColor = String.format("#%06X", 0xFFFFFF and value)
        }

}

data class Config(
    var textSize: Int = 24,
    var marginSpacing: Float = 40f,
    var lineHeight: Float = 10f,
    var fontSpacing: Float = 5f,
    var lineHeightRatio: Float = 1.1f,
    var paragraphSpacing: Float = 5f,
    var textColor: String = "#000000"
)
