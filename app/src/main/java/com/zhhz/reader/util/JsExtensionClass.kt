package com.zhhz.reader.util

import cn.hutool.core.codec.Base64
import cn.hutool.crypto.symmetric.SymmetricCrypto
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.TypeReference
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.runBlocking
import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.lang.Thread.sleep
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.concurrent.thread

/**
 * js扩展类, 在js中通过java变量调用
 * 例如： java.desDecrypt(x,x,x,x)
 */
interface JsExtensionClass {

    fun ajax(
        okHttpClient: OkHttpClient,
        url: String,
        method: String = "GET",
        mediaType: String = "application/json",
        header: String = "{}",
        data: String = ""
    ): String? {
        var builder: Request.Builder = Request.Builder().url(url)
        builder = if (method.equals("POST",true)) {
            val body: RequestBody = data.toRequestBody(mediaType.toMediaTypeOrNull())
            builder.post(body)
        } else {
            builder.get()
        }

        builder = builder.headers(
            (JSON.parseObject(
                header, object : TypeReference<HashMap<String?, String?>?>(){}.type
            ) as HashMap<String, String>).toHeaders()
        )

        return runBlocking {
            kotlin.runCatching {
                okHttpClient.newCall(builder.build()).execute().body?.string()
            }.onFailure {
                //log("ajax(${url}) error\n${it.localizedMessage}
                it.printStackTrace()
                LogUtil.warning(it.message)
            }.getOrElse {
                val byteArrayOutputStream = ByteArrayOutputStream()
                it.printStackTrace(PrintStream(byteArrayOutputStream))
                byteArrayOutputStream.toString()
            }
        }

    }

    fun timeout(runnable: Runnable ,long: Long){
        thread(start =true) {
            sleep(long)
            runnable.run()
        }
    }

    fun md5(
        str: String
    ) : String {
        return StringUtil.getMD5(str)
    }

    /**
     * base64解密
     */
    fun base64Decrypt(str: String): String {
        return Base64.decodeStr(str)
    }

    /**
     * base64加密
     */
    fun base64Encrypt(str: String): String {
        return Base64.encode(str)
    }

    /**
     * 字符串DES解密
     * @param str 待解密的字符串
     * @param key DES 解密的key
     * @param paddings DES加密类型
     * @param iv ECB模式的偏移向量
     */
    fun desDecrypt(
        str: String,
        key: String,
        paddings: String = "DES/ECB/PKCS5Padding",
        iv: String
    ): String? {
        return crypt(str, key, "DES", paddings, iv, false)?.let { String(it) }
    }

    /**
     * 字符串DES加密
     * @param str 待解密的字符串
     * @param key DES 解密的key
     * @param paddings DES加密类型
     * @param iv ECB模式的偏移向量
     */
    fun desEncrypt(
        str: String,
        key: String,
        paddings: String = "DES/ECB/PKCS5Padding",
        iv: String
    ): String? {
        return crypt(str, key, "DES", paddings, iv, true)?.let { String(it) }
    }

    /**
     * AES 字符串解密
     * @param str 待解密的字符串
     * @param key AES 解密的key
     * @param paddings AES加密类型
     * @param iv ECB模式的偏移向量
     */
    fun aesDecrypt(
        str: String,
        key: String,
        paddings: String = "AES/CBC/PKCS5Padding",
        iv: String
    ): String? {
        return crypt(str, key, "AES", paddings, iv, false)?.let { String(it) }
    }

    /**
     * AES 字符串加密
     * @param str 待加密的字符串
     * @param key AES 解密的key
     * @param paddings AES加密类型
     * @param iv ECB模式的偏移向量
     */
    fun aesEncrypt(
        str: String,
        key: String,
        paddings: String = "AES/CBC/PKCS5Padding",
        iv: String
    ): String? {
        return crypt(str, key, "AES", paddings, iv, false)?.let { String(it) }
    }

    /**
     * 返回对称加密或解密的字节。
     * @param data 待处理字符串
     * @param key 解密的key
     * @param mode 加密的方式
     * @param paddings 加密的类型
     * @param iv ECB模式的偏移向量
     * @param isEncrypt 加密 or 解密
     */
    private fun crypt(
        data: String?,
        key: String?,
        mode: String,
        paddings: String?,
        iv: String?,
        isEncrypt: Boolean
    ): ByteArray? {
        if (data.isNullOrEmpty() || key.isNullOrEmpty()) return null
        val keySpec = SecretKeySpec(key.toByteArray(), mode)
        var params: AlgorithmParameterSpec? = null
        if (!iv.isNullOrEmpty()) {
            params = IvParameterSpec(iv.toByteArray())
        }
        val symmetricCrypto = SymmetricCrypto(paddings, keySpec, params)
        return if (isEncrypt) symmetricCrypto.encrypt(data) else symmetricCrypto.decrypt(data)
    }

}