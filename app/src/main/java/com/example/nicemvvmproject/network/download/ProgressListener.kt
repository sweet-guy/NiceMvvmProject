package com.example.nicemvvmproject.network.download

/**
 * @Description:
 * @Author yanxin
 * @Date:  2025/10/20
 */

interface ProgressListener {
    fun onProgress(bytesReadOrWritten: Long, contentLength: Long, done: Boolean)
}