package com.example.nicemvvmproject.base

import com.example.nicemvvmproject.network.ApiService
import com.example.nicemvvmproject.network.RetrofitClient

open class BaseRepository(
    baseUrl: String? = null
) {
    protected val apiService: ApiService = if (baseUrl.isNullOrBlank()) {
        RetrofitClient.createService(ApiService::class.java, RetrofitClient.CURRENT_BASE_URL)
    } else {
        RetrofitClient.createService(ApiService::class.java, baseUrl)
    }

}



