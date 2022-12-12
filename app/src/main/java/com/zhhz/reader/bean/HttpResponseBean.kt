package com.zhhz.reader.bean

import okhttp3.Headers

class HttpResponseBean {
    //请求是否成功
    var isStatus = false

    //请求内容
    var data: String = ""

    //请求失败提示
    var error: String = ""

    //请求相应头部
    var requestHeader: Headers? = null
}