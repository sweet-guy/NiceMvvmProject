package com.example.nicemvvmproject.network.download

import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.Source
import okio.buffer

/**
 * @Description:
 * @Author yanxin
 * @Date:  2025/10/20
 */

class ProgressResponseBody(
    private val delegate: ResponseBody,
    private val listener: ProgressListener
) : ResponseBody() {

    override fun contentType(): MediaType? = delegate.contentType()

    override fun contentLength(): Long = delegate.contentLength()

    override fun source(): BufferedSource {
        return source(delegate.source()).buffer()
    }

    private fun source(source: Source): Source {
        val total = contentLength()
        var bytesReadTotal = 0L
        return object : ForwardingSource(source) {
            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                if (bytesRead != -1L) bytesReadTotal += bytesRead
                listener.onProgress(bytesReadTotal, total, bytesRead == -1L)
                return bytesRead
            }
        }
    }
}