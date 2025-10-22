package com.example.nicemvvmproject.network

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * 通用请求参数/头拦截器
 * - 追加公共 Header（如 token、设备信息等）
 * - 追加公共 Query 参数（如 appVersion、timestamp 等）
 */
class CommonParamsInterceptor(
    private val providerGetter: () -> RequestParamsProvider?
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val provider = providerGetter.invoke()

        // 追加公共 Header
        val requestBuilder: Request.Builder = originalRequest.newBuilder()
        provider?.provideHeaders()?.forEach { (key, value) ->
            if (key.isNotEmpty() && value.isNotEmpty()) {
                requestBuilder.header(key, value)
            }
        }

        // 仅对 GET/DELETE 等带查询参数的请求统一追加 Query 参数
        val originalUrl: HttpUrl = originalRequest.url
        val urlBuilder = originalUrl.newBuilder()
        provider?.provideQueryParams()?.forEach { (key, value) ->
            if (key.isNotEmpty() && value.isNotEmpty()) {
                urlBuilder.addQueryParameter(key, value)
            }
        }

        val newUrl = urlBuilder.build()
        val newRequest = requestBuilder.url(newUrl).build()
        return chain.proceed(newRequest)
    }
}

/**
 * 提供公共 Header / Query 参数的数据源
 */
interface RequestParamsProvider {
    fun provideHeaders(): Map<String, String> = emptyMap()
    fun provideQueryParams(): Map<String, String> = emptyMap()
}



