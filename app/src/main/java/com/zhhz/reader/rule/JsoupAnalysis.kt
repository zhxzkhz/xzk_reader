package com.zhhz.reader.rule

import cn.hutool.core.codec.Base64
import cn.hutool.core.util.ObjectUtil
import com.zhhz.reader.bean.BookBean
import com.zhhz.reader.bean.HttpResponseBean
import com.zhhz.reader.bean.SearchResultBean
import com.zhhz.reader.bean.rule.RuleJsonBean
import com.zhhz.reader.util.DiskCache.SCRIPT_ENGINE
import com.zhhz.reader.util.OrderlyMap
import com.zhhz.reader.util.StringUtil
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.util.Collections
import java.util.regex.Pattern
import javax.script.ScriptException
import javax.script.SimpleBindings

class JsoupAnalysis : Analysis {
    constructor(ruleJsonBean: RuleJsonBean) : super(ruleJsonBean)

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

    private val mailPattern = Pattern.compile("([^-]*)->(.*)")
    private val mailPattern1 = Pattern.compile("(.*)->(.*)")

    private fun parseJsoup(s1: Any, reg1: String): String {
        var value = s1
        var reg = reg1
        if (reg.isEmpty()) {
            return (value as? String) ?: (value as Elements).text()
        }
        if (detail != null && reg.contains("$")) {
            reg = reg.replace("\${title}", detail!!.title).replace("\${author}", detail!!.author)
        }

        //优化方法，支持多次使用方法
        val regexp = reg.split("@")
        for (regX in regexp) {
            val matcher = mailPattern.matcher(regX)
            if (!matcher.find()) {
                value = (value as Elements?)!!.text()
                break
            }
            val k = matcher.group(1)
            var v = matcher.group(2)
            if (v == null) v = ""
            if (k == null) return value.toString()
            if (k != "attr" && k != "attrs" && value !is String) {
                value = (value as Elements).text()
            }
            when (k) {
                "attr" -> {
                    value = (value as Elements).attr(v)
                }

                "attrs" -> {
                    if ((value as Elements).size == 1) {
                        value = value.attr(v)
                    } else {
                        var tmp = ""
                        value.forEach {
                            tmp = tmp + "\n" + it.attr(v)
                        }
                        value = tmp.substring(1)
                    }
                }

                "match" -> {
                    val p = Pattern.compile(v)
                    val m = p.matcher(value.toString())
                    if (m.find()) {
                        value = m.group()
                    }
                }

                "replace" -> {
                    val m = mailPattern1.matcher(v)
                    if (m.find()) {
                        value = value.toString().replace(m.group(1).orEmpty(), m.group(2).orEmpty())
                    }
                }

                "replaceAll" -> {
                    val m = mailPattern1.matcher(v)
                    if (m.find()) {
                        value = value.toString().replace(m.group(1).orEmpty().toRegex(), m.group(2).orEmpty())
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
        return value.toString()
    }

    override fun bookSearch(keyWord: String, page: Int,callback: AnalysisCallBack.SearchCallBack, label: String) {

        val url = json.search.url.replace("\${key}", keyWord).replace("\${page}", "$page")
        http(url) { result ->
            val al: MutableList<SearchResultBean> = ArrayList()

            if (!result.isStatus) {
                callback.accept(al)
                return@http
            }

            val element = Jsoup.parse(result.data)

            val list = element.select(json.search.list)
            for (dl in list) {
                val searchResultBean = SearchResultBean()
                searchResultBean.name = name
                val source: MutableList<String> = ArrayList()
                source.add(label)
                searchResultBean.source = source
                val rule = parseArray(json.search.name)
                searchResultBean.title = parseJsoup(dl.select(rule[0]), rule[1])
                val detail = parseArray(json.search.detail)
                searchResultBean.url =
                    toAbsoluteUrl(parseJsoup(dl.select(detail[0]), detail[1].ifEmpty { "attr->href" }), url)
                if (ObjectUtil.isNotEmpty(json.search.author)) {
                    val author = parseArray(json.search.author)
                    searchResultBean.author = parseJsoup(dl.select(author[0]), author[1])
                }
                if (ObjectUtil.isNotEmpty(json.search.cover)) {
                    val cover = parseArray(json.search.cover)
                    searchResultBean.cover =
                        toAbsoluteUrl(parseJsoup(dl.select(cover[0]), cover[1].ifEmpty { "attr->src" }), url)
                }
                al.add(searchResultBean)
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

            val element = Jsoup.parse(result.data)

            val title = parseArray(json.detail.name)
            book.title = parseJsoup(element.select(title[0]), title[1])
            book.setComic(json.comic)
            if (ObjectUtil.isNotEmpty(json.detail.author)) {
                val obj = parseArray(json.detail.author)
                book.author = parseJsoup(element.select(obj[0]), obj[1])
            }

            if (ObjectUtil.isNotEmpty(json.detail.intro)) {
                val obj = parseArray(json.detail.intro)
                book.intro = parseJsoup(element.select(obj[0]), obj[1])
            }
            if (ObjectUtil.isNotEmpty(json.detail.cover)) {
                val obj = parseArray(json.detail.cover)
                book.cover = toAbsoluteUrl(parseJsoup(element.select(obj[0]), obj[1].ifEmpty { "attr->src" }), url)
            }
            if (ObjectUtil.isNotEmpty(json.detail.updateTime)) {
                val obj = parseArray(json.detail.updateTime)
                book.updateTime = parseJsoup(element.select(obj[0]), obj[1])
            }
            if (ObjectUtil.isNotEmpty(json.detail.status)) {
                val obj = parseArray(json.detail.status)
                book.status = parseJsoup(element.select(obj[0]), obj[1]).contains("完结")
            }
            if (ObjectUtil.isNotEmpty(json.detail.lastChapter)) {
                val obj = parseArray(json.detail.lastChapter)
                book.lastChapter = parseJsoup(element.select(obj[0]), obj[1])
            }
            if (json.detail.catalog == null || json.detail.catalog.isEmpty()) {
                book.catalogue = url
            } else if (json.detail.catalog.startsWith("js@")) {
                var resultJs: Any? = null
                val simpleBindings = intiBindings(url, callback, element)
                try {
                    resultJs = SCRIPT_ENGINE.eval(
                        Base64.decodeStr(json.detail.catalog.substring(3)), simpleBindings
                    )
                } catch (e: ScriptException) {
                    log(e)
                    e.printStackTrace()
                }
                resultJs = jsToJavaObject(simpleBindings["result"] ?: resultJs)
                if (ObjectUtil.isEmpty(resultJs)) {
                    callback.accept(null)
                    return@http
                }
                book.catalogue = resultJs.toString()
            } else {
                val obj = parseArray(json.detail.catalog)
                val str =
                    toAbsoluteUrl(parseJsoup(element.select(obj[0]), obj[1].ifEmpty { "attr->href" }), url)
                book.catalogue = str
            }

            //加入 isComic 用于区分出来相同名字的小说和漫画
            book.bookId = StringUtil.getMD5(book.title + "▶☀" + isComic + "☀◀" + book.author)
            callback.accept(book)
        }
    }

    override fun bookDirectory(url: String, callback: AnalysisCallBack.DirectoryCallBack) {
        http(url) { result ->
            val lhm: OrderlyMap
            if (!result.isStatus) {
                callback.accept(OrderlyMap(), url)
                return@http
            }
            val element = Jsoup.parse(result.data)
            val catalog = json.catalog
            if (ObjectUtil.isNotEmpty(catalog.js)) {
                try {
                    SCRIPT_ENGINE.eval(Base64.decodeStr(catalog.js), intiBindings(url, callback, element))
                } catch (e: ScriptException) {
                    log(e)
                    e.printStackTrace()
                }
                return@http
            }

            lhm = catalogAnalysis(url, element)

            if (ObjectUtil.isNotEmpty(catalog.page)) {
                val tmp = parseArray(catalog.page)
                val page =
                    toAbsoluteUrl(parseJsoup(element.select(tmp[0]), tmp[1].ifEmpty { "attr->href" }), url)
                if (page.isNotEmpty() && page != url) {
                    bookDirectory(page) { it, urlTemp ->
                        if (it.isNotEmpty) {
                            lhm.putAll(it)
                            callback.accept(lhm, urlTemp)
                        } else {
                            callback.accept(OrderlyMap(), urlTemp)
                        }
                    }
                } else {
                    callback.accept(lhm, url)
                }
            } else {
                callback.accept(lhm, url)
            }
        }
    }

    private fun intiBindings(url: String, callback: Any, element: Element): SimpleBindings {
        val bindings = SimpleBindings()
        bindings["xlua_rule"] = this
        bindings["element"] = element
        bindings["url"] = url
        bindings["callback"] = callback
        return bindings
    }

    private fun catalogAnalysis(url: String?, data: Element): OrderlyMap {
        val lhm = OrderlyMap()
        var bookletName: Array<String>? = null
        val list: Elements
        val catalog = json.catalog
        val booklet = catalog.booklet
        if (ObjectUtil.isNotNull(booklet) && ObjectUtil.isNotNull(booklet.list) && ObjectUtil.isNotEmpty(booklet.name)) {
            bookletName = parseArray(booklet.name)
            //查询卷名和章节名
            list = data.select(booklet.list + " , " + catalog.list)
            // 倒序 ，并且 标题 和 章节不是一个标签才支持倒序
            if (catalog.inverted && catalog.name != booklet.name) {
                val listB = data.select(booklet.list)
                val listType = ArrayList<Int?>()
                val map = HashMap<Element, Int>()
                for (i in list.indices) {
                    map[list[i]] = i
                }
                for (element in listB) {
                    listType.add(map[element])
                }
                list.sortWith { a: Element, b: Element ->
                    if (listB.contains(a)) {
                        if (listB.contains(b)) {
                            return@sortWith 0
                        }
                        //固定范围标签位置
                        if (listB.indexOf(a) > existScope(listType, map[b]!!)) {
                            return@sortWith 1
                        }
                        return@sortWith -1
                    } else if (!listB.contains(b)) {
                        // 判断一个范围的进行倒序
                        if (existScope(listType, map[b]!!) == existScope(listType, map[a]!!)) {
                            if (map[a]!! > map[b]!!) {
                                return@sortWith -1
                            } else {
                                return@sortWith 1
                            }
                        }
                    } else {
                        //固定范围标签位置
                        if (listB.indexOf(b) == existScope(listType, map[a]!!)) {
                            return@sortWith 1
                        }
                    }
                    map[a]!! - map[b]!!
                }
                map.clear()
                listB.clear()
                listType.clear()
            }
        } else {
            list = data.select(catalog.list)
            //倒序
            if (ObjectUtil.isNotNull(catalog.inverted) && catalog.inverted) {
                Collections.reverse(list)
            }
        }
        for (element in list) {
            var bookletNames: Elements? = null
            if (bookletName != null) {
                bookletNames = element.select(bookletName[0])
            }
            if (bookletNames != null && bookletNames.size > 0) {
                lhm[parseJsoup(bookletNames, bookletName!![1])] = ""
            } else {
                val chapter = parseArray(catalog.chapter)
                val aUrl =
                    parseJsoup(element.select(chapter[0]), chapter[1].ifEmpty { "attr->href" })
                if (aUrl.isNotEmpty()) {
                    val obj = parseArray(catalog.name)
                    lhm[parseJsoup(element.select(obj[0]), obj[1])] = toAbsoluteUrl(aUrl, url)
                }
            }
        }
        return lhm
    }

    /**
     * 排序函数，用于返回章节存在的区间
     *
     * @param index 排序前位置
     * @return 排序后位置
     */
    private fun existScope(listType: ArrayList<Int?>, index: Int): Int {
        var a = -1
        for (i in listType.indices) {
            if (index > listType[i]!!) {
                a = i
            }
        }
        return a
    }


    override fun bookContent(url: String, callback: AnalysisCallBack.ContentCallBack, label: Any) {
        http(url) { result ->
            if (!result.isStatus) {
                callback.accept(result, label)
                return@http
            }
            val data: Document = Jsoup.parse(result.data)
            //data.outputSettings().prettyPrint(false)
            val chapter = json.chapter
            val element = data as Element
            var str = ""
            if (ObjectUtil.isNotEmpty(chapter.content)) {
                val contentX = parseArray(chapter.content)
                val content = element.select(contentX[0])
                val filterStr = StringBuilder()
                val sb = StringBuilder()
                if (ObjectUtil.isNotEmpty(chapter.filter)) {
                    for (filter in chapter.filter) {
                        if (filter.startsWith("@")) {
                            filterStr.append(',').append(filter.substring(1))
                        }
                    }
                    val content1: Elements = if (filterStr.isEmpty()) {
                        content.select(contentX[0])
                    } else {
                        content.select(contentX[0] + ">" + filterStr.substring(1))
                    }
                    for (ele in content1) {
                        ele.remove()
                    }
                    if (content.size == 1) {
                        content.html(content.html())
                    }
                }
                if (isComic) {
                    if (ObjectUtil.isEmpty(contentX[1])) {
                        sb.append(content.html())
                    } else {
                        sb.append(parseJsoup(content, contentX[1]))
                    }
                    str = sb.toString()
                } else {
                    for (textNode in content.textNodes()) {
                        sb.append("\n")
                        sb.append(textNode.text().trim { it <= ' ' })
                    }
                    str = sb.toString()
                    str = parseJsoup(str, contentX[1])
                    for ((key, value) in replace_map) {
                        str = str.replace(key, value)
                    }
                    //屏蔽规则
                    if (ObjectUtil.isNotEmpty(chapter.purify)) {
                        for (purify in chapter.purify) {
                            str = str.replace(purify.toRegex(), "")
                        }
                    }
                }
                str = str.replace("^\n*|\n*$".toRegex(), "")
            }

            val httpResponseBean = HttpResponseBean()
            httpResponseBean.isStatus = true

            //执行js
            if (ObjectUtil.isNotEmpty(chapter.js)) {
                val bindings = SimpleBindings()
                bindings["xlua_rule"] = this
                bindings["element"] = element
                bindings["data"] = str
                bindings["url"] = url
                bindings["label"] = label
                bindings["callback"] = callback
                bindings["CallBackData"] = httpResponseBean

                try {
                    val tempJs = SCRIPT_ENGINE.eval(Base64.decodeStr(chapter.js), bindings)
                    str = jsToJavaObject(bindings["result"] ?: tempJs)
                } catch (e: Exception) {
                    log(e)
                    e.printStackTrace()
                }
                //返回false代表 js 内部处理
                if (str == "false") {
                    return@http
                }
            }
            httpResponseBean.data = str

            if (ObjectUtil.isNotEmpty(chapter.page)) {
                val tmp = parseArray(chapter.page)
                val page =
                    toAbsoluteUrl(parseJsoup(element.select(tmp[0]), tmp[1].ifEmpty { "attr->href" }), url)
                if (page.isNotEmpty() && page != url) {
                    bookContent(page, { dataA: HttpResponseBean, labelA: Any ->
                        if (dataA.isStatus) {
                            if (isComic) {
                                dataA.data = str + '\n' + dataA.data
                            } else {
                                dataA.data = str + dataA.data
                            }
                        }
                        callback.accept(dataA, labelA)
                    }, label)
                } else {
                    callback.accept(httpResponseBean, label)
                }
            } else {
                callback.accept(httpResponseBean, label)
            }
        }
    }


    //特殊表,用于转换HTML转义
    val replace_map: HashMap<String, String> = HashMap()

    init {
        replace_map["<p>"] = ""
        replace_map["</p>"] = ""
        replace_map["&nbsp;"] = ""
        replace_map["&lt;"] = "<"
        replace_map["&gt;"] = ">"
        replace_map["<br><br>"] = "\n"
        replace_map["<br />"] = "\n"
        replace_map["<br>"] = "\n"
        replace_map["<br>\n<br>"] = "\n"
        replace_map["\n\n"] = "\n"
    }

}