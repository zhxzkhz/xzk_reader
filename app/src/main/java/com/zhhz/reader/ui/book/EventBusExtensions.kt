package com.zhhz.reader.ui.book

import com.jeremyliao.liveeventbus.LiveEventBus

inline fun <reified EVENT> postEvent(tag: String, event: EVENT) {
    LiveEventBus.get<EVENT>(tag).post(event)
}