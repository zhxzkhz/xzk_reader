package com.zhhz.reader.bean

import com.alibaba.fastjson2.JSONObject

class DrawerItem(
    val title: String,
    val tags: List<JSONObject>, // 子标签列表
    val source: String,
    var isExpanded: Boolean = false, // 是否展开
)