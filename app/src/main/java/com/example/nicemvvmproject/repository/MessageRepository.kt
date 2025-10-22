package com.example.nicemvvmproject.repository

import com.example.nicemvvmproject.base.BaseRepository
import com.example.nicemvvmproject.bean.User
import com.example.nicemvvmproject.network.download.ProgressListener
import com.example.nicemvvmproject.network.download.ProgressRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import java.io.File

class MessageRepository(baseUrl: String? = null) : BaseRepository(baseUrl) {

    suspend fun loadWelcomeMessage(): User {
        apiService.getWelcomeMessage()
        return User(0, "", "")
    }

    //上传文件
    suspend fun uploadWithProgress(
        file: File,
        mimeType: String,
        onProgress: ProgressListener
    ): Boolean {
        val requestBody = ProgressRequestBody(file, mimeType.toMediaTypeOrNull(), onProgress)
        val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
        val resp = apiService.uploadFile(part)
        return resp.isSuccessful
    }

    //下载文件
    suspend fun downloadWithProgress(
        url: String,
        destFile: File,
        onProgress: ProgressListener
    ): Boolean {
        val response = apiService.downloadFile(url)
        if (!response.isSuccessful || response.body() == null) return false
        val body = response.body()!!
        destFile.outputStream().use { out ->
            body.byteStream().use { input ->
                val buffer = ByteArray(8 * 1024)
                var read: Int
                var totalRead = 0L
                val total = body.contentLength()
                while (input.read(buffer).also { read = it } != -1) {
                    out.write(buffer, 0, read)
                    totalRead += read
                    onProgress.onProgress(totalRead, total, totalRead == total)
                }
            }
        }
        return true
    }
}


