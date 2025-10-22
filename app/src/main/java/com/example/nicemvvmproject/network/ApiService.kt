package com.example.nicemvvmproject.network

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Streaming
import retrofit2.http.Url

interface ApiService {

    // 示例接口：获取欢迎文案（可替换为你的真实接口）
    @GET("/api/welcome")
    suspend fun getWelcomeMessage(): WelcomeResponse

    @Multipart
    @POST("upload")
    suspend fun uploadFile(@Part file: MultipartBody.Part): Response<Unit>

    @Streaming
    @GET
    suspend fun downloadFile(@Url url: String): Response<ResponseBody>
}

data class WelcomeResponse(
    val message: String
)



