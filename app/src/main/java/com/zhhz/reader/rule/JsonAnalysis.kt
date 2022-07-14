package com.zhhz.reader.rule

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.zhhz.reader.bean.BookBean
import com.zhhz.reader.bean.SearchResultBean
import com.zhhz.reader.util.AutoBase64
import com.zhhz.reader.util.DiskCache.SCRIPT_ENGINE
import com.zhhz.reader.util.JsExtensionClass
import java.net.URLEncoder
import java.util.regex.Pattern
import javax.script.SimpleBindings


class JsonAnalysis : Analysis,JsExtensionClass {
    constructor(path: String?) : super(path) {}

    constructor(jsonObject: JSONObject?) : super(jsonObject) {}

    private fun parseArray(s: String): Array<String> {
        val v1: String
        var v2 = ""
        val index = s.indexOf('@')
        if (index == -1) {
            v1 = ""
            v2 = s
        } else if (index > -1) {
            v1 = s.substring(0, index)
            v2 = s.substring(index + 1)
        } else {
            v1 = s
        }
        return arrayOf(v1, v2)
    }

    private fun parse(json: Any?, reg: String): Any = json.let {
        when (it) {
            is JSONObject -> return it[reg] ?: ""
            is JSONArray -> return it[reg.toInt()] ?: ""
            else -> return (json ?: "").toString()
        }
    }

    fun parseJson(json: Any, reg: String, bindings: SimpleBindings): Any {
        if (reg.isEmpty()) return ""
        var jsonTemp: Any = json
        val regs = parseArray(reg)

        if (regs[0].isNotEmpty()) {
            val pattern = "\\$([^$.]+)".toRegex()
            val found = pattern.findAll(regs[0])
            found.forEach { f ->
                jsonTemp = parse(jsonTemp, f.value.substring(1))
            }
        }

        if (regs[1].isEmpty()) return jsonTemp

        val mailPattern = Pattern.compile("([^-]*)->(.*)")
        //优化方法，支持多次使用方法
        val regexp = regs[1].split("@").toTypedArray()
        var s: Any = jsonTemp
        for (reg_x in regexp) {
            val matcher = mailPattern.matcher(reg_x)
            if (!matcher.find()) {
                s = jsonTemp.toString()
                break
            }
            val k = matcher.group(1)
            var v = matcher.group(2)
            if (v == null) v = ""
            assert(k != null)
            if (k != "js" && s !is String) {
                s = s.toString()
            }
            when (k) {
                "js" -> {
                    bindings["data"] = s
                    bindings["out"] = System.out
                    s = SCRIPT_ENGINE.eval(AutoBase64.decodeToString(v), bindings).toString()
                    println(s)
                }
                "match" -> {
                    val p = Pattern.compile(v)
                    val m = p.matcher(s as String)
                    if (m.find()) {
                        s = m.group()
                    }
                }
                "replace" -> {
                    val m = mailPattern.matcher(v)
                    if (m.find()) {
                        s = (s as String).replace(m.group(1).orEmpty(), m.group(2).orEmpty())
                    }
                }
            }
        }
        return s
    }

    override fun BookSearch(key_word: String, callback: CallBack, md5: String) {
        var key: String = key_word;
        if (json["encode"] != null) {
            key = URLEncoder.encode(key_word, charset)
        }

        val bindings = SimpleBindings()
        bindings["java"] = this
        val search = json.getJSONObject("search")
        var url = search.getString("url").replace("\${key}", key)
        val page = search.getString("page")
        if (page != null) {
            url = url.replace("\${page}", page)
        }

        Http(url, { data: Any?, msg: Any?, label: Any? ->
            if (data == null) {
                callback.run(null, msg, label)
                return@Http
            }
            val al: MutableList<SearchResultBean> = ArrayList()
            val json: JSON = JSON.parse(data as String) as JSON
            val list = parseJson(json, search.getString("list"), bindings)

            if (list is List<*>) {
                list.forEach { book ->
                    if (book != null) {
                        val result = SearchResultBean()
                        val source: List<String> = ArrayList()
                        source.plus(md5)
                        result.name = name
                        result.title =
                            parseJson(book, search.getString("name"), bindings).toString()
                        result.author =
                            parseJson(book, search.getString("author"), bindings).toString()
                        result.cover =
                            parseJson(book, search.getString("cover"), bindings).toString()
                        al.add(result)
                    }
                }
            }
            callback.run(al, null, null)
            bindings.clear()
        }, true)

    }

    override fun BookDirectory(url: String, callback: CallBack) {}
    override fun BookDetail(url: String, callback: CallBack) {}
    override fun BookChapters(book: BookBean, url: String, callback: CallBack, random: Any) {}
    override fun BookContent(url: String, callback: CallBack, random: Any) {}
}