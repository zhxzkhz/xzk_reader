package com.zhhz.reader.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.loader.app.LoaderManager
import cn.hutool.core.codec.Base64
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.zhhz.reader.MyApplication
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.script.SimpleBindings

@GlideModule
class XluaGlideModule : AppGlideModule(),JsExtensionClass{

    override fun isManifestParsingEnabled(): Boolean {
        return false
    }

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        glide = builder
        if (MyApplication.DeBug) {
            builder.setLogLevel(Log.DEBUG)
            LoaderManager.enableDebugLogging(true)
        }
        //储存上限10G图片
        /*builder.setDiskCache(
            DiskLruCacheFactory(
                DiskCache.path + File.separator + "Disc_ImageCache",
                10L * 1024 * 1024 * 1024
            )
        )*/
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val builder = OkHttpClient.Builder()

        //忽略ssl证书错误
        builder.sslSocketFactory(SSLSocketClient.getSSLSocketFactory(),SSLSocketClient.getTrustManager())
        builder.hostnameVerifier { _, _ -> true }
        val al = ArrayList<Protocol>()
        al.add(Protocol.HTTP_1_1)
        al.add(Protocol.HTTP_2)
        builder.protocols(al)

        //添加拦截器（ProgressInterceptor 用于进度获取）
        //builder.addInterceptor(new ProgressInterceptor());

        builder.addInterceptor { chain: Interceptor.Chain ->
            var request = chain.request()
            var response: Response
            val url = request.url.toString()
            val js: String
            if (url.contains("ImageDecryption://")) {
                val arr = url.split("ImageDecryption://")
                js = arr[1]
                request = request.newBuilder().url(arr[0]).build()
                response = chain.proceed(request)
            } else {
                return@addInterceptor chain.proceed(request)
            }

            val bt = response.body?.bytes()
            val options = BitmapFactory.Options()
            val bitmap = bt?.let { BitmapFactory.decodeByteArray(bt, 0, it.size,options) } ?: return@addInterceptor response
            val simpleBindings = SimpleBindings()
            simpleBindings["xlua_rule"] = this@XluaGlideModule
            simpleBindings["resource"] = bitmap
            val bit = Bitmap.createBitmap(bitmap.width, bitmap.height,options.outConfig)
            val format = when (options.outMimeType){
                "image/webp" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSLESS
                } else {
                    Bitmap.CompressFormat.WEBP
                }
                "image/jpeg" -> Bitmap.CompressFormat.JPEG
                else -> Bitmap.CompressFormat.PNG
            }
            simpleBindings["bitmap"] = bit
            val body: ResponseBody = try {
                val byteArrayOutputStream = ByteArrayOutputStream()
                DiskCache.SCRIPT_ENGINE.eval(Base64.decodeStr(js), simpleBindings)
                bit.compress(format, 100, byteArrayOutputStream)
                byteArrayOutputStream.toByteArray().toResponseBody(response.body?.contentType())
            } catch (e: Exception) {
                LogUtil.error(e)
                return@addInterceptor response
                //throw new RuntimeException(e);
            } finally {
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                }
                if (!bit.isRecycled) {
                    bitmap.recycle()
                }
            }
            response = response.newBuilder().body(body).build()
            response
        }
        registry.replace(
            GlideUrl::class.java,
            InputStream::class.java,
            OkHttpUrlLoader.Factory(builder.build())
        )
        super.registerComponents(context, glide, registry)
    }

    companion object {
        lateinit var glide: GlideBuilder
    }
}