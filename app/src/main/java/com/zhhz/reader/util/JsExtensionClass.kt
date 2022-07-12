package com.zhhz.reader.util

import cn.hutool.crypto.symmetric.SymmetricCrypto
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * js扩展类, 在js中通过java变量调用
 *
 */
interface JsExtensionClass {

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
     * @param str 待处理字符串
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
        if ((data == null) || data.isEmpty() || (key == null) || key.isEmpty()) return null
        val keySpec = SecretKeySpec(key.toByteArray(), mode)
        var params: AlgorithmParameterSpec? = null
        if (iv != null && iv.isNotEmpty()) {
            params = IvParameterSpec(iv.toByteArray())
        }
        val symmetricCrypto = SymmetricCrypto(paddings, keySpec, params)
        return if (isEncrypt) symmetricCrypto.encrypt(data) else symmetricCrypto.decrypt(data)
    }

}