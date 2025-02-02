package com.zhhz.reader.bean

class ContentBean {
    //是否成功
    var isStatus = true

    //成功显示内容
    var data: Any? = null
        set(value) {
            isStatus = true
            field = value
        }

    //失败提示内容
    var error: String = ""
        set(value) {
            isStatus = false
            field = value
        }

    //是否往前翻一页
    var previousPage = false

}
