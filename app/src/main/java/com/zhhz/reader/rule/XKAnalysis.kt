package com.zhhz.reader.rule

import cn.hutool.core.codec.Base64
import cn.hutool.core.util.ObjectUtil
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson2.JSONPath
import com.zhhz.reader.bean.BookBean
import com.zhhz.reader.bean.HttpResponseBean
import com.zhhz.reader.bean.LeaderboardResultBean
import com.zhhz.reader.bean.SearchResultBean
import com.zhhz.reader.bean.rule.RuleJsonBean
import com.zhhz.reader.util.DiskCache
import com.zhhz.reader.util.OrderlyMap
import com.zhhz.reader.util.StringUtil
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.mozilla.javascript.NativeArray
import java.util.regex.Pattern
import javax.script.ScriptException
import javax.script.SimpleBindings

class XKAnalysis(ruleJsonBean: RuleJsonBean) : Analysis(ruleJsonBean) {


    private val headRegex =
        """(?<!\{\{)(?:@(?:js|json|css):|\$\.)""".toRegex(RegexOption.IGNORE_CASE)

    //private val analysisPattern = """((?<!\{\{)(?:@(?:js|json|css):|\$\.))((?:[^@]|@(?!js|json|css)|\$\.)+)""".toRegex(RegexOption.IGNORE_CASE)
    private val analysisPattern =
        """((?<!\{\{)(?:@(?:js|json|css):|\$\.))((?:(?!@js:|@json:|@css:|\$\.|\{\{).)+)""".toRegex(
            RegexOption.IGNORE_CASE
        )

    //防重复次数标记
    private var count = 0

    private fun split(str: String): Array<String> {
        val list = ArrayList<String>()
        val arr = str.toCharArray()
        var flag = 0
        var last = 0.toChar()
        var lastIndex = 0
        for ((index, c) in arr.withIndex()) {
            if (c == '{' && last == '{') {
                flag++
            } else if (c == '}' && last == '}') {
                flag--
            } else if (c == '#' && last == '#' && flag < 1) {
                flag = 0
                list.add(str.substring(lastIndex, index - 1))
                lastIndex = index + 1
            }
            last = c
        }
        list.add(str.substring(lastIndex))
        return list.toArray(arrayOf())
    }

    private fun parse(
        obj: Any,
        rule: String,
        bindings: SimpleBindings = SimpleBindings(),
        absoluteUrl: String = "",
        attr: String = "src",
        isString: Boolean = true
    ): Any {
        var dataTemp = obj
        var originalRule = ""
        val tmpArrayTemp = split(rule)
        var ruleTemp = tmpArrayTemp[0]
        val tmpArray = ruleTemp.split(headRegex, 2)

        if (tmpArray[0].isNotBlank()) {
            originalRule = tmpArray[0]
            if (tmpArray.size == 1) {
                if (obj is Element) {
                    originalRule = ""
                    ruleTemp = "@css:${tmpArray[0]}"
                } else if (!originalRule.contains("\\{\\{.+?\\}\\}".toRegex())) {
                    return originalRule
                }
            }
        }

        val patterns = analysisPattern.findAll(ruleTemp)
        for ((index, match) in patterns.withIndex()) {
            //用于判断是否是最后一个规则
            val bool = index == patterns.count() - 1

            when (match.groupValues[1]) {
                "@js:" -> {
                    bindings["value"] = dataTemp
                    var tempJs: Any
                    try {
                        tempJs = DiskCache.SCRIPT_ENGINE.eval(
                            Base64.decodeStr(match.groupValues[2]),
                            bindings
                        )
                    } catch (e: Exception) {
                        log(e)
                        return ""
                    }
                    if (bool) {
                        originalRule = ""
                        dataTemp = bindings["result"] ?: tempJs
                    } else {
                        originalRule = jsToJavaObject(bindings["result"] ?: tempJs)
                    }
                }

                "@css:" -> {
                    dataTemp = getElementTexts((dataTemp as Element), match.groupValues[2])
                }

                "@json:", "$." -> {
                    dataTemp = getJsonTexts(dataTemp, "$." + match.groupValues[2], bindings)
                }
            }
        }

        if (patterns.count() == 0) {
            if (obj is JSON && originalRule.isNotBlank()) {
                dataTemp = getJsonTexts(dataTemp, originalRule, bindings)
                //清空值，防止重复
                originalRule = ""
            }
        }

        if (originalRule.isNotBlank()) {
            dataTemp = originalRule + dataTemp
        }

        return if (isString) {
            contentJudgment(dataTemp, absoluteUrl, attr, tmpArrayTemp.drop(1))
        } else {
            dataTemp
        }
    }

    private fun getJsonTexts(dataTemp: Any, text: String, bindings: SimpleBindings): Any {
        var tmp1 = text
        tmp1 = tmp1.replace("\\{\\{(.+?)}}".toRegex()) {
            if (it.groupValues[1].contains("^(?:@json:|\\$\\.)".toRegex())) {
                parse(dataTemp, it.groupValues[1], bindings).toString()
            } else {
                parse(dataTemp, "@js:" + Base64.encode(it.groupValues[1]), bindings).toString()
            }
        }

        val i = tmp1.indexOf("\$.")
        return if (i > 0) {
            tmp1.substring(0, i) + JSONPath.eval(dataTemp, tmp1.substring(i))
        } else if (i == 0) {
            JSONPath.eval(dataTemp, tmp1)
        } else {
            tmp1
        }
    }

    private fun getElementTexts(element: Element?, rule: String): ArrayList<Any> {
        val textArray = ArrayList<Any>()
        if (element == null || rule.isEmpty()) return textArray
        val elements = Elements()
        val cssArray = rule.split("@")

        elements.addAll(element.select(cssArray[0]))

        if (elements.size == 0) return textArray

        //如果是最后一个规则
        if (cssArray.size == 2 || cssArray.size == 3) {
            when (cssArray[1]) {
                "text" -> for (e in elements) {
                    val text = e.text()
                    if (text.isNotEmpty()) {
                        textArray.add(text)
                    }
                }

                "textNodes" -> for (e in elements) {
                    val tn = arrayListOf<String>()
                    val contentEs = e.textNodes()
                    for (item in contentEs) {
                        val text = item.text().trim { it <= ' ' }
                        if (text.isNotEmpty()) {
                            tn.add(text)
                        }
                    }
                    if (tn.isNotEmpty()) {
                        textArray.add(tn.joinToString("\n"))
                    }
                }

                "ownText" -> for (e in elements) {
                    val text = e.ownText()
                    if (text.isNotEmpty()) {
                        textArray.add(text)
                    }
                }

                "html" -> {
                    elements.select("script").remove()
                    elements.select("style").remove()
                    val html = elements.outerHtml()
                    if (html.isNotEmpty()) {
                        textArray.add(html)
                    }
                }

                "all" -> {
                    textArray.add(elements.outerHtml())
                }

                else -> {
                    var index = -1
                    try {
                        index = cssArray[1].toInt()
                    } catch (_: NumberFormatException) {
                    }
                    if (index > -1) {
                        textArray.add(elements[index])
                    } else {
                        for (e in elements) {
                            val url = e.attr(cssArray[1])
                            if (url.isBlank() || textArray.contains(url)) continue
                            textArray.add(url)
                        }
                    }
                }
            }
            if (cssArray.size == 3) {
                try {
                    val index = cssArray[2].toInt()
                    val tmp = if (index < 0) {
                        textArray[textArray.size + index]
                    } else {
                        textArray[index]
                    }
                    textArray.clear()
                    textArray.add(tmp)
                } catch (_: NumberFormatException) {
                }
            }

        } else {
            textArray.addAll(elements)
        }

        return textArray
    }

    private fun contentJudgment(
        obj: Any,
        absoluteUrl: String,
        attr: String,
        rules: List<String>
    ): String {

        if (obj is List<*> && obj.size == 0) return ""

        if (obj is List<*>) {
            if (obj.size == 1) {
                return extendRules(
                    if (obj[0] is String) {
                        if (absoluteUrl.isNotEmpty()) {
                            toAbsoluteUrl(obj[0].toString(), absoluteUrl)
                        } else {
                            obj[0].toString()
                        }
                    } else {
                        if (absoluteUrl.isNotEmpty()) {
                            toAbsoluteUrl((obj[0] as Element).attr(attr), absoluteUrl)
                        } else {
                            (obj[0] as Element).text()
                        }
                    }, rules
                )
            }

            var tmp = ""
            obj.forEach {
                tmp += if (it is Element) {
                    if (absoluteUrl.isNotEmpty()) {
                        toAbsoluteUrl(extendRules(it.attr(attr), rules), absoluteUrl) + "\n"
                    } else {
                        extendRules(it.text(), rules) + "\n"
                    }
                } else {
                    extendRules(it.toString(), rules) + "\n"
                }
            }
            if (tmp.isNotEmpty()) {
                tmp = tmp.substring(0, tmp.length - 1)
            }
            return tmp
        }

        return if (absoluteUrl.isNotEmpty()) {
            toAbsoluteUrl(obj.toString(), absoluteUrl)
        } else {
            obj.toString()
        }

    }

    private val mailPattern = Pattern.compile("([^-]*)->(.*)")

    private fun extendRules(str: String, rules: List<String>): String {
        var value = str

        for (regex in rules) {
            val matcher = mailPattern.matcher(regex)
            if (!matcher.find()) {
                return value
            }
            val k = matcher.group(1)
            var v = matcher.group(2)
            if (v == null) v = ""
            if (k == null) return value

            when (k) {

                "match" -> {
                    val p = Pattern.compile(v)
                    val m = p.matcher(value)
                    if (m.find()) {
                        value = m.group()
                    }
                }

                "replace" -> {
                    val m = mailPattern.matcher(v)
                    if (m.find()) {
                        value = value.replace(m.group(1).orEmpty(), m.group(2).orEmpty())
                    }
                }

                "replaceAll" -> {
                    val m = mailPattern.matcher(v)
                    if (m.find()) {
                        value = value.replace(m.group(1).orEmpty().toRegex(), m.group(2).orEmpty())
                    }
                }

                "append" -> {
                    value += v
                }

                "set" -> {
                    setShareValue(v, value)
                    value = ""
                }

                "get" -> {
                    if (value.isNotEmpty()) {
                        value += getShareValue(v)
                    } else {
                        value = getShareValue(v).toString()
                    }
                }

            }
        }

        return value
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

    //运行js进行解密
    private fun jsDecryption(s: String, bindings: SimpleBindings): String {
        bindings["data"] = s
        try {
            return DiskCache.SCRIPT_ENGINE.eval(Base64.decodeStr(json.jsDecryption), bindings)
                .toString()
        } catch (e: Exception) {
            log(e)
        }
        return ""
    }


    override fun bookSearch(
        keyWord: String,
        page: Int,
        callback: AnalysisCallBack.SearchCallBack,
        label: String
    ) {
        val bindings = SimpleBindings()
        bindings["java"] = this

        bindings["callback"] = callback
        val search = json.search
        val url = search.url.replace("\${key}", keyWord).replace("\${page}", "$page")
        bindings["url"] = url

        http(toAbsoluteUrl(parse("", url).toString(), this.url)) { result ->
            val al: MutableList<SearchResultBean> = ArrayList()

            if (!result.isStatus) {
                callback.accept(al)
                return@http
            }
            val data = typeAutoConvert(result, bindings)

            val list = parse(data, search.list, isString = false)

            if (list is List<*>) {
                for (tmpData in list) {
                    if (ObjectUtil.isEmpty(tmpData)) continue
                    val searchResultBean = SearchResultBean()
                    val source: ArrayList<String> = ArrayList()
                    source.add(label)
                    searchResultBean.source = source
                    searchResultBean.name = name
                    searchResultBean.title = parse(tmpData!!, search.name, bindings).toString()
                    if (ObjectUtil.isNotEmpty(search.author))
                        searchResultBean.author = parse(tmpData, search.author, bindings).toString()
                    if (ObjectUtil.isNotEmpty(search.cover))
                        searchResultBean.cover =
                            parse(tmpData, search.cover, bindings, url).toString()
                    if (ObjectUtil.isNotEmpty(search.intro))
                        searchResultBean.intro = parse(tmpData, search.intro, bindings).toString()
                    if (ObjectUtil.isNotEmpty(search.lastChapter))
                        searchResultBean.lastChapter =
                            parse(tmpData, search.lastChapter, bindings).toString()
                    searchResultBean.url =
                        parse(tmpData, search.detail, bindings, url, "href").toString()
                    al.add(searchResultBean)
                }
            }
            callback.accept(al)

        }

    }

    override fun bookLeaderboard(
        leaderboardUrl: String,
        page: Int,
        callback: AnalysisCallBack.LeaderboardCallBack,
        label: String
    ) {
        val bindings = SimpleBindings()
        bindings["java"] = this

        bindings["callback"] = callback
        val search = json.search
        val leaderboard = json.leaderboard
        val url = leaderboardUrl.replace("\${page}", "$page")
        bindings["url"] = url

        http(toAbsoluteUrl(url)) { result ->
            val al: ArrayList<LeaderboardResultBean> = ArrayList()

            if (!result.isStatus) {
                callback.accept(al)
                return@http
            }
            val data = typeAutoConvert(result, bindings)

            val list = parse(data, leaderboard.list ?: search.list, isString = false)

            if (list is List<*>) {
                for (tmpData in list) {
                    if (ObjectUtil.isEmpty(tmpData)) continue
                    val leaderboardData = LeaderboardResultBean()
                    val source: ArrayList<String> = ArrayList()
                    source.add(label)
                    leaderboardData.source = source
                    leaderboardData.name = name
                    leaderboardData.title =
                        parse(tmpData!!, leaderboard.name ?: search.name, bindings).toString()
                    if (ObjectUtil.isNotEmpty(leaderboard.author ?: search.author))
                        leaderboardData.author =
                            parse(tmpData, leaderboard.author ?: search.author, bindings).toString()
                    if (ObjectUtil.isNotEmpty(leaderboard.cover ?: search.cover))
                        leaderboardData.cover =
                            parse(
                                tmpData,
                                leaderboard.cover ?: search.cover,
                                bindings,
                                url
                            ).toString()
                    if (ObjectUtil.isNotEmpty(leaderboard.lastChapter ?: search.lastChapter))
                        leaderboardData.lastChapter = parse(
                            tmpData,
                            leaderboard.lastChapter ?: search.lastChapter,
                            bindings
                        ).toString()
                    if (ObjectUtil.isNotEmpty(leaderboard.intro ?: search.intro))
                        leaderboardData.intro =
                            parse(tmpData, leaderboard.intro ?: search.intro, bindings).toString()
                    leaderboardData.url = parse(
                        tmpData,
                        leaderboard.detail ?: search.detail,
                        bindings,
                        url,
                        "href"
                    ).toString()
                    al.add(leaderboardData)
                }
            }
            callback.accept(al)

        }

    }


    override fun bookDetail(url: String, callback: AnalysisCallBack.DetailCallBack) {

        http(url) { result ->
            val book = BookBean()

            if (!result.isStatus) {
                callback.accept(book)
                return@http
            }
            val bindings = SimpleBindings()
            bindings["java"] = this
            bindings["url"] = url
            bindings["callback"] = callback
            val data = typeAutoConvert(result, bindings)

            book.title = parse(data, json.detail.name, bindings).toString()
            book.setComic(json.comic)
            if (ObjectUtil.isNotEmpty(json.detail.author)) {
                book.author = parse(data, json.detail.author, bindings).toString()
            }

            if (ObjectUtil.isNotEmpty(json.detail.intro)) {
                book.intro = parse(data, json.detail.intro, bindings).toString()
            }
            if (ObjectUtil.isNotEmpty(json.detail.cover)) {
                book.cover = parse(data, json.detail.cover, bindings, url).toString()
            }
            if (ObjectUtil.isNotEmpty(json.detail.updateTime)) {
                book.updateTime = parse(data, json.detail.updateTime, bindings).toString()
            }
            if (ObjectUtil.isNotEmpty(json.detail.status)) {
                book.status = parse(data, json.detail.status, bindings).toString()
                    .contains("完结")
            }
            if (ObjectUtil.isNotEmpty(json.detail.lastChapter)) {
                book.lastChapter = parse(data, json.detail.lastChapter, bindings).toString()
            }

            if (ObjectUtil.isEmpty(json.detail.catalog)) {
                book.catalogue = url
            } else if (json.detail.catalog.startsWith("js@")) {
                var resultJs: Any? = null
                try {
                    resultJs = DiskCache.SCRIPT_ENGINE.eval(
                        Base64.decodeStr(json.detail.catalog.substring(3)),
                        bindings
                    )
                } catch (e: ScriptException) {
                    log(e)
                    e.printStackTrace()
                }
                resultJs = resultJs ?: DiskCache.SCRIPT_ENGINE["result"]
                if (resultJs == null) {
                    callback.accept(null)
                    return@http
                }
                book.catalogue = resultJs.toString()
            } else {
                book.catalogue = parse(data, json.detail.catalog, bindings, url, "href").toString()
            }

            //加入 isComic 用于区分出来相同名字的小说和漫画
            book.bookId = StringUtil.getMD5(book.title + "▶☀" + isComic + "☀◀" + book.author)
            callback.accept(book)
        }
    }

    override fun bookDirectory(url: String, callback: AnalysisCallBack.DirectoryCallBack) {
        val bindings = SimpleBindings()
        bindings["java"] = this
        bindings["url"] = url
        bindings["callback"] = callback
        if (count++ > 100) {
            return callback.accept(OrderlyMap(), url)
        }
        http(url) { result ->
            val lhm = OrderlyMap()
            if (!result.isStatus) {
                log(result.error)
                count = 0
                callback.accept(lhm, url)
                return@http
            }
            val data = typeAutoConvert(result, bindings)

            if (ObjectUtil.isNotEmpty(json.catalog.js)) {
                try {
                    DiskCache.SCRIPT_ENGINE.eval(Base64.decodeStr(json.catalog.js), bindings)
                } catch (e: ScriptException) {
                    log(e)
                    e.printStackTrace()
                }
                return@http
            }

            var list = parse(data, json.catalog.list, bindings, isString = false)

            if (list is List<*>) {
                if (json.catalog.inverted) {
                    list = list.reversed()
                }
            }
            if (list is List<*>) {
                for (it in list) {
                    if (it != null)
                        lhm[parse(it, json.catalog.name, bindings).toString()] =
                            parse(it, json.catalog.chapter, bindings, url, "href").toString()
                }
            }

            if (ObjectUtil.isNotEmpty(json.catalog.page)) {
                val page = parse(data, json.catalog.page, bindings, url, "href").toString()
                if (page.isNotEmpty() && page != url) {
                    bookDirectory(page) { lhm1: OrderlyMap, url1: String ->
                        if (json.catalog.inverted) {
                            lhm1.putAll(lhm)
                            callback.accept(lhm1, url1)
                        } else {
                            lhm.putAll(lhm1)
                            callback.accept(lhm, url1)
                        }

                    }
                } else {
                    count = 0
                    callback.accept(lhm, url)
                }
            } else {
                count = 0
                callback.accept(lhm, url)
            }

        }
    }

    override fun bookContent(url: String, callback: AnalysisCallBack.ContentCallBack, label: Any) {
        val bindings = SimpleBindings()
        bindings["java"] = this
        bindings["url"] = url
        bindings["callback"] = callback
        bindings["label"] = label
        val httpResponseBean = HttpResponseBean()
        httpResponseBean.isStatus = true
        bindings["CallBackData"] = httpResponseBean
        if (count++ > 100) {
            httpResponseBean.isStatus = false
            httpResponseBean.error = "循环获取已超过100次，请检查规则是否有误"
            return callback.accept(httpResponseBean, label)
        }
        http(url) { result ->
            var s = ""
            if (!result.isStatus) {
                callback.accept(result, label)
                return@http
            }
            val data = typeAutoConvert(result, bindings)

            if (data is Element) {
                val filterStr = StringBuilder()
                if (ObjectUtil.isNotEmpty(json.chapter.filter)) {
                    for (filter in json.chapter.filter) {
                        filterStr.append(',').append(filter)
                    }
                    if (filterStr.isNotEmpty()) {
                        data.select(filterStr.toString()).iterator()
                        for (ele in data.select(filterStr.toString())) {
                            ele.remove()
                        }
                    }
                }
            }

            if (ObjectUtil.isNotEmpty(json.chapter.content)) {
                s = parse(data, json.chapter.content, bindings).toString()
            }

            bindings["value"] = s
            //用于图片二进制解密
            if (ObjectUtil.isNotEmpty(json.chapter.encrypted)) {
                var resultTmp = ""
                var bool = isComic
                if (json.chapter.encrypted.startsWith("@js:")) {
                    try {
                        val tempJs =
                            DiskCache.SCRIPT_ENGINE.eval(
                                Base64.decodeStr(json.chapter.encrypted.substring(4)),
                                bindings
                            )
                        val tmp = bindings["result"] ?: tempJs
                        if (tmp is NativeArray){
                            bool = false
                            s = tmp.joinToString("\n")
                        } else {
                            resultTmp = jsToJavaObject(tmp)
                        }
                    } catch (e: Exception) {
                        httpResponseBean.isStatus = false
                        httpResponseBean.error = e.message.toString()
                        log(e)
                    }
                }
                if (bool){
                    var tmp = ""
                    s.split("\n").map {
                        tmp += "\n"
                        tmp += it
                        tmp += if (resultTmp.isEmpty()){
                            "ImageDecryption:///" + json.chapter.encrypted
                        } else {
                            "ImageDecryption:///$resultTmp"
                        }
                    }
                    s = tmp.substring(1)
                }
            }

            bindings["value"] = s
            //屏蔽规则
            if (ObjectUtil.isNotEmpty(json.chapter.purify)) {
                for (purify in json.chapter.purify) {
                    if (purify.startsWith("@js:")) {
                        val tempJs = DiskCache.SCRIPT_ENGINE.eval(Base64.decodeStr(purify.substring(4)), bindings)
                        s = jsToJavaObject(bindings["result"] ?: tempJs)
                    } else {
                        s = s.replace(purify.toRegex(), "")
                    }
                }
            }


            httpResponseBean.data = s
            if (ObjectUtil.isNotEmpty(json.chapter.page)) {
                val page = parse(data, json.chapter.page, bindings, url, "href").toString()
                if (page.isNotEmpty() && page != url) {
                    bookContent(page, { dataA: HttpResponseBean, labelA: Any ->
                        if (dataA.isStatus) {
                            if (isComic) {
                                dataA.data = s + '\n' + dataA.data
                            } else {
                                dataA.data = s + dataA.data
                            }
                        }
                        callback.accept(dataA, labelA)
                    }, label)
                } else {
                    count = 0
                    callback.accept(httpResponseBean, label)
                }
            } else {
                count = 0
                callback.accept(httpResponseBean, label)
            }

        }
    }

    private val emptyJson = JSON.parse("{}")
    //根据内容识别返回JSON或者Jsoup
    private fun typeAutoConvert(obj: HttpResponseBean, bindings: SimpleBindings): Any {
        val tmp = obj.data.trim()
        return if (tmp.startsWith("[") || tmp.startsWith("{")) {
            try {
                responseParse(obj, bindings)
            } catch (e: Exception){
                log(e)
                emptyJson
            }

        } else {
            val data = Jsoup.parse(tmp)
            bindings["data"] = data
            return data
        }
    }


}