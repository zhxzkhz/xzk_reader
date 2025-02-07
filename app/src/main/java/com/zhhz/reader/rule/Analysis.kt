package com.zhhz.reader.rule

import cn.hutool.core.codec.Base64
import cn.hutool.core.util.ObjectUtil
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.zhhz.reader.bean.BookBean
import com.zhhz.reader.bean.HttpResponseBean
import com.zhhz.reader.bean.rule.RuleJsonBean
import com.zhhz.reader.util.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.*
import java.net.InetAddress
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import javax.script.ScriptException
import javax.script.SimpleBindings

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
        var share = true
        if (isHaveSearch) {
            charset = json.search.charset ?: json.charset
            http = json.search.url.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        } else {
            http = "http"
            charset = json.charset
        }
        if (charset.isEmpty()) charset = "utf8"
        val builder = OkHttpClient.Builder().connectTimeout(30,TimeUnit.SECONDS).readTimeout(30,TimeUnit.SECONDS).writeTimeout(30,TimeUnit.SECONDS)
        if (json.cache) {
            share = false
            builder.addInterceptor(DiskCache.interceptor)
        }
        if (json.cookieJar) {
            share = false
            builder.cookieJar(CookiesManager())
        }
        if (json.dns.isNotEmpty()) {
            share = false
            builder.dns(object :Dns{
                override fun lookup(hostname: String): List<InetAddress> {
                    return JSON.parseArray(json.dns).map {
                        InetAddress.getByName(it as String)
                    }
                }
            })
        }

        if (ObjectUtil.isEmpty(json.init)) {
            client = if (share) {
                share_client
            } else {
                builder.build()
            }
        } else {
            val bindings = SimpleBindings()
            bindings["builder"] = builder
            bindings["xlua_rule"] = this
            bindings["java"] = this
            client = try {
                val obj = DiskCache.SCRIPT_ENGINE.eval(Base64.decodeStr(json.init), bindings)
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

    fun toAbsoluteUrl(relativePath: String, absoluteBasePath: String = url): String {
        if (relativePath.equals("skip", ignoreCase = true)) return relativePath
        return if (relativePath.isNotEmpty() && !relativePath.startsWith("http")) {
            try {

                val absoluteUrl = if (!absoluteBasePath.startsWith("http")){
                    URL("$http://$url")
                } else {
                    URL(absoluteBasePath)
                }

                URL(absoluteUrl, relativePath).toString()
            } catch (e: MalformedURLException) {
                e.printStackTrace()
                ""
            }
        } else relativePath
    }

    /**
     * 判断是否有搜索
     * @return boolean
     */
    val isHaveSearch: Boolean
        get() = ObjectUtil.isNotNull(json.search) && ObjectUtil.isNotEmpty(json.search.url)

    abstract fun bookSearch(keyWord: String,page: Int, callback: AnalysisCallBack.SearchCallBack, label: String)
    abstract fun bookLeaderboard(leaderboardUrl: String, page: Int, callback: AnalysisCallBack.LeaderboardCallBack, label: String)
    abstract fun bookDetail(url: String, callback: AnalysisCallBack.DetailCallBack)
    abstract fun bookDirectory(url: String, callback: AnalysisCallBack.DirectoryCallBack)
    fun bookChapters(book: BookBean, url: String, callback: AnalysisCallBack.ContentCallBack, label: Any) {
        //val file = File(DiskCache.path + File.separator + "book" + File.separator + book.book_id + File.separator + "book_chapter" + File.separator + url.substring(url.lastIndexOf('/') + 1))
        val file = File(DiskCache.path + File.separator + "book" + File.separator + book.bookId + File.separator + "book_chapter" + File.separator + StringUtil.getMD5(url))

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

    fun http(url: String, callBack: AnalysisCallBack.CallBack) {
        Coroutine.async {
        //url等于skip跳过请求
        if (url.equals("skip", ignoreCase = true)) {
            val httpResponseBean = HttpResponseBean()
            httpResponseBean.isStatus = true
            callBack.accept(httpResponseBean)
            return@async
        }
        if (url.contains("@post->")) {
            httpPost(url, callBack)
        } else {
            httpGet(url, callBack)
        }
        }.onError {
            it.printStackTrace()
            val httpResponseBean = HttpResponseBean()
            httpResponseBean.isStatus = false
            callBack.accept(httpResponseBean)
        }
    }

    fun httpPost(url: String, callback: AnalysisCallBack.CallBack) {
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
    fun httpGet(url: String, callback: AnalysisCallBack.CallBack) {
        var header: String? = null
        val urlTemp = if (url.contains("\$header")) {
            val ar = url.split("\\\$header".toRegex(), limit = 2)
            header = ar[1]
            ar[0]
        } else {
            url
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

    private fun initHeader(builder: Request.Builder,httpUrl: String) {
        if (json.header != null) {
            var h = json.header
            if (h.contains("^js@|@js:".toRegex())) {
                try {
                    val bindings = SimpleBindings()
                    bindings["xlua_rule"] = this
                    bindings["java"] = this
                    bindings["url"] = httpUrl

                    h = jsToJavaObject(
                        DiskCache.SCRIPT_ENGINE.eval(
                            Base64.decodeStr(h.split("js@|@js:".toRegex())[1]),
                            bindings
                        )
                    )
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
        (client ?: share_client).newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                httpResponseBean.isStatus = false
                httpResponseBean.error = e.message.toString()
                callback.accept(httpResponseBean)
            }
            override fun onResponse(call: Call, response: Response) {
                httpResponseBean.isStatus = response.code == 200
                var charset: String? = null
                if (ObjectUtil.isNotNull(response.body)) {
                    val contentType = response.body?.contentType()
                    if (contentType?.charset() != null) {
                        charset = contentType.charset()?.name()
                    }
                }

                var s = ""
                try {
                    s = if (this@Analysis.charset == charset) {
                        response.body?.string()
                    } else {
                        charset = this@Analysis.charset
                        response.body?.bytes()?.toString(Charset.forName(charset))
                    }.orEmpty()
                    if (httpResponseBean.isStatus) DiskCache.FileSave(DiskCache.path, call, s.toByteArray(charset = Charset.forName(charset)))
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


    companion object {
        var share_client: OkHttpClient = OkHttpClient.Builder().addInterceptor(DiskCache.interceptor).connectTimeout(30,TimeUnit.SECONDS).readTimeout(30,TimeUnit.SECONDS).writeTimeout(30,TimeUnit.SECONDS).build()
        private var logError: AnalysisCallBack.LogError? = null
        @JvmStatic
        fun setLogError(error: AnalysisCallBack.LogError) {
            logError = error
        }

        @Throws(IOException::class)
        fun readText(path: String): RuleJsonBean {
            return CompletableFuture.supplyAsync {
                try {
                    return@supplyAsync JSON.parseObject(FileUtil.readFile(path),RuleJsonBean::class.java)
                } catch (e: IOException) {
                    LogUtil.error(e)
                    throw FileNotFoundException("文件读取异常 -> $path")
                }
            }.join()
        }
    }
}