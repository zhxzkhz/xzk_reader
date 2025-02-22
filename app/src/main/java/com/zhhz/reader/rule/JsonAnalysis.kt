package com.zhhz.reader.rule

import cn.hutool.core.codec.Base64
import cn.hutool.core.util.ObjectUtil
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONPath
import com.zhhz.reader.bean.BookBean
import com.zhhz.reader.bean.HttpResponseBean
import com.zhhz.reader.bean.LeaderboardResultBean
import com.zhhz.reader.bean.SearchResultBean
import com.zhhz.reader.bean.rule.RuleJsonBean
import com.zhhz.reader.util.DiskCache.SCRIPT_ENGINE
import com.zhhz.reader.util.OrderlyMap
import java.util.regex.Pattern
import javax.script.ScriptException
import javax.script.SimpleBindings


class JsonAnalysis(jsonObject: RuleJsonBean) : Analysis(jsonObject) {


    //规则分割
    private fun parseArray(s: String): Array<String> {
        val v1: String
        val v2: String
        val index = s.indexOf('@')
        if (index == -1) {
            v1 = s
            v2 = ""
        } else {
            v1 = s.substring(0, index)
            v2 = s.substring(index + 1)
        }
        return arrayOf(v1, v2)
    }

    private fun parseRule(data: Any, reg: String, bindings: SimpleBindings): Any {
        if (reg.isEmpty()) return data
        var dataTemp: Any = data
        val regs = parseArray(reg)

        //循环JSONPath解析  xx{$a}xx{$b} = xxx data[a] xxx data[b]
        regs[0] = regs[0].replace("\\{([^{}]+)\\}".toRegex()) {
            it.groupValues
            val regx = parseArray(it.groupValues[1])
            val temp = JSONPath.eval(dataTemp, regx[0])
            if (regx[1].isEmpty()) return@replace temp.toString()
            return@replace regexRule(regx[1], temp, bindings).toString()
        }
        val i = regs[0].indexOf("\$")
        dataTemp = if (i > 0) {
            regs[0].substring(0, i) + JSONPath.eval(dataTemp, regs[0].substring(i))
        } else if (i == 0) {
            JSONPath.eval(dataTemp, regs[0].substring(i))
        } else {
            regs[0]
        }

        if (regs[1].isEmpty()) return dataTemp

        return regexRule(regs[1], dataTemp, bindings)
    }

    //@js->xxx@match->xxx
    private val mailPattern = Pattern.compile("([^-]*)->(.*)")

    private fun regexRule(regs: String, tmp: Any, bindings: SimpleBindings): Any {
        var value = tmp

        //优化方法，支持多次使用方法
        val regexp = regs.split("@")
        for (regex in regexp) {
            val matcher = mailPattern.matcher(regex)
            if (!matcher.find()) {
                return value
            }
            val k = matcher.group(1)
            var v = matcher.group(2)
            if (v == null) v = ""
            if (k == null) return value

            when (k) {
                "js" -> {
                    bindings["value"] = value
                    value = SCRIPT_ENGINE.eval(Base64.decodeStr(v), bindings)
                }

                "match" -> {
                    val p = Pattern.compile(v)
                    val m = p.matcher(value.toString())
                    if (m.find()) {
                        value = m.group()
                    }
                }

                "replace" -> {
                    val m = mailPattern.matcher(v)
                    if (m.find()) {
                        value =
                            (value.toString()).replace(m.group(1).orEmpty(), m.group(2).orEmpty())
                    }
                }

                "replaceAll" -> {
                    val m = mailPattern.matcher(v)
                    if (m.find()) {
                        value = (value.toString()).replace(
                            m.group(1).orEmpty().toRegex(),
                            m.group(2).orEmpty()
                        )
                    }
                }

                "append" -> {
                    value = value.toString() + v
                }

                "set" -> {
                    setShareValue(v, value)
                    value = ""
                }

                "get" -> {
                    if (value is String && value.length > 0) {
                        value += getShareValue(v)
                    } else {
                        value = getShareValue(v)
                    }
                }

            }
        }
        return value
    }


    //运行js进行解密
    private fun jsDecryption(s: String, bindings: SimpleBindings): String {
        bindings["data"] = s
        return SCRIPT_ENGINE.eval(Base64.decodeStr(json.jsDecryption), bindings).toString()
    }

    private fun responseParse(result: HttpResponseBean, bindings: SimpleBindings): JSON {
        //把原数据保存到 bindings里面
        bindings["response"] = result
        val data: JSON = (if (json.jsDecryption.isEmpty() || result.data.isEmpty()) {
            result.data
        } else {
            jsDecryption(result.data, bindings)
        }).ifEmpty { "{}" }.let {
            JSON.parse(it) as JSON
        }
        //把解密后的值保存到 bindings里面
        bindings["data"] = data
        return data
    }

    override fun bookSearch(
        keyWord: String,
        page: Int,
        callback: AnalysisCallBack.SearchCallBack,
        label: String
    ) {
        val bindings = SimpleBindings()
        bindings["xlua_rule"] = this
        bindings["java"] = this
        bindings["keyWord"] = keyWord
        bindings["callback"] = callback
        val search = json.search
        val url = search.url.replace("\${key}", keyWord).replace("\${page}", "$page")
        bindings["url"] = url

        http(url) { result ->
            val al: MutableList<SearchResultBean> = ArrayList()
            if (!result.isStatus) {
                callback.accept(al)
                return@http
            }

            val data: JSON
            
            try {
                data = responseParse(result, bindings)
            } catch (e :Exception){
                log(e)
                callback.accept(null)
                return@http
            }

            val list = parseRule(data, search.list, bindings)

            if (list is List<*>) {
                list.forEach { book ->
                    if (book != null) {
                        val searchResultBean = SearchResultBean()
                        val source: ArrayList<String> = ArrayList()
                        source.add(label)
                        searchResultBean.source = source
                        searchResultBean.name = name
                        searchResultBean.title = parseRule(book, search.name, bindings).toString()
                        if (ObjectUtil.isNotEmpty(search.author))
                            searchResultBean.author =
                                parseRule(book, search.author, bindings).toString()

                        if (ObjectUtil.isNotEmpty(search.cover))
                            searchResultBean.cover = toAbsoluteUrl(
                                parseRule(book, search.cover, bindings).toString(),
                                url
                            )

                        if (ObjectUtil.isNotEmpty(search.lastChapter))
                            searchResultBean.lastChapter = toAbsoluteUrl(
                                parseRule(
                                    book,
                                    search.lastChapter,
                                    bindings
                                ).toString(), url
                            )

                        if (ObjectUtil.isNotEmpty(search.intro))
                            searchResultBean.intro = toAbsoluteUrl(
                                parseRule(book, search.intro, bindings).toString(),
                                url
                            )

                        searchResultBean.url =
                            toAbsoluteUrl(parseRule(book, search.detail, bindings).toString(), url)
                        al.add(searchResultBean)
                    }
                }
            }
            callback.accept(al)
            bindings.clear()
        }

    }

    override fun bookLeaderboard(
        leaderboardUrl: String,
        page: Int,
        callback: AnalysisCallBack.LeaderboardCallBack,
        label: String
    ) {
        val bindings = SimpleBindings()
        bindings["xlua_rule"] = this
        bindings["java"] = this
        bindings["leaderboardUrl"] = leaderboardUrl
        bindings["callback"] = callback
        val leaderboard = json.leaderboard
        val search = json.search

        val url: String = leaderboardUrl.replace("\${page}", "$page")

        http(url) { result ->
            val al: ArrayList<LeaderboardResultBean> = ArrayList()
            if (!result.isStatus) {
                callback.accept(al)
                return@http
            }

            val data: JSON
            
            try {
                data = responseParse(result, bindings)
            } catch (e :Exception){
                log(e)
                callback.accept(null)
                return@http
            }

            val list = parseRule(data, leaderboard.list ?: search.list, bindings)

            if (list is List<*>) {
                list.forEach { book ->
                    if (book != null) {
                        val leaderboardData = LeaderboardResultBean()
                        val source: ArrayList<String> = ArrayList()
                        source.add(label)
                        leaderboardData.source = source
                        leaderboardData.name = name
                        leaderboardData.title =
                            parseRule(book, leaderboard.name ?: search.name, bindings).toString()
                        if (ObjectUtil.isNotEmpty(leaderboard.author) || ObjectUtil.isNotEmpty(
                                search.author
                            )
                        )
                            leaderboardData.author = parseRule(
                                book,
                                leaderboard.author ?: search.author,
                                bindings
                            ).toString()

                        if (ObjectUtil.isNotEmpty(leaderboard.cover) || ObjectUtil.isNotEmpty(search.cover))
                            leaderboardData.cover = toAbsoluteUrl(
                                parseRule(
                                    book,
                                    leaderboard.cover ?: search.cover,
                                    bindings
                                ).toString(), url
                            )

                        if (ObjectUtil.isNotEmpty(leaderboard.lastChapter) || ObjectUtil.isNotEmpty(
                                search.lastChapter
                            )
                        )
                            leaderboardData.lastChapter = toAbsoluteUrl(
                                parseRule(
                                    book,
                                    leaderboard.lastChapter ?: search.lastChapter,
                                    bindings
                                ).toString(), url
                            )

                        if (ObjectUtil.isNotEmpty(leaderboard.intro) || ObjectUtil.isNotEmpty(search.intro))
                            leaderboardData.intro = toAbsoluteUrl(
                                parseRule(
                                    book,
                                    leaderboard.intro ?: search.intro,
                                    bindings
                                ).toString(), url
                            )

                        leaderboardData.url = toAbsoluteUrl(
                            parseRule(
                                book,
                                leaderboard.detail ?: search.detail,
                                bindings
                            ).toString(), url
                        )
                        al.add(leaderboardData)
                    }
                }
            }
            callback.accept(al)
            bindings.clear()
        }

    }


    override fun bookDetail(url: String, callback: AnalysisCallBack.DetailCallBack) {
        val bindings = SimpleBindings()
        bindings["xlua_rule"] = this
        bindings["url"] = url
        bindings["callback"] = callback
        http(url) { result ->
            val book = BookBean()
            if (!result.isStatus) {
                log(result.error)
                callback.accept(book)
                return@http
            }

            val data: JSON
            
            try {
                data = responseParse(result, bindings)
            } catch (e :Exception){
                log(e)
                callback.accept(null)
                return@http
            }

            book.title = parseRule(data, json.detail.name, bindings).toString()
            book.setComic(json.comic)
            if (ObjectUtil.isNotEmpty(json.detail.author))
                book.author = parseRule(data, json.detail.author, bindings).toString()

            if (ObjectUtil.isNotEmpty(json.detail.cover))
                book.cover = parseRule(data, json.detail.cover, bindings).toString()

            if (ObjectUtil.isNotEmpty(json.detail.lastChapter))
                book.lastChapter = parseRule(data, json.detail.lastChapter, bindings).toString()

            if (ObjectUtil.isNotEmpty(json.detail.status))
                book.status =
                    parseRule(data, json.detail.status, bindings).toString().contains("完结")

            if (ObjectUtil.isNotEmpty(json.detail.intro))
                book.intro = parseRule(data, json.detail.intro, bindings).toString()

            if (ObjectUtil.isNotEmpty(json.detail.updateTime))
                book.updateTime = parseRule(data, json.detail.updateTime, bindings).toString()

            if (json.detail.catalog.isEmpty()) {
                book.catalogue = url
            } else if (json.detail.catalog.startsWith("js@")) {
                var resultJs: Any? = null
                try {
                    resultJs = SCRIPT_ENGINE.eval(
                        Base64.decodeStr(json.detail.catalog.substring(3)),
                        bindings
                    )
                } catch (e: ScriptException) {
                    log(e)
                    e.printStackTrace()
                }
                resultJs = resultJs ?: SCRIPT_ENGINE["result"]
                if (resultJs == null) {
                    callback.accept(null)
                    return@http
                }
                book.catalogue = resultJs.toString()
            } else {
                book.catalogue =
                    toAbsoluteUrl(parseRule(data, json.detail.catalog, bindings).toString(), url)
            }

            callback.accept(book)
        }

    }

    override fun bookDirectory(url: String, callback: AnalysisCallBack.DirectoryCallBack) {
        val bindings = SimpleBindings()
        bindings["xlua_rule"] = this
        bindings["url"] = url
        bindings["callback"] = callback
        http(url) { result ->
            val lhm = OrderlyMap()
            if (!result.isStatus) {
                log(result.error)
                callback.accept(lhm, url)
                return@http
            }

            val data: JSON
            
            try {
                data = responseParse(result, bindings)
            } catch (e :Exception){
                log(e)
                callback.accept(null,url)
                return@http
            }

            if (ObjectUtil.isNotEmpty(json.catalog.js)) {
                try {
                    SCRIPT_ENGINE.eval(Base64.decodeStr(json.catalog.js), bindings)
                } catch (e: ScriptException) {
                    log(e)
                    e.printStackTrace()
                }
                return@http
            }

            var list = parseRule(data, json.catalog.list, bindings)
            if (list is List<*>) {
                if (json.catalog.inverted) {
                    list = list.reversed()
                }
            }
            if (list is List<*>) {
                list.forEach {
                    if (it != null)
                        lhm[parseRule(it, json.catalog.name, bindings).toString()] =
                            parseRule(it, json.catalog.chapter, bindings).toString()
                }
            }

            callback.accept(lhm, url)

        }
    }

    override fun bookContent(url: String, callback: AnalysisCallBack.ContentCallBack, label: Any) {
        val bindings = SimpleBindings()
        bindings["xlua_rule"] = this
        bindings["url"] = url
        bindings["callback"] = callback
        bindings["label"] = label
        val httpResponseBean = HttpResponseBean()
        httpResponseBean.isStatus = true
        bindings["CallBackData"] = httpResponseBean
        http(url) { result ->
            var s = ""
            if (!result.isStatus) {
                callback.accept(result, label)
                return@http
            }

            val data: JSON
            
            try {
                data = responseParse(result, bindings)
            } catch (e :Exception){
                log(e)
                result.isStatus = false
                result.error = e.message.toString()
                callback.accept(result, label)
                return@http
            }

            if (ObjectUtil.isNotEmpty(json.chapter.content)) {
                s = parseRule(data, json.chapter.content, bindings).toString()
            }
            bindings["value"] = s
            //执行js
            if (ObjectUtil.isNotEmpty(json.chapter.encrypted)) {
                try {
                    val tempJs = SCRIPT_ENGINE.eval(Base64.decodeStr(json.chapter.encrypted), bindings)
                    s = jsToJavaObject(bindings["result"] ?: tempJs)
                } catch (e: Exception) {
                    httpResponseBean.isStatus = false
                    httpResponseBean.error = e.message.toString()
                    log(e)
                    e.printStackTrace()
                }
                //返回false代表 js 内部处理
                if (s == "false") {
                    return@http
                }
            }
            httpResponseBean.data = s
            callback.accept(httpResponseBean, label)

        }
    }

}