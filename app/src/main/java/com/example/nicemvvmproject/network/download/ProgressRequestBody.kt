package com.example.nicemvvmproject.network.download

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.io.File

/**
 * @Description:
 * @Author yanxin
 * @Date:  2025/10/20
 */

class ProgressRequestBody(
    private val file: File,
    private val contentType: MediaType?,
    private val listener: ProgressListener
) : RequestBody() {

    override fun contentType(): MediaType? = contentType

    override fun contentLength(): Long = file.length()

    override fun writeTo(sink: BufferedSink) {
        val length = contentLength()
        file.inputStream().use { input ->
            val source = input.source()
            var total = 0L
            var read: Long
            val bufferSize = 8L * 1024L
            while (source.read(sink.buffer, bufferSize).also { read = it } != -1L) {
                total += read
                sink.flush()
                listener.onProgress(total, length, total == length)
            }
        }
    }
}