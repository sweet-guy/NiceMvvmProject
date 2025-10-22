package com.example.nicemvvmproject.network

import com.example.nicemvvmproject.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    val rsaPublicKeyBase64 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A..." // 服务器提供的RSA公钥Base64
    private const val DEFAULT_TIMEOUT_SECONDS = 20L
    const val CURRENT_BASE_URL = "https://api.example.com/"
    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level =
                if (BuildConfig.LOG_ENABLE) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
    }

    // 默认参数提供器（可由 App 初始化时注入）
    @Volatile
    private var requestParamsProvider: RequestParamsProvider? = null

    fun setRequestParamsProvider(provider: RequestParamsProvider) {
        requestParamsProvider = provider
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(CommonParamsInterceptor { requestParamsProvider })
            .addInterceptor(HybridEncryptDecryptInterceptor(rsaPublicKeyBase64))
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun <T> createService(service: Class<T>, baseUrl: String): T {
        return createRetrofit(baseUrl).create(service)
    }
}


