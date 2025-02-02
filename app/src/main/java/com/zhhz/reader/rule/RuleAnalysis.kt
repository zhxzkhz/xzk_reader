package com.zhhz.reader.rule

import cn.hutool.core.util.ObjectUtil
import com.zhhz.reader.bean.BookBean
import com.zhhz.reader.bean.rule.RuleJsonBean
import com.zhhz.reader.rule.AnalysisCallBack.*
import com.zhhz.reader.util.StringUtil
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class RuleAnalysis(jsonObject: RuleJsonBean, bool: Boolean) {

    lateinit var analysis: Analysis

    @Throws(IOException::class)
    constructor(path: String, bool: Boolean = false) : this(Analysis.readText(path), bool)

    init {
        val type = jsonObject.type
        // 0 为 jsoup ， 1 为 json , 2 为自动识别
        when (type) {
            0 -> {
                analysis = JsoupAnalysis(jsonObject)
            }
            1 -> {
                analysis = JsonAnalysis(jsonObject)
            }
            2 -> {
                analysis = XKAnalysis(jsonObject)
            }
        }
        if (bool) {
            analyses_map[StringUtil.getMD5(analysis.name)] = analysis
        }
    }

    fun bookSearch(keyWord: String, page: Int, callback: SearchCallBack, label: String) {
        var keyWord1 = keyWord
        if (ObjectUtil.isNotEmpty(analysis.json.encode)) {
            try {
                keyWord1 = URLEncoder.encode(keyWord1, analysis.charset)
            } catch (e: UnsupportedEncodingException) {
                analysis.log(e)
                e.printStackTrace()
            }
        }
        analysis.bookSearch(keyWord1,page, callback, label)
    }

    /**
     * 获取书本目录
     *
     * @param url      目录地址
     * @param callback 回调函数
     */
    fun bookDirectory(url: String, callback: DirectoryCallBack) {
        analysis.bookDirectory(url, callback)
    }

    fun bookDetail(url: String, callback: DetailCallBack) {
        analysis.bookDetail(url, callback)
    }

    fun bookChapters(bookBean: BookBean, url: String, callback: ContentCallBack, label: Any) {
        analysis.bookChapters(bookBean, url, callback, label)
    }

    companion object {
        //储存已加载的规则
        @JvmField
        val analyses_map = LinkedHashMap<String, Analysis>()
    }
}