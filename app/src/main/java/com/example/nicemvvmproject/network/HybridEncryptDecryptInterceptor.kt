package com.example.nicemvvmproject.network

import android.util.Base64
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import java.nio.charset.Charset
import java.security.KeyFactory
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * @Description:数据加解密
 * @Author yanxin
 * @Date:  2025/10/22
 */

class HybridEncryptDecryptInterceptor(private val rsaPublicKeyBase64: String) : Interceptor {

    // 会话 AES 密钥（每次启动随机生成）
    private val aesKey: String = generateRandomAESKey()

    // RSA 公钥对象
    private val rsaPublicKey: PublicKey = loadRSAPublicKey(rsaPublicKeyBase64)

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        // 请求加密
        request = encryptRequest(request)

        // 执行请求
        val response = chain.proceed(request)

        // 响应解密
        return decryptResponse(response)
    }

    /**
     * 请求加密
     */
    private fun encryptRequest(request: Request): Request {
        return when (request.method) {
            "POST", "PUT" -> {
                val encryptedBody = encryptRequestBody(request.body)
                request.newBuilder()
                    .header("X-AES-Key", rsaEncrypt(aesKey)) // AES密钥用RSA加密后放到请求头
                    .method(request.method, encryptedBody)
                    .build()
            }
            "GET" -> {
                val encryptedUrl = encryptGetUrl(request.url)
                request.newBuilder()
                    .header("X-AES-Key", rsaEncrypt(aesKey))
                    .url(encryptedUrl)
                    .build()
            }
            else -> request
        }
    }

    /**
     * 加密 POST/PUT 请求体
     */
    private fun encryptRequestBody(body: RequestBody?): RequestBody? {
        if (body == null) return null

        val buffer = Buffer()
        body.writeTo(buffer)
        val charset: Charset = Charset.forName("UTF-8")
        val originalData = buffer.readString(charset)

        val encryptedData = aesEncrypt(originalData, aesKey)

        return RequestBody.create("text/plain".toMediaTypeOrNull(), encryptedData)
    }

    /**
     * 加密 GET 请求参数
     */
    private fun encryptGetUrl(url: HttpUrl): HttpUrl {
        val builder = url.newBuilder()
        for (i in 0 until url.querySize) {
            val name = url.queryParameterName(i)
            val value = url.queryParameterValue(i)
            if (value != null) {
                builder.setQueryParameter(name, aesEncrypt(value, aesKey))
            }
        }
        return builder.build()
    }

    /**
     * 响应解密
     */
    private fun decryptResponse(response: Response): Response {
        val body = response.body ?: return response
        val source = body.source()
        source.request(Long.MAX_VALUE)
        val buffer = source.buffer
        val charset = Charset.forName("UTF-8")
        val encryptedData = buffer.clone().readString(charset)

        val decryptedData = aesDecrypt(encryptedData, aesKey)

        val newBody = ResponseBody.create(body.contentType(), decryptedData)
        return response.newBuilder()
            .body(newBody)
            .build()
    }

    /**
     * AES 加密
     */
    private fun aesEncrypt(data: String, key: String): String {
        val secretKeySpec = SecretKeySpec(key.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
        val encryptedBytes = cipher.doFinal(data.toByteArray())
        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
    }

    /**
     * AES 解密
     */
    private fun aesDecrypt(data: String, key: String): String {
        val secretKeySpec = SecretKeySpec(key.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
        val decodedBytes = Base64.decode(data, Base64.NO_WRAP)
        val decryptedBytes = cipher.doFinal(decodedBytes)
        return String(decryptedBytes, Charset.forName("UTF-8"))
    }

    /**
     * RSA 加密 AES 密钥
     */
    private fun rsaEncrypt(data: String): String {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey)
        val encryptedBytes = cipher.doFinal(data.toByteArray())
        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
    }

    /**
     * 加载 RSA 公钥
     */
    private fun loadRSAPublicKey(base64Key: String): PublicKey {
        val decodedKey = Base64.decode(base64Key, Base64.NO_WRAP)
        val keySpec = X509EncodedKeySpec(decodedKey)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(keySpec)
    }

    /**
     * 生成随机 AES 密钥（16位）
     */
    private fun generateRandomAESKey(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val random = SecureRandom()
        return (1..16)
            .map { chars[random.nextInt(chars.length)] }
            .joinToString("")
    }
}