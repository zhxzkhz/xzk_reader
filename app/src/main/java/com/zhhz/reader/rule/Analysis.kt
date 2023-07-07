package com.zhhz.reader.rule

import cn.hutool.core.util.ObjectUtil
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.zhhz.reader.bean.BookBean
import com.zhhz.reader.bean.HttpResponseBean
import com.zhhz.reader.bean.rule.RuleJsonBean
import com.zhhz.reader.util.AutoBase64
import com.zhhz.reader.util.DiskCache
import com.zhhz.reader.util.JsExtensionClass
import com.zhhz.reader.util.StringUtil
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.mozilla.javascript.NativeArray
import org.mozilla.javascript.NativeJavaObject
import java.io.*
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.Charset
import java.util.*
import javax.script.ScriptException
import javax.script.SimpleBindings
import kotlin.concurrent.thread

abstract class Analysis(var json: RuleJsonBean): JsExtensionClass {

    //规则网站地址
    var url: String = json.url

    //规则名字
    var name: String = json.name

    //是否属于漫画
    var isComic: Boolean = json.comic

    //规则使用文本编码
    var charset: String

    //用于标记 响应协议；
    private var http: String

    //书本信息
    var detail: BookBean? = null

    //用于存储数据
    private val shareMap = HashMap<Any, Any>()
    private var client: OkHttpClient? = null

    fun ajax(
        url: String,
        method: String,
        mediaType: String,
        header: String,
        data: String
    ): String? {
        return (if (client == null) share_client else client)?.let { super.ajax(it, url, method, mediaType, header, data) }
    }

    fun ajax(
        url: String,
        method: String,
        mediaType: String
    ): String? {
        return (if (client == null) share_client else client)?.let { ajax(it, url, method, mediaType) }
    }

    constructor(path: String) : this(readText(path))

    init {
        if (isHaveSearch) {
            charset = json.search.charset
            http = json.search.url.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        } else {
            http = "http"
            charset = json.charset
        }
        if (charset.isEmpty()) charset = "utf8"
        val builder = OkHttpClient.Builder()
        if (json.cache) {
            builder.addInterceptor(DiskCache.interceptor)
        }
        if (json.cookieJar) {
            builder.cookieJar(object : CookieJar {
                private val cookieStore = HashMap<String, List<Cookie>>()
                override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                    cookieStore[url.host] = cookieStore[url.host] ?: cookies
                }

                override fun loadForRequest(url: HttpUrl): List<Cookie> {
                    val cookies = cookieStore[url.host]
                    return cookies ?: ArrayList()
                }
            })
        }
        if (ObjectUtil.isEmpty(json.init)) {
            client = if (json.cookieJar) {
                builder.build()
            } else {
                share_client
            }
        } else {
            val bindings = SimpleBindings()
            bindings["builder"] = builder
            bindings["xlua_rule"] = this
            client = try {
                val obj = DiskCache.SCRIPT_ENGINE.eval(AutoBase64.decodeToString(json.init), bindings)
                if (obj is OkHttpClient.Builder) {
                    obj.build()
                } else {
                    builder.build()
                }
            } catch (e: ScriptException) {
                log(e)
                builder.build()
            }
        }
    }

    fun log(e: Exception) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        e.printStackTrace(PrintStream(byteArrayOutputStream))
        log(byteArrayOutputStream.toString())
    }

    fun log(s: Any) {
        if (logError != null) {
            logError!!.log(s.toString())
        }
    }

    //设置共享数据
    fun setShareValue(key: Any, value: Any) {
        shareMap[key] = value
    }

    //获取共享数据
    fun getShareValue(key: Any): Any {
        return shareMap[key]?:""
    }

    fun toAbsoluteUrl(relativePath: String, absoluteBasePath: String?): String {
        if (relativePath.equals("skip", ignoreCase = true)) return relativePath
        return if (relativePath.isNotEmpty() && !relativePath.startsWith("http")) {
            try {
                val absoluteUrl = URL(absoluteBasePath)
                URL(absoluteUrl, relativePath).toString()
            } catch (e: MalformedURLException) {
                ""
            }
        } else relativePath
    }

    val isHaveSearch: Boolean
        /**
         * 判断是否有搜索
         * @return boolean
         */
        get() = ObjectUtil.isNotNull(json.search) && ObjectUtil.isNotEmpty(json.search.url)

    abstract fun bookSearch(keyWord: String, callback: AnalysisCallBack.SearchCallBack, label: String)
    abstract fun bookDetail(url: String, callback: AnalysisCallBack.DetailCallBack)
    abstract fun bookDirectory(url: String, callback: AnalysisCallBack.DirectoryCallBack)
    fun bookChapters(book: BookBean, url: String, callback: AnalysisCallBack.ContentCallBack, label: Any) {
        //val file = File(DiskCache.path + File.separator + "book" + File.separator + book.book_id + File.separator + "book_chapter" + File.separator + url.substring(url.lastIndexOf('/') + 1))
        val file = File(DiskCache.path + File.separator + "book" + File.separator + book.book_id + File.separator + "book_chapter" + File.separator + StringUtil.getMD5(url))

        if (!Objects.requireNonNull(file.parentFile).isDirectory) {
            if (!file.parentFile?.mkdirs()!!) {
                callback.accept(null, label)
            }
        }
        if (file.isFile) {
            try {
                FileInputStream(file).use { fis ->
                    val size = fis.available()
                    val bytes = ByteArray(size)
                    if (fis.read(bytes) == size) {
                        val h = HttpResponseBean()
                        h.isStatus = true
                        h.data = String(bytes)
                        callback.accept(h, label)
                        return
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        bookContent(url, AnalysisCallBack.ContentCallBack { data: HttpResponseBean, tag: Any ->
            if (!data.isStatus) {
                callback.accept(data, tag)
                return@ContentCallBack
            }
            try {
                FileOutputStream(file).use { fos -> fos.write(data.data.toByteArray()) }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            callback.accept(data, tag)
        }, label)
    }

    abstract fun bookContent(url: String, callback: AnalysisCallBack.ContentCallBack, label: Any)

    fun Http(url: String, callBack: AnalysisCallBack.CallBack) {
        thread  {
        //url等于skip跳过请求
        if (url.equals("skip", ignoreCase = true)) {
            val httpResponseBean = HttpResponseBean()
            httpResponseBean.isStatus = true
            callBack.accept(httpResponseBean)
            return@thread
        }
        if (url.contains("@post->")) {
            Http_Post(url, callBack)
        } else {
            Http_Get(url, callBack)
        }
        }
    }

    fun Http_Post(url: String, callback: AnalysisCallBack.CallBack) {
        var header: String? = null
        val data: String
        val arr= url.split("@post->".toRegex(), limit = 2)
        if (arr[1].isNotEmpty() && arr[1].contains("\$header")) {
            val ar = arr[1].split("\\\$header".toRegex(), limit = 2)
            data = ar[0]
            header = ar[1]
        } else {
            data = arr[1]
        }
        val mt: MediaType? = if (data.startsWith("{")) {
            "application/json; charset=$charset".toMediaTypeOrNull()
        } else {
            "application/x-www-form-urlencoded;charset=$charset".toMediaTypeOrNull()
        }

        val builder: Request.Builder = Request.Builder().url(arr[0]).post(data.toRequestBody(mt))
        if (header != null) {
            val jsonObject = JSONObject.parseObject(header)
            for ((key, value) in jsonObject) {
                builder.addHeader(key, value.toString())
            }
        }
        initHeader(builder,arr[0])
        newCall(builder.build(), callback)
    }

    /**
     * GET请求
     *
     * @param url      地址
     * @param callback 回调事件
     */
    fun Http_Get(url: String, callback: AnalysisCallBack.CallBack) {
        var urlTemp = url
        var header: String? = null
        if (urlTemp.contains("\$header")) {
            val ar = urlTemp.split("\\\$header".toRegex(), limit = 2)
            urlTemp = ar[0]
            header = ar[1]
        }
        val builder: Request.Builder = Request.Builder().url(urlTemp)
        initHeader(builder,url)
        if (header != null) {
            val jsonObject = JSONObject.parseObject(header)
            for ((key, value) in jsonObject) {
                builder.addHeader(key, value.toString())
            }
        }
        newCall(builder.build(), callback)
    }

    private fun initHeader(builder: Request.Builder,url: String) {
        if (json.header != null) {
            var h = json.header
            if (h.indexOf("js@") == 0) {
                try {
                    val bindings = SimpleBindings()
                    bindings["xlua_rule"] = this
                    bindings["logError"] = logError
                    bindings["url"] = url
                    h = jsToJavaObject(DiskCache.SCRIPT_ENGINE.eval(AutoBase64.decodeToString(h.substring(3)), bindings))
                } catch (e: ScriptException) {
                    e.printStackTrace()
                    h = "{}"
                }
            }
            if (h.isNotEmpty()) {
                val header = JSONObject.parseObject(h)
                for ((key, value) in header) {
                    builder.addHeader(key, value.toString())
                }
                header.clear()
            }
        }
    }

    private fun newCall(request: Request, callback: AnalysisCallBack.CallBack) {
        val httpResponseBean = HttpResponseBean()
        httpResponseBean.requestHeader = request.headers
        client!!.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                httpResponseBean.isStatus = false
                httpResponseBean.error = e.message.toString()
                callback.accept(httpResponseBean)
            }
            override fun onResponse(call: Call, response: Response) {
                httpResponseBean.isStatus = response.code == 200
                var charset: String? = null
                if (ObjectUtil.isNotNull(response.body)) {
                    val contentType = response.body.contentType()
                    if (contentType?.charset() != null) {
                        charset = contentType.charset()?.name()
                    }
                }
                if (charset == null) {
                    charset = this@Analysis.charset
                }
                var s = ""
                try {
                    s = String(response.body.bytes(), Charset.forName(charset))
                    DiskCache.FileSave(DiskCache.path, call, s)
                } catch (e: IOException) {
                    httpResponseBean.isStatus = false
                    httpResponseBean.error = e.message!!
                    log(e)
                    e.printStackTrace()
                }
                httpResponseBean.data = s
                callback.accept(httpResponseBean)
                response.close()
            }
        })
    }

    /**
     * 将js类型转换成java类型
     *
     * @param value js类型
     * @return java类型
     */
    fun jsToJavaObject (value: Any?): String {
        if (value == null) return ""
        val str: String = if (value is NativeArray) {
            val stringBuilder = StringBuilder()
            for (o in value) {
                stringBuilder.append(o).append("\n")
            }
            if (stringBuilder.isNotEmpty())
                stringBuilder.delete(stringBuilder.length - 1, stringBuilder.length)
            stringBuilder.toString()
        } else if (value.javaClass == NativeJavaObject::class.java) {
            (value as NativeJavaObject).unwrap().toString()
        } else {
            value.toString()
        }
        return str
    }


    companion object {
        var share_client: OkHttpClient = OkHttpClient.Builder().addInterceptor(DiskCache.interceptor).build()
        private var logError: AnalysisCallBack.LogError? = null
        @JvmStatic
        fun setLogError(error: AnalysisCallBack.LogError) {
            logError = error
        }

        @Throws(IOException::class)
        fun readText(path: String): RuleJsonBean {
            val file = File(path)
            if (!file.isFile) throw FileNotFoundException("文件未找到 -> $file")
            val fis: FileInputStream
            try {
                fis = FileInputStream(path)
                val size = fis.available()
                val bytes = ByteArray(size)
                if (fis.read(bytes) != size) throw IOException("文件读取异常")
                fis.close()
                return JSON.parseObject(bytes,RuleJsonBean::class.java)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            throw IOException("加载异常")
        }
    }
}